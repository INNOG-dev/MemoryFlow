package fr.innog.memoryflow.ui.fragments.startfragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fr.innog.memoryflow.R
import fr.innog.memoryflow.databinding.ItemSliderBinding

class SliderAdapter(private val items: List<SliderItem>) : RecyclerView.Adapter<SliderAdapter.SliderViewHolder>() {

    inner class SliderViewHolder(val binding: ItemSliderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        val binding = ItemSliderBinding.inflate(LayoutInflater.from(parent.context),parent, false)

        return SliderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        val item = items[position]
        holder.binding.sliderImage.setImageResource(item.image)
        holder.binding.sliderText.text = item.text
    }

    override fun getItemCount() = items.size
}