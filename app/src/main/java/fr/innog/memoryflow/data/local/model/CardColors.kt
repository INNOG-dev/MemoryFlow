package fr.innog.memoryflow.data.local.model

object CardColors {
    val purple = CardColor(0xFFab008e.toInt(), "Violet")
    val red  = CardColor(0xFFEF4444.toInt(), "Rouge")
    val blue   = CardColor(0xFF3B82F6.toInt(), "Bleu")
    val green   = CardColor(0xFF22C55E.toInt(), "Vert")
    val yellow  = CardColor(0xFFEAB308.toInt(), "Jaune")
    val orange = CardColor(0xFFF97316.toInt(), "Orange")

    val colors = listOf(purple, red, blue, green, yellow, orange)
}