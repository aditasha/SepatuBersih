package com.aditasha.sepatubersih.presentation.auth

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
import androidx.navigation.fragment.findNavController
import com.aditasha.sepatubersih.R
import com.aditasha.sepatubersih.databinding.FragmentRegisterBinding
import com.aditasha.sepatubersih.domain.model.Result
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private val authViewModel: AuthViewModel by activityViewModels()
    private var _binding: FragmentRegisterBinding? = null

    private val binding get() = _binding!!

    @Inject
    lateinit var firebaseAuth: FirebaseAuth
    private var currentUser: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emailEditText = binding.emailEditText
        val userNameEditText = binding.userNameEditText
        val numberEditText = binding.phoneNumberEditText
        val passwordEditText = binding.passwordEditText
        val registerButton = binding.register
        val loadingProgressBar = binding.loading


        currentUser = authViewModel.currentUser
        var number = ""

        val callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                firebaseAuth.currentUser?.updatePhoneNumber(credential)
                loadingProgressBar.isVisible = false
                val action = RegisterFragmentDirections.actionRegisterFragmentToHomeFragment()
                findNavController().navigate(action)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                e.localizedMessage?.let { showRegisterFailed(it) }
            }

            override fun onCodeSent(
                verificationId: String,
                resendToken: PhoneAuthProvider.ForceResendingToken
            ) {

            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.authForm.collectLatest { state ->
                    binding.apply {
                        if (state.emailError == true) {
                            email.error = getString(R.string.invalid_email)
                        } else {
                            email.error = null
                            email.isErrorEnabled = false
                        }

                        if (state.emailError == true) {
                            email.error = getString(R.string.invalid_email)
                        } else {
                            email.error = null
                            email.isErrorEnabled = false
                        }

                        if (state.numberError == true) {
                            phoneNumber.error = getString(R.string.invalid_number)
                        } else {
                            phoneNumber.error = null
                            phoneNumber.isErrorEnabled = false
                        }

                        if (state.passwordError == true) {
                            password.error = getString(R.string.invalid_password)
                        } else {
                            password.error = null
                            password.isErrorEnabled = false
                        }
                    }
                    registerButton.isEnabled = state.isDataValid
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.authResult.collect { loginResult ->
                loadingProgressBar.isVisible = false
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
                            updateUiWithUser(currentUser!!)
                        }
                    }
                    is Result.Error -> {
                        loginResult.exception.localizedMessage?.let { showRegisterFailed(it) }
                    }
                    is Result.Loading -> loadingProgressBar.isVisible = true
                }
            }
        }

        emailEditText.doOnTextChanged { text, _, _, _ ->
            if (text != null) checkForm()
        }

        userNameEditText.doOnTextChanged { text, _, _, _ ->
            if (text != null) checkForm()
        }

        numberEditText.doOnTextChanged { text, _, _, _ ->
            if (text != null) checkForm()
        }

        passwordEditText.doOnTextChanged { text, _, _, _ ->
            if (text != null) checkForm()
        }


        registerButton.setOnClickListener {
            loadingProgressBar.isVisible = true
//            number = PhoneNumberUtils.formatNumberToE164(numberEditText.text.toString(), "ID").toString()
            number = "+1 650-555-1234"
            firebaseAuth.firebaseAuthSettings.setAutoRetrievedSmsCodeForPhoneNumber(
                number,
                "123456"
            )
            authViewModel.register(
                emailEditText.text.toString(),
                passwordEditText.text.toString()
            )
        }
    }

    private fun checkForm() {
        binding.apply {
            authViewModel.checkAuthForm(
                emailEditText.text.toString(),
                userNameEditText.text.toString(),
                phoneNumberEditText.text.toString(),
                passwordEditText.text.toString()
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

    companion object {
        const val VERIFY_FLAG = "verifyNumber"
    }
}
