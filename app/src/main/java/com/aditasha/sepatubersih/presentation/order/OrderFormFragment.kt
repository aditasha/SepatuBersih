package com.aditasha.sepatubersih.presentation.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.NavigationUiSaveStateControl
import androidx.recyclerview.widget.LinearLayoutManager
import com.aditasha.sepatubersih.R
import com.aditasha.sepatubersih.databinding.FragmentOrderFormBinding
import com.aditasha.sepatubersih.domain.model.Result
import com.aditasha.sepatubersih.domain.model.SbAddress
import com.aditasha.sepatubersih.domain.model.SbOrder
import com.aditasha.sepatubersih.domain.model.SbShoes
import com.aditasha.sepatubersih.presentation.profile.ProfileBottomSheet
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.DecimalFormat

@AndroidEntryPoint
class OrderFormFragment : Fragment() {
    private var _binding: FragmentOrderFormBinding? = null

    private val binding get() = _binding!!

    private val orderViewModel: OrderViewModel by activityViewModels()

    private var selectedAddress = SbAddress()
    private val shoesList = arrayListOf<SbShoes>()
    private val shoesAdapter = ShoesListAdapter(this::onDeleteClick)

    private var price = PRICE

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFragmentResultListener(ProfileBottomSheet.ADDRESS) { _, bundle ->
            @Suppress("DEPRECATION") val sbAddress =
                bundle.getParcelable(ProfileBottomSheet.ADDRESS_DATA) as? SbAddress
            binding.addressItem.apply {
                if (sbAddress != null) {
                    addressName.text = sbAddress.name
                    address.text = sbAddress.address
                    addressNotes.text = sbAddress.note
                    address.isVisible = true
                    addressNotes.isVisible = true
                    selectedAddress = sbAddress
                    orderButtonCheck()
                }
            }
        }

        setFragmentResultListener(ProfileBottomSheet.SHOES) { _, bundle ->
            @Suppress("DEPRECATION") val sbShoes =
                bundle.getParcelable(ProfileBottomSheet.SHOES_DATA) as? SbShoes
            binding.addressItem.apply {
                if (sbShoes != null) {
                    if (!shoesList.contains(sbShoes)) {
                        shoesList.add(sbShoes)
                        shoesAdapter.submitList(shoesList)
                        updatePrice(shoesAdapter.itemCount)
                        orderButtonCheck()
                    }
                }
            }
        }

        binding.apply {
            recycler.apply {
                setHasFixedSize(false)
                layoutManager = LinearLayoutManager(requireActivity())
                adapter = shoesAdapter
            }

            shoesButton.setOnClickListener {
                val action =
                    OrderFormFragmentDirections.actionOrderFormFragmentToProfileBottomSheet(
                        ProfileBottomSheet.SHOES,
                        true
                    )
                findNavController().navigate(action)
            }

            addressItem.root.setOnClickListener {
                val action =
                    OrderFormFragmentDirections.actionOrderFormFragmentToProfileBottomSheet(
                        ProfileBottomSheet.ADDRESS,
                        true
                    )
                findNavController().navigate(action)
            }

            orderButton.setOnClickListener {
                val sbOrder = SbOrder(
                    shoes = shoesList,
                    address = selectedAddress,
                    price = this@OrderFormFragment.price
                )
                orderViewModel.addOrder(sbOrder)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            orderViewModel.orderResult.collectLatest { result ->
                when (result) {
                    is Result.Error -> {}
                    is Result.Loading -> {}
                    is Result.Success -> {
                        val bottomNav =
                            requireActivity().findViewById<BottomNavigationView>(R.id.navigation)
                        val orderMenu = bottomNav.menu.findItem(R.id.orderFragment)
                        @OptIn(NavigationUiSaveStateControl::class)
                        NavigationUI.onNavDestinationSelected(orderMenu, findNavController(), false)
                    }
                }
            }
        }
    }

    private fun onDeleteClick(key: String) {
        for (shoe in shoesList) {
            if (shoe.key == key) {
                shoesList.remove(shoe)
                break
            }
        }
        shoesAdapter.submitList(shoesList)
        updatePrice(shoesAdapter.itemCount)
        orderButtonCheck()
    }

    private fun updatePrice(amount: Int) {
        val price = PRICE * amount
        this.price = price
        val format = DecimalFormat("0.000")
        val text = getString(R.string.price_placeholder, format.format(price))
        binding.price.text = text
    }

    private fun orderButtonCheck() {
        binding.orderButton.isEnabled =
            shoesAdapter.itemCount > 0 && binding.addressItem.address.text.isNotBlank()
    }

    override fun onResume() {
        super.onResume()
        updatePrice(shoesAdapter.itemCount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val PRICE = 50.000
    }
}