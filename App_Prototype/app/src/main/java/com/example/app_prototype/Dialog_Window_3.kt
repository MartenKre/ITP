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

class Dialog_Window_3(val mainActivity3: MainActivity3): DialogFragment() {

    lateinit var abbruch_button: Button
    lateinit var ok_button: Button
    lateinit var mapFragment : SupportMapFragment
    lateinit var googleMap: GoogleMap
    lateinit var stopp_1: Button
    var marker_icon: BitmapDescriptor? = null
    var selected_stop = ""
    var stopp_marker: Marker? = null

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
            stopp_marker = googleMap.addMarker(MarkerOptions().position(LatLng(47.71512161982119, 9.067548798840004)).title("Zwischenstopp").icon(marker_icon))
            draw_route()
        })

        abbruch_button = rootView.findViewById<Button>(R.id.button2)
        ok_button = rootView.findViewById<Button>(R.id.button3)
        stopp_1 = rootView.findViewById<Button>(R.id.button7)
        stopp_1.setBackgroundColor(0xA9A9A9)
        ok_button.isEnabled = false

        abbruch_button.setOnClickListener{
            dismiss()
        }

        stopp_1.setOnClickListener{
            if (selected_stop != "Stopp 1")
            {
                selected_stop = "Stopp 1"
                ok_button.isEnabled = true
                stopp_1.setBackgroundColor(0xFF00BFFF.toInt())
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(stopp_marker!!.position, 15f))
            }
            else
            {
                selected_stop = ""
                stopp_1.setBackgroundColor(0xFFA9A9A9.toInt())
                ok_button.isEnabled = false
                var lat = (MainActivity.start_coord.latitude + MainActivity.ziel_coord.latitude) / 2
                var lng = (MainActivity.start_coord.longitude + MainActivity.ziel_coord.longitude) / 2
                val iniliatization_location = LatLng(lat, lng)
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(iniliatization_location, 11f))
            }
        }


        return rootView
    }

    fun draw_route(){
        var url = getDirectionsUrl(MainActivity.start_coord, stopp_marker!!.position)
        var color = Color.RED
        GetDirection(url, color).execute()
        url = getDirectionsUrl(stopp_marker!!.position, MainActivity.ziel_coord)
        color = Color.MAGENTA
        GetDirection(url, color).execute()
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

    inner class GetDirection(val url: String, val color: Int) : AsyncTask<Void, Void, List<List<LatLng>>>(){
        override fun doInBackground(vararg p0: Void?): List<List<LatLng>> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body!!.string()
            val result = ArrayList<List<LatLng>>()
            try{
                val respObj = Gson().fromJson(data, GoogleMapDTO::class.java)
                val path = ArrayList<LatLng>()

                for (i in 0..(respObj.routes[0].legs[0].steps.size-1)){
                    path.addAll(decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))
                }
                result.add(path)
            }
            catch (e: Exception){

                e.printStackTrace()
            }
            return result
        }

        override fun onPostExecute(result: List<List<LatLng>>?) {
            val lineoption = PolylineOptions()
            for (i in result!!.indices){
                lineoption.addAll(result[i])
                lineoption.width(10f)
                lineoption.color(color)
                lineoption.geodesic(true)
            }
            MainActivity.global_polyline = lineoption
            googleMap.addPolyline(lineoption)
        }

    }

    fun decodePolyline(encoded: String): List<LatLng> {

        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val latLng = LatLng((lat.toDouble() / 1E5),(lng.toDouble() / 1E5))
            poly.add(latLng)
        }

        return poly
    }

    fun getDirectionsUrl(origin:LatLng,dest:LatLng) : String{
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&sensor=false&mode=driving&key=AIzaSyADgx8m94egOCMWAhUlDhFG_dwiG9CSre8"
    }
}