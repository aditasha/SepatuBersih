package com.aditasha.sepatubersih.presentation.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.aditasha.sepatubersih.data.RealtimeDatabaseConstants
import com.aditasha.sepatubersih.databinding.LayoutArticleItemBinding
import com.aditasha.sepatubersih.domain.model.SbArticle
import com.aditasha.sepatubersih.presentation.GlideApp
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.storage.FirebaseStorage

class FirebaseArticleAdminAdapter(
    options: FirebaseRecyclerOptions<SbArticle>,
    private val firebaseStorage: FirebaseStorage
) :
    FirebaseRecyclerAdapter<SbArticle, FirebaseArticleAdminAdapter.OrderViewHolder>(options) {
    private lateinit var articleOnClickCallback: ArticleAdminOnClickCallback

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding =
            LayoutArticleItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int, model: SbArticle) {
        holder.bind(model)
    }

    override fun onDataChanged() {
        super.onDataChanged()
        articleOnClickCallback.onDataChanged()
    }

    inner class OrderViewHolder(private var binding: LayoutArticleItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: SbArticle) {
            binding.apply {
                val imageRef = firebaseStorage.reference.child(RealtimeDatabaseConstants.ARTICLE)
                    .child(data.key!!)
                    .child(data.image!!)

                val circularProgressDrawable = CircularProgressDrawable(itemView.context)
                circularProgressDrawable.setColorSchemeColors(android.R.attr.colorPrimary)
                circularProgressDrawable.strokeWidth = 5f
                circularProgressDrawable.centerRadius = 15f
                circularProgressDrawable.start()

                GlideApp.with(itemView.context)
                    .load(imageRef)
//                    .placeholder(circularProgressDrawable)
                    .into(articlesImage)

                articlesTitle.text = data.name
                articlesSubTitle.text = data.desc
                iconEdit.isVisible = true
                iconDelete.isVisible = true
                iconEdit.setOnClickListener { articleOnClickCallback.onArticleEdit(data) }
                iconDelete.setOnClickListener { articleOnClickCallback.onArticleDelete(data.key!!, data.name!!) }
                root.setOnClickListener { articleOnClickCallback.onArticleClicked(data.link!!) }
            }
        }
    }

    fun setOnClickCallback(orderOnClickCallback: ArticleAdminOnClickCallback) {
        this.articleOnClickCallback = orderOnClickCallback
    }
}