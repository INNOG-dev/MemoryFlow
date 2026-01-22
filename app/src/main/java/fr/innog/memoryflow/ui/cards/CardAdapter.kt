package fr.innog.memoryflow.ui.cards

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fr.innog.memoryflow.data.local.entity.Card
import fr.innog.memoryflow.databinding.ItemCardBinding
import fr.innog.memoryflow.data.local.model.CardColors
import org.json.JSONObject

class CardAdapter(private val editBtnCallback: (Card) -> Unit) : ListAdapter<Card, CardAdapter.CardViewHolder>(CardDiffCallback()) {

    class CardViewHolder(val binding: ItemCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CardViewHolder {
        val binding = ItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = getItem(position)

        with(holder.binding.webView) {
            settings.javaScriptEnabled = true
            holder.binding.webView.setBackgroundColor(Color.TRANSPARENT)

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    evaluateJavascript(
                        "setContent(${JSONObject.quote(card.question)})",
                        null
                    )
                }
            }

            loadUrl("file:///android_asset/display.html")
        }

        holder.binding.cardEditBtn.setOnClickListener { view ->
            editBtnCallback(card)
        }

        holder.binding.card.setCardBackgroundColor(CardColors.colors[card.cardColorIndex].color)
    }

    class CardDiffCallback : DiffUtil.ItemCallback<Card>() {
        override fun areItemsTheSame(oldItem: Card, newItem: Card) = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Card, newItem: Card) = oldItem == newItem
    }


}