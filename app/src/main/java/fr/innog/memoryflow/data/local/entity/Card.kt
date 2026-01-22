package fr.innog.memoryflow.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import fr.innog.memoryflow.data.local.model.CardReviewData

@Entity(foreignKeys = [
    ForeignKey(
        entity = Deck::class,
        parentColumns = ["uid"],
        childColumns = ["deckId"],
        onDelete = ForeignKey.Companion.CASCADE
    )
],
indices = [Index("deckId")])
data class Card(@PrimaryKey(autoGenerate = true) val id : Long = 0,
                val deckId : Long,
                val question : String,
                val answer : String,
                val quizData : String?,
                val cardColorIndex : Int,
                @Embedded
                val cardReviewData: CardReviewData)