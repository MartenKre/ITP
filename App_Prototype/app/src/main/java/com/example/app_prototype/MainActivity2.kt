package com.example.app_prototype

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity2 : AppCompatActivity() {

    lateinit var start_textview: TextView
    lateinit var ziel_textview: TextView
    lateinit var preis_textview: TextView
    lateinit var fahrzeit_textview: TextView
    lateinit var textView_shuttleinfo: TextView
    lateinit var textView_main_caption: TextView
    lateinit var transport_textview: TextView
    lateinit var preis: String
    lateinit var fahrzeit: String
    lateinit var start: String
    lateinit var ziel: String
    lateinit var mapFragment : SupportMapFragment
    lateinit var googleMap2: GoogleMap
    lateinit var last_timer: CountDownTimer
    lateinit var intent_page3: Intent
    var shuttle_marker: Marker? = null
    var start_marker: Marker? = null
    var ziel_marker: Marker? = null
    var map_ready: Boolean = false
    var marker_icon: BitmapDescriptor? = null
    var active_timer = false
    val Data_Longitude: MutableList<String> = mutableListOf()
    val Data_Latitude: MutableList<String> = mutableListOf()
    var camera_moved_by_program = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        mapFragment = supportFragmentManager.findFragmentById(R.id.map_dialog) as SupportMapFragment
        mapFragment.getMapAsync(OnMapReadyCallback {
            googleMap2 = it
            map_ready = true
            val iniliatization_location = LatLng(47.69, 9.18)
            googleMap2.animateCamera(CameraUpdateFactory.newLatLngZoom(iniliatization_location, 9f))
            marker_icon = bitmapDescriptorFromVector(this, R.drawable.ic_shuttle_white)
            shuttle_marker = googleMap2.addMarker(MarkerOptions().position(iniliatization_location).title("Shuttle").icon(marker_icon))
            start_marker = googleMap2.addMarker(MarkerOptions().position(MainActivity.start_coord).title("Start"))
            ziel_marker = googleMap2.addMarker(MarkerOptions().position(MainActivity.ziel_coord).title("Ziel"))
            googleMap2.addPolyline(MainActivity.global_polyline)

            googleMap2.setOnCameraMoveListener {
                val timer = object: CountDownTimer(5000, 5000) {
                    override fun onTick(millisUntilFinished: Long) {
                    }

                    override fun onFinish() {
                        if (last_timer == this){
                            var location = shuttle_marker!!.position
                            camera_moved_by_program = true
                            googleMap2.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
                            active_timer = false
                        }
                    }
                }
                Log.d("ML", "move registered")
                if (camera_moved_by_program == false) {
                    last_timer = timer
                    active_timer = true
                    timer.start()
                }
                else
                    camera_moved_by_program = false
            }
        })

        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        intent_page3 = Intent(this, MainActivity3::class.java)  //Initialize Intent
        //startActivity(intent_page3)  //uncomment to go to page 3 directly

        start_textview = findViewById<TextView>(R.id.start_textview)
        ziel_textview = findViewById<TextView>(R.id.ziel_texttview)
        preis_textview = findViewById<TextView>(R.id.preis_textview2)
        fahrzeit_textview = findViewById<TextView>(R.id.fahrzeit_textview2)
        textView_shuttleinfo = findViewById<TextView>(R.id.textView_shuttleinfo)
        textView_main_caption = findViewById<TextView>(R.id.textView2)
        transport_textview = findViewById<TextView>(R.id.transport_textview)

        preis = MainActivity.preis
        start = MainActivity.start
        ziel = MainActivity.ziel
        fahrzeit = MainActivity.fahrzeit

        start_textview.text = "Start: $start"
        ziel_textview.text = "Ziel: $ziel"
        preis_textview.text = "Preis: $preis"
        fahrzeit_textview.text = "Fahrzeit: $fahrzeit"
        var transport_str = "-"
        if (MainActivity.rollstuhl == true)
        {
            transport_str = "Rollstuhl"
        }
        if (MainActivity.kinderwagen == true)
        {
            if (transport_str == "-")
                transport_str = "Kinderwagen"
            else
                transport_str += ", Kinderwagen"
        }
        if (MainActivity.fahrrad == true)
        {
            if (transport_str == "-")
                transport_str = "Fahrrad"
            else
                transport_str += ", Fahrrad"
        }
        transport_textview.text= "Transport: $transport_str"

        val csv_file = InputStreamReader(assets.open("gps_data_02.csv"))
        val reader = BufferedReader(csv_file)
        var line : String?
        var recording_time : Long = 0

        while (reader.readLine().also { line = it } != null){
            val row : List<String> = line!!.split(";")
            if(row[0] == "sep=")
                continue
            Data_Longitude.add(row[22])
            Data_Latitude.add(row[21])
            if(row[29] != "Time since start in ms ")
                recording_time = row[29].toLong()
        }

        var index = 1
        val timer = object: CountDownTimer(recording_time/2, 500) {
            override fun onTick(millisUntilFinished: Long) {
                var remaining_time = (millisUntilFinished / 60000).toString()
                if (remaining_time > "1")
                    textView_shuttleinfo.text = "Das Shuttle ist in $remaining_time Minuten bei Ihnen"
                else if (remaining_time == "1")
                    textView_shuttleinfo.text = "Das Shuttle ist in $remaining_time Minute bei Ihnen"
                else
                    textView_shuttleinfo.text = "Das Shuttle ist weniger als 1 Minute bei Ihnen"

                if (index <= Data_Latitude.lastIndex)
                {
                    draw_marker_on_map(index)
                }
                else{
                    Log.d("Index", index.toString())
                    Log.d("Index", Data_Latitude.lastIndex.toString())
                }
                index += 2
            }

            override fun onFinish() {
                textView_shuttleinfo.text = "Das Shuttle befindet sich nun an der Startposition"
                textView_main_caption.text = "Das Shuttle ist da!"
                display_toast()
                open_Activity3()
            }
        }
        timer.start()
    }

    fun open_Activity3(){
        // wait 5 Sec then start Activity3
        val timer = object: CountDownTimer(5000, 5000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                startActivity(intent_page3)
            }
        }
        timer.start()
    }

    fun display_toast()
    {
        val toast = Toast.makeText(this, "Das Shuttle ist an der Startposition", Toast.LENGTH_SHORT)
        toast.show()
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

    fun draw_marker_on_map(index: Int){   //marker type -> start/ziel
        if (map_ready == true){
            val current_location = LatLng(Data_Latitude[index].toDouble(), Data_Longitude[index].toDouble())
            val prev_location = shuttle_marker?.position
            //shuttle_marker!!.remove()
            shuttle_marker?.position = current_location
            //shuttle_marker = googleMap2.addMarker(MarkerOptions().position(current_location).title("Shuttle").icon(marker_icon))
            if(active_timer == false)
            {
                if (current_location.latitude != prev_location!!.latitude) {
                    camera_moved_by_program = true
                    googleMap2.moveCamera(CameraUpdateFactory.newLatLngZoom(current_location, 15f))
                }
            }
        }
    }

}