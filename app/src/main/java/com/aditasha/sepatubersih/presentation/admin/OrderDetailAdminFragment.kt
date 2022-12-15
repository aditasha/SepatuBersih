package com.aditasha.sepatubersih.presentation.admin

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.telephony.TelephonyManager
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.os.BuildCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.aditasha.sepatubersih.R
import com.aditasha.sepatubersih.ServerTime
import com.aditasha.sepatubersih.data.RealtimeDatabaseConstants
import com.aditasha.sepatubersih.databinding.FragmentOrderDetailAdminBinding
import com.aditasha.sepatubersih.domain.model.SbOrder
import com.aditasha.sepatubersih.domain.model.SbShoes
import com.aditasha.sepatubersih.presentation.GlideApp
import com.aditasha.sepatubersih.presentation.order.PreciseCountdown
import com.aditasha.sepatubersih.presentation.order.ShoesSimpleListAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@BuildCompat.PrereleaseSdkCheck
@AndroidEntryPoint
class OrderDetailAdminFragment : Fragment() {
    private var _binding: FragmentOrderDetailAdminBinding? = null

    private val binding get() = _binding!!

    private val args: OrderDetailAdminFragmentArgs by navArgs()

    private val orderAdminViewModel: OrderAdminViewModel by activityViewModels()

    @Inject
    lateinit var firebaseDatabase: FirebaseDatabase

    @Inject
    lateinit var firebaseStorage: FirebaseStorage
    private lateinit var ref: DatabaseReference
    private lateinit var proofRef: StorageReference
    private var valueEventListener: ValueEventListener? = null

    @Inject
    lateinit var serverTime: ServerTime
    private var preciseCountdown: PreciseCountdown? = null
    private var sbOrder: SbOrder? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderDetailAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ref = firebaseDatabase.reference.child(RealtimeDatabaseConstants.ORDER_DETAIL)
            .child(args.key)

