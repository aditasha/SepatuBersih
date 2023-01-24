package com.aditasha.sepatubersih.presentation.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aditasha.sepatubersih.R
import com.aditasha.sepatubersih.data.RealtimeDatabaseConstants
import com.aditasha.sepatubersih.databinding.FragmentOrderListBinding
import com.aditasha.sepatubersih.domain.model.Result
import com.aditasha.sepatubersih.domain.model.SbOrderItem
import com.firebase.ui.database.paging.DatabasePagingOptions
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class OrderListFragment : Fragment() {

    private var _binding: FragmentOrderListBinding? = null
    private val binding get() = _binding!!

    private val orderViewModel: OrderViewModel by activityViewModels()

    private val currentUser
        get() = orderViewModel.currentUser

    private var orderAdapter: FirebaseOrderAdapter? = null
    private lateinit var pagingOptions: DatabasePagingOptions<SbOrderItem>

    @Inject
    lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var orderRef: DatabaseReference

    private var childListener: ChildEventListener? = null

    @Inject
    lateinit var firebaseAuth: FirebaseAuth
    private val authListener = FirebaseAuth.AuthStateListener {
        binding.loginLayout.isVisible = currentUser == null
        binding.root.isRefreshing = false
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(authListener)
    }

    override fun onStop() {
        super.onStop()
        firebaseAuth.removeAuthStateListener(authListener)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingScreen()
        val orderType = arguments?.getString(FragmentPageAdapter.PAGE)

        binding.login.setOnClickListener {
            val action = OrderFragmentDirections.actionOrderFragmentToLoginFragment()
            findNavController().navigate(action)
        }

        if (orderType != null)
            if (currentUser != null) {

                when (orderType) {
                    RealtimeDatabaseConstants.ONGOING -> {
                        orderRef =
                            firebaseDatabase.reference.child(RealtimeDatabaseConstants.USER_ORDER_ITEM)
                                .child(RealtimeDatabaseConstants.ONGOING)
                                .child(currentUser?.uid!!)

                        viewLifecycleOwner.lifecycleScope.launch {
                            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                checkOrder()
                            }
                        }

                        viewLifecycleOwner.lifecycleScope.launch {
                            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                orderViewModel.updateResult.collectLatest { result ->
                                    if (result is Result.Success) {
                                        orderAdapter?.submitData(
                                            viewLifecycleOwner.lifecycle,
                                            PagingData.empty()
                                        )
                                        orderAdapter?.refresh()
                                    }
                                }
                            }
                        }
                    }
                    RealtimeDatabaseConstants.COMPLETE -> {
                        orderRef =
                            firebaseDatabase.reference.child(RealtimeDatabaseConstants.USER_ORDER_ITEM)
                                .child(RealtimeDatabaseConstants.COMPLETE)
                                .child(currentUser?.uid!!)
                    }
                }

                setupRecycler(orderType)
            }
    }

    private fun setupRecycler(orderType: String) {
        currentUser?.let {
            val config = PagingConfig(10, enablePlaceholders = false)

            pagingOptions = DatabasePagingOptions.Builder<SbOrderItem>()
                .setLifecycleOwner(this)
                .setQuery(orderRef, config, SbOrderItem::class.java)
                .build()

            orderAdapter = FirebaseOrderAdapter(pagingOptions)
            orderAdapter?.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

            val divider =
                MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
            divider.setDividerInsetEndResource(requireContext(), R.dimen.length_24dp)
            divider.setDividerInsetStartResource(requireContext(), R.dimen.length_24dp)
            divider.dividerColor = ContextCompat.getColor(
                requireContext(),
                com.google.android.libraries.places.R.color.quantum_grey400
            )

            binding.apply {
                val recyclerLayout = LinearLayoutManager(requireActivity())
//                recyclerLayout.reverseLayout = true
//                recyclerLayout.stackFromEnd = true
                orderListRecycler.apply {
                    layoutManager = recyclerLayout
                    adapter = orderAdapter
                    setHasFixedSize(true)
                    addItemDecoration(divider)
                }
                root.setOnRefreshListener {
                    if (orderType == RealtimeDatabaseConstants.ONGOING) checkOrder()
                    else {
                        orderAdapter?.refresh()
                    }
                }
                val adapter = orderListRecycler.adapter as FirebaseOrderAdapter
                viewLifecycleOwner.lifecycleScope.launch {
                    viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        adapter.loadStateFlow
                            .distinctUntilChangedBy { it.refresh }
                            .collect { loadState ->
                                loadState.onState(
                                    showLoading = { loading ->
                                        if (loading) {
                                            loadingScreen()
                                            orderListRecycler.post {
                                                orderListRecycler.smoothScrollToPosition(
                                                    0
                                                )
                                            }
                                        }
                                    },
                                    showData = { data ->
                                        if (data) notLoading()
                                    },
                                    showError = { message ->
                                        errorScreen(orderType, message)
                                    }
                                )
                            }
                    }
                }
            }

            childListener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    orderAdapter?.refresh()
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        orderAdapter?.submitData(viewLifecycleOwner.lifecycle, PagingData.empty())
                        orderAdapter?.refresh()
                    }
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onCancelled(error: DatabaseError) {}

            }

            childListener?.let { orderRef.addChildEventListener(it) }

            orderAdapter?.setOnClickCallback(object : OrderOnClickCallback {
                override fun onOrderClicked(key: String) {
                    val action =
                        OrderFragmentDirections.actionOrderFragmentToOrderDetailFragment(key)
                    findNavController().navigate(action)
//                    val orderNodeRef = firebaseDatabase.reference.child(RealtimeDatabaseConstants.ORDER_NODE)
//                        .child(key)
//                    orderNodeRef.addListenerForSingleValueEvent(object: ValueEventListener {
//                        override fun onDataChange(snapshot: DataSnapshot) {
//                            val node = snapshot.value as String
//                            val action = OrderFragmentDirections.actionOrderFragmentToOrderDetailFragment(key, node)
//                            findNavController().navigate(action)
//                        }
//
//                        override fun onCancelled(error: DatabaseError) {
//                        }
//
//                    })
                }

            })

        }
    }

    private inline fun CombinedLoadStates.onState(
        showLoading: (Boolean) -> Unit,
        showData: (Boolean) -> Unit,
        showError: (String) -> Unit,
    ) {
        showLoading(refresh is LoadState.Loading)

        showData(source.append is LoadState.NotLoading)

        val errorState = source.append as? LoadState.Error
            ?: source.prepend as? LoadState.Error
            ?: source.refresh as? LoadState.Error
            ?: append as? LoadState.Error
            ?: prepend as? LoadState.Error
            ?: refresh as? LoadState.Error


        errorState?.let { showError(it.error.toString()) }
    }

    private fun checkOrder() {
        orderRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var expired = false
                for (orderSnap in snapshot.children) {
                    val order = orderSnap.getValue(SbOrderItem::class.java)
                    if (order != null)
                        if (Date().time > order.endTimestamp!! && order.status == RealtimeDatabaseConstants.NEED_PAYMENT) {
                            expired = true
                            orderViewModel.updateOrderFromItem(order)
                        }
                }
                if (!expired) {
                    orderAdapter?.refresh()
                }
            }

            override fun onCancelled(error: DatabaseError) {}

        })
    }

    private fun loadingScreen() {
        binding.apply {
            emptyText.isVisible = false
            root.isRefreshing = true
        }
    }

    private fun errorScreen(orderType: String, error: String?) {
        binding.apply {
            if (orderAdapter?.itemCount == 0) {
                when (orderType) {
                    RealtimeDatabaseConstants.ONGOING -> {
                        emptyText.text = getString(R.string.empty_ongoing)
                    }
                    RealtimeDatabaseConstants.COMPLETE -> {
                        emptyText.text = getString(R.string.empty_completed)
                    }
                }
            } else {
                emptyText.text = error
            }
            emptyText.isVisible = true
            root.isRefreshing = false
        }
    }

    private fun notLoading() {
        binding.apply {
            emptyText.isVisible = false
            val date = Date().time
            val sfd = SimpleDateFormat("dd MMMM yyyy, HH:mm")
            val text = getString(R.string.updated_on_placeholder, sfd.format(date))
            updateTime.text = text
            root.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        childListener?.let { orderRef.removeEventListener(it) }
        _binding = null
    }
}