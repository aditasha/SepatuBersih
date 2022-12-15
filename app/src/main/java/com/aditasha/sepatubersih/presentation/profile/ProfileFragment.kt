package com.aditasha.sepatubersih.presentation.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.aditasha.sepatubersih.R
import com.aditasha.sepatubersih.databinding.FragmentProfileBinding
import com.aditasha.sepatubersih.presentation.auth.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private val authViewModel: AuthViewModel by activityViewModels()
    private var _binding: FragmentProfileBinding? = null

    private val binding get() = _binding!!

    @Inject
    lateinit var firebaseAuth: FirebaseAuth
    private val authListener = FirebaseAuth.AuthStateListener {
        val user = authViewModel.currentUser
        binding.loggedIn.isVisible = user != null
        binding.loginLayout.isVisible = user == null
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
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = authViewModel.currentUser
        if (currentUser != null) {
            binding.userName.text = currentUser.displayName
            binding.userEmail.text = currentUser.email
            binding.userNumber.text = currentUser.phoneNumber
        }

        binding.addressItem.profileItemTextView.text = getString(R.string.registered_address)
        binding.shoesItem.profileItemTextView.text = getString(R.string.registered_shoes)
        binding.profileCard.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToProfileEditFragment()
            findNavController().navigate(action)
        }
        binding.addressItem.root.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToProfileBottomSheet(
                ProfileBottomSheet.ADDRESS
            )
            findNavController().navigate(action)
        }
        binding.shoesItem.root.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToProfileBottomSheet(
                ProfileBottomSheet.SHOES
            )
            findNavController().navigate(action)
        }
        binding.login.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToLoginFragment()
            findNavController().navigate(action)
        }
        binding.logout.setOnClickListener {
            authViewModel.logout()
            val action = ProfileFragmentDirections.actionProfileFragmentToHomeFragment()
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}