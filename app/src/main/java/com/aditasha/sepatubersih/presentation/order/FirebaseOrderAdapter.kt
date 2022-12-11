package com.aditasha.sepatubersih.presentation.order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.aditasha.sepatubersih.R
import com.aditasha.sepatubersih.data.RealtimeDatabaseConstants
import com.aditasha.sepatubersih.databinding.LayoutOrderItemBinding
import com.aditasha.sepatubersih.domain.model.SbOrderItem
import com.firebase.ui.database.paging.DatabasePagingOptions
import com.firebase.ui.database.paging.FirebaseRecyclerPagingAdapter
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class FirebaseOrderAdapter(options: DatabasePagingOptions<SbOrderItem>) :
    FirebaseRecyclerPagingAdapter<SbOrderItem, FirebaseOrderAdapter.OrderViewHolder>(options) {
    private lateinit var orderOnClickCallback: OrderOnClickCallback

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding =
            LayoutOrderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: OrderViewHolder, position: Int, model: SbOrderItem) {
        viewHolder.bind(model)
    }

    inner class OrderViewHolder(private var binding: LayoutOrderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: SbOrderItem) {
            val resource = itemView.resources
            val date = Date(data.orderTimestamp!!)
            val sfd = SimpleDateFormat("dd MMMM yyyy, HH:mm")
            val timestamp = sfd.format(date)
            binding.apply {
                val format = DecimalFormat("0.000")
                val text = resource.getString(R.string.price_placeholder, format.format(data.price))
                totalPrice.text = text
                orderTimestamp.text = timestamp
                orderStatus.text = data.status
                orderStatus.background = when (data.status) {
                    RealtimeDatabaseConstants.NEED_PAYMENT -> ContextCompat.getDrawable(
                        itemView.context,
                        R.drawable.order_yellow
                    )
                    RealtimeDatabaseConstants.COMPLETE -> ContextCompat.getDrawable(
                        itemView.context,
                        R.drawable.order_green
                    )
                    RealtimeDatabaseConstants.CANCELLED -> ContextCompat.getDrawable(
                        itemView.context,
                        R.drawable.order_red
                    )
                    RealtimeDatabaseConstants.EXPIRED -> ContextCompat.getDrawable(
                        itemView.context,
                        R.drawable.order_red
                    )
                    else -> ContextCompat.getDrawable(itemView.context, R.drawable.order_blue)
                }
                root.setOnClickListener { orderOnClickCallback.onOrderClicked(data.key!!) }
            }
        }
    }

    fun setOnClickCallback(orderOnClickCallback: OrderOnClickCallback) {
        this.orderOnClickCallback = orderOnClickCallback
    }

}