package com.aditasha.sepatubersih.presentation.profile

import android.app.Dialog
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.aditasha.sepatubersih.R
import com.aditasha.sepatubersih.data.RealtimeDatabaseConstants
import com.aditasha.sepatubersih.databinding.FragmentProfileBottomSheetBinding
import com.aditasha.sepatubersih.domain.model.SbAddress
import com.aditasha.sepatubersih.domain.model.SbShoes
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class ProfileBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentProfileBottomSheetBinding? = null

    private val binding get() = _binding!!

    private val args: ProfileBottomSheetArgs by navArgs()

    private val profileViewModel: ProfileViewModel by activityViewModels()

    @Inject
    lateinit var firebaseDatabase: FirebaseDatabase
    private var firebaseAddressAdapter: FirebaseAddressAdapter? = null
    private var firebaseShoesAdapter: FirebaseShoesAdapter? = null
    private var currentUser: FirebaseUser? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext())
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.behavior.isDraggable = false
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tag = args.tag

        binding.loading.isVisible = true
        currentUser = profileViewModel.currentUser
        if (currentUser != null) {
            when (tag) {
                ADDRESS -> addressList()
                SHOES -> shoesList()
            }
        }
    }

    private fun addressList() {
        val reference = firebaseDatabase.reference.child(RealtimeDatabaseConstants.ADDRESS)
            .child(currentUser!!.uid)

        val options = FirebaseRecyclerOptions.Builder<SbAddress>()
            .setLifecycleOwner(this)
            .setQuery(reference, SbAddress::class.java)
            .build()

        val addressAdapter = FirebaseAddressAdapter(options)
        firebaseAddressAdapter = addressAdapter

        addressAdapter.setOnClickCallback(object : ProfileOnClickCallback {
            override fun onDeleteClicked(key: String, name: String) {
                deleteDialog(key, name, true)
            }

            override fun onEditClicked(data: Parcelable) {
//                val action =
//                    ProfileBottomSheetDirections.actionProfileBottomSheetToAddAddressFragment(
//                        data as SbAddress
//                    )
//                findNavController().navigate(action)
                val addAddress = AddAddressFragment()
                val args = bundleOf("address" to data as SbAddress)
                addAddress.arguments = args
                addAddress.show(childFragmentManager, "address")
            }

            override fun onOrderClicked(data: Parcelable) {
                if (args.order) {
                    setFragmentResult(ADDRESS, bundleOf(ADDRESS_DATA to data as SbAddress))
                    dismiss()
                }
            }

            override fun onDataChanged() {
                binding.apply {
                    loading.isVisible = false
                    emptyText.isVisible = addressAdapter.itemCount < 1
                }
            }

        })

        binding.apply {
            recycler.apply {
                setHasFixedSize(false)
                layoutManager = LinearLayoutManager(requireActivity())
                adapter = firebaseAddressAdapter
            }

            addButton.text = getString(R.string.add_address)

            addButton.setOnClickListener {
//                val action =
//                    ProfileBottomSheetDirections.actionProfileBottomSheetToAddAddressFragment()
//                findNavController().navigate(action)
                AddAddressFragment().show(childFragmentManager, "address")
            }

            emptyText.text = getString(R.string.saved_address_empty)
        }
    }

    private fun shoesList() {
        profileViewModel.newShoesFormState()
        val reference = firebaseDatabase.reference.child(RealtimeDatabaseConstants.SHOES)
            .child(currentUser!!.uid)

        val options = FirebaseRecyclerOptions.Builder<SbShoes>()
            .setLifecycleOwner(this)
            .setQuery(reference, SbShoes::class.java)
            .build()

        val shoesAdapter = FirebaseShoesAdapter(options)
        firebaseShoesAdapter = shoesAdapter

        shoesAdapter.setOnClickCallback(object : ProfileOnClickCallback {
            override fun onDeleteClicked(key: String, name: String) {
                deleteDialog(key, name, false)
            }

            override fun onEditClicked(data: Parcelable) {
//                val action =
//                    ProfileBottomSheetDirections.actionProfileBottomSheetToAddShoesFragment(data as SbShoes)
//                findNavController().navigate(action)
                val addShoes = AddShoesFragment()
                val args = bundleOf("shoes" to data as SbShoes)
                addShoes.arguments = args
                addShoes.show(childFragmentManager, "shoes")
            }

            override fun onOrderClicked(data: Parcelable) {
                if (args.order) {
                    setFragmentResult(SHOES, bundleOf(SHOES_DATA to data as SbShoes))
                    dismiss()
                }
            }

            override fun onDataChanged() {
                binding.apply {
                    loading.isVisible = false
                    emptyText.isVisible = shoesAdapter.itemCount < 1
                }
            }

        })

        binding.apply {
            recycler.apply {
                setHasFixedSize(false)
                layoutManager = LinearLayoutManager(requireActivity())
                adapter = firebaseShoesAdapter
            }

            addButton.text = getString(R.string.add_shoes)

            addButton.setOnClickListener {
//                val action =
//                    ProfileBottomSheetDirections.actionProfileBottomSheetToAddShoesFragment()
//                findNavController().navigate(action)
                AddShoesFragment().show(childFragmentManager, "shoes")
            }

            emptyText.text = getString(R.string.saved_shoes_empty)
        }
    }

    private fun deleteDialog(key: String, name: String, isAddress: Boolean) {
        val title = if (isAddress) getString(R.string.address_delete) else getString(R.string.shoes_delete)
        val message = if (isAddress) getString(R.string.address_delete_confirmation, name) else getString(R.string.shoes_delete_confirmation, name)
        MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_App_MaterialAlertDialog)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                if (isAddress) profileViewModel.deleteAddress(key)
                else profileViewModel.deleteShoes(key)
                binding.loading.isVisible = true
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    override fun onResume() {
        super.onResume()
        firebaseAddressAdapter?.notifyDataSetChanged()
        firebaseShoesAdapter?.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ADDRESS = "AddressBottomSheet"
        const val SHOES = "ShoesBottomSheet"
        const val ADDRESS_DATA = "address"
        const val SHOES_DATA = "shoes"
    }
}