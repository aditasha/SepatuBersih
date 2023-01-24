package com.aditasha.sepatubersih.presentation.auth

import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aditasha.sepatubersih.R
import com.aditasha.sepatubersih.databinding.FragmentRegisterBinding
import com.aditasha.sepatubersih.domain.model.Result
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
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

    private var callback: PhoneAuthProvider.OnVerificationStateChangedCallbacks? = null
    private var otpFragment: OtpFragment? = null
    private var verifId: String? = null
    private var resendToken: ForceResendingToken? = null
    private var number = ""

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


        childFragmentManager.setFragmentResultListener(OtpUtils.RESEND, this) { _, _ ->
            resendToken?.let {
                resendToken(it)
            }
        }

        currentUser = authViewModel.currentUser

        callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                childFragmentManager.setFragmentResult(
                    OtpUtils.VERIFY,
                    bundleOf(OtpUtils.OTP to credential)
                )
            }

            override fun onVerificationFailed(e: FirebaseException) {
                e.localizedMessage?.let {
                    otpFragment?.dismiss()
                    showRegisterFailed(it)
                }
            }

            override fun onCodeSent(
                verificationId: String,
                resendToken: ForceResendingToken
            ) {
                verifId = verificationId
                this@RegisterFragment.resendToken = resendToken
                if (otpFragment != null) {
                    childFragmentManager.setFragmentResult(
                        OtpUtils.RESENDTOKEN, bundleOf(
                            "verifId" to verifId,
                            "resendToken" to this@RegisterFragment.resendToken
                        )
                    )
                } else {
                    otpFragment = OtpFragment()
                    val args = bundleOf(
                        "verifId" to verifId,
                        "resendToken" to this@RegisterFragment.resendToken,
                        "number" to number
                    )
                    otpFragment?.arguments = args
                    otpFragment?.show(childFragmentManager, "otp")
                }
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

                        if (state.nameError == true) {
                            userName.error = getString(R.string.name_cant_less)
                        } else {
                            userName.error = null
                            userName.isErrorEnabled = false
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
                when (loginResult) {
                    is Result.Success -> {
                        if (loginResult.data != null) {
                            callback?.let {
                                val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                                    .setPhoneNumber(number)
                                    .setActivity(requireActivity())
                                    .setTimeout(30L, TimeUnit.SECONDS)
                                    .setCallbacks(it)
                                    .build()
                                PhoneAuthProvider.verifyPhoneNumber(options)
                            }
//                            updateUiWithUser(currentUser!!)
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

        binding.phoneNumberEditText.doOnTextChanged { text, _, _, _ ->
            if (text != null) checkForm()
        }

        passwordEditText.doOnTextChanged { text, _, _, _ ->
            if (text != null) checkForm()
        }


        registerButton.setOnClickListener {
            loadingProgressBar.isVisible = true
            number =
                PhoneNumberUtils.formatNumberToE164(numberEditText.text.toString(), "ID").toString()
//            number = "+1 650-555-1234"
//            firebaseAuth.firebaseAuthSettings.setAutoRetrievedSmsCodeForPhoneNumber(
//                number,
//                "123456"
//            )
            authViewModel.register(
                emailEditText.text.toString(),
                userNameEditText.text.toString(),
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

    private fun resendToken(resendToken: ForceResendingToken) {
        callback?.let {
            val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(number)
                .setActivity(requireActivity())
                .setTimeout(30L, TimeUnit.SECONDS)
                .setCallbacks(it)
                .setForceResendingToken(resendToken)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
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
