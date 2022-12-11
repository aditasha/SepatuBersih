package com.aditasha.sepatubersih.presentation.profile

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.aditasha.sepatubersih.R
import com.aditasha.sepatubersih.databinding.FragmentAddShoesBinding
import com.aditasha.sepatubersih.domain.model.Result
import com.aditasha.sepatubersih.domain.model.SbShoes
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddShoesFragment : Fragment() {
    private var _binding: FragmentAddShoesBinding? = null

    private val binding get() = _binding!!

    private val args: AddShoesFragmentArgs by navArgs()

    private val profileViewModel: ProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddShoesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        val argsShoes = args.shoes
        if (argsShoes != null) {
            binding.apply {
                nameEditText.setText(argsShoes.name)
                brandTypeEditText.setText(argsShoes.brandType)
                colorEditText.setText(argsShoes.color)
                notesEditText.setText(argsShoes.notes)
                addShoes.text = getString(R.string.update_shoes)
            }
        }

        binding.apply {
            nameEditText.doOnTextChanged { text, _, _, _ ->
                if (text != null) checkForm()
            }

            brandTypeEditText.doOnTextChanged { text, _, _, _ ->
                if (text != null) checkForm()
            }

            colorEditText.doOnTextChanged { text, _, _, _ ->
                if (text != null) checkForm()
            }

            viewLifecycleOwner.lifecycleScope.launch {
                profileViewModel.shoesForm.collectLatest { state ->
                    addShoes.isEnabled = state.isDataValid

                    if (state.nameError) {
                        name.error = getString(R.string.name_cant_empty)
                    } else {
                        name.error = null
                        name.isErrorEnabled = false
                    }

                    if (state.brandTypeError) {
                        brandType.error = getString(R.string.brand_type_cant_empty)
                    } else {
                        brandType.error = null
                        brandType.isErrorEnabled = false
                    }

                    if (state.colorError) {
                        color.error = getString(R.string.color_cant_empty)
                    } else {
                        color.error = null
                        color.isErrorEnabled = false
                    }
                }
            }

            addShoes.setOnClickListener {
                val notes = notesEditText.text.toString()
                val shoes = SbShoes(
                    nameEditText.text.toString(),
                    brandTypeEditText.text.toString(),
                    colorEditText.text.toString(),
                    notes.ifBlank { "-" }
                )
                if (args.shoes != null) {
                    shoes.key = args.shoes!!.key
                    profileViewModel.updateShoes(shoes)
                } else profileViewModel.addShoes(shoes)
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
                            val snack = result.exception.localizedMessage?.let {
                                Snackbar.make(
                                    requireView(),
                                    it,
                                    Snackbar.LENGTH_SHORT
                                )
                            }
                            val params = snack?.view?.layoutParams as CoordinatorLayout.LayoutParams
                            params.gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
                            params.setMargins(0, AddAddressFragment.ACTION_BAR_HEIGHT, 0, 0)
                            snack.view.layoutParams = params
                            snack.show()
                        }
                        is Result.Loading -> loading.isVisible = true
                    }
                }
            }
        }
    }

    private fun checkForm() {
        binding.apply {
            profileViewModel.checkShoesForm(
                nameEditText.text.toString(),
                brandTypeEditText.text.toString(),
                colorEditText.text.toString()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}