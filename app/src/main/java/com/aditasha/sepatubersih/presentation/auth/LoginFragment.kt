package com.aditasha.sepatubersih.presentation.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.aditasha.sepatubersih.AdminActivity
import com.aditasha.sepatubersih.DriverActivity
import com.aditasha.sepatubersih.R
import com.aditasha.sepatubersih.databinding.FragmentLoginBinding
import com.aditasha.sepatubersih.domain.model.Result
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val authViewModel: AuthViewModel by activityViewModels()
    private var _binding: FragmentLoginBinding? = null

    private val binding get() = _binding!!

    @Inject
    lateinit var firebaseDatabase: FirebaseDatabase
    private var notAdmin = false
    private var notDriver = false

//    private val firebaseAuth = FirebaseAuth.getInstance()
//    private val authListener = FirebaseAuth.AuthStateListener {
//        val user = it.currentUser
//        if (user != null) {
//            findNavController().popBackStack(R.id.loginFragment, true)
//            findNavController().navigate(R.id.homeFragment)
//        }
//    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
//        firebaseAuth.addAuthStateListener(authListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
//        firebaseAuth.removeAuthStateListener(authListener)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emailEditText = binding.emailEditText
        val passwordEditText = binding.passwordEditText
        val loginButton = binding.login
        val registerButton = binding.register
        val loadingProgressBar = binding.loading

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

                        if (state.passwordError == true) {
                            password.error = getString(R.string.invalid_password)
                        } else {
                            password.error = null
                            password.isErrorEnabled = false
                        }
                    }
                    loginButton.isEnabled = state.isDataValid
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.authResult.collectLatest { loginResult ->
                loadingProgressBar.isVisible = true
                when (loginResult) {
                    is Result.Success -> {
                        loadingProgressBar.isVisible = false
                        val data = loginResult.data as FirebaseUser
                        firebaseDatabase.reference.child("admin")
                            .addListenerForSingleValueEvent(object :
                                ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    fetchAdmin()
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    if (error.code == -3) {
                                        notAdmin = true
                                        fetchCustomer(data)
                                    }
                                }

                            })

                        firebaseDatabase.reference.child("driver")
                            .addListenerForSingleValueEvent(object :
                                ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    fetchDriver()
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    if (error.code == -3) {
                                        notDriver = true
                                        fetchCustomer(data)
                                    }
                                }

                            })
                    }
                    is Result.Error -> {
                        loadingProgressBar.isVisible = false
                        showLoginFailed(R.string.login_failed)
                    }
                    is Result.Loading -> loadingProgressBar.isVisible = true
                }
            }
        }


        emailEditText.doOnTextChanged { text, _, _, _ ->
            if (text != null) {
                checkForm()
            }
        }

        passwordEditText.doOnTextChanged { text, _, _, _ ->
            if (text != null) {
                checkForm()
            }
        }

        loginButton.setOnClickListener {
            loadingProgressBar.visibility = View.VISIBLE
            authViewModel.login(
                emailEditText.text.toString(),
                passwordEditText.text.toString()
            )
        }

        registerButton.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
            findNavController().navigate(action)
        }
    }

    private fun checkForm() {
        binding.apply {
            authViewModel.checkAuthForm(
                email = emailEditText.text.toString(),
                password = passwordEditText.text.toString()
            )
        }
    }

    private fun updateUiWithUser(user: FirebaseUser) {
        val welcome = getString(R.string.welcome, user.displayName)
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, welcome, Toast.LENGTH_LONG).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }

    private fun fetchAdmin() {
        val intent = Intent(requireActivity(), AdminActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun fetchDriver() {
        val intent = Intent(requireActivity(), DriverActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun fetchCustomer(data: FirebaseUser) {
        if (notAdmin && notDriver) {
            val action = LoginFragmentDirections.actionLoginFragmentToHomeFragment()
            findNavController().navigate(action)
            updateUiWithUser(data)
        }
    }

    companion object {
        const val LOGIN_SUCCESS = "success"
    }
}