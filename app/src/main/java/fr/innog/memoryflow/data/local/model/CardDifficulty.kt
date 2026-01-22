package fr.innog.memoryflow.data.local.model

enum class CardDifficulty(val initialInterval : Long) {
    AGAIN(60),
    HARD(AGAIN.initialInterval*20),
    GOOD(60*60*24),
    EASY(GOOD.initialInterval*2)
}