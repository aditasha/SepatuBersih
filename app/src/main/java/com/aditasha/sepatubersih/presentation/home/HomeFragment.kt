package com.aditasha.sepatubersih.presentation.home

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.aditasha.sepatubersih.R
import com.aditasha.sepatubersih.data.RealtimeDatabaseConstants
import com.aditasha.sepatubersih.databinding.FragmentHomeBinding
import com.aditasha.sepatubersih.domain.model.SbArticle
import com.aditasha.sepatubersih.presentation.auth.AuthViewModel
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private val authViewModel: AuthViewModel by activityViewModels()

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @Inject
    lateinit var firebaseAuth: FirebaseAuth
    @Inject
    lateinit var firebaseDatabase: FirebaseDatabase
    @Inject
    lateinit var firebaseStorage: FirebaseStorage

    private var articleAdapter: FirebaseArticleAdapter? = null

    private val authListener = FirebaseAuth.AuthStateListener {
        val user = authViewModel.currentUser
        if (user == null) {
            binding.welcome.text = getString(R.string.home_welcome, "guest")
        } else binding.welcome.text = getString(R.string.home_welcome, user.displayName)
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(authListener)
    }

    override fun onStop() {
        super.onStop()
        firebaseAuth.removeAuthStateListener(authListener)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val query = firebaseDatabase.reference.child(RealtimeDatabaseConstants.ARTICLE)
            .orderByChild("reverseStamp")

        val recyclerOptions = FirebaseRecyclerOptions.Builder<SbArticle>()
            .setLifecycleOwner(this)
            .setQuery(query, SbArticle::class.java)
            .build()

        articleAdapter = FirebaseArticleAdapter(recyclerOptions, firebaseStorage)

        binding.apply {
            articleRecycler.apply {
                layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
                adapter = articleAdapter
            }
        }

        articleAdapter?.setOnClickCallback(object : ArticleOnClickCallback {
            override fun onArticleClicked(link: String) {
                CustomTabsIntent.Builder().build().launchUrl(requireContext(), Uri.parse(link))
            }

            override fun onDataChanged() {
            }

        })

        binding.servicesCard.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToServicesDetailFragment()
            findNavController().navigate(action)
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