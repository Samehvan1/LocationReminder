package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.media.audiofx.Equalizer.Settings
import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.*
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Transformations.map
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import kotlinx.android.synthetic.main.fragment_select_location.*
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var mMap: GoogleMap
    private var marker: Marker? = null
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private val fusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        var mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnsave.setOnClickListener {
            onLocationSelected()
        }
        requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    getMyLocation()
                } else {
                    Toast.makeText(requireContext(),"Access denied ",Toast.LENGTH_LONG).show()
                    showshouldShowRequestPermissionRationale()
                }
            }
    }

    private fun onLocationSelected() {
        marker?.let { mrkr ->
            _viewModel.latitude.value = mrkr.position.latitude
            _viewModel.longitude.value = mrkr.position.longitude
            _viewModel.reminderSelectedLocationStr.value = mrkr.title
            _viewModel.navigationCommand.value = NavigationCommand.Back

        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(p0: GoogleMap) {
        mMap = p0
        setMapLongClick(mMap)
        setMapStyle(mMap)
        setPOIClick(mMap)
        getMyLocation()
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            map.clear()
            marker = map.addMarker(
                MarkerOptions().position(latLng).title(getString(R.string.dropped_pin))
            )
            marker?.showInfoWindow()
            map.animateCamera(CameraUpdateFactory.newLatLng(latLng))
            btnsave.visibility = VISIBLE
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )
        } catch (e: Resources.NotFoundException) {
            Log.e("MapStyleFile", "Can not find style file error: ", e)
        }
    }

    private fun setPOIClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            map.clear()
            marker = map.addMarker(MarkerOptions().position(poi.latLng).title(poi.name))
            marker?.showInfoWindow()
            map.animateCamera(CameraUpdateFactory.newLatLng(poi.latLng))
            btnsave.visibility = VISIBLE
        }
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun getMyLocation() {
        if(!isPermissionGranted()){
            requestPermissions()
            return
        }
        mMap.setMyLocationEnabled(true)
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
            it?.let {
                var loca = LatLng(it.latitude, it.longitude)
                mMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(loca, 18f)
                )
                marker = mMap.addMarker(
                    MarkerOptions().position(loca).title("My Location")
                )
                marker?.showInfoWindow()
            }
        }

    }

    private fun requestPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
                getMyLocation()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
            showshouldShowRequestPermissionRationale()
        }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getMyLocation()
        } else {
            showshouldShowRequestPermissionRationale()
        }

    }

    private fun showshouldShowRequestPermissionRationale() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            Snackbar.make(binding.mapHolder,R.string.permission_denied_explanation,Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.settings){
                    startActivity(Intent().apply {
                        action=android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data= Uri.fromParts("package",BuildConfig.APPLICATION_ID,null)
                        flags=Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()

        } else {
            requestPermissions()
        }
    }
}
