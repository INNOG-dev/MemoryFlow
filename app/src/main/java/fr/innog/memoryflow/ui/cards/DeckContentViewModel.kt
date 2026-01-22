package fr.innog.memoryflow.ui.cards

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.innog.memoryflow.core.navigation.NavKeys
import fr.innog.memoryflow.data.local.entity.Card
import fr.innog.memoryflow.data.mapper.isDueToStudy
import fr.innog.memoryflow.data.repository.CardRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.emptyList

@HiltViewModel
class DeckContentViewModel  @Inject constructor(private var repository: CardRepository,  savedStateHandle: SavedStateHandle) : ViewModel() {

    val deckId: Long = savedStateHandle[NavKeys.ARG_DECK_ID] ?: error("deckId manquant")

    val cards = repository.getCards(deckId).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val timeTicker = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(60_000) // toutes les 60 secondes
        }
    }

    val cardsToStudy = combine(cards, timeTicker)
    { cards, now ->
        cards.filter { it.cardReviewData.nextReviewDate <= now }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )


    fun hasAnyCard() : Boolean
    {
        return cards.value.isNotEmpty()
    }

    fun deleteCard(card: Card)
    {
        viewModelScope.launch {
            repository.deleteCard(card)
        }
    }

    fun duplicateCard(card: Card)
    {
        val copy = card.copy(0,card.deckId, card.question, card.answer,card.quizData, card.cardColorIndex)

        saveCard(copy)
    }

    fun saveCard(card: Card)
    {
        viewModelScope.launch {
            repository.insertCard(card)
        }
    }


}