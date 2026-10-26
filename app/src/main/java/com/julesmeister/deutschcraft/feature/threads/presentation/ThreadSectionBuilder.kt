package com.julesmeister.deutschcraft.feature.threads.presentation

import com.julesmeister.deutschcraft.feature.threads.domain.ConversationThread
import com.julesmeister.deutschcraft.feature.threads.domain.ThreadSection
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import javax.inject.Inject

class ThreadSectionBuilder @Inject constructor(
    private val clock: Clock
) {

    fun build(
        threads: List<ConversationThread>
    ): List<ThreadSection> {
        if (threads.isEmpty()) {
            return emptyList()
        }

        val today =
            currentLocalDate()

        val todayEpochDay =
            today.toEpochDay()

        val yesterdayEpochDay =
            today
                .minusDays(1)
                .toEpochDay()

        return threads
            .groupBy { thread ->
                thread.epochDay
            }
            .toSortedMap(
                compareByDescending { epochDay ->
                    epochDay
                }
            )
            .map { entry ->
                val epochDay =
                    entry.key

                val dailyThreads =
                    entry.value

                ThreadSection(
                    id =
                        epochDay,
                    title =
                        createSectionTitle(
                            epochDay =
                                epochDay,
                            todayEpochDay =
                                todayEpochDay,
                            yesterdayEpochDay =
                                yesterdayEpochDay
                        ),
                    threads =
                        dailyThreads
                            .sortedByDescending { thread ->
                                thread.updatedAt
                            }
                )
            }
    }

    private fun createSectionTitle(
        epochDay: Long,
        todayEpochDay: Long,
        yesterdayEpochDay: Long
    ): String {
        return when (epochDay) {
            todayEpochDay -> {
                TODAY_LABEL
            }

            yesterdayEpochDay -> {
                YESTERDAY_LABEL
            }

            else -> {
                formatDate(
                    epochDay =
                        epochDay
                )
            }
        }
    }

    private fun formatDate(
        epochDay: Long
    ): String {
        val date =
            LocalDate.ofEpochDay(
                epochDay
            )

        return date.format(
            DATE_FORMATTER
        )
    }

    private fun currentLocalDate():
            LocalDate {
        return clock
            .instant()
            .atZone(
                clock.zone
            )
            .toLocalDate()
    }

    private companion object {
        const val TODAY_LABEL =
            "Heute"

        const val YESTERDAY_LABEL =
            "Gestern"

        val DATE_FORMATTER:
                DateTimeFormatter =
            DateTimeFormatter
                .ofLocalizedDate(
                    FormatStyle.LONG
                )
                .withLocale(
                    Locale.GERMAN
                )
    }
}