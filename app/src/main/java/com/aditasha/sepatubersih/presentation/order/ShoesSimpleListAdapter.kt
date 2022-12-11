package com.aditasha.sepatubersih.presentation.order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aditasha.sepatubersih.R
import com.aditasha.sepatubersih.databinding.LayoutShoesSimpleBinding
import com.aditasha.sepatubersih.domain.model.SbShoes
import java.text.DecimalFormat
import javax.inject.Singleton

@Singleton
class ShoesSimpleListAdapter :
    ListAdapter<SbShoes, ShoesSimpleListAdapter.ListViewHolder>(DIFF_UTIL) {
    private val basePrice = 50L

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding =
            LayoutShoesSimpleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val data = getItem(position)
        holder.bind(data)
    }

    inner class ListViewHolder(private var binding: LayoutShoesSimpleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: SbShoes) {
            val format = DecimalFormat("0.000")
            val shoesPrice = itemView.resources.getString(R.string.price_placeholder, format.format(basePrice))
            binding.apply {
                brandType.text = data.brandType
                color.text = itemView.resources.getString(R.string.color_placeholder, data.color)
                notes.text = itemView.resources.getString(R.string.notes_placeholder, data.notes)
                price.text = shoesPrice
            }
        }
    }

    companion object {
        private val DIFF_UTIL = object : DiffUtil.ItemCallback<SbShoes>() {
            override fun areItemsTheSame(oldItem: SbShoes, newItem: SbShoes): Boolean {
                return oldItem.key == newItem.key
            }

            override fun areContentsTheSame(oldItem: SbShoes, newItem: SbShoes): Boolean {
                return oldItem == newItem
            }
        }
    }
}
