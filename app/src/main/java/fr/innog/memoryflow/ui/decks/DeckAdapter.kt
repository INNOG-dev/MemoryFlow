package fr.innog.memoryflow.ui.decks

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fr.innog.memoryflow.R
import fr.innog.memoryflow.data.local.entity.Deck
import fr.innog.memoryflow.data.local.relation.DeckWithCards
import fr.innog.memoryflow.databinding.ItemDeckLayoutBinding

class DeckAdapter(private val listener: DeckListener) : ListAdapter<DeckWithCards, DeckAdapter.DeckViewHolder>(DeckDiffCallback()) {

    interface DeckListener
    {
        fun onEdit(deck: Deck)
        fun onDelete(deck: Deck)

        fun onClicked(deck: Deck)

        fun getProgress(deckWithCards: DeckWithCards) : Int

        fun getCardCountToStudy(deckWithCards: DeckWithCards) : Int
    }

    class DeckViewHolder(val binding: ItemDeckLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeckAdapter.DeckViewHolder {
        val binding = ItemDeckLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeckViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeckAdapter.DeckViewHolder, position: Int) {
        val deckContainer = getItem(position)
        holder.binding.deckName.text = deckContainer.deck.name

        holder.binding.deckDetails.text = "${deckContainer.cards.size} ${if (deckContainer.cards.size > 1) "cartes" else "carte"} • ${listener.getCardCountToStudy(deckContainer)} à réviser"

        holder.binding.deckActionBtn.setOnClickListener { view ->
            showPopupMenu(view, deckContainer.deck)
        }

        holder.binding.deckCard.setOnClickListener { view ->
            listener.onClicked(deckContainer.deck)
        }

        holder.binding.StudyProgressbar.progress = listener.getProgress(deckContainer)
    }

    fun showPopupMenu(anchor: View, deck: Deck)
    {
        val popupMenu = PopupMenu(
            anchor.context,
            anchor,
            Gravity.END,
            0,
            R.style.ThemeOverlay_MemoryFlow_PopupMenu)


        popupMenu.setOnMenuItemClickListener { item ->
            when(item.itemId) {
                R.id.action_edit -> {
                    listener.onEdit(deck)
                    true
                }

                R.id.action_delete -> {
                    listener.onDelete(deck)
                    true
                }

                else -> false
            }

        }

        popupMenu.menuInflater.inflate(R.menu.menu_deck_options, popupMenu.menu)
        popupMenu.setForceShowIcon(true)
        popupMenu.show()
    }

    class DeckDiffCallback : DiffUtil.ItemCallback<DeckWithCards>() {
        override fun areItemsTheSame(oldItem: DeckWithCards, newItem: DeckWithCards) = oldItem.deck.uid == newItem.deck.uid
        override fun areContentsTheSame(oldItem: DeckWithCards, newItem: DeckWithCards) = oldItem == newItem
    }

}