package com.polotechnologies.delvis.ui.location.googleMapFragment

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.*
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.polotechnologies.delvis.R
import com.polotechnologies.delvis.databinding.FragmentMapBinding
import java.util.*


/**
 * A [Fragment] to host google maps.
 */
class MapFragment : Fragment(), PlaceSelectionListener {

    private lateinit var mBinding: FragmentMapBinding
    private lateinit var placesClient: PlacesClient
    private lateinit var autoCompleteFragment: AutocompleteSupportFragment

    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_map, container, false)
        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        initializePlaces()

        return mBinding.root
    }

    private fun initializePlaces() {
        if (!Places.isInitialized()) {
            Places.initialize(context!!, getString(R.string.google_maps_key))
            placesClient = Places.createClient(context!!)
        }
        autoCompleteFragment = childFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        autoCompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS))
        autoCompleteFragment.setCountry("KE")
        autoCompleteFragment.setHint("Address, Location")
        autoCompleteFragment.setOnPlaceSelectedListener(this)
    }

    override fun onPlaceSelected(place: Place) {
        val userRef = firestore.collection("users").document(mAuth.currentUser!!.uid)
        userRef.update("user_location_address", place.address)
            .addOnSuccessListener {
                Toast.makeText(context, "Delivery Address Set : ${place.address}", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_mapFragment_to_homeHostFragment)
            }.addOnFailureListener {exception ->
                Toast.makeText(context, "Failed to set address:  ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onError(status: Status) {
        Toast.makeText(context, "Error : ${status.statusMessage}", Toast.LENGTH_SHORT).show()
    }

}
