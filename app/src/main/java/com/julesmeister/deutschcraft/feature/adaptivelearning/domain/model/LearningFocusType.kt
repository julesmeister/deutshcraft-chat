package com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model

/*
 * Describes the broad learning area represented by an
 * adaptive-learning focus.
 *
 * DeutschCraft does not track only incorrect grammar.
 * It also tracks vocabulary and communication behaviors
 * expected at the learner's current CEFR level.
 */
enum class LearningFocusType {

    /*
     * Language-form issues such as:
     *
     * - Article case
     * - Verb conjugation
     * - Word order
     * - Tense usage
     */
    GRAMMAR,

    /*
     * Word-level issues such as:
     *
     * - Incorrect word choice
     * - Avoided vocabulary
     * - Repeated translations
     * - Low successful-use count
     */
    VOCABULARY,

    /*
     * Communicative behaviors that distinguish CEFR levels.
     *
     * Examples:
     *
     * - Giving reasons
     * - Connecting ideas
     * - Telling a story
     * - Expressing an opinion
     */
    COMMUNICATION
}