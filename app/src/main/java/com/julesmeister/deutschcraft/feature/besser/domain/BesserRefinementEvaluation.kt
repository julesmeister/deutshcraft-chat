package com.julesmeister.deutschcraft.feature.besser.domain

enum class RefinementVerdict {
    PASSED,
    PARTIAL,
    NOT_DEMONSTRATED
}

data class BesserRefinementEvaluation(
    val demonstrated: Boolean,
    val score: Int,
    val feedback: String,
    val evidence: String
) {

    val verdict: RefinementVerdict
        get() =
            when {
                demonstrated &&
                        score >= PASS_THRESHOLD ->
                    RefinementVerdict.PASSED

                score >= PARTIAL_THRESHOLD ->
                    RefinementVerdict.PARTIAL

                else ->
                    RefinementVerdict.NOT_DEMONSTRATED
            }

    companion object {

        const val PASS_THRESHOLD =
            80

        const val PARTIAL_THRESHOLD =
            50
    }
}