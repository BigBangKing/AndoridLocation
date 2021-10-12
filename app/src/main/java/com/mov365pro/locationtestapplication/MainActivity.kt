package com.mov365pro.locationtestapplication

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.widget.Toast

import java.util.Locale

import androidx.annotation.NonNull
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.mov365pro.locationtestapplication.GpsUtils.onGpsListener
import androidx.constraintlayout.motion.widget.Debug.getLocation
import android.annotation.SuppressLint
import android.app.Activity

import android.content.Intent


class MainActivity : AppCompatActivity() {

    private var mFusedLocationClient: FusedLocationProviderClient? = null

    private var wayLatitude = 0.0
    private var wayLongitude: Double = 0.0
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var btnLocation: Button
    private lateinit var txtLocation: TextView
    private lateinit var btnContinueLocation: Button
    private lateinit var txtContinueLocation: TextView
    private var stringBuilder: StringBuilder = java.lang.StringBuilder()

    private var isContinue = false
    private var isGPS = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.txtContinueLocation = findViewById(R.id.txtContinueLocation);
        this.btnContinueLocation = findViewById(R.id.btnContinueLocation);
        this.txtLocation = findViewById(R.id.txtLocation);
        this.btnLocation = findViewById(R.id.btnLocation);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
        locationRequest.interval = 10 * 1000; // 10 seconds
        locationRequest.fastestInterval = 5 * 1000; // 5 seconds

        GpsUtils(this).turnGPSOn(object : onGpsListener {
            override fun gpsStatus(isGPSEnable: Boolean) {
                // turn on GPS
                isGPS = isGPSEnable
            }
        })

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    if (location != null) {
                        wayLatitude = location.latitude
                        wayLongitude = location.longitude
                        if (!isContinue) {
                            txtLocation.text =
                                String.format(Locale.US, "%s - %s", wayLatitude, wayLongitude)
                        } else {
                            stringBuilder.append(wayLatitude)
                            stringBuilder.append("-")
                            stringBuilder.append(wayLongitude)
                            stringBuilder.append("\n\n")
                            txtContinueLocation.text = stringBuilder.toString()
                        }
                        if (!isContinue && mFusedLocationClient != null) {
                            mFusedLocationClient!!.removeLocationUpdates(locationCallback)
                        }
                    }
                }
            }
        }

        btnLocation.setOnClickListener { v: View? ->
            if (!isGPS) {
                Toast.makeText(this, "Please turn on GPS", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            isContinue = false
            getLocation()
        }

        btnContinueLocation.setOnClickListener { v: View? ->
            if (!isGPS) {
                Toast.makeText(this, "Please turn on GPS", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            isContinue = true
            stringBuilder = StringBuilder()
            getLocation()
        }


    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                AppConstants.LOCATION_REQUEST
            )
        } else {
            if (isContinue) {
                mFusedLocationClient!!.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    null
                )
            } else {
                mFusedLocationClient!!.lastLocation.addOnSuccessListener(
                    this@MainActivity
                ) { location: Location? ->
                    if (location != null) {
                        wayLatitude = location.latitude
                        wayLongitude = location.longitude
                        txtLocation.text = String.format(
                            Locale.US,
                            "%s - %s",
                            wayLatitude,
                            wayLongitude
                        )
                    } else {
                        mFusedLocationClient!!.requestLocationUpdates(
                            locationRequest,
                            locationCallback,
                            null
                        )
                    }
                }
            }
        }
    }


    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1000 -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    if (isContinue) {
                        mFusedLocationClient!!.requestLocationUpdates(
                            locationRequest,
                            locationCallback,
                            null
                        )
                    } else {
                        mFusedLocationClient!!.lastLocation.addOnSuccessListener(
                            this@MainActivity
                        ) { location: Location? ->
                            if (location != null) {
                                wayLatitude = location.latitude
                                wayLongitude = location.longitude
                                txtLocation.text = String.format(
                                    Locale.US,
                                    "%s - %s",
                                    wayLatitude,
                                    wayLongitude
                                )
                            } else {
                                mFusedLocationClient!!.requestLocationUpdates(
                                    locationRequest,
                                    locationCallback,
                                    null
                                )
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == AppConstants.GPS_REQUEST) {
                isGPS = true // flag maintain before get location
            }
        }
    }

}