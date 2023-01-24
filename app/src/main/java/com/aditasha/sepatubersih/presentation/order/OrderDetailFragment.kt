package com.aditasha.sepatubersih.presentation.order

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.BuildCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.aditasha.sepatubersih.R
import com.aditasha.sepatubersih.ServerTime
import com.aditasha.sepatubersih.data.RealtimeDatabaseConstants
import com.aditasha.sepatubersih.databinding.FragmentOrderDetailBinding
import com.aditasha.sepatubersih.domain.model.Result
import com.aditasha.sepatubersih.domain.model.SbOrder
import com.aditasha.sepatubersih.domain.model.SbShoes
import com.aditasha.sepatubersih.presentation.GlideApp
import com.aditasha.sepatubersih.presentation.uriToFile
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.modernstorage.photopicker.PhotoPicker
import dagger.hilt.android.AndroidEntryPoint
import id.zelory.compressor.Compressor
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@BuildCompat.PrereleaseSdkCheck
@AndroidEntryPoint
class   OrderDetailFragment : Fragment() {
    private var _binding: FragmentOrderDetailBinding? = null

    private val binding get() = _binding!!

    private val args: OrderDetailFragmentArgs by navArgs()

    private val orderViewModel: OrderViewModel by activityViewModels()
    private var currentUser: FirebaseUser? = null

    @Inject
    lateinit var firebaseDatabase: FirebaseDatabase

    @Inject
    lateinit var firebaseStorage: FirebaseStorage
    private lateinit var ref: DatabaseReference
    private var valueEventListener: ValueEventListener? = null

    @Inject
    lateinit var serverTime: ServerTime
    private var preciseCountdown: PreciseCountdown? = null
    private var sbOrder: SbOrder? = null

    private val photoPicker =
        registerForActivityResult(PhotoPicker()) { uriList ->
            if (uriList != null && uriList.isNotEmpty()) proofConfirmDialog(uriList[0])
        }

    private var loadingDialog: androidx.appcompat.app.AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUser = orderViewModel.currentUser

        ref = firebaseDatabase.reference.child(RealtimeDatabaseConstants.ORDER_DETAIL)
            .child(args.key)

