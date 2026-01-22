package fr.innog.memoryflow.data.repository

import fr.innog.memoryflow.data.dao.CardDao
import fr.innog.memoryflow.data.dao.DeckDao
import fr.innog.memoryflow.data.local.entity.Deck
import fr.innog.memoryflow.data.local.relation.DeckWithCards
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.count
import javax.inject.Inject

class DeckRepository @Inject constructor(private val deckDao: DeckDao) {

    fun getAllDecks(): Flow<List<Deck>> {
        return deckDao.getAllDecks()
    }

    fun getAllDecksWithCards(): Flow<List<DeckWithCards>> {
        return deckDao.getAllDecksWithCards()
    }

    suspend fun insertDeck(deck: Deck) {
        deckDao.insert(deck)
    }

    suspend fun deleteDeck(deck: Deck) {
        deckDao.delete(deck)
    }

    suspend fun updateDeck(deck: Deck) {
        deckDao.update(deck)
    }

    suspend fun deckExist(deck: Deck) : Boolean {
        return deckDao.deckExist(deck.name)
    }


    suspend fun getDeckById(id: Long): DeckWithCards {
        return deckDao.getDeckById(id)
    }

}