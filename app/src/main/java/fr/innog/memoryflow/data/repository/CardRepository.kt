package fr.innog.memoryflow.data.repository

import fr.innog.memoryflow.data.dao.CardDao
import fr.innog.memoryflow.data.local.entity.Card
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class CardRepository @Inject constructor(private val cardDao: CardDao) {

    fun getCards(deckId : Long) : Flow<List<Card>> {
        return cardDao.getCards(deckId)
    }

    fun getCardsToStudy(deckId : Long, now : Long) : Flow<List<Card>> {
        return cardDao.getCardsToStudy(deckId, now)
    }


    suspend fun insertCard(card: Card)
    {
        cardDao.insert(card)
    }

    suspend fun deleteCard(card: Card)
    {
        cardDao.delete(card)
    }

    suspend fun getCard(cardId : Long) : Card
    {
        return cardDao.getCard(cardId)
    }

    suspend fun updateCard(card: Card)
    {
        cardDao.update(card)
    }



}