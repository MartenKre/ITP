package com.example.app_prototype

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.Exception
import kotlin.math.*

class Dialog_Window_3(val mainActivity3: MainActivity3): DialogFragment() {

    lateinit var abbruch_button: Button
    lateinit var ok_button: Button
    lateinit var mapFragment : SupportMapFragment
    lateinit var googleMap: GoogleMap
    lateinit var stopp_1: Button
    lateinit var fahrzeit_textview: TextView
    lateinit var preis_textview: TextView
    lateinit var new_ziel_coord: LatLng
    var marker_icon: BitmapDescriptor? = null
    var selected_stop = ""
    var stopp_marker: Marker? = null
    var fahrzeit_total_min = 0.toDouble()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var rootView: View = inflater.inflate(R.layout.dialog_window_3, container, false)

        marker_icon = bitmapDescriptorFromVector(mainActivity3, R.drawable.ic_baseline_local_parking_24)

        mapFragment = childFragmentManager.findFragmentById(R.id.map_dialog) as SupportMapFragment
        mapFragment.getMapAsync(OnMapReadyCallback {
            googleMap = it

            var lat = (MainActivity.start_coord.latitude + MainActivity.ziel_coord.latitude) / 2
            var lng = (MainActivity.start_coord.longitude + MainActivity.ziel_coord.longitude) / 2
            val iniliatization_location = LatLng(lat, lng)
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(iniliatization_location, 11f))
            googleMap.addMarker(MarkerOptions().position(MainActivity.start_coord).title("Start"))
            googleMap.addMarker(MarkerOptions().position(MainActivity.ziel_coord).title("Ziel"))
            stopp_marker = googleMap.addMarker(MarkerOptions().position(MainActivity3.Stopp1_coord).title("Zwischenstopp").icon(marker_icon))
            draw_route()
        })

        abbruch_button = rootView.findViewById<Button>(R.id.button2)
        ok_button = rootView.findViewById<Button>(R.id.button3)
        stopp_1 = rootView.findViewById<Button>(R.id.button7)
        preis_textview = rootView.findViewById<TextView>(R.id.textView20)
        fahrzeit_textview = rootView.findViewById<TextView>(R.id.textView21)
        stopp_1.setBackgroundColor(0xA9A9A9)
        ok_button.isEnabled = false

        abbruch_button.setOnClickListener{
            dismiss()
        }

        ok_button.setOnClickListener{
            MainActivity.ziel_coord = new_ziel_coord
            mainActivity3.number_stops = 0
            MainActivity.fahrzeit = fahrzeit_textview.text.toString()
            mainActivity3.new_target_locatiopn_selected()
            dismiss()
        }

        stopp_1.setOnClickListener{
            if (selected_stop != "Stopp 1")
            {
                selected_stop = "Stopp 1"
                new_ziel_coord = MainActivity3.Stopp1_coord
                ok_button.isEnabled = true
                stopp_1.setBackgroundColor(0xFF00BFFF.toInt())
                calculate_preis_and_zeit(stopp_marker!!.position)
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(stopp_marker!!.position, 15f))
            }
            else
            {
                selected_stop = ""
                stopp_1.setBackgroundColor(0xFFA9A9A9.toInt())
                ok_button.isEnabled = false
                preis_textview.text = ""
                fahrzeit_textview.text = ""
                var lat = (MainActivity.start_coord.latitude + MainActivity.ziel_coord.latitude) / 2
                var lng = (MainActivity.start_coord.longitude + MainActivity.ziel_coord.longitude) / 2
                val iniliatization_location = LatLng(lat, lng)
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(iniliatization_location, 11f))
            }
        }


        return rootView
    }

    fun calculate_preis_and_zeit(stopp_coord: LatLng) {

        // check if Start and Ziel have been set
        var distance = haversine_distance(MainActivity.start_coord, stopp_coord)
        var price = MainActivity.preis_dbl - distance * 0.3
        var price_rounded = "%.2f".format(price)
        var fahrzeit_koeff = 0.toDouble()
        if (distance < 5)
            fahrzeit_koeff = 3.0
        else if (distance < 15)
            fahrzeit_koeff = 1.5
        else if (distance < 25)
            fahrzeit_koeff = 1.2
        else
            fahrzeit_koeff = 1.0
        fahrzeit_total_min = distance * fahrzeit_koeff
        var fahrzeit_min = (round((fahrzeit_total_min-(MainActivity3.passed_time/60))%60)).toInt().toString().padStart(2, '0')
        var fahrzeit_h = (round(fahrzeit_total_min-(MainActivity3.passed_time/60)) / 60).toInt().toString().padStart(2, '0')
        preis_textview.text = "$price_rounded €"
        fahrzeit_textview.text = "$fahrzeit_h h $fahrzeit_min"
    }

    fun haversine_distance(mk1: LatLng, mk2: LatLng) : Double {
        var R = 6371 // Radius of the Earth in km
        var rlat1 = mk1.latitude * (Math.PI/180) // Convert degrees to radians
        var rlat2 = mk2.latitude * (Math.PI/180) // Convert degrees to radians
        var difflat = rlat2-rlat1 // Radian difference (latitudes)
        var difflon = (mk2.longitude-mk1.longitude) * (Math.PI/180) // Radian difference (longitudes)

        var d = 2 * R * asin(sqrt(sin(difflat/2) * sin(difflat/2) + cos(rlat1) * cos(rlat2) * sin(difflon/2) * sin(difflon/2)))
        return d
    }

    fun time_updated(){
        if (fahrzeit_textview.text != "")
        {
            var fahrzeit_min = (round((fahrzeit_total_min-(MainActivity3.passed_time/60))%60)).toInt().toString().padStart(2, '0')
            var fahrzeit_h = (round(fahrzeit_total_min-(MainActivity3.passed_time/60)) / 60).toInt().toString().padStart(2, '0')
            fahrzeit_textview.text = "$fahrzeit_h h $fahrzeit_min"
        }
    }

    fun draw_route(){
        googleMap.addPolyline(MainActivity3.polyline_list[0])
        googleMap.addPolyline(MainActivity3.polyline_list[1])
    }

    private fun bitmapDescriptorFromVector(
        context: Context,
        @DrawableRes vectorDrawableResourceId: Int
    ): BitmapDescriptor? {
        val background = ContextCompat.getDrawable(context, R.drawable.ic_baseline_location_on_24)
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
}