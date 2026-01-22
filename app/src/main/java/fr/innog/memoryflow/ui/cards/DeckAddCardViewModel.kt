package fr.innog.memoryflow.ui.cards

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.innog.memoryflow.core.navigation.NavKeys
import fr.innog.memoryflow.data.local.entity.Card
import fr.innog.memoryflow.data.local.model.Answer
import fr.innog.memoryflow.data.local.model.CardReviewData
import fr.innog.memoryflow.data.mapper.getQuizAnswers
import fr.innog.memoryflow.data.repository.CardRepository
import fr.innog.memoryflow.data.local.model.CardSide
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeckAddCardViewModel @Inject constructor(private var repository: CardRepository, savedStateHandle: SavedStateHandle) : ViewModel() {


    data class UIData(var rectoHtml: String = "", var versoHtml: String = "" , var currentSide : CardSide = CardSide.RECTO, var selectedCardColorIndex : Int = 0)

    var data : UIData = UIData()

    var currentEditingCard : Card? = null

    val answers = MutableList(4) {
        Answer("", false)
    }

    val deckId: Long = savedStateHandle[NavKeys.ARG_DECK_ID] ?: error("deckId manquant")


    suspend fun getCard(cardId : Long) : Card
    {
        return repository.getCard(cardId)
    }

    fun setEditingCard(cardId: Long)
    {
        setSide(CardSide.RECTO)

        if(currentEditingCard != null) return

        if(cardId.toInt() != -1)
        {
            viewModelScope.launch {
                currentEditingCard = getCard(cardId)

                //loading data
                if(currentEditingCard != null)
                {
                    val _currentEditingCard = currentEditingCard!!

                    data.rectoHtml = _currentEditingCard.question
                    data.versoHtml = _currentEditingCard.answer
                    data.selectedCardColorIndex = _currentEditingCard.cardColorIndex

                    _currentEditingCard.getQuizAnswers().forEachIndexed { index, answer ->
                        onQuizAnswerFilled(index, answer.value)
                        onQuizAnswerChecked(index, answer.isCorrect)
                    }

                }
            }
        }
    }

    fun setSide(side: CardSide)
    {
        data.currentSide = side
    }

    fun getContent(side: CardSide) : String
    {
        return if(side == CardSide.RECTO) data.rectoHtml else data.versoHtml
    }

    fun setContent(html: String, side: CardSide)
    {
        if(side == CardSide.RECTO)
        {
            data.rectoHtml = html
        }
        else
        {
            data.versoHtml = html
        }
    }

    fun editCard()
    {
        if(currentEditingCard != null)
        {
            val _currentEditingCard = currentEditingCard!!
            viewModelScope.launch {
                val copy = _currentEditingCard.copy(_currentEditingCard.id,_currentEditingCard.deckId, data.rectoHtml, data.versoHtml,Gson().toJson(answers), data.selectedCardColorIndex)

                repository.updateCard(copy)
            }
        }
    }

    fun addCard()
    {
        val card = Card(
            0, deckId, data.rectoHtml, data.versoHtml, Gson().toJson(answers), data.selectedCardColorIndex,
            CardReviewData()
        )

        viewModelScope.launch {
            repository.insertCard(card)
        }
    }

    fun onColorSelected(colorIndex : Int)
    {
        data.selectedCardColorIndex = colorIndex
    }

    fun onQuizAnswerFilled(index: Int, value: String)
    {
        answers[index].value = value
    }

    fun onQuizAnswerChecked(index: Int, state: Boolean)
    {
        answers[index].isCorrect = state
    }

    fun getFilledAnswerCount() : Int
    {
        return answers.count { answer -> !answer.value.isEmpty() }
    }

    fun hasQuiz() : Boolean
    {
        return getFilledAnswerCount() > 0
    }

    fun getCorrectAnswerCount() : Int
    {
        return answers.count { answer -> answer.isCorrect }
    }

    fun isContentEmpty(side : CardSide): Boolean {
        return getContent(side)
            .replace("<p><br></p>", "")
            .replace("&nbsp;", "")
            .trim()
            .isEmpty()
    }



}