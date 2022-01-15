package com.example.app_prototype

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.EditText
import android.widget.TextView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity2 : AppCompatActivity() {

    lateinit var start_textview: TextView
    lateinit var ziel_textview: TextView
    lateinit var preis_textview: TextView
    lateinit var fahrzeit_textview: TextView
    lateinit var textView_shuttleinfo: TextView
    lateinit var preis: String
    lateinit var fahrzeit: String
    lateinit var start: String
    lateinit var ziel: String
    lateinit var mapFragment : SupportMapFragment
    lateinit var googleMap: GoogleMap
    lateinit var shuttle_marker: MarkerOptions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(OnMapReadyCallback {
            googleMap = it

            val iniliatization_location = LatLng(47.69, 9.18)
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(iniliatization_location, 9f))

        })

        start_textview = findViewById<TextView>(R.id.start_textview)
        ziel_textview = findViewById<TextView>(R.id.ziel_texttview)
        preis_textview = findViewById<TextView>(R.id.preis_textview2)
        fahrzeit_textview = findViewById<TextView>(R.id.fahrzeit_textview2)
        textView_shuttleinfo = findViewById<TextView>(R.id.textView_shuttleinfo)

        preis = MainActivity.preis
        start = MainActivity.start
        ziel = MainActivity.ziel
        fahrzeit = MainActivity.fahrzeit

        start_textview.text = "Start: $start"
        ziel_textview.text = "Ziel: $ziel"
        preis_textview.text = "Preis: $preis"
        fahrzeit_textview.text = "Fahrzeit: $fahrzeit"

        val timer = object: CountDownTimer(20000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                textView_shuttleinfo.text = millisUntilFinished.toString()
            }

            override fun onFinish() {
                
            }
        }
        timer.start()
    }

    override fun onBackPressed() {
        //super.onBackPressed()
        return
    }

    fun draw_marker_on_map(marker: MarkerOptions){   //marker type -> start/ziel
        googleMap.clear()
        googleMap.addMarker(marker)
    }

}