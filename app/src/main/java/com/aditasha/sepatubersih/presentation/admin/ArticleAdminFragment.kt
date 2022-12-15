package com.aditasha.sepatubersih.presentation.admin

import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.os.BuildCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.aditasha.sepatubersih.data.RealtimeDatabaseConstants
import com.aditasha.sepatubersih.databinding.FragmentArticleAdminBinding
import com.aditasha.sepatubersih.domain.model.SbArticle
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@BuildCompat.PrereleaseSdkCheck
@AndroidEntryPoint
class ArticleAdminFragment : Fragment() {
    private var _binding: FragmentArticleAdminBinding? = null

    private val binding get() = _binding!!

    private val adminViewModel: OrderAdminViewModel by activityViewModels()

    @Inject
    lateinit var firebaseDatabase: FirebaseDatabase

    @Inject
    lateinit var firebaseStorage: FirebaseStorage

    private var articleAdapter: FirebaseArticleAdminAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArticleAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loading.isVisible = true

        val query = firebaseDatabase.reference.child(RealtimeDatabaseConstants.ARTICLE)
            .orderByChild("reverseStamp")

        val recyclerOptions = FirebaseRecyclerOptions.Builder<SbArticle>()
            .setLifecycleOwner(this)
            .setQuery(query, SbArticle::class.java)
            .build()
        articleAdapter = FirebaseArticleAdminAdapter(recyclerOptions, firebaseStorage)

        binding.apply {
            recycler.apply {
                layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
                adapter = articleAdapter
            }
        }

        articleAdapter?.setOnClickCallback(object : ArticleAdminOnClickCallback {
            override fun onArticleClicked(link: String) {
                CustomTabsIntent.Builder().build().launchUrl(requireContext(), Uri.parse(link))
            }

            override fun onArticleEdit(data: Parcelable) {
                val addArticle = AddArticleFragment()
                val args = bundleOf("article" to data as SbArticle)
                addArticle.arguments = args
                addArticle.show(childFragmentManager, "article")
            }

            override fun onArticleDelete(key: String) {
                adminViewModel.deleteArticle(key)
            }

            override fun onDataChanged() {
                binding.loading.isVisible = false
            }

        })

        binding.apply {
            addButton.setOnClickListener {
                AddArticleFragment().show(childFragmentManager, "article")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        articleAdapter?.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}