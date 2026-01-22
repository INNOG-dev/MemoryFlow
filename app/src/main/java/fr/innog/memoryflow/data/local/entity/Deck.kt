package fr.innog.memoryflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Deck(@PrimaryKey(autoGenerate = true) val uid: Long = 0,
                var name: String)