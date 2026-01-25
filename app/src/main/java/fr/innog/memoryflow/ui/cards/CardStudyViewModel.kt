package fr.innog.memoryflow.ui.cards

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.innog.memoryflow.data.local.entity.Card
import fr.innog.memoryflow.data.local.model.QuizState
import fr.innog.memoryflow.data.mapper.getCorrectAnswers
import fr.innog.memoryflow.data.repository.CardRepository
import fr.innog.memoryflow.data.local.model.CardDifficulty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardStudyViewModel @Inject constructor(val repository: CardRepository) : ViewModel() {

    data class StudyUIState(
        val cardsToStudy: List<Card> = emptyList(),
        val currentDisplayedCard : Card? = null,
        val currentCardIndex: Int = 0,
        val isFinished: Boolean = false
    )

    private val _uiState = MutableStateFlow(StudyUIState())
    val uiState = _uiState.asStateFlow()

    var quizState = MutableStateFlow<QuizState>(QuizState.Idle)
        private set

    private val _selectedAnswers = MutableStateFlow<List<String>>(emptyList())

    val selectedAnswers = _selectedAnswers.asStateFlow()

    var lastSelectedAnswer : String? = null

    var deckId: Long = -1

    fun startStudySession(deckId : Long)
    {
        this.deckId = deckId

        viewModelScope.launch {
            val cards = repository.getCardsToStudy(deckId, System.currentTimeMillis()).first()
            _uiState.update {
                it.copy(cards, cards[it.currentCardIndex])
            }
        }
    }

    fun startQuiz()
    {
        quizState.value = QuizState.Idle
        lastSelectedAnswer = null
        _selectedAnswers.update { current -> emptyList() }
    }

    fun validateAnswer(answer: String)
    {
        val currentCard = _uiState.value.currentDisplayedCard

        if(currentCard != null)
        {
            if (quizState.value == QuizState.Idle) {
                val correctAnswers = currentCard.getCorrectAnswers()
                lastSelectedAnswer = answer

                if(!correctAnswers.contains(answer))
                {
                    quizState.value = QuizState.Wrong
                }
                else
                {
                    _selectedAnswers.update { current ->
                        current + answer
                    }

                    if(correctAnswers.size == _selectedAnswers.value.size) validateAnswers(_selectedAnswers.value)
                }
            }
        }
    }

    fun validateAnswers(answers : List<String>)
    {
        val currentCard = _uiState.value.currentDisplayedCard
        if(currentCard != null)
        {
            if (quizState.value == QuizState.Idle) {
                val correctAnswers = currentCard.getCorrectAnswers()
                quizState.value = if(correctAnswers.containsAll(answers)) QuizState.Correct else QuizState.Wrong
            }
        }
    }

    fun onApplyIntervalTime(difficulty: CardDifficulty) {
        applyIntervalTimes(difficulty)
        updateCard()
        nextCard()
    }

    fun updateCard()
    {
        val currentCard = _uiState.value.currentDisplayedCard!!
        viewModelScope.launch {
            repository.updateCard(currentCard)
        }
    }

    fun nextCard()
    {
        if(_uiState.value.currentCardIndex+1 > _uiState.value.cardsToStudy.size-1)
        {
            _uiState.update {
                it.copy(isFinished = true)
            }
        }
        else
        {
            _uiState.update {
                val index = it.currentCardIndex + 1
                val card = it.cardsToStudy[index]
                it.copy(it.cardsToStudy, card,index)
            }
        }
    }

    fun applyIntervalTimes(difficulty: CardDifficulty) {
        val currentCard = _uiState.value.currentDisplayedCard

        if(currentCard != null)
        {
            val review = currentCard.cardReviewData

            val prev = review.timeInterval
            when (difficulty) {
                CardDifficulty.AGAIN -> {
                    review.easeFactor -= 0.5f
                    review.easeFactor = review.easeFactor.coerceIn(1.3f, 3.0f)

                    review.timeInterval = maxOf(
                        CardDifficulty.AGAIN.initialInterval,
                        (prev * 0.5f).toLong()
                    )
                }

                CardDifficulty.HARD -> {
                    review.easeFactor -= 0.25f
                    review.easeFactor = review.easeFactor.coerceIn(1.3f, 3.0f)

                    review.timeInterval =  maxOf(
                        CardDifficulty.HARD.initialInterval,
                        (prev * 0.85f).toLong()
                    )
                }

                CardDifficulty.GOOD -> {
                    review.easeFactor += 0.25f
                    review.easeFactor = review.easeFactor.coerceIn(1.3f, 3.0f)

                    review.timeInterval =  maxOf(
                        CardDifficulty.GOOD.initialInterval,
                        (prev * review.easeFactor).toLong()
                    )
                }

                CardDifficulty.EASY -> {
                    review.easeFactor += 0.8f
                    review.easeFactor = review.easeFactor.coerceIn(1.3f, 3.0f)

                    review.timeInterval =  maxOf(
                        CardDifficulty.EASY.initialInterval,
                        (prev * review.easeFactor * 1.3).toLong()
                    )
                }
            }
            review.nextReviewDate = System.currentTimeMillis() + review.timeInterval * 1000L
        }
    }

    fun computeNextInterval(
        currentInterval: Long,
        easeFactor: Float,
        difficulty: CardDifficulty
    ): Long {
        return when (difficulty) {


            CardDifficulty.AGAIN -> {
                maxOf(
                    CardDifficulty.AGAIN.initialInterval,
                    (currentInterval.toDouble() * 0.5).toLong()
                )
            }

            CardDifficulty.HARD -> {
                maxOf(
                    CardDifficulty.HARD.initialInterval,
                    (currentInterval.toDouble() * 0.85).toLong()
                )
            }

            CardDifficulty.GOOD -> {
                maxOf(
                    CardDifficulty.GOOD.initialInterval,
                    (currentInterval.toDouble() * easeFactor).toLong()
                )
            }

            CardDifficulty.EASY -> {
                maxOf(
                    CardDifficulty.EASY.initialInterval,
                    (currentInterval.toDouble() * easeFactor * 1.3).toLong()
                )
            }
        }
    }

    fun getIntervalPreview(difficulty: CardDifficulty): Long {
        val card = _uiState.value.currentDisplayedCard ?: return -1
        val review = card.cardReviewData

        return computeNextInterval(
            currentInterval = review.timeInterval,
            easeFactor = review.easeFactor,
            difficulty = difficulty
        )
    }



}