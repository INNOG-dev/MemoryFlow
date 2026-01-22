package fr.innog.memoryflow.data.local.model

sealed class QuizState {
    object Idle : QuizState()
    object Correct : QuizState()
    object Wrong : QuizState()
}