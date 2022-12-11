package com.aditasha.sepatubersih.presentation.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aditasha.sepatubersih.R
import com.aditasha.sepatubersih.databinding.FragmentProfileEditBinding
import com.aditasha.sepatubersih.domain.model.Result
import com.aditasha.sepatubersih.presentation.auth.AuthViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class ProfileEditFragment : Fragment() {
    private val authViewModel: AuthViewModel by activityViewModels()
    private var _binding: FragmentProfileEditBinding? = null

    private val binding get() = _binding!!

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var number = ""
        val callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                firebaseAuth.currentUser?.updatePhoneNumber(credential)
                binding.loading.isVisible = false
                requireActivity().onNavigateUp()
            }

            override fun onVerificationFailed(e: FirebaseException) {
                e.localizedMessage?.let { showRegisterFailed(it) }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.authForm.collectLatest { state ->
                    binding.apply {
                        if (state.nameError == true) {
                            name.error = getString(R.string.name_cant_empty)
                        } else {
                            name.error = null
                            name.isErrorEnabled = false
                        }

                        if (state.numberError == true) {
                            phoneNumber.error = getString(R.string.invalid_number)
                        } else {
                            phoneNumber.error = null
                            phoneNumber.isErrorEnabled = false
                        }

                    }
                    binding.saveProfile.isEnabled = state.isDataValid
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.authResult.collect { loginResult ->
                when (loginResult) {
                    is Result.Success -> {
                        if (loginResult.data != null) {
                            val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                                .setPhoneNumber(number)
                                .setActivity(requireActivity())
                                .setTimeout(30L, TimeUnit.SECONDS)
                                .setCallbacks(callback)
                                .build()
                            PhoneAuthProvider.verifyPhoneNumber(options)
                            authViewModel.currentUser?.let { updateUiWithUser(it) }
                        }
                    }
                    is Result.Error -> {
                        loginResult.exception.localizedMessage?.let { showRegisterFailed(it) }
                    }
                    is Result.Loading -> binding.loading.isVisible = true
                }
            }
        }

        val user = authViewModel.currentUser
        if (user != null) {
            binding.apply {
                emailEditText.setText(user.email)
                nameEditText.setText(user.displayName)
                phoneNumberEditText.setText(user.phoneNumber)
            }
        }

        binding.apply {
            nameEditText.doOnTextChanged { text, _, _, _ ->
                if (text != null) checkForm()
            }

            phoneNumberEditText.doOnTextChanged { text, _, _, _ ->
                if (text != null) checkForm()
            }
        }

        binding.saveProfile.setOnClickListener {
            binding.loading.isVisible = true
//            number = PhoneNumberUtils.formatNumberToE164(numberEditText.text.toString(), "ID").toString()
            number = "+1 650-555-1234"
            firebaseAuth.firebaseAuthSettings.setAutoRetrievedSmsCodeForPhoneNumber(
                number,
                "123456"
            )
            authViewModel.editProfile(
                binding.nameEditText.text.toString()
            )
        }
    }

    private fun checkForm() {
        binding.apply {
            authViewModel.checkProfileEditForm(
                nameEditText.text.toString(),
                phoneNumberEditText.text.toString()
            )
        }
    }

    private fun updateUiWithUser(user: FirebaseUser) {
        val welcome = getString(R.string.welcome, user.displayName)
        // TODO : initiate successful logged in experience
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, welcome, Toast.LENGTH_LONG).show()
    }

    private fun showRegisterFailed(errorString: String) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}