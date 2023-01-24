package com.aditasha.sepatubersih.presentation.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.aditasha.sepatubersih.MainActivity
import com.aditasha.sepatubersih.R
import com.aditasha.sepatubersih.data.RealtimeDatabaseConstants
import com.aditasha.sepatubersih.databinding.FragmentOrderListAdminBinding
import com.aditasha.sepatubersih.domain.model.SbOrderItem
import com.aditasha.sepatubersih.presentation.auth.AuthViewModel
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OrderListAdminFragment : Fragment() {

    private var _binding: FragmentOrderListAdminBinding? = null
    private val binding get() = _binding!!

    private var orderAdapter: FirebaseOrderAdminAdapter? = null

    private val authViewModel: AuthViewModel by activityViewModels()
    private val orderAdminViewModel: OrderAdminViewModel by activityViewModels()

    @Inject
    lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var orderQuery: Query
    private lateinit var query: Query
    private val filterOrder get() = orderAdminViewModel.filterOrder

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderListAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        orderQuery = firebaseDatabase.reference
            .child(RealtimeDatabaseConstants.ALL_ORDER_ITEM)
            .orderByChild("status")
        binding.loading.isVisible = true

        binding.toolbar.title =
            if (filterOrder == RealtimeDatabaseConstants.NEED_VERIF) getString(R.string.need_verification)
            else filterOrder
        query = orderQuery.equalTo(filterOrder).limitToFirst(15)

        val recyclerOptions = FirebaseRecyclerOptions.Builder<SbOrderItem>()
            .setLifecycleOwner(this)
            .setQuery(query, SbOrderItem::class.java)
            .build()
        orderAdapter = FirebaseOrderAdminAdapter(recyclerOptions)

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_filter -> {
                    val popupMenu =
                        PopupMenu(requireContext(), binding.toolbar.findViewById(it.itemId))
                    popupMenu.menuInflater.inflate(R.menu.menu_order_admin_popup, popupMenu.menu)

                    popupMenu.setOnMenuItemClickListener { popup ->
                        when (popup.itemId) {
                            R.id.need_verif -> {
                                orderAdminViewModel.filterOrder =
                                    RealtimeDatabaseConstants.NEED_VERIF
                                binding.toolbar.title = getString(R.string.need_verification)
                                query = orderQuery.equalTo(RealtimeDatabaseConstants.NEED_VERIF)
                                    .limitToFirst(15)
                                updateOptions(query)
                                true
                            }
                            R.id.pickup_queue -> {
                                orderAdminViewModel.filterOrder =
                                    RealtimeDatabaseConstants.QUEUE_FOR_PICKUP
                                binding.toolbar.title = getString(R.string.pickup_queue)
                                query =
                                    orderQuery.equalTo(RealtimeDatabaseConstants.QUEUE_FOR_PICKUP)
                                        .limitToFirst(15)
                                updateOptions(query)
                                true
                            }
                            R.id.pickup_process -> {
                                orderAdminViewModel.filterOrder = RealtimeDatabaseConstants.PICKUP
                                binding.toolbar.title = getString(R.string.pickup_process)
                                query =
                                    orderQuery.equalTo(RealtimeDatabaseConstants.PICKUP)
                                        .limitToFirst(15)
                                updateOptions(query)
                                true
                            }
                            R.id.arrived_store -> {
                                orderAdminViewModel.filterOrder =
                                    RealtimeDatabaseConstants.ARRIVED_STORE
                                binding.toolbar.title = getString(R.string.arrived_on_store)
                                query =
                                    orderQuery.equalTo(RealtimeDatabaseConstants.ARRIVED_STORE)
                                        .limitToFirst(15)
                                updateOptions(query)
                                true
                            }
                            R.id.washing -> {
                                orderAdminViewModel.filterOrder = RealtimeDatabaseConstants.WASHING
                                binding.toolbar.title = getString(R.string.washing)
                                query =
                                    orderQuery.equalTo(RealtimeDatabaseConstants.WASHING)
                                        .limitToFirst(15)
                                updateOptions(query)
                                true
                            }
                            R.id.finish_washing -> {
                                orderAdminViewModel.filterOrder = RealtimeDatabaseConstants.FINISH_WASHING
                                binding.toolbar.title = getString(R.string.finish_washing)
                                query =
                                    orderQuery.equalTo(RealtimeDatabaseConstants.FINISH_WASHING)
                                        .limitToFirst(15)
                                updateOptions(query)
                                true
                            }
                            R.id.delivery_queue -> {
                                orderAdminViewModel.filterOrder =
                                    RealtimeDatabaseConstants.QUEUE_FOR_DELIV
                                binding.toolbar.title = getString(R.string.delivery_queue)
                                query =
                                    orderQuery.equalTo(RealtimeDatabaseConstants.QUEUE_FOR_DELIV)
                                        .limitToFirst(15)
                                updateOptions(query)
                                true
                            }
                            R.id.delivery_process -> {
                                orderAdminViewModel.filterOrder = RealtimeDatabaseConstants.DELIV
                                binding.toolbar.title = getString(R.string.delivery_process)
                                query = orderQuery.equalTo(RealtimeDatabaseConstants.DELIV)
                                    .limitToFirst(15)
                                updateOptions(query)
                                true
                            }
                            R.id.customer_pickup -> {
                                orderAdminViewModel.filterOrder = RealtimeDatabaseConstants.CUSTOMER_PICKUP
                                binding.toolbar.title = getString(R.string.customer_pickup)
                                query = orderQuery.equalTo(RealtimeDatabaseConstants.CUSTOMER_PICKUP)
                                    .limitToFirst(15)
                                updateOptions(query)
                                true
                            }
                            R.id.complete -> {
                                orderAdminViewModel.filterOrder =
                                    RealtimeDatabaseConstants.STATUS_COMPLETE
                                binding.toolbar.title = getString(R.string.complete)
                                query =
                                    orderQuery.equalTo(RealtimeDatabaseConstants.STATUS_COMPLETE)
                                        .limitToFirst(15)
                                updateOptions(query)
                                true
                            }
                            R.id.canceled -> {
                                orderAdminViewModel.filterOrder =
                                    RealtimeDatabaseConstants.CANCELLED
                                binding.toolbar.title = getString(R.string.canceled)
                                query =
                                    orderQuery.equalTo(RealtimeDatabaseConstants.CANCELLED)
                                        .limitToFirst(15)
                                updateOptions(query)
                                true
                            }
                            else -> false
                        }
                    }
                    popupMenu.show()
                    true
                }

                R.id.action_menu_logout -> {
                    val popupMenu =
                        PopupMenu(requireContext(), binding.toolbar.findViewById(it.itemId))
                    popupMenu.menuInflater.inflate(R.menu.menu_logout_popup, popupMenu.menu)

                    popupMenu.setOnMenuItemClickListener { popup ->
                        if (popup.itemId == R.id.logout) {
                            authViewModel.logout()
                            val intent = Intent(requireActivity(), MainActivity::class.java)
                            startActivity(intent)
                            requireActivity().finish()
                            true
                        } else false
                    }
                    popupMenu.show()
                    true
                }
                else -> false
            }
        }

        orderAdapter?.setOnClickCallback(object : OrderAdminOnClickCallback {
            override fun onOrderClicked(key: String) {
                val action =
                    OrderListAdminFragmentDirections.actionAdminOrderListFragmentToAdminOrderDetailFragment(
                        key
                    )
                findNavController().navigate(action)
            }

            override fun onDataChanged() {
                binding.loading.isVisible = false
                binding.emptyText.isVisible = orderAdapter?.itemCount == 0
            }

        })

        val divider = MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        divider.setDividerInsetEndResource(requireContext(), R.dimen.length_24dp)
        divider.setDividerInsetStartResource(requireContext(), R.dimen.length_24dp)
        divider.dividerColor = ContextCompat.getColor(
            requireContext(),
            com.google.android.libraries.places.R.color.quantum_grey400
        )

        binding.apply {
            recycler.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(requireActivity())
                adapter = orderAdapter
                addItemDecoration(divider)
            }
        }
    }

    private fun updateOptions(query: Query) {
        binding.loading.isVisible = true
        val recyclerOptions = FirebaseRecyclerOptions.Builder<SbOrderItem>()
            .setLifecycleOwner(this)
            .setQuery(query, SbOrderItem::class.java)
            .build()
        orderAdapter?.updateOptions(recyclerOptions)
        orderAdapter?.notifyDataSetChanged()
    }

//    private fun loadingScreen() {
//        binding.apply {
//            emptyText.isVisible = false
//            root.isRefreshing = true
//        }
//    }
//
//    private fun errorScreen(orderType: String, error: String?) {
//        binding.apply {
//            if (orderAdapter?.itemCount == 0) {
//                when (orderType) {
//                    RealtimeDatabaseConstants.ONGOING -> {
//                        emptyText.text = "You haven't place any order"
//                    }
//                    RealtimeDatabaseConstants.COMPLETE -> {
//                        emptyText.text = "Your completed order is empty"
//                    }
//                }
//            } else {
//                emptyText.text = error
//            }
//            emptyText.isVisible = true
//            root.isRefreshing = false
//        }
//    }
//
//    private fun notLoading() {
//        binding.apply {
//            emptyText.isVisible = false
//            val date = Date().time
//            val sfd = SimpleDateFormat("dd MMMM yyyy, HH:mm")
//            val text = "Updated on " + sfd.format(date)
//            updateTime.text = text
//            root.isRefreshing = false
//        }
//    }

    override fun onResume() {
        super.onResume()
        orderAdapter?.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}