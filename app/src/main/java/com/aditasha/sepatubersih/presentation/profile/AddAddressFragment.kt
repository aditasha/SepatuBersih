package com.aditasha.sepatubersih.presentation.profile

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.aditasha.sepatubersih.BuildConfig
import com.aditasha.sepatubersih.R
import com.aditasha.sepatubersih.databinding.FragmentAddAddressBinding
import com.aditasha.sepatubersih.domain.model.Result
import com.aditasha.sepatubersih.domain.model.SbAddress
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class AddAddressFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentAddAddressBinding? = null

    private val binding get() = _binding!!

    private val args: AddAddressFragmentArgs by navArgs()

    private val profileViewModel: ProfileViewModel by activityViewModels()

    private var located = false
    private lateinit var geocoder: Geocoder
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var map: GoogleMap
    private var address = MutableSharedFlow<Address>()

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    // Precise location access granted.
                    getMyLastLocation()
                }
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    // Only approximate location access granted.
                    getMyLastLocation()
                }
                else -> {
                    val snack = Snackbar.make(
                        requireView(),
                        "Allow location permission to set address",
                        Snackbar.LENGTH_SHORT
                    )
                    val params = snack.view.layoutParams as CoordinatorLayout.LayoutParams
                    params.gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
                    params.setMargins(0, ACTION_BAR_HEIGHT, 0, 0)
                    snack.view.layoutParams = params
                    snack.show()
                    findNavController().navigateUp()
                }
            }
        }

    private val resolutionLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            when (result.resultCode) {
                AppCompatActivity.RESULT_OK ->
                    Log.i(
                        ContentValues.TAG,
                        "onActivityResult: All location settings are satisfied."
                    )
                AppCompatActivity.RESULT_CANCELED ->
                    Toast.makeText(
                        requireActivity(),
                        "Anda harus mengaktifkan GPS untuk menggunakan aplikasi ini!",
                        Toast.LENGTH_SHORT
                    ).show()
            }
        }

    private val startAutoComplete =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            when (result.resultCode) {
                AppCompatActivity.RESULT_OK -> {
                    val intent = result.data
                    if (intent != null) {
                        val place = Autocomplete.getPlaceFromIntent(intent)
                        fetchAutoComplete(place)
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    val intent = result.data
                    if (intent != null) {
                        val status = Autocomplete.getStatusFromIntent(intent)
                        val snack = Snackbar.make(
                            requireView(),
                            status.statusMessage.toString(),
                            Snackbar.LENGTH_SHORT
                        )
                        val params = snack.view.layoutParams as CoordinatorLayout.LayoutParams
                        params.gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
                        params.setMargins(0, ACTION_BAR_HEIGHT, 0, 0)
                        snack.view.layoutParams = params
                        snack.show()
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddAddressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        Places.initialize(requireActivity(), BuildConfig.MAPS_API_KEY)
        Places.createClient(requireActivity())

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        geocoder = Geocoder(requireActivity())
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.googleMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_search -> startAutoComplete()
            }
            true
        }

        binding.apply {
            nameEditText.doOnTextChanged { text, _, _, _ ->
                if (text != null) profileViewModel.checkAddressForm(text.toString())
            }

            addAddress.setOnClickListener {
                val notes = notesEditText.text.toString()
                val latLng = map.cameraPosition.target
                val address = SbAddress(
                    nameEditText.text.toString(),
                    addressEditText.text.toString(),
                    notes.ifBlank { "-" },
                    latLng.latitude, latLng.longitude
                )
                if (args.address != null) {
                    address.key = args.address!!.key
                    profileViewModel.updateAddress(address)
                } else profileViewModel.addAddress(address)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            profileViewModel.addressName.collectLatest { notBlank ->
                binding.apply {
                    addAddress.isEnabled = notBlank
                    if (!notBlank) {
                        name.error = getString(R.string.name_cant_empty)
                    } else {
                        name.error = null
                        name.isErrorEnabled = false
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            profileViewModel.addressResult.collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        binding.loading.isVisible = false
                        if (args.address != null) {
                            Toast.makeText(
                                requireActivity(),
                                getString(R.string.success_update_address),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else {
                            Toast.makeText(
                                requireActivity(),
                                getString(R.string.success_add_address),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        findNavController().navigateUp()
                    }
                    is Result.Error -> {
                        binding.loading.isVisible = false
                        val snack = result.exception.localizedMessage?.let {
                            Snackbar.make(
                                requireView(),
                                it,
                                Snackbar.LENGTH_SHORT
                            )
                        }
                        val params = snack?.view?.layoutParams as CoordinatorLayout.LayoutParams
                        params.gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
                        params.setMargins(0, ACTION_BAR_HEIGHT, 0, 0)
                        snack.view.layoutParams = params
                        snack.show()
                    }
                    is Result.Loading -> binding.loading.isVisible = true
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        createLocationRequest()

        viewLifecycleOwner.lifecycleScope.launch {
            address.collectLatest {
                showAddress(it)
            }
        }

        map.setOnCameraMoveStartedListener {
            binding.root.requestDisallowInterceptTouchEvent(true)
        }

        map.setOnCameraIdleListener {
            binding.root.requestDisallowInterceptTouchEvent(false)

            if (located) {
                val latLng = map.cameraPosition.target
                if (Build.VERSION.SDK_INT < 33) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        val addresses =
                            geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                        if (!addresses.isNullOrEmpty()) address.emit(addresses[0])
                    }
                } else {
                    val geocodeListener = Geocoder.GeocodeListener {
                        val address = it[0]
                        showAddress(address)
                    }
                    geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1, geocodeListener)
                }
            }
        }
    }

    private fun createLocationRequest() {
        val locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, TimeUnit.SECONDS.toMillis(1))
                .setWaitForAccurateLocation(true)
                .build()
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(requireActivity())
        client.checkLocationSettings(builder.build())
            .addOnSuccessListener {
                getMyLastLocation()
            }
            .addOnFailureListener { exception ->
                located = false

                if (exception is ResolvableApiException) {
                    try {
                        resolutionLauncher.launch(
                            IntentSenderRequest.Builder(exception.resolution).build()
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                        Toast.makeText(
                            requireActivity(),
                            sendEx.localizedMessage?.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }

    @SuppressLint("MissingPermission")
    private fun getMyLastLocation() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    located = true
                    val argsAddress = args.address
                    if (argsAddress != null) {
                        val latLng = LatLng(argsAddress.latitude!!, argsAddress.longitude!!)
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16F))
                        binding.apply {
                            addressEditText.setText(argsAddress.address)
                            nameEditText.setText(argsAddress.name)
                            notesEditText.setText(argsAddress.note)
                            addAddress.text = getString(R.string.update_address)
                        }
                    } else {
                        val latLng = LatLng(location.latitude, location.longitude)
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16F))
                    }
                } else {
                    located = false
                    Toast.makeText(
                        requireActivity(),
                        "Location is not found. Try Again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            requireActivity(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun showMarker(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        map.addMarker(
            MarkerOptions()
                .position(latLng)
        )
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16F))
    }

    private fun showAddress(address: Address) {
        val addressText = address.getAddressLine(0)
        requireActivity().runOnUiThread { binding.addressEditText.setText(addressText) }
    }

    @Suppress("DEPRECATION")
    private fun startAutoComplete() {
        if (located) {
            val fields = listOf(
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG
            )

            val latLng = map.projection.visibleRegion.latLngBounds
            val bounds = RectangularBounds.newInstance(latLng)

            // Build the autocomplete intent with field, country, and type filters applied

            // Build the autocomplete intent with field, country, and type filters applied
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .setCountry("ID")
                .setLocationBias(bounds)
                .setTypeFilter(TypeFilter.ADDRESS)
                .build(requireActivity())
            startAutoComplete.launch(intent)
        }
    }

    private fun fetchAutoComplete(place: Place) {
        val latLng = place.latLng
        val address = place.address
        if (latLng != null) map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16F))
        if (address != null) binding.addressEditText.setText(address)
    }

    companion object {
        const val ACTION_BAR_HEIGHT = 168
        const val META_DATA = "com.google.android.geo.API_KEY"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}