package com.example.app_prototype

import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import java.io.IOException
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.loader.content.AsyncTaskLoader
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.Exception
import java.lang.reflect.Executable
import android.app.Activity
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.core.widget.addTextChangedListener
import kotlin.math.*


class MainActivity : AppCompatActivity() {

    lateinit var mapFragment : SupportMapFragment
    lateinit var googleMap: GoogleMap
    lateinit var start_marker: MarkerOptions
    lateinit var ziel_marker: MarkerOptions
    lateinit var route: Polyline
    lateinit var current_location_btn: Button
    lateinit var call_shuttle_btn: Button
    lateinit var start_textview: EditText
    lateinit var ziel_textview: EditText
    lateinit var preis_textview: TextView
    lateinit var zeit_textview: TextView
    lateinit var start_suggestion: AutoCompleteTextView
    lateinit var intent_page2: Intent
    companion object {
        lateinit var start_coord: LatLng
        lateinit var ziel_coord: LatLng
        var preis = "-"
        var fahrzeit = "-"
        var start = "-"
        var ziel = "-"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(OnMapReadyCallback {
            googleMap = it

            val iniliatization_location = LatLng(47.69, 9.18)
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(iniliatization_location, 9f))

        })

        var dialog = Dialog_Window_1(this)      //initialize dialog window
        intent_page2 = Intent(this, MainActivity2::class.java)  //Initialize Intent
        //startActivity(intent_page2)       // uncomment to jump directly to page2

        //Initialize Buttons
        current_location_btn = findViewById<Button>(R.id.current_location_button)
        call_shuttle_btn = findViewById<Button>(R.id.call_shuttle_button)
        start_textview = findViewById<EditText>(R.id.start)
        ziel_textview = findViewById<EditText>(R.id.ziel)
        preis_textview = findViewById<TextView>(R.id.preis_textView)
        zeit_textview = findViewById<TextView>(R.id.fahrzeit_textview)
        //start_suggestion = findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView)

        /*
        start_suggestion.addTextChangedListener(object : TextWatcher
        {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                // activates every time a key is pressed
                var addresses = find_addresses("Start")
                if (addresses.size == 2)
                {
                    Log.d("S1", "${addresses[1]}")
                    var suggestions = arrayOf("${addresses[1]}")
                    var adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1, suggestions)
                    start_suggestion.setAdapter(adapter)
                    start_suggestion.showDropDown()
                }
                else if (addresses.size == 3)
                {
                    Log.d("S2", "${addresses[1]}, ${addresses[2]}")
                    var suggestions = arrayOf("${addresses[1]}", "${addresses[2]}")
                    var adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1, suggestions)
                    start_suggestion.setAdapter(adapter)
                    start_suggestion.showDropDown()
                }
                else if (addresses.size == 4)
                {
                    Log.d("S3", "${addresses[1]}, ${addresses[2]}, ${addresses[3]}")
                    var suggestions = arrayOf("${addresses[1]}", "${addresses[2]}", "${addresses[3]}")
                    var adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1, suggestions)
                    start_suggestion.setAdapter(adapter)
                    start_suggestion.showDropDown()
                }
                else
                    Log.d("SS", "no addresses found: ${addresses.size}")
            }

        })*/

        //start_suggestion.setOnFocusChangeListener { view, b ->  if (b) start_suggestion.showDropDown()}

