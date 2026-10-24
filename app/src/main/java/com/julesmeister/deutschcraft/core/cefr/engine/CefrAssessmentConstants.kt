package com.julesmeister.deutschcraft.core.cefr.engine

internal object AssessmentConstants {

    const val MINIMUM_SCORE = 0
    const val MAXIMUM_SCORE = 100

    const val MINIMUM_CONFIDENCE = 0.0
    const val MAXIMUM_CONFIDENCE = 1.0

    const val DEFAULT_INITIAL_SCORE = 0
    const val DEFAULT_INITIAL_CONFIDENCE = 0.0

    const val MAXIMUM_RECENT_EVIDENCE = 30

    const val CONFIDENCE_EVIDENCE_TARGET = 40.0

    const val LOCAL_EVIDENCE_WEIGHT = 0.60
    const val REMOTE_AI_EVIDENCE_WEIGHT = 1.00
    const val HYBRID_EVIDENCE_WEIGHT = 1.15

    const val MINIMUM_PROGRESSION_CONFIDENCE = 0.65

    const val SCORE_READINESS_WEIGHT = 0.35
    const val SKILL_READINESS_WEIGHT = 0.25
    const val MESSAGE_READINESS_WEIGHT = 0.15
    const val ACTIVE_DAY_READINESS_WEIGHT = 0.10
    const val ERROR_READINESS_WEIGHT = 0.15

    const val GRAMMAR_OVERALL_WEIGHT = 0.20
    const val VOCABULARY_OVERALL_WEIGHT = 0.20
    const val WRITTEN_INTERACTION_OVERALL_WEIGHT = 0.15
    const val FLUENCY_OVERALL_WEIGHT = 0.10
    const val COHERENCE_OVERALL_WEIGHT = 0.15
    const val ACCURACY_OVERALL_WEIGHT = 0.10
    const val RANGE_OVERALL_WEIGHT = 0.10
}