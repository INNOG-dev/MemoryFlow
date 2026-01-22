package fr.innog.memoryflow.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import fr.innog.memoryflow.data.local.entity.Card
import fr.innog.memoryflow.data.local.entity.Deck

data class DeckWithCards(
    @Embedded val deck: Deck,

    @Relation(
        parentColumn = "uid",
        entityColumn = "deckId"
    )
    val cards: List<Card>)