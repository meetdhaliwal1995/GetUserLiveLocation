package com.interviewcurrentlocation

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    var client: FusedLocationProviderClient? = null
    private lateinit var mmap: GoogleMap
    private lateinit var lastLocation: Location
    var currentmarker: Marker? = null
    private var livelocation: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        client = LocationServices.getFusedLocationProviderClient(this)

        fetchlocaton()

    }

    private fun fetchlocaton() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                1000
            )
            return
        }

        val task = client?.lastLocation
        task?.addOnSuccessListener { location ->
            if (location != null) {
                this.lastLocation = location
                val mapFragment = supportFragmentManager
                    .findFragmentById(R.id.map) as SupportMapFragment
                mapFragment.getMapAsync(this)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {

            1000 -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchlocaton()
            } else {
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show()
                showPermissionSetting()


            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mmap = googleMap

        val latlong = LatLng(lastLocation.latitude, lastLocation.longitude)
        drawmarker(latlong)
        mmap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDrag(p0: Marker) {
            }

            override fun onMarkerDragEnd(p0: Marker) {

                if (lastLocation != null) {
                    currentmarker?.remove()
                    val newLatlong = LatLng(p0.position.latitude, p0.position.latitude)
                    drawmarker(newLatlong)
                }
            }

            override fun onMarkerDragStart(p0: Marker) {
            }
        })
    }


    private fun drawmarker(latlong: LatLng) {
        val markerOptions = MarkerOptions().position(latlong).title(" UserLocation")
            .snippet(getAddress(latlong.latitude, latlong.longitude)).draggable(true)
        currentmarker = mmap.addMarker(markerOptions)

        var latLng = LatLng(latlong.latitude, latlong.longitude)
        var cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15f)

        mmap.animateCamera(cameraUpdate)
        livelocation = getAddress(latlong.latitude, latlong.longitude)
        startService()
        currentmarker?.showInfoWindow()
    }

    private fun getAddress(lat: Double, lon: Double): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        val address = geocoder.getFromLocation(lat, lon, 1)
        return address[0].getAddressLine(0).toString()
    }

    public fun startService() {
        var intent = Intent(this, ForgroundService::class.java)
        intent.putExtra("userLocation", livelocation)
        Log.e("intentloc", livelocation.toString())
//        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
        startService(intent)
    }

    public fun stopService() {
        var intent = Intent(this, ForgroundService::class.java)
        startService(intent)
    }

    fun showPermissionSetting() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this,
                "Storage Permission needed. Please allow in App Settings for Uploading Image functionality.",
                Toast.LENGTH_LONG
            ).show()
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", this.packageName, null)
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            this.startActivityForResult(intent, 89)
        }
    }
}


