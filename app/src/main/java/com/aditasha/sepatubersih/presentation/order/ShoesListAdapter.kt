package com.aditasha.sepatubersih.presentation.order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aditasha.sepatubersih.R
import com.aditasha.sepatubersih.databinding.LayoutShoesItemBinding
import com.aditasha.sepatubersih.domain.model.SbShoes
import javax.inject.Singleton

@Singleton
class ShoesListAdapter(private val deleteClickListener: (String) -> Unit) :
    ListAdapter<SbShoes, ShoesListAdapter.ListViewHolder>(
        DIFF_UTIL
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding =
            LayoutShoesItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val data = getItem(position)
        holder.bind(data, deleteClickListener)
    }

    inner class ListViewHolder(private var binding: LayoutShoesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: SbShoes, deleteClickListener: (String) -> Unit) {
            binding.apply {
                shoesName.text = data.name
                brandType.text = data.brandType
                color.text = itemView.resources.getString(R.string.color_placeholder, data.color)
                notes.text = itemView.resources.getString(R.string.notes_placeholder, data.notes)
                iconEdit.isVisible = false
                iconDelete.setOnClickListener { deleteClickListener(data.key!!) }
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
