package fr.innog.memoryflow.ui.decks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.innog.memoryflow.data.local.entity.Deck
import fr.innog.memoryflow.data.local.relation.DeckWithCards
import fr.innog.memoryflow.data.mapper.isDueToStudy
import fr.innog.memoryflow.data.repository.CardRepository
import fr.innog.memoryflow.data.repository.DeckRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeckListViewModel @Inject constructor(private var repository: DeckRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    val decks = repository.getAllDecksWithCards().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val filteredDecks = combine(decks, _searchQuery) {
            list, query ->

        if(query.isBlank()) list
        else list.filter { it.deck.name.startsWith(query, true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun search(keyword: String)
    {
        _searchQuery.value = keyword
    }

    fun renameDeck(name: String, deck: Deck) {
        viewModelScope.launch {
            val updatedDeck = deck.copy(name = name)
            repository.updateDeck(updatedDeck)
        }
    }

    fun deleteDeck(deck: Deck) {
        viewModelScope.launch {
            repository.deleteDeck(deck)
        }
    }

    fun getDeckToStudyCount() : Int
    {
        return decks.value.count { deck ->
            deck.cards.any { it.isDueToStudy() }
        }
    }

    fun getCardCountToStudyInDeck(deck: DeckWithCards) : Int
    {
        return deck.cards.count { it -> it.isDueToStudy() }
    }

    suspend fun addDeck(deckName : String) : Boolean
    {
        val deck = Deck(name=deckName)

        val exist = repository.deckExist(deck)

        if(!exist)
            repository.insertDeck(deck)

        return !exist
    }

}