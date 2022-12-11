package com.aditasha.sepatubersih.presentation.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aditasha.sepatubersih.R
import com.aditasha.sepatubersih.databinding.LayoutShoesItemBinding
import com.aditasha.sepatubersih.domain.model.SbShoes
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import javax.inject.Singleton

@Singleton
class FirebaseShoesAdapter(options: FirebaseRecyclerOptions<SbShoes>) :
    FirebaseRecyclerAdapter<SbShoes, FirebaseShoesAdapter.ShoesSheetViewHolder>(options) {
    private lateinit var profileOnClickCallback: ProfileOnClickCallback

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoesSheetViewHolder {
        val binding =
            LayoutShoesItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ShoesSheetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShoesSheetViewHolder, position: Int, model: SbShoes) {
        holder.bind(model)
    }

    override fun onDataChanged() {
        super.onDataChanged()
        profileOnClickCallback.onDataChanged()
    }

    inner class ShoesSheetViewHolder(private var binding: LayoutShoesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: SbShoes) {
            binding.apply {
                shoesName.text = data.name
                brandType.text = data.brandType
                color.text = itemView.resources.getString(R.string.color_placeholder, data.color)
                notes.text = itemView.resources.getString(R.string.notes_placeholder, data.notes)
                iconEdit.setOnClickListener { profileOnClickCallback.onEditClicked(data) }
                iconDelete.setOnClickListener { profileOnClickCallback.onDeleteClicked(data.key!!) }
                binding.root.setOnClickListener { profileOnClickCallback.onOrderClicked(data) }
            }
        }
    }

    fun setOnClickCallback(profileOnClickCallback: ProfileOnClickCallback) {
        this.profileOnClickCallback = profileOnClickCallback
    }
}