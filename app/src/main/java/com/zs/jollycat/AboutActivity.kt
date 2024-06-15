package com.zs.jollycat

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class AboutActivity : AppCompatActivity(), OnMapReadyCallback {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private lateinit var mapView: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val fragment = supportFragmentManager
            .findFragmentById(R.id.mapFragmentView) as SupportMapFragment
        fragment.getMapAsync(this)

        val companyName = findViewById<TextView>(R.id.companyName)
        val companyDesc = findViewById<TextView>(R.id.companyDesc)

        companyName.text = "JollyCat: Connecting Cat Lovers with Feline Companions"
        companyDesc.text = "JollyCat is a company dedicated to bringing cat lovers and their perfect furry friends together."

        val backButton: Button = findViewById(R.id.btn_back)
        backButton.setOnClickListener {
            onBackPressed()
        }
        val btnOpenMaps = findViewById<Button>(R.id.btn_maps)

        btnOpenMaps.setOnClickListener {
            val jollyCatLocation = LatLng(-6.20175, 106.78208)
            val uri = Uri.parse("geo:$jollyCatLocation.latitude,$jollyCatLocation.longitude")
            val mapIntent = Intent(Intent.ACTION_VIEW, uri)
            mapIntent.setPackage("com.google.android.apps.maps")

            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent);
            } else {
                Toast.makeText(this, "Google Maps app not found!", Toast.LENGTH_SHORT).show();
            }
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mapView = googleMap

        val jollyCatLocation = LatLng(-6.20175, 106.78208)
        mapView.addMarker(MarkerOptions().position(jollyCatLocation).title("JollyCat's Store"))
        mapView.moveCamera(CameraUpdateFactory.newLatLngZoom(jollyCatLocation, 15f))
    }

}
