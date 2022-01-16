package com.example.app_prototype

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import java.io.File

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
    lateinit var googleMap2: GoogleMap
    lateinit var last_timer: CountDownTimer
    var shuttle_marker: Marker? = null
    var map_ready: Boolean = false
    var marker_icon: BitmapDescriptor? = null
    var active_timer = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(OnMapReadyCallback {
            googleMap2 = it
            map_ready = true
            val iniliatization_location = LatLng(47.69, 9.18)
            googleMap2.animateCamera(CameraUpdateFactory.newLatLngZoom(iniliatization_location, 9f))
            marker_icon = bitmapDescriptorFromVector(this, R.drawable.ic_shuttle_white)
            shuttle_marker = googleMap2.addMarker(MarkerOptions().position(iniliatization_location).title("Shuttle").icon(marker_icon))

            googleMap2.setOnCameraMoveListener {
                val timer = object: CountDownTimer(5000, 5000) {
                    override fun onTick(millisUntilFinished: Long) {
                    }

                    override fun onFinish() {
                        if (last_timer == this){
                            var location = shuttle_marker!!.position
                            googleMap2.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
                            active_timer = false
                        }
                    }
                }
                last_timer = timer
                active_timer = true
                timer.start()
            }
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

        //var csv_file_string = applicationInfo.dataDir + File.separatorChar + "csvfile.csv" //path to csv file (in assets folder)

        val timer = object: CountDownTimer(20000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                var remaining_time = (millisUntilFinished / 1000).toString()
                textView_shuttleinfo.text = "Das Shuttle ist in $remaining_time Minuten bei Ihnen"
                draw_marker_on_map()
            }

            override fun onFinish() {
                textView_shuttleinfo.text = "Das Shuttle befindet sich nun an der Startposition"
            }
        }
        timer.start()
    }


    private fun bitmapDescriptorFromVector(
        context: Context,
        @DrawableRes vectorDrawableResourceId: Int
    ): BitmapDescriptor? {
        val background = ContextCompat.getDrawable(context, R.drawable.ic_marker_blue)
        background!!.setBounds(0, 0, background.intrinsicWidth, background.intrinsicHeight)
        val vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId)
        val left = (background.intrinsicWidth - vectorDrawable!!.intrinsicWidth) / 2
        val top = (background.intrinsicHeight - vectorDrawable.intrinsicHeight) / 3
        vectorDrawable.setBounds(left, top, left + vectorDrawable.intrinsicWidth, top + vectorDrawable.intrinsicHeight)
        /*
        vectorDrawable!!.setBounds(
            40,
            20,
            vectorDrawable.intrinsicWidth + 40,
            vectorDrawable.intrinsicHeight + 20
        )*/
        val bitmap = Bitmap.createBitmap(
            background.intrinsicWidth,
            background.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        background.draw(canvas)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
    
    override fun onBackPressed() {
        //super.onBackPressed()
        return
    }

    fun draw_marker_on_map(){   //marker type -> start/ziel
        if (map_ready == true){
            val current_location = LatLng(47.66876, 9.16962)
            shuttle_marker!!.remove()
            shuttle_marker = googleMap2.addMarker(MarkerOptions().position(current_location).title("Shuttle").icon(marker_icon))
            if(active_timer == false)
                googleMap2.moveCamera(CameraUpdateFactory.newLatLngZoom(current_location, 16f))
        }
    }

}