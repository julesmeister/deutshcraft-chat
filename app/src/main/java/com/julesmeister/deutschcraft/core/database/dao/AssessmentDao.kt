package com.julesmeister.deutschcraft.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.julesmeister.deutschcraft.core.cefr.model.AssessmentSource
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.core.cefr.model.GrammarFeature
import com.julesmeister.deutschcraft.core.cefr.model.MistakeSeverity
import com.julesmeister.deutschcraft.core.database.entity.AssessmentEvidenceEntity
import com.julesmeister.deutschcraft.core.database.entity.DetectedMistakeEntity
import com.julesmeister.deutschcraft.core.database.entity.EvidenceGrammarFeatureEntity
import com.julesmeister.deutschcraft.core.database.model.AssessmentScoreAverageRow
import com.julesmeister.deutschcraft.core.database.model.DailyAssessmentCountRow
import com.julesmeister.deutschcraft.core.database.model.GrammarFeatureSummaryRow
import com.julesmeister.deutschcraft.core.database.model.MistakeSummaryRow
import com.julesmeister.deutschcraft.core.database.relation.AssessmentEvidenceWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AssessmentDao {

    /*
     * Evidence writes
     */

    @Upsert
    abstract suspend fun upsertEvidence(
        evidence: AssessmentEvidenceEntity
    )

    @Upsert
    abstract suspend fun upsertEvidenceList(
        evidence: List<AssessmentEvidenceEntity>
    )

    @Upsert
    abstract suspend fun upsertGrammarFeatures(
        grammarFeatures:
        List<EvidenceGrammarFeatureEntity>
    )

    @Upsert
    abstract suspend fun upsertMistakes(
        mistakes: List<DetectedMistakeEntity>
    )

    /*
     * Child-record cleanup
     */

    @Query(
        """
        DELETE FROM evidence_grammar_features
        WHERE evidenceId = :evidenceId
        """
    )
    abstract suspend fun deleteGrammarFeaturesForEvidence(
        evidenceId: String
    )

    @Query(
        """
        DELETE FROM detected_mistakes
        WHERE evidenceId = :evidenceId
        """
    )
    abstract suspend fun deleteMistakesForEvidence(
        evidenceId: String
    )

    /*
     * Atomic assessment save
     */

    @Transaction
    open suspend fun saveAssessment(
        evidence: AssessmentEvidenceEntity,
        grammarFeatures:
        List<EvidenceGrammarFeatureEntity>,
        mistakes: List<DetectedMistakeEntity>
    ) {
        require(
            grammarFeatures.all { feature ->
                feature.evidenceId == evidence.id
            }
        ) {
            "Every grammar feature must belong " +
                    "to evidence ${evidence.id}"
        }

        require(
            mistakes.all { mistake ->
                mistake.evidenceId == evidence.id
            }
        ) {
            "Every mistake must belong " +
                    "to evidence ${evidence.id}"
        }

        upsertEvidence(evidence)

        /*
         * Delete old details before inserting the latest details.
         * This prevents stale rows after reanalysis.
         */
        deleteGrammarFeaturesForEvidence(evidence.id)
        deleteMistakesForEvidence(evidence.id)

        if (grammarFeatures.isNotEmpty()) {
            upsertGrammarFeatures(grammarFeatures)
        }

        if (mistakes.isNotEmpty()) {
            upsertMistakes(mistakes)
        }
    }

    /*
     * Basic evidence reads
     */

    @Query(
        """
        SELECT *
        FROM assessment_evidence
        WHERE id = :evidenceId
        LIMIT 1
        """
    )
    abstract suspend fun getEvidence(
        evidenceId: String
    ): AssessmentEvidenceEntity?

    @Query(
        """
        SELECT *
        FROM assessment_evidence
        WHERE messageId = :messageId
        LIMIT 1
        """
    )
    abstract suspend fun getEvidenceForMessage(
        messageId: String
    ): AssessmentEvidenceEntity?

    @Query(
        """
        SELECT *
        FROM assessment_evidence
        WHERE conversationId = :conversationId
        ORDER BY analyzedAt ASC
        """
    )
    abstract fun observeConversationEvidence(
        conversationId: String
    ): Flow<List<AssessmentEvidenceEntity>>

    @Query(
        """
        SELECT *
        FROM assessment_evidence
        WHERE profileId = :profileId
        ORDER BY analyzedAt DESC
        """
    )
    abstract fun observeProfileEvidence(
        profileId: String
    ): Flow<List<AssessmentEvidenceEntity>>

    @Query(
        """
        SELECT *
        FROM assessment_evidence
        WHERE profileId = :profileId
        ORDER BY analyzedAt DESC
        LIMIT :limit
        """
    )
    abstract suspend fun getRecentEvidence(
        profileId: String,
        limit: Int
    ): List<AssessmentEvidenceEntity>

    /*
     * Evidence with children
     */

    @Transaction
    @Query(
        """
        SELECT *
        FROM assessment_evidence
        WHERE id = :evidenceId
        LIMIT 1
        """
    )
    abstract suspend fun getEvidenceWithDetails(
        evidenceId: String
    ): AssessmentEvidenceWithDetails?

    @Transaction
    @Query(
        """
        SELECT *
        FROM assessment_evidence
        WHERE messageId = :messageId
        LIMIT 1
        """
    )
    abstract suspend fun getMessageAssessmentWithDetails(
        messageId: String
    ): AssessmentEvidenceWithDetails?

    @Transaction
    @Query(
        """
        SELECT *
        FROM assessment_evidence
        WHERE profileId = :profileId
        ORDER BY analyzedAt DESC
        LIMIT :limit
        """
    )
    abstract suspend fun getRecentEvidenceWithDetails(
        profileId: String,
        limit: Int
    ): List<AssessmentEvidenceWithDetails>

    @Transaction
    @Query(
        """
        SELECT *
        FROM assessment_evidence
        WHERE profileId = :profileId
        ORDER BY analyzedAt DESC
        """
    )
    abstract fun observeEvidenceWithDetails(
        profileId: String
    ): Flow<List<AssessmentEvidenceWithDetails>>

    /*
     * Grammar feature queries
     */

    @Query(
        """
        SELECT *
        FROM evidence_grammar_features
        WHERE evidenceId = :evidenceId
        ORDER BY grammarFeature ASC
        """
    )
    abstract suspend fun getGrammarFeaturesForEvidence(
        evidenceId: String
    ): List<EvidenceGrammarFeatureEntity>

    @Query(
        """
        SELECT
            grammarFeature AS grammarFeature,
            COUNT(*) AS totalEvidenceCount,
            SUM(
                CASE
                    WHEN demonstratedSuccessfully = 1
                    THEN 1
                    ELSE 0
                END
            ) AS successfulEvidenceCount,
            AVG(confidence) AS averageConfidence
        FROM evidence_grammar_features AS feature
        INNER JOIN assessment_evidence AS evidence
            ON feature.evidenceId = evidence.id
        WHERE evidence.profileId = :profileId
        GROUP BY grammarFeature
        ORDER BY totalEvidenceCount DESC
        """
    )
    abstract fun observeGrammarFeatureSummaries(
        profileId: String
    ): Flow<List<GrammarFeatureSummaryRow>>

    @Query(
        """
        SELECT COUNT(*)
        FROM evidence_grammar_features AS feature
        INNER JOIN assessment_evidence AS evidence
            ON feature.evidenceId = evidence.id
        WHERE evidence.profileId = :profileId
          AND feature.grammarFeature = :grammarFeature
          AND feature.demonstratedSuccessfully = 1
        """
    )
    abstract suspend fun getSuccessfulFeatureCount(
        profileId: String,
        grammarFeature: GrammarFeature
    ): Int

    /*
     * Mistake queries
     */

    @Query(
        """
        SELECT *
        FROM detected_mistakes
        WHERE evidenceId = :evidenceId
        ORDER BY severity DESC, detectedAt DESC
        """
    )
    abstract suspend fun getMistakesForEvidence(
        evidenceId: String
    ): List<DetectedMistakeEntity>

    @Query(
        """
        SELECT mistake.*
        FROM detected_mistakes AS mistake
        INNER JOIN assessment_evidence AS evidence
            ON mistake.evidenceId = evidence.id
        WHERE evidence.profileId = :profileId
          AND mistake.isResolved = 0
        ORDER BY
            mistake.lastObservedAt DESC,
            mistake.severity DESC
        """
    )
    abstract fun observeUnresolvedMistakes(
        profileId: String
    ): Flow<List<DetectedMistakeEntity>>

    @Query(
        """
        SELECT
            mistake.category AS category,
            mistake.grammarFeature AS grammarFeature,
            mistake.severity AS severity,
            SUM(mistake.occurrenceCount) AS totalOccurrences,
            MAX(mistake.lastObservedAt) AS latestOccurrenceAt
        FROM detected_mistakes AS mistake
        INNER JOIN assessment_evidence AS evidence
            ON mistake.evidenceId = evidence.id
        WHERE evidence.profileId = :profileId
          AND mistake.isResolved = 0
        GROUP BY
            mistake.category,
            mistake.grammarFeature,
            mistake.severity
        ORDER BY
            totalOccurrences DESC,
            latestOccurrenceAt DESC
        """
    )
    abstract fun observeMistakeSummaries(
        profileId: String
    ): Flow<List<MistakeSummaryRow>>

    @Query(
        """
        SELECT COUNT(*)
        FROM detected_mistakes AS mistake
        INNER JOIN assessment_evidence AS evidence
            ON mistake.evidenceId = evidence.id
        WHERE evidence.profileId = :profileId
          AND mistake.severity = :severity
          AND mistake.detectedAt >= :sinceTimestamp
        """
    )
    abstract suspend fun countRecentMistakesBySeverity(
        profileId: String,
        severity: MistakeSeverity,
        sinceTimestamp: Long
    ): Int

    @Query(
        """
        UPDATE detected_mistakes
        SET
            isResolved = 1,
            lastObservedAt = :resolvedAt
        WHERE id = :mistakeId
        """
    )
    abstract suspend fun markMistakeResolved(
        mistakeId: String,
        resolvedAt: Long
    ): Int

    /*
     * Score aggregation
     */

    @Query(
        """
        SELECT
            COALESCE(AVG(grammarScore), 0.0)
                AS grammarAverage,
            COALESCE(AVG(vocabularyScore), 0.0)
                AS vocabularyAverage,
            COALESCE(AVG(coherenceScore), 0.0)
                AS coherenceAverage,
            COALESCE(AVG(accuracyScore), 0.0)
                AS accuracyAverage,
            COALESCE(AVG(rangeScore), 0.0)
                AS rangeAverage,
            COUNT(*) AS assessmentCount
        FROM assessment_evidence
        WHERE profileId = :profileId
          AND analyzedAt >= :sinceTimestamp
        """
    )
    abstract suspend fun getAverageScoresSince(
        profileId: String,
        sinceTimestamp: Long
    ): AssessmentScoreAverageRow

    @Query(
        """
        SELECT COUNT(*)
        FROM assessment_evidence
        WHERE profileId = :profileId
        """
    )
    abstract suspend fun countAssessments(
        profileId: String
    ): Int

    @Query(
        """
        SELECT COUNT(DISTINCT messageId)
        FROM assessment_evidence
        WHERE profileId = :profileId
        """
    )
    abstract suspend fun countAssessedMessages(
        profileId: String
    ): Int

    @Query(
        """
        SELECT COUNT(*)
        FROM assessment_evidence
        WHERE profileId = :profileId
          AND assessedLevel = :level
        """
    )
    abstract suspend fun countEvidenceForLevel(
        profileId: String,
        level: CefrLevel
    ): Int

    @Query(
        """
        SELECT COUNT(*)
        FROM assessment_evidence
        WHERE profileId = :profileId
          AND source = :source
        """
    )
    abstract suspend fun countEvidenceBySource(
        profileId: String,
        source: AssessmentSource
    ): Int

    /*
     * SQLite date grouping uses Unix seconds,
     * while analyzedAt is stored as milliseconds.
     */

    @Query(
        """
        SELECT
            strftime(
                '%Y-%m-%d',
                analyzedAt / 1000,
                'unixepoch',
                'localtime'
            ) AS dayKey,
            COUNT(*) AS assessmentCount
        FROM assessment_evidence
        WHERE profileId = :profileId
        GROUP BY dayKey
        ORDER BY dayKey DESC
        LIMIT :limit
        """
    )
    abstract suspend fun getDailyAssessmentCounts(
        profileId: String,
        limit: Int
    ): List<DailyAssessmentCountRow>

    /*
     * Sync operations
     */

    @Query(
        """
        SELECT *
        FROM assessment_evidence
        WHERE syncStatus IN (
            'PENDING_UPLOAD',
            'PENDING_DELETE'
        )
        ORDER BY analyzedAt ASC
        """
    )
    abstract suspend fun getEvidencePendingSync():
            List<AssessmentEvidenceEntity>

    /*
     * Deletion
     */

    @Query(
        """
        DELETE FROM assessment_evidence
        WHERE id = :evidenceId
        """
    )
    abstract suspend fun deleteEvidence(
        evidenceId: String
    ): Int

    @Query(
        """
        DELETE FROM assessment_evidence
        WHERE conversationId = :conversationId
        """
    )
    abstract suspend fun deleteConversationEvidence(
        conversationId: String
    ): Int

    @Query(
        """
        DELETE FROM assessment_evidence
        WHERE profileId = :profileId
        """
    )
    abstract suspend fun deleteProfileEvidence(
        profileId: String
    ): Int
}