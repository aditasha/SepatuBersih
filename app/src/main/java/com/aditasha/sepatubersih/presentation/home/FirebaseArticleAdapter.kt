package com.aditasha.sepatubersih.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aditasha.sepatubersih.data.RealtimeDatabaseConstants
import com.aditasha.sepatubersih.databinding.LayoutArticleItemBinding
import com.aditasha.sepatubersih.domain.model.SbArticle
import com.aditasha.sepatubersih.presentation.GlideApp
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.storage.FirebaseStorage

class FirebaseArticleAdapter(
    options: FirebaseRecyclerOptions<SbArticle>,
    private val firebaseStorage: FirebaseStorage
) :
    FirebaseRecyclerAdapter<SbArticle, FirebaseArticleAdapter.OrderViewHolder>(options) {
    private lateinit var articleOnClickCallback: ArticleOnClickCallback

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
                GlideApp.with(itemView.context)
                    .load(imageRef)
                    .into(articlesImage)
                articlesTitle.text = data.name
                articlesSubTitle.text = data.desc
                root.setOnClickListener { articleOnClickCallback.onArticleClicked(data.link!!) }
            }
        }
    }

    fun setOnClickCallback(orderOnClickCallback: ArticleOnClickCallback) {
        this.articleOnClickCallback = orderOnClickCallback
    }
}