package com.aditasha.sepatubersih.presentation.driver

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.aditasha.sepatubersih.R
import com.aditasha.sepatubersih.data.RealtimeDatabaseConstants
import com.aditasha.sepatubersih.databinding.FragmentOrderListDriverBinding
import com.aditasha.sepatubersih.domain.model.SbOrderItem
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OrderOngoingListDriverFragment : Fragment() {

    private var _binding: FragmentOrderListDriverBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var orderDriverAdapter: FirebaseOrderDriverAdapter? = null
    @Inject
    lateinit var firebaseDatabase: FirebaseDatabase
    private val orderDriverViewModel: OrderDriverViewModel by activityViewModels()

    private val currentUser
        get() = orderDriverViewModel.currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentOrderListDriverBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecycler()

//        val fragmentPageAdapter = DriverFragmentPageAdapter(this)
//        binding.viewPager2.adapter = fragmentPageAdapter
//        binding.viewPager2.isUserInputEnabled = false
//        TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
//            tab.text = when (position) {
//                0 -> getString(R.string.ongoing_list)
//                1 -> getString(R.string.not_assigned_list)
//                else -> ""
//            }
//        }.attach()

    }

    private fun setupRecycler() {
        val query =
            firebaseDatabase.reference.child(RealtimeDatabaseConstants.ALL_ORDER_ITEM)
                .orderByChild("driver")
                .equalTo(currentUser?.uid)
                .limitToFirst(15)

        val recyclerOptions = FirebaseRecyclerOptions.Builder<SbOrderItem>()
            .setLifecycleOwner(this)
            .setQuery(query, SbOrderItem::class.java)
            .build()
        orderDriverAdapter = FirebaseOrderDriverAdapter(recyclerOptions)

        val divider = MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        divider.setDividerInsetEndResource(requireContext(), R.dimen.length_24dp)
        divider.setDividerInsetStartResource(requireContext(), R.dimen.length_24dp)
        divider.dividerColor = ContextCompat.getColor(
            requireContext(),
            com.google.android.libraries.places.R.color.quantum_grey400
        )

        binding.apply {
            val recyclerLayout = LinearLayoutManager(requireActivity())
            orderListRecycler.apply {
                layoutManager = recyclerLayout
                adapter = orderDriverAdapter
                setHasFixedSize(true)
                addItemDecoration(divider)
            }
        }

        orderDriverAdapter?.setOnClickCallback(object : OrderDriverOnClickCallback {
            override fun onOrderClicked(key: String) {
                val action =
                    OrderOngoingListDriverFragmentDirections.actionOrderDriverFragmentToOrderDetailDriverFragment(
                        key
                    )
                findNavController().navigate(action)
            }

            override fun onDataChanged() {
                binding.loading.isVisible = false
                binding.emptyText.isVisible = orderDriverAdapter?.itemCount == 0
            }

        })
    }

    override fun onResume() {
        super.onResume()
        orderDriverAdapter?.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}