        current_location_btn.setOnClickListener{
            start_textview.setText("Aktuelle Position")
            val current_location = LatLng(47.66876, 9.16962)
            start_marker = MarkerOptions().position(current_location).title("Start")
            draw_marker_on_map(false, start_marker)
            start = "Aktuelle Position"
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current_location, 15f))
            calculate_preis_and_zeit()
        }

        call_shuttle_btn.setOnClickListener {
            //Open Dialog Window
            if(this::ziel_marker.isInitialized and this::ziel_marker.isInitialized)
                dialog.show(supportFragmentManager, "customDialog")
            else{
                val toast = Toast.makeText(this, "Geben Sie zuerst Start und Ziel ein", Toast.LENGTH_SHORT)
                toast.show()
            }
        }

        ziel_textview.setOnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_DONE)
            {
                //Log.d("AL", "test1")
                searchLocation("Ziel")
                //val url = getDirectionsUrl(start_marker.position, ziel_marker.position)
                //GetDirection(url).execute()
                hideSoftKeyboard()
                calculate_preis_and_zeit()
                true
            } else
            {
                false
            }
        }

        start_textview.setOnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_DONE)
            {
                searchLocation("Start")
                hideSoftKeyboard()
                calculate_preis_and_zeit()
                true
            } else
            {
                false
            }
        }

    }

    fun open_Activity2(){
        start_coord = start_marker.position
        ziel_coord = ziel_marker.position
        startActivity(intent_page2)
    }

    fun draw_marker_on_map(marker_type: Boolean, marker: MarkerOptions){   //marker type -> start/ziel
        googleMap.clear()
        if (marker_type == false){
            if(this::ziel_marker.isInitialized)
                googleMap.addMarker(ziel_marker)
        }
        else{
            if(this::start_marker.isInitialized)
                googleMap.addMarker(start_marker)
        }
        googleMap.addMarker(marker)
    }

    fun calculate_preis_and_zeit() {
        if (this::start_marker.isInitialized and this::ziel_marker.isInitialized) {
            // check if Start and Ziel have been set
            var distance = haversine_distance(start_marker, ziel_marker)
            var price = distance * 0.3
            var price_rounded = "%.2f".format(price)
            var fahrzeit_koeff = 0
            if (distance < 10)
                fahrzeit_koeff = 3
            else
                fahrzeit_koeff = 1
            var fahrzeit_min = (round((distance * fahrzeit_koeff)%60)).toInt().toString().padStart(2, '0')
            var fahrzeit_h = (round(distance * fahrzeit_koeff) / 60).toInt().toString().padStart(2, '0')
            preis_textview.text = "Preis: $price_rounded €"
            zeit_textview.text = "Fahrzeit: $fahrzeit_h h $fahrzeit_min"

            //set global variables
            preis = "$price_rounded €"
            fahrzeit = "$fahrzeit_h h $fahrzeit_min"
        }
        else{
            return
        }

    }


    fun haversine_distance(mk1: MarkerOptions, mk2: MarkerOptions) : Double {
        var R = 6371 // Radius of the Earth in km
        var rlat1 = mk1.position.latitude * (Math.PI/180) // Convert degrees to radians
        var rlat2 = mk2.position.latitude * (Math.PI/180) // Convert degrees to radians
        var difflat = rlat2-rlat1 // Radian difference (latitudes)
        var difflon = (mk2.position.longitude-mk1.position.longitude) * (Math.PI/180) // Radian difference (longitudes)

        var d = 2 * R * asin(sqrt(sin(difflat/2) * sin(difflat/2) + cos(rlat1) * cos(rlat2)* sin(difflon/2)* sin(difflon/2)));
        return d;
    }

    inner class GetDirection(val url: String) : AsyncTask<Void, Void, List<List<LatLng>>>(){
        override fun doInBackground(vararg p0: Void?): List<List<LatLng>> {
            Log.d("AL", "test3")
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body().toString()
            val result = ArrayList<List<LatLng>>()
            try{
                val respObj = Gson().fromJson(data, GoogleMapDTO::class.java)
                val path = ArrayList<LatLng>()

                for (i in 0..(respObj.routes[0].legs[0].steps.size-1)){
                    val startLatLng = LatLng(respObj.routes[0].legs[0].steps[i].start_location.lat.toDouble()
                        ,respObj.routes[0].legs[0].steps[i].start_location.lng.toDouble())
                    path.add(startLatLng)
                    val endLatLng = LatLng(respObj.routes[0].legs[0].steps[i].end_location.lat.toDouble()
                        ,respObj.routes[0].legs[0].steps[i].end_location.lng.toDouble())
                    path.add(endLatLng)
                }
                result.add(path)
            }
            catch (e:Exception){
                e.printStackTrace()
            }
            return result
        }

        override fun onPostExecute(result: List<List<LatLng>>?) {
            val lineoption = PolylineOptions()
            for (i in result!!.indices){
                lineoption.addAll(result[i])
                lineoption.width(10f)
                lineoption.color(Color.BLUE)
                lineoption.geodesic(true)
            }
            googleMap.addPolyline(lineoption)
        }

    }

    fun getDirectionsUrl(origin:LatLng,dest:LatLng) : String{
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&sensor=false&mode=driving&key=AIzaSyDAQadcivpP4xwhdLyLD1O6cns6mvmV6Ao"
    }

    fun searchLocation(id_edittext: String) {
        lateinit var textview: EditText
        if (id_edittext == "Ziel") {
            textview = findViewById<EditText>(R.id.ziel)
        }
        else {
            textview = findViewById<EditText>(R.id.start)
        }
        lateinit var location: String
        location = textview.text.toString()
        var addressList: List<Address>? = null

        if (location == null || location == "") {
            return
        }
        else{
            val geoCoder = Geocoder(this)
            try {
                addressList = geoCoder.getFromLocationName(location, 1)

            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (addressList != null) {
                // check if no address was found
                if (addressList.size == 0) {
                    val toast = Toast.makeText(this, "Es konnte keine Adresse gefunden werden", Toast.LENGTH_SHORT)
                    toast.show()
                    return
                }
            }
            val address = addressList!![0]
            val latLng = LatLng(address.latitude, address.longitude)
            if (id_edittext == "Ziel") {
                ziel_marker = MarkerOptions().position(latLng).title(id_edittext)
                draw_marker_on_map(true, ziel_marker)
                ziel = address.featureName.toString()
                ziel_textview.setText(address.featureName.toString(), TextView.BufferType.EDITABLE)
            }
            else{
                start_marker = MarkerOptions().position(latLng).title(id_edittext)
                draw_marker_on_map(false, start_marker)
                start = address.featureName.toString()
                start_textview.setText(address.featureName.toString(), TextView.BufferType.EDITABLE)
            }
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }
    }

    /*
    fun find_addresses(id_edittext: String) : MutableList<String> {
        val result = mutableListOf("")
        lateinit var textview: EditText
        if (id_edittext == "Ziel") {
            //todo: hier ziel edittext einbauen
            textview = findViewById<EditText>(R.id.ziel)
        }
        else {
            textview = findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView)
        }
        lateinit var location: String
        location = textview.text.toString()
        var addressList: List<Address>? = null

        if (location == null || location == "") {
            return result
        }
        else{
            val geoCoder = Geocoder(this)
            try {
                addressList = geoCoder.getFromLocationName(location, 1)

            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (addressList != null) {
                // check if no address was found
                if (addressList.size == 0) {
                    return result
                }
            }
            val address1 = addressList!![0]
            result.add(address1.featureName)
            if (addressList.size >= 2){
                val address2 = addressList!![1]
                result.add(address2.featureName)
            }
            if (addressList.size >= 3){
                val address3 = addressList!![2]
                result.add(address3.featureName)
            }
            return result
        }
    }*/
}


fun Activity.hideSoftKeyboard(){
    (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).apply {
        hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }
}

