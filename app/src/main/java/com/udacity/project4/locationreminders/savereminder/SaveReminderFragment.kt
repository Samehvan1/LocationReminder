package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.Instrumentation.ActivityResult
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.gesture.GestureUtils
import android.net.Uri
import android.provider.Settings
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var reminderDataItem: ReminderDataItem
    private lateinit var locationLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var permissionsLauncher: ActivityResultLauncher<Array<String>>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel
        geofencingClient = LocationServices.getGeofencingClient(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude
            val longitude = _viewModel.longitude.value
            reminderDataItem =
                ReminderDataItem(title, description.value, location, latitude.value, longitude)
            if (_viewModel.validateEnteredData(reminderDataItem)) {
                if (isPermissionGranted()) {
                    checkLocationAndStartGeoFence()
                } else grantPermissions()
            }
        }
        permissionsLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                if (it.all { res -> res.value!! }) {
                    if (isPermissionGranted()) checkLocationAndStartGeoFence()
                    else grantPermissions()
                } else {
                    Snackbar.make(
                        binding.hodlerLayout,
                        R.string.select_poi,
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .setAction(R.string.settings) {
                            startActivity(Intent().apply {
                                action =
                                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            })
                        }.show()
                }
            }
        locationLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
                if (it.resultCode == RESULT_OK) {
                    Toast.makeText(requireContext(), "permission granted", Toast.LENGTH_LONG).show()
                    addGeoFence()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "permission result " + it.resultCode,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private val geoFencePendingIntent: PendingIntent by lazy {
        val intnt = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(requireContext(), 50, intnt, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun checkLocationAndStartGeoFence() {
        var request = LocationRequest.create()
        request.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        var builder = LocationSettingsRequest.Builder().addLocationRequest(request)
        var settingsClient = LocationServices.getSettingsClient(requireActivity())
        var response = settingsClient.checkLocationSettings(builder.build())
        response.addOnCompleteListener {
            if (it.isSuccessful) {
                addGeoFence()
            }
        }
        response.addOnFailureListener {
            if (it is ResolvableApiException) {
                try {
                    var request2 = IntentSenderRequest.Builder(it.resolution).build()
                    locationLauncher.launch(request2)
                } catch (ex: IntentSender.SendIntentException) {
                    Log.d("Location Failed", "can not retrieve location settings " + ex.message)
                }
            } else {
                Snackbar.make(
                    requireView(),
                    "Could not check location, please make sure to grant permissions",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(android.R.string.ok) {
                        checkLocationAndStartGeoFence()
                    }.show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun addGeoFence() {
        val curGeo = reminderDataItem
        val geo = Geofence.Builder()
            .setCircularRegion(curGeo.latitude!!, curGeo.longitude!!, 100f)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setRequestId(curGeo.id)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()
        val geoRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geo)
            .build()
        geofencingClient.addGeofences(geoRequest, geoFencePendingIntent).run {
            addOnSuccessListener {
                _viewModel.validateAndSaveReminder(reminderDataItem)
            }
            addOnFailureListener {
                Toast.makeText(requireContext(), "Error when adding geofence", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun isPermissionGranted(): Boolean {
        return isForePermissionGranted() && isBackGroundGranted()
    }

    private fun isForePermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun grantPermissions() {
        if (isForePermissionGranted() && isBackGroundGranted()) {
            checkLocationAndStartGeoFence()
        } else if (!isForePermissionGranted()) {

            grantForeGroundPermisson()
        } else if (!isBackGroundGranted()) {
            if (runningQVersion)
                permissionsLauncher.launch(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
        }
    }

    private val runningQVersion = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    @TargetApi(Build.VERSION_CODES.Q)
    private fun isBackGroundGranted(): Boolean {
        if (runningQVersion)
            return ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        else return true
    }

    @TargetApi(Build.VERSION_CODES.Q)
    private fun grantForeGroundPermisson() {

        permissionsLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001) {
            if (resultCode == Activity.RESULT_OK) checkLocationAndStartGeoFence()
        } else if (requestCode == 29) {
            if (resultCode == Activity.RESULT_OK) addGeoFence()
        } else checkLocationAndStartGeoFence()
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED ||
            (runningQVersion && grantResults[1] != PackageManager.PERMISSION_GRANTED)
        ) {
            Snackbar.make(
                binding.hodlerLayout,
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.settings) {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
        } else addGeoFence()
    }
}
