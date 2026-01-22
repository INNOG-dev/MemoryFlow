package fr.innog.memoryflow.data.mapper

import fr.innog.memoryflow.data.local.entity.Card
import fr.innog.memoryflow.data.local.model.Answer
import fr.innog.memoryflow.data.parser.QuizParser

fun Card.getQuizAnswers() : List<Answer> = QuizParser.parse(quizData)

fun Card.getCorrectAnswers() : MutableList<String> {
    val correctAnswers = mutableListOf<String>()
    getQuizAnswers().forEach { answer ->
        if(answer.isCorrect) correctAnswers.add(answer.value)
    }
    return correctAnswers
}

fun Card.hasQuiz() : Boolean {
    return getQuizAnswers().find { !it.value.isEmpty() } != null
}

fun Card.isDueToStudy(): Boolean {
    return cardReviewData.nextReviewDate <= System.currentTimeMillis()
}