package com.aditasha.sepatubersih.presentation.profile

import android.app.Dialog
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.aditasha.sepatubersih.R
import com.aditasha.sepatubersih.databinding.FragmentAddShoesBinding
import com.aditasha.sepatubersih.domain.model.Result
import com.aditasha.sepatubersih.domain.model.SbShoes
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddShoesFragment : DialogFragment() {
    private var _binding: FragmentAddShoesBinding? = null

    private val binding get() = _binding!!

    //    private val args: AddShoesFragmentArgs by navArgs()
    private var shoesArgs: SbShoes? = null

    private val profileViewModel: ProfileViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.ThemeOverlay_App_FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddShoesBinding.inflate(inflater, container, false)
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

        binding.toolbar.setNavigationOnClickListener { dismiss() }

        if (arguments != null)
            shoesArgs = requireArguments().getParcelable("shoes")

//        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        val argsShoes = shoesArgs

        binding.apply {
            viewLifecycleOwner.lifecycleScope.launch {
                profileViewModel.shoesForm.collectLatest { state ->
                    addShoes.isEnabled = state.isDataValid

                    if (state.nameError == true) {
                        name.error = getString(R.string.name_cant_empty)
                    } else {
                        name.error = null
                        name.isErrorEnabled = false
                    }

                    if (state.brandTypeError == true) {
                        brandType.error = getString(R.string.brand_type_cant_empty)
                    } else {
                        brandType.error = null
                        brandType.isErrorEnabled = false
                    }

                    if (state.colorError == true) {
                        color.error = getString(R.string.color_cant_empty)
                    } else {
                        color.error = null
                        color.isErrorEnabled = false
                    }
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {
                profileViewModel.shoesResult.collectLatest { result ->
                    when (result) {
                        is Result.Success -> {
                            loading.isVisible = false
                            if (argsShoes != null) {
                                Toast.makeText(
                                    requireActivity(),
                                    getString(R.string.success_update_shoes),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    requireActivity(),
                                    getString(R.string.success_add_shoes),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            findNavController().navigateUp()
                        }
                        is Result.Error -> {
                            loading.isVisible = false
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
                                result.exception.localizedMessage?.let {
                                    val snack = Snackbar.make(
                                        requireView(),
                                        it,
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
                        is Result.Loading -> loading.isVisible = true
                    }
                }
            }

            if (argsShoes != null) {
                nameEditText.setText(argsShoes.name)
                brandTypeEditText.setText(argsShoes.brandType)
                colorEditText.setText(argsShoes.color)
                notesEditText.setText(argsShoes.notes)
                addShoes.text = getString(R.string.update_shoes)
            }

            nameEditText.doOnTextChanged { text, _, _, _ ->
                val string = text?.toString()
                profileViewModel.checkName(string)
            }

            brandTypeEditText.doOnTextChanged { text, _, _, _ ->
                val string = text?.toString()
                profileViewModel.checkBrandType(string)
            }

            colorEditText.doOnTextChanged { text, _, _, _ ->
                val string = text?.toString()
                profileViewModel.checkColor(string)
            }

            addShoes.setOnClickListener {
                val notes = notesEditText.text.toString()
                val shoes = SbShoes(
                    nameEditText.text.toString(),
                    brandTypeEditText.text.toString(),
                    colorEditText.text.toString(),
                    notes.ifBlank { "-" }
                )
                if (shoesArgs != null) {
                    shoes.key = shoesArgs!!.key
                    profileViewModel.updateShoes(shoes)
                } else profileViewModel.addShoes(shoes)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}