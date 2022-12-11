package com.aditasha.sepatubersih.presentation.driver

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
import com.aditasha.sepatubersih.R
import com.aditasha.sepatubersih.data.RealtimeDatabaseConstants
import com.aditasha.sepatubersih.databinding.FragmentOrderListDriverBinding
import com.aditasha.sepatubersih.domain.model.SbOrderItem
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OrderListDriverFragment : Fragment() {

    private var _binding: FragmentOrderListDriverBinding? = null
    private val binding get() = _binding!!

    private val orderDriverViewModel: OrderDriverViewModel by activityViewModels()

    private val currentUser
        get() = orderDriverViewModel.currentUser

    private var orderDriverAdapter: FirebaseOrderDriverAdapter? = null

    @Inject
    lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var orderRef: DatabaseReference
    private lateinit var query: Query

    private var childListener: ChildEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderListDriverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val orderType = arguments?.getString(DriverFragmentPageAdapter.PAGE)

        binding.loading.isVisible = true
        if (orderType != null)
            currentUser?.let { currentUser ->
//                loadingScreen()
                when (orderType) {
                    DriverFragmentPageAdapter.ONGOING -> {
                        query =
                            firebaseDatabase.reference.child(RealtimeDatabaseConstants.ALL_ORDER_ITEM)
                                .orderByChild("driver")
                                .equalTo(currentUser.uid)
                                .limitToFirst(15)
                    }
                    DriverFragmentPageAdapter.COMPLETE -> {
                        query =
                            firebaseDatabase.reference.child(RealtimeDatabaseConstants.ALL_ORDER_ITEM)
                                .orderByChild("status")
                                .equalTo(orderDriverViewModel.filterOrder)
                                .limitToFirst(15)

                        binding.filter.apply {
                            isVisible = true
                            text = getString(R.string.pickup_order)
                            setOnClickListener {
                                val popupMenu = PopupMenu(requireContext(), it)
                                popupMenu.menuInflater.inflate(
                                    R.menu.menu_order_driver_popup,
                                    popupMenu.menu
                                )

                                popupMenu.setOnMenuItemClickListener { popup ->
                                    when (popup.itemId) {
                                        R.id.pickup_order -> {
                                            orderDriverViewModel.filterOrder =
                                                RealtimeDatabaseConstants.QUEUE_FOR_PICKUP
                                            query = firebaseDatabase.reference.child(
                                                RealtimeDatabaseConstants.ALL_ORDER_ITEM
                                            )
                                                .orderByChild("status")
                                                .equalTo(RealtimeDatabaseConstants.QUEUE_FOR_PICKUP)
                                                .limitToFirst(15)
                                            updateRecycler()
                                            binding.filter.text = getString(R.string.pickup_order)
                                            true
                                        }
                                        R.id.deliv_order -> {
                                            orderDriverViewModel.filterOrder =
                                                RealtimeDatabaseConstants.QUEUE_FOR_DELIV
                                            query = firebaseDatabase.reference.child(
                                                RealtimeDatabaseConstants.ALL_ORDER_ITEM
                                            )
                                                .orderByChild("status")
                                                .equalTo(RealtimeDatabaseConstants.QUEUE_FOR_DELIV)
                                                .limitToFirst(15)
                                            updateRecycler()
                                            binding.filter.text = getString(R.string.delivery_order)
                                            true
                                        }
                                        else -> false
                                    }
                                }
                                popupMenu.show()
                            }
                        }
                    }
                }
                setupRecycler()
            }
    }

    private fun setupRecycler() {
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
                    OrderDriverFragmentDirections.actionOrderDriverFragmentToOrderDetailDriverFragment(
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

    private fun updateRecycler() {
        binding.loading.isVisible = true
        val recyclerOptions = FirebaseRecyclerOptions.Builder<SbOrderItem>()
            .setLifecycleOwner(this)
            .setQuery(query, SbOrderItem::class.java)
            .build()

        orderDriverAdapter?.updateOptions(recyclerOptions)
        orderDriverAdapter?.notifyDataSetChanged()
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

    override fun onDestroyView() {
        super.onDestroyView()
        childListener?.let { orderRef.removeEventListener(it) }
        _binding = null
    }
}