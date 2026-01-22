package fr.innog.memoryflow.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import fr.innog.memoryflow.data.local.entity.Card
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {

    @Query("SELECT * FROM card INNER JOIN deck ON deck.uid = :deckId AND deck.uid = card.deckId")
    fun getCards(deckId : Long) : Flow<List<Card>>

    @Query("SELECT * FROM card INNER JOIN deck ON deck.uid = :deckId AND deck.uid = card.deckId AND card.nextReviewDate <= :now")
    fun getCardsToStudy(deckId : Long, now : Long) : Flow<List<Card>>

    @Insert
    suspend fun insert(card: Card)

    @Delete
    suspend fun delete(card: Card)

    @Update
    suspend fun update(card: Card)

    @Query("SELECT * FROM card WHERE id = :cardId")
    suspend fun getCard(cardId: Long) : Card
}