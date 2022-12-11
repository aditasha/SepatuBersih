package com.aditasha.sepatubersih

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.aditasha.sepatubersih.databinding.FragmentHomeBinding
import com.aditasha.sepatubersih.presentation.auth.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
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

        binding.servicesCard.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToServicesDetailFragment()
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}