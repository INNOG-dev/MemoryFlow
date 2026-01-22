package fr.innog.memoryflow.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import fr.innog.memoryflow.data.local.entity.Deck
import fr.innog.memoryflow.data.local.relation.DeckWithCards
import kotlinx.coroutines.flow.Flow

@Dao
interface DeckDao {

    @Query("SELECT * FROM deck")
    fun getAllDecks() : Flow<List<Deck>>

    @Transaction
    @Query("SELECT * FROM deck")
    fun getAllDecksWithCards() : Flow<List<DeckWithCards>>

    @Insert
    suspend fun insert(deck: Deck)

    @Delete
    suspend fun delete(deck: Deck)

    @Update
    suspend fun update(deck: Deck)

    @Query("SELECT EXISTS(SELECT 1 FROM deck WHERE name = :deckName)")
    suspend fun deckExist(deckName: String) : Boolean

    @Transaction
    @Query("SELECT * FROM deck WHERE uid = :deckId")
    suspend fun getDeckById(deckId : Long) : DeckWithCards

}