        valueEventListener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val order = snapshot.getValue(SbOrder::class.java)!!
                sbOrder = order
                proofRef = firebaseStorage.reference.child(RealtimeDatabaseConstants.ORDER_DETAIL)
                    .child(order.key!!)
                    .child(order.proof!!)
                val orderDate = Date(order.orderTimestamp!!)
                val statusDate = Date(order.statusTimestamp!!)
                val sfd = SimpleDateFormat("dd MMMM yyyy, HH:mm")
                val orderAddress = order.address!!
                val format = DecimalFormat("0.000")
                val totalPrice = getString(R.string.price_placeholder, format.format(order.price))
                binding.apply {
                    val needVerif = order.status == RealtimeDatabaseConstants.NEED_VERIF
                    val finishedWashing = order.status == RealtimeDatabaseConstants.WASHING
                    val selfPickup = order.status == RealtimeDatabaseConstants.CUSTOMER_PICKUP
                    val arrived = order.status == RealtimeDatabaseConstants.ARRIVED_STORE

                    finishButton.isVisible = needVerif
                    updateButton.isVisible = finishedWashing || needVerif || selfPickup || arrived

                    orderId.text = getString(R.string.order_id_placeholder, order.id)
                    orderTimestamp.text = sfd.format(orderDate)
                    orderStatus.text = order.status
                    statusTimestamp.text =
                        getString(R.string.updated_on_placeholder, sfd.format(statusDate))
                    profileName.text = order.name
                    profileEmail.text = order.email
                    val number = formatNumber(order.number)
                    profileNumber.text = number
                    profileNumber.setOnClickListener {
                        val browserIntent =
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(getString(R.string.wa_uri_string, number))
                            )
                        startActivity(browserIntent)
                    }
                    profileNumber.paint.isUnderlineText = true
                    profileNumber.movementMethod = LinkMovementMethod.getInstance()
                    address.text = orderAddress.address
                    address.paint.isUnderlineText = true
                    address.setOnClickListener {
//                        val strUri =
//                            "http://maps.google.com/maps?q=loc:" + orderAddress.latitude.toString() + "," + orderAddress.longitude.toString() + " (" + orderAddress.address + ")"
                        val strUri = getString(
                            R.string.maps_uri_string,
                            orderAddress.latitude.toString(),
                            orderAddress.longitude.toString(),
                            orderAddress.address
                        )
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(strUri))
                        intent.setClassName(
                            "com.google.android.apps.maps",
                            "com.google.android.maps.MapsActivity"
                        )
                        startActivity(intent)
                    }
                    addressNotes.text = getString(R.string.notes_placeholder, orderAddress.note)
                    setRecycler(order.shoes!!)
                    price.text = totalPrice
                }


            }

            override fun onCancelled(error: DatabaseError) {}
        }

        valueEventListener?.let { ref.addValueEventListener(it) }

        binding.apply {
            proofButton.setOnClickListener {
                proofDialog()
            }
            finishButton.setOnClickListener {
                updateStatusDialog(update = false)
            }
            updateButton.setOnClickListener {
                sbOrder?.let { sbOrder ->
                    updateStatusDialog(update = true, sbOrder.status!!)
                }
            }
        }
    }

    private fun setRecycler(sbShoes: ArrayList<SbShoes>) {
        val simpleAdapter = ShoesSimpleListAdapter()
        val divider = MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        divider.setDividerInsetEndResource(requireContext(), R.dimen.length_16dp)
        divider.setDividerInsetStartResource(requireContext(), R.dimen.length_16dp)
        divider.dividerColor = ContextCompat.getColor(
            requireContext(),
            com.google.android.libraries.places.R.color.quantum_grey400
        )
        simpleAdapter.submitList(sbShoes)
        binding.recycler.apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = simpleAdapter
            addItemDecoration(divider)
        }
    }

    private fun proofDialog() {
        val inflater = this.layoutInflater
        val uploadView = inflater.inflate(R.layout.layout_image_dialog, null)
        val imageView = uploadView.findViewById<ImageView>(R.id.imageView)

        GlideApp.with(this)
            .load(proofRef)
            .into(imageView)

        MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_App_MaterialAlertDialog)
            .setView(uploadView)
            .setTitle(getString(R.string.proof_of_payment))
            .setPositiveButton(getString(R.string.close)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun updateStatusDialog(update: Boolean, status: String = "") {
        val title =
            if (update) getString(R.string.update_status) else getString(R.string.cancel_order)
        val message = when (status) {
            RealtimeDatabaseConstants.NEED_VERIF -> {
                getString(R.string.status_to_pickup_queue)
            }
            RealtimeDatabaseConstants.ARRIVED_STORE -> {
                getString(R.string.status_to_washing)
            }
            RealtimeDatabaseConstants.WASHING -> {
                getString(R.string.status_to_finish_washing)
            }
            RealtimeDatabaseConstants.CUSTOMER_PICKUP -> {
                getString(R.string.status_to_complete)
            }
            else -> {
                getString(R.string.status_cancel_order)
            }
        }
        MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_App_MaterialAlertDialog)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                sbOrder?.let { sbOrder ->
                    when (status) {
                        RealtimeDatabaseConstants.NEED_VERIF -> {
                            updateOrderStatus(
                                sbOrder,
                                RealtimeDatabaseConstants.ONGOING,
                                RealtimeDatabaseConstants.QUEUE_FOR_PICKUP
                            )
                        }
                        RealtimeDatabaseConstants.ARRIVED_STORE -> {
                            updateOrderStatus(
                                sbOrder,
                                RealtimeDatabaseConstants.ONGOING,
                                RealtimeDatabaseConstants.WASHING
                            )
                        }
                        RealtimeDatabaseConstants.WASHING -> {
                            updateOrderStatus(
                                sbOrder,
                                RealtimeDatabaseConstants.ONGOING,
                                RealtimeDatabaseConstants.FINISH_WASHING
                            )
                        }
                        RealtimeDatabaseConstants.CUSTOMER_PICKUP -> {
                            updateOrderStatus(
                                sbOrder,
                                RealtimeDatabaseConstants.COMPLETE,
                                RealtimeDatabaseConstants.STATUS_COMPLETE
                            )
                        }
                        else -> {
                            updateOrderStatus(
                                sbOrder,
                                RealtimeDatabaseConstants.COMPLETE,
                                RealtimeDatabaseConstants.CANCELLED
                            )
                        }
                    }
                    dialog.dismiss()
                }
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun formatNumber(number: String?): String? {
        val phoneUtil = PhoneNumberUtil.getInstance()
        val formattedNumber: Phonenumber.PhoneNumber
        return try {
            val manager =
                requireActivity().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val networkCountryIso: String = manager.networkCountryIso
            formattedNumber = phoneUtil.parse(
                number,
                networkCountryIso.uppercase(Locale.getDefault())
            )
            phoneUtil.format(formattedNumber, PhoneNumberUtil.PhoneNumberFormat.E164)
        } catch (e: NumberParseException) {
            e.printStackTrace()
            number
        }
    }

    private fun updateOrderStatus(sbOrder: SbOrder, node: String, status: String) {
        orderAdminViewModel.updateOrderStatus(sbOrder, node, status)
    }

    override fun onStop() {
        super.onStop()
        valueEventListener?.let { ref.removeEventListener(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        preciseCountdown?.dispose()
        preciseCountdown = null
        _binding = null
    }
}