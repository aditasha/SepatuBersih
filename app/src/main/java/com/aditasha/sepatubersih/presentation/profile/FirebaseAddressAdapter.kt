package com.aditasha.sepatubersih.presentation.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aditasha.sepatubersih.R
import com.aditasha.sepatubersih.databinding.LayoutAddressItemBinding
import com.aditasha.sepatubersih.domain.model.SbAddress
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import javax.inject.Singleton

@Singleton
class FirebaseAddressAdapter(options: FirebaseRecyclerOptions<SbAddress>) :
    FirebaseRecyclerAdapter<SbAddress, FirebaseAddressAdapter.AddressSheetViewHolder>(options) {
    private lateinit var profileOnClickCallback: ProfileOnClickCallback

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressSheetViewHolder {
        val binding =
            LayoutAddressItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AddressSheetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AddressSheetViewHolder, position: Int, model: SbAddress) {
        holder.bind(model)
    }

    override fun onDataChanged() {
        super.onDataChanged()
        profileOnClickCallback.onDataChanged()
    }

    inner class AddressSheetViewHolder(private var binding: LayoutAddressItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: SbAddress) {
            binding.apply {
                addressName.text = data.name
                address.text = data.address
                addressNotes.text = itemView.resources.getString(R.string.notes_placeholder, data.note)
                iconEdit.setOnClickListener { profileOnClickCallback.onEditClicked(data) }
                iconDelete.setOnClickListener { profileOnClickCallback.onDeleteClicked(data.key!!, data.name!!) }
                binding.root.setOnClickListener { profileOnClickCallback.onOrderClicked(data) }
            }
        }
    }

    fun setOnClickCallback(profileOnClickCallback: ProfileOnClickCallback) {
        this.profileOnClickCallback = profileOnClickCallback
    }
}