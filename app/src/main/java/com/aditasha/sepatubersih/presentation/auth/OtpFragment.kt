package com.aditasha.sepatubersih.presentation.auth

import android.app.Dialog
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.aditasha.sepatubersih.InsetsWithKeyboardCallback
import com.aditasha.sepatubersih.R
import com.aditasha.sepatubersih.databinding.FragmentOtpBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@AndroidEntryPoint
class OtpFragment : DialogFragment() {
    private var _binding: FragmentOtpBinding? = null

    private val binding get() = _binding!!

    @Inject
    lateinit var firebaseAuth: FirebaseAuth
    private var verifId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var number: String? = null
    private var edit = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
        setStyle(STYLE_NORMAL, R.style.ThemeOverlay_App_FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOtpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return dialog
    }

    @Suppress("DEPRECATION")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val insetsWithKeyboardCallback = dialog?.window?.let { InsetsWithKeyboardCallback(it) }
        ViewCompat.setOnApplyWindowInsetsListener(binding.root, insetsWithKeyboardCallback)
        ViewCompat.setWindowInsetsAnimationCallback(binding.root, insetsWithKeyboardCallback)

        setFragmentResultListener(OtpUtils.VERIFY) { _, bundle ->
            val credential = bundle.getParcelable(OtpUtils.OTP) as? PhoneAuthCredential
            if (credential != null) {
                binding.pinView.setText(credential.smsCode)
            }
        }

        setFragmentResultListener(OtpUtils.RESENDTOKEN) { _, bundle ->
            verifId = bundle.getString("verifId")
            resendToken = bundle.getParcelable("resendToken") as? ForceResendingToken
        }

        if (arguments != null) {
            verifId = requireArguments().getString("verifId")
            resendToken = requireArguments().getParcelable("resendToken") as? ForceResendingToken
            number = requireArguments().getString("number")
            edit = requireArguments().getBoolean("edit")
        }

        binding.apply {
            otpMessage.text = resources.getString(R.string.otp_sent_number, number)
            pinView.doOnTextChanged { _, _, _, _ ->
                confirmButton.isEnabled = pinView.text?.length == 6
            }
            confirmButton.setOnClickListener {
                verifId?.let {
                    binding.loading.isVisible = true
                    confirmButton.isEnabled = false
                    val credential = PhoneAuthProvider.getCredential(it, pinView.text.toString())
                    viewLifecycleOwner.lifecycleScope.launch {
                        try {
                            if (edit) {
                                firebaseAuth.currentUser?.unlink(PhoneAuthProvider.PROVIDER_ID)
                                    ?.await()
                                firebaseAuth.currentUser?.linkWithCredential(credential)?.await()
                                Toast.makeText(
                                    requireActivity(),
                                    getString(R.string.success_update_profile),
                                    Toast.LENGTH_LONG
                                ).show()
                                findNavController().navigateUp()
                                dismiss()
                            } else {
                                firebaseAuth.currentUser?.linkWithCredential(credential)?.await()
                                val action =
                                    RegisterFragmentDirections.actionRegisterFragmentToHomeFragment()
                                Toast.makeText(
                                    requireActivity(),
                                    getString(
                                        R.string.welcome,
                                        firebaseAuth.currentUser?.displayName
                                    ),
                                    Toast.LENGTH_LONG
                                ).show()
                                findNavController().navigate(action)
                                dismiss()
                            }
                        } catch (e: Exception) {
                            binding.loading.isVisible = false
                            e.localizedMessage?.let { error ->
                                val tv = TypedValue()
                                var actionBarHeight: Int? = null
                                if (requireActivity().theme.resolveAttribute(
                                        android.R.attr.actionBarSize,
                                        tv,
                                        true
                                    )
                                ) {
                                    actionBarHeight = TypedValue.complexToDimensionPixelSize(
                                        tv.data,
                                        resources.displayMetrics
                                    )
                                }
                                if (actionBarHeight != null) {
                                    val snack = Snackbar.make(
                                        requireView(),
                                        error,
                                        Snackbar.LENGTH_LONG
                                    )
                                    val params =
                                        snack.view.layoutParams as CoordinatorLayout.LayoutParams
                                    params.gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
                                    params.setMargins(0, actionBarHeight, 0, 0)
                                    snack.view.layoutParams = params
                                    snack.setTextMaxLines(10)
                                    snack.show()
                                }
                            }
                        }
                    }
                }
            }
            resendButton.setOnClickListener {
                setFragmentResult(OtpUtils.RESEND, bundleOf())
            }
        }

//        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}