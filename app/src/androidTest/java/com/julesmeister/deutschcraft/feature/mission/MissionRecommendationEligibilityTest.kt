package com.julesmeister.deutschcraft.feature.mission

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.julesmeister.deutschcraft.core.database.DeutschCraftDatabase
import com.julesmeister.deutschcraft.core.database.dao.MissionThreadRecommendationDao
import com.julesmeister.deutschcraft.core.database.migration.DatabaseMigrations
import com.julesmeister.deutschcraft.feature.chat.data.factory.ConversationFactory
import com.julesmeister.deutschcraft.feature.chat.data.local.ChatLocalDataSource
import com.julesmeister.deutschcraft.feature.mission.data.LocalFirstMissionRepository
import com.julesmeister.deutschcraft.feature.mission.data.cache.MissionOfferCache
import com.julesmeister.deutschcraft.feature.mission.data.recommendation.RoomMissionThreadRecommendationStore
import com.julesmeister.deutschcraft.feature.mission.domain.MissionDifficulty
import com.julesmeister.deutschcraft.feature.mission.domain.catalog.RestaurantOrderingMission
import com.julesmeister.deutschcraft.feature.mission.domain.generated.GeneratedMissionDraft
import com.julesmeister.deutschcraft.feature.mission.domain.generated.GeneratedMissionObjectiveDraft
import com.julesmeister.deutschcraft.feature.mission.domain.generated.MissionOpportunity
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionScenarioType
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionThreadRecommendation
import com.julesmeister.deutschcraft.feature.mission.domain.usecase.AcceptMissionOfferUseCase
import com.julesmeister.deutschcraft.feature.mission.domain.usecase.StartRecommendedMissionThreadUseCase
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MissionRecommendationEligibilityTest {
    private lateinit var database: DeutschCraftDatabase
    private lateinit var dao: MissionThreadRecommendationDao
    private val clock = Clock.fixed(Instant.ofEpochMilli(100_000), ZoneOffset.UTC)

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            DeutschCraftDatabase::class.java
        ).build()
        dao = database.missionThreadRecommendationDao()
        recommendation("candidate", "RESTAURANT_ORDERING")
        recommendation("fresh", "INTRODUCING_YOURSELF")
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun oldCrossLevelCompletionIsExcludedFromEveryEligibilitySurface() = runBlocking {
        recommendation("old", "RESTAURANT_ORDERING", "COMPLETED", "A1")
        assertEligibleIds(setOf("fresh"))
        assertTrue("RESTAURANT_ORDERING" in dao.getBlockedScenarioTypes())
    }

    @Test
    fun actualArchivedMissionStateBlocksDespiteRecommendationStatusLag() = runBlocking {
        recommendation("old", "RESTAURANT_ORDERING", "STARTED", "A1")
        mission("attempt", "ACTIVE", sourceId = "old")
        assertEligibleIds(setOf("fresh"))
        database.openHelper.writableDatabase.execSQL(
            "UPDATE conversation_missions SET status = 'PAUSED' WHERE id = 'attempt'"
        )
        assertEligibleIds(setOf("fresh"))
        database.openHelper.writableDatabase.execSQL(
            "UPDATE conversation_missions SET status = 'ABANDONED' WHERE id = 'attempt'"
        )
        assertEligibleIds(setOf("candidate", "fresh"))
        assertFalse(dao.hasActiveScenario("A1", "RESTAURANT_ORDERING", clock.millis()))
    }

    @Test
    fun rootTemplateIdentitySurvivesAnActiveBranchChange() = runBlocking {
        mission("attempt", "ACTIVE", rootId = RestaurantOrderingMission.TEMPLATE_ID)
        assertEligibleIds(setOf("fresh"))
    }

    @Test
    fun completionIsRetainedAtomicallyAfterDeletingASeededThread() = runBlocking {
        mission("attempt", "ACTIVE", rootId = RestaurantOrderingMission.TEMPLATE_ID)
        val repository = LocalFirstMissionRepository(database, database.missionDao(), clock, Json)
        repository.completeMission("attempt")
        database.openHelper.writableDatabase.execSQL("DELETE FROM conversations WHERE id = 'attempt-thread'")

        assertEligibleIds(setOf("fresh"))
        assertTrue("RESTAURANT_ORDERING" in dao.getAllCompletedScenarioTypes())
        dao.deleteOldInactiveRecommendations(Long.MAX_VALUE)
        assertTrue("RESTAURANT_ORDERING" in dao.getAllCompletedScenarioTypes())
    }

    @Test
    fun migrationBackfillsSeededAndRecommendationCompletionsWithoutConversationText() = runBlocking {
        recommendation("old", "INTRODUCING_YOURSELF", "COMPLETED", "A1")
        mission("attempt", "COMPLETED", rootId = RestaurantOrderingMission.TEMPLATE_ID)
        database.openHelper.writableDatabase.execSQL("DROP TABLE mission_scenario_completions")
        DatabaseMigrations.MIGRATION_18_19.migrate(database.openHelper.writableDatabase)
        database.openHelper.writableDatabase.execSQL("DELETE FROM conversation_missions")
        database.openHelper.writableDatabase.execSQL("DELETE FROM mission_thread_recommendations WHERE id = 'old'")

        assertEquals(
            setOf("RESTAURANT_ORDERING", "INTRODUCING_YOURSELF"),
            dao.getAllCompletedScenarioTypes().toSet()
        )
        assertEligibleIds(emptySet())
    }

    @Test
    fun ordinaryContextIncludesArchivedLearnerMessagesButNotAssistantOrMissionText() = runBlocking {
        insert("conversations", mapOf("id" to "ordinary", "isArchived" to 1, "cefrLevel" to "A2"))
        mission("attempt", "ABANDONED")
        message("learner", "ordinary", "USER", "An ordinary topic")
        message("assistant", "ordinary", "ASSISTANT", "Do not copy the assistant")
        message("mission", "attempt-thread", "USER", "Do not treat a mission as ordinary")

        assertEquals(listOf("An ordinary topic"), dao.getRecentOrdinaryLearnerMessages(0, 40))
        assertTrue(dao.getRecentOrdinaryLearnerMessages(clock.millis() + 1, 40).isEmpty())
    }

    @Test
    fun startRechecksEligibilityAndPreventsDuplicateConversations() = runBlocking {
        val store = RoomMissionThreadRecommendationStore(dao, clock, Json)
        val offerCache = MissionOfferCache()
        val repository = LocalFirstMissionRepository(database, database.missionDao(), clock, Json)
        val starter = StartRecommendedMissionThreadUseCase(
            ConversationFactory(
                ChatLocalDataSource(database, database.conversationDao(), database.messageDao()),
                clock
            ),
            offerCache,
            store,
            AcceptMissionOfferUseCase(repository, offerCache),
            database
        )
        val offered = MissionThreadRecommendation(
            id = "startable",
            scenarioType = MissionScenarioType.RESTAURANT_ORDERING,
            title = "A restaurant conversation",
            previewText = "Welcome.",
            opportunity = MissionOpportunity(
                0.9f, "A useful situation.",
                GeneratedMissionDraft(
                    "A restaurant conversation", "Order a meal.", "At a restaurant.",
                    "Customer", "Waiter", MissionDifficulty.A2, "M",
                    List(8) { index ->
                        GeneratedMissionObjectiveDraft(
                            "Order $index", "Order a meal.", "Order expressed.",
                            coachingHints = listOf("First hint", "Second hint", "Third hint"),
                            targetVocabulary = listOf("First word", "Second word"),
                            examplePhrases = listOf("First phrase", "Second phrase"),
                            isRequired = true
                        )
                    }
                )
            )
        )
        store.save(offered)
        starter(offered)
        try {
            starter(offered)
            error("An already started recommendation was accepted twice")
        } catch (expected: IllegalArgumentException) {
            assertTrue(expected.message.orEmpty().contains("no longer available"))
        }
        database.openHelper.writableDatabase.query("SELECT COUNT(*) FROM conversations").use {
            assertTrue(it.moveToFirst())
            assertEquals(1, it.getInt(0))
        }
        assertEligibleIds(setOf("fresh"))
    }

    private suspend fun assertEligibleIds(expected: Set<String>) {
        val time = clock.millis()
        assertEquals(expected.size, dao.countEligibleRecommendations("A2", time))
        assertEquals(expected, dao.getEligibleRecommendations("A2", time, 100).map { it.recommendation.id }.toSet())
        assertEquals(expected, dao.getAllEligibleRecommendations("A2", time).map { it.recommendation.id }.toSet())
        val next = dao.getNextEligibleRecommendation("A2", time)?.recommendation?.id
        assertTrue(next in expected || (next == null && expected.isEmpty()))
        assertEquals(next, dao.observeNextEligibleRecommendation("A2", time).first()?.recommendation?.id)
    }

    private fun recommendation(id: String, scenario: String, status: String = "READY", level: String = "A2") =
        insert(
            "mission_thread_recommendations",
            mapOf(
                "id" to id, "scenarioType" to scenario, "status" to status, "cefrLevel" to level,
                "title" to id, "scenario" to "Scenario $id", "learnerRole" to "Learner",
                "assistantRole" to "Assistant"
            )
        )

    private fun mission(id: String, status: String, sourceId: String? = null, rootId: String? = null) {
        insert("conversations", mapOf("id" to "$id-thread", "isArchived" to 1, "cefrLevel" to "A2"))
        insert(
            "mission_templates",
            mapOf("id" to "$id-branch", "title" to "$id branch", "scenario" to "Branch", "difficulty" to "A2")
        )
        if (rootId != null) {
            insert("mission_templates", mapOf("id" to rootId, "difficulty" to "A2"))
        }
        insert(
            "conversation_missions",
            mapOf(
                "id" to id, "conversationId" to "$id-thread", "missionTemplateId" to "$id-branch",
                "rootMissionTemplateId" to rootId, "sourceRecommendationId" to sourceId, "status" to status
            )
        )
    }

    private fun message(id: String, conversationId: String, sender: String, content: String) =
        insert(
            "messages",
            mapOf(
                "id" to id, "conversationId" to conversationId, "sender" to sender,
                "content" to content, "createdAt" to clock.millis(), "sequenceNumber" to id.length.toLong()
            )
        )

    private fun insert(table: String, overrides: Map<String, Any?>) {
        val values = ContentValues()
        val sql = database.openHelper.writableDatabase
        sql.query("PRAGMA table_info(`$table`)").use { columns ->
            while (columns.moveToNext()) {
                val name = columns.getString(columns.getColumnIndexOrThrow("name"))
                if (name in overrides) {
                    when (val value = overrides[name]) {
                        null -> values.putNull(name)
                        is String -> values.put(name, value)
                        is Int -> values.put(name, value)
                        is Long -> values.put(name, value)
                        else -> error("Unsupported fixture value: $value")
                    }
                } else if (columns.getInt(columns.getColumnIndexOrThrow("notnull")) != 0 &&
                    columns.isNull(columns.getColumnIndexOrThrow("dflt_value"))
                ) {
                    when (columns.getString(columns.getColumnIndexOrThrow("type"))) {
                        "TEXT" -> values.put(name, "")
                        "INTEGER" -> values.put(name, 0L)
                        "REAL" -> values.put(name, 0.0)
                        else -> error("Unsupported fixture column: $name")
                    }
                }
            }
        }
        sql.insert(table, SQLiteDatabase.CONFLICT_ABORT, values)
    }
}
