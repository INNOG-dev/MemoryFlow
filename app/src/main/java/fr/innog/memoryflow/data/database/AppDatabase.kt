package fr.innog.memoryflow.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import fr.innog.memoryflow.data.dao.CardDao
import fr.innog.memoryflow.data.dao.DeckDao
import fr.innog.memoryflow.data.local.entity.Card
import fr.innog.memoryflow.data.local.entity.Deck

@Database (
    entities = [Deck::class, Card::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deckDao(): DeckDao

    abstract fun cardDao(): CardDao
}