        valueEventListener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val order = snapshot.getValue(SbOrder::class.java)!!
                sbOrder = order
                val orderDate = Date(order.orderTimestamp!!)
                val statusDate = Date(order.statusTimestamp!!)
                val sfd = SimpleDateFormat("dd MMMM yyyy, HH:mm")
                val orderAddress = order.address!!
                val format = DecimalFormat("0.000")
                val totalPrice = getString(R.string.price_placeholder, format.format(order.price))
                binding.apply {
                    val needPayment = order.status!! == RealtimeDatabaseConstants.NEED_PAYMENT
                    val finishedWashing = order.status!! == RealtimeDatabaseConstants.FINISH_WASHING
                    paymentTimer.isVisible = needPayment
                    proofButton.isVisible = needPayment
                    paymentTutorButton.isVisible = needPayment
                    pickupButton.isVisible = finishedWashing
                    delivButton.isVisible = finishedWashing

                    orderId.text = getString(R.string.order_id_placeholder, order.id)
                    orderTimestamp.text = sfd.format(orderDate)
                    orderStatus.text = order.status
                    statusTimestamp.text =
                        getString(R.string.updated_on_placeholder, sfd.format(statusDate))
                    addressName.text = orderAddress.name
                    address.text = orderAddress.address
                    addressNotes.text = getString(R.string.notes_placeholder, orderAddress.note)
                    setRecycler(order.shoes!!)
                    price.text = totalPrice

                    if (order.status!! == RealtimeDatabaseConstants.NEED_PAYMENT) {
                        lifecycleScope.launch { setTimer(order.endTimestamp!!) }
                        paymentStatus.text =
                            getString(R.string.need_proof)
                    } else paymentStatus.isVisible = false
//                    else if (order.status!! == RealtimeDatabaseConstants.CANCELLED || order.status!! == RealtimeDatabaseConstants.EXPIRED)
//                        paymentStatus.isVisible = false
//                    else {
//                        paymentStatus.text = getString(R.string.paid)
//                        val param = paymentStatus.layoutParams as LinearLayoutCompat.LayoutParams
//                        param.gravity = Gravity.END
//                        paymentStatus.layoutParams = param
//                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        valueEventListener?.let { ref.addValueEventListener(it) }

        viewLifecycleOwner.lifecycleScope.launch {
            orderViewModel.uploadProof.collectLatest { result ->
                when (result) {
                    is Result.Loading -> {}
                    is Result.Error -> {}
                    is Result.Success -> {
                        val proofKey = result.data as String
                        updateStatusNeedVerif(proofKey)
                        loadingDialog?.dismiss()
                    }
                }
            }
        }

        binding.apply {
            proofButton.setOnClickListener {
                photoPicker.launch(PhotoPicker.Args(PhotoPicker.Type.IMAGES_ONLY, 1))
            }
            paymentTutorButton.setOnClickListener {
                howToPayDialog()
            }
            pickupButton.setOnClickListener {
                pickupDelivConfirmDialog(pickup = true)
            }
            delivButton.setOnClickListener {
                pickupDelivConfirmDialog(pickup = false)
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

    private suspend fun setTimer(endTime: Long) {
        preciseCountdown?.dispose()
        val currentTime = Date().time + serverTime.getServerTime()
        val totalTime = endTime - currentTime
        if (totalTime > 0) {
            preciseCountdown = object : PreciseCountdown(totalTime, 1000L) {
                override fun onTick(timeLeft: Long) {
                    val time = Date(timeLeft)
                    val sfd = SimpleDateFormat("mm:ss")

                    requireActivity().runOnUiThread {
                        _binding?.let {
                            it.paymentTimer.text =
                                getString(R.string.expired_in_placeholder, sfd.format(time))
                        }
                    }
                }

                override fun onFinished() {
                    onTick(0)
                    updateStatusExpired()
                    dispose()
                    preciseCountdown = null
                }
            }
            preciseCountdown?.start()
        } else updateStatusExpired()
    }

    private fun updateStatusExpired() {
        sbOrder?.let { sbOrder ->
            orderViewModel.updateOrderStatus(
                sbOrder,
                RealtimeDatabaseConstants.COMPLETE,
                RealtimeDatabaseConstants.EXPIRED
            )
//            valueEventListener?.let { ref.removeEventListener(it) }
//            ref = firebaseDatabase.reference.child(RealtimeDatabaseConstants.ORDER_DETAIL)
//                .child(RealtimeDatabaseConstants.COMPLETE)
//                .child(sbOrder.key!!)
//            valueEventListener?.let { ref.addValueEventListener(it) }
        }
    }

    private fun updateStatusNeedVerif(proofKey: String) {
        sbOrder?.let { sbOrder ->
            preciseCountdown?.dispose()
            orderViewModel.updateOrderStatus(
                sbOrder,
                RealtimeDatabaseConstants.ONGOING,
                RealtimeDatabaseConstants.NEED_VERIF,
                proofKey
            )
//            valueEventListener?.let { ref.removeEventListener(it) }
//            ref = firebaseDatabase.reference.child(RealtimeDatabaseConstants.ORDER)
//                .child(RealtimeDatabaseConstants.ONGOING)
//                .child(sbOrder.key!!)
//            valueEventListener?.let { ref.addValueEventListener(it) }
        }
    }

    private fun updateStatusPickupDeliv(pickup: Boolean) {
        sbOrder?.let { sbOrder ->
            if (pickup)
                orderViewModel.updateOrderStatus(
                    sbOrder,
                    RealtimeDatabaseConstants.ONGOING,
                    RealtimeDatabaseConstants.CUSTOMER_PICKUP
                )
            else
                orderViewModel.updateOrderStatus(
                    sbOrder,
                    RealtimeDatabaseConstants.ONGOING,
                    RealtimeDatabaseConstants.QUEUE_FOR_DELIV
                )
        }
    }

    private fun proofOfPaymentResult(uri: Uri) {
        sbOrder?.let {
            viewLifecycleOwner.lifecycleScope.launch {
                val tempFile = uriToFile(uri, requireContext())
                val reducedSize = Compressor.compress(requireContext(), tempFile)
                orderViewModel.uploadProof(reducedSize, it.key!!)
                loadingDialog()
            }
        }
    }

    private fun proofConfirmDialog(uri: Uri) {
        val inflater = this.layoutInflater
        val uploadView = inflater.inflate(R.layout.layout_image_dialog, null)
        val imageView = uploadView.findViewById<ImageView>(R.id.imageView)

        Glide.with(this)
            .load(uri)
            .into(imageView)

        MaterialAlertDialogBuilder(requireContext())
            .setView(uploadView)
            .setTitle(getString(R.string.proof_of_payment))
            .setMessage(getString(R.string.want_to_upload_proof))
            .setPositiveButton(getString(R.string.upload)) { dialog, _ ->
                proofOfPaymentResult(uri)
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setOnDismissListener {
                Glide.with(this).clear(imageView)
            }
            .show()
    }

    private fun loadingDialog() {
        val inflater = this.layoutInflater
        val loadingView = inflater.inflate(R.layout.layout_loading_dialog, null)
        val textView = loadingView.findViewById<TextView>(R.id.loadingText)
        textView.text = getString(R.string.uploading_proof)
        val builder = MaterialAlertDialogBuilder(
            requireContext(),
            R.style.ThemeOverlay_App_MaterialAlertDialog
        )
            .setView(loadingView)
            .setCancelable(false)

        loadingDialog = builder.create()
        loadingDialog?.show()
    }

    private fun pickupDelivConfirmDialog(pickup: Boolean) {
        val title =
            if (pickup) getString(R.string.confirm_self_pickup) else getString(R.string.confirm_delivery)
        val message =
            if (pickup) getString(R.string.message_self_pickup) else getString(R.string.message_delivery)
        MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_App_MaterialAlertDialog)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                updateStatusPickupDeliv(pickup)
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun howToPayDialog() {
        viewLifecycleOwner.lifecycleScope.launch {
            val ref = firebaseStorage.reference.child("qris")
                .child("Sepatu Bersih QRIS.png")
            try {
                val url = ref.downloadUrl.await()
                if (url != null) {
                    val inflater = this@OrderDetailFragment.layoutInflater
                    val howToView = inflater.inflate(R.layout.layout_how_to_pay_dialog, null)
                    val imageView = howToView.findViewById<ImageView>(R.id.imageView)

                    val circularProgressDrawable = CircularProgressDrawable(requireContext())
                    circularProgressDrawable.setColorSchemeColors(android.R.attr.colorPrimary)
                    circularProgressDrawable.strokeWidth = 5f
                    circularProgressDrawable.centerRadius = 15f
                    circularProgressDrawable.start()

                    GlideApp.with(this@OrderDetailFragment)
                        .load(ref)
//                        .placeholder(circularProgressDrawable)
                        .into(imageView)

                    val textView = howToView.findViewById<TextView>(R.id.messageDialog)
                    val title = getString(R.string.detail_how_to_pay)
                    textView.text = setMessageWithQrisLink(url)
                    textView.movementMethod = LinkMovementMethod.getInstance()

                    MaterialAlertDialogBuilder(
                        requireContext(),
                        R.style.ThemeOverlay_App_MaterialAlertDialog
                    )
                        .setTitle(title)
                        .setView(howToView)
                        .setPositiveButton(getString(R.string.close)) { dialog, _ ->
                            dialog.dismiss()
                        }.show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setMessageWithQrisLink(uri: Uri): SpannableString {
        //The text and the URL
        val content = getString(R.string.how_to_pay_message)
        //Clickable Span will help us to make clickable a text
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                //To open the url in a browser
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }

            override fun updateDrawState(textPaint: TextPaint) {
                super.updateDrawState(textPaint)
            }
        }
        val startIndex = content.indexOf("here")
        val endIndex = startIndex + "here".length
        //SpannableString will be created with the full content and
        // the clickable content all together
        val spannableString = SpannableString(content)
        //only the word 'link' is clickable
        spannableString.setSpan(
            clickableSpan,
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannableString
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