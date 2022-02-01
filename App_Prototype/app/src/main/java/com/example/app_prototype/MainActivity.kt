package com.example.app_prototype

import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import java.io.IOException
import android.util.Log
import android.view.inputmethod.InputMethodManager
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.Exception
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.content.pm.ActivityInfo
import android.provider.VoicemailContract
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.core.widget.addTextChangedListener
import com.google.android.gms.common.api.Status
import android.widget.*
import java.lang.reflect.Field
import kotlin.math.*
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;


class MainActivity : AppCompatActivity() {

    lateinit var mapFragment : SupportMapFragment
    lateinit var googleMap: GoogleMap
    lateinit var start_marker: MarkerOptions
    lateinit var ziel_marker: MarkerOptions
    lateinit var route: Polyline
    lateinit var current_location_btn: Button
    lateinit var call_shuttle_btn: Button
    lateinit var start_textview: AutocompleteSupportFragment
    lateinit var ziel_textview: AutocompleteSupportFragment
    lateinit var preis_textview: TextView
    lateinit var zeit_textview: TextView
    lateinit var luggage_options: ImageView
    lateinit var intent_page2: Intent
    companion object {
        lateinit var start_coord: LatLng
        lateinit var ziel_coord: LatLng
        var global_polyline = PolylineOptions()
        var preis = "-"
        var preis_dbl = 0.toDouble()
        var fahrzeit = "-"
        var start = "-"
        var ziel = "-"
        var kinderwagen = false
        var rollstuhl = false
        var fahrrad = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //initialize Places client
        Places.initialize(getApplicationContext(),"AIzaSyADgx8m94egOCMWAhUlDhFG_dwiG9CSre8")
        val placesClient = Places.createClient(this)
        val request =
            FindAutocompletePredictionsRequest.builder()
                .setCountries("DE","CH")
                .setTypeFilter(TypeFilter.ADDRESS)
                .build()
        placesClient.findAutocompletePredictions(request)

        mapFragment = supportFragmentManager.findFragmentById(R.id.map_dialog) as SupportMapFragment
        mapFragment.getMapAsync(OnMapReadyCallback {
            googleMap = it

            val iniliatization_location = LatLng(47.69, 9.18)
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(iniliatization_location, 9f))

        })
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        var dialog = Dialog_Window_1(this)      //initialize dialog window
        intent_page2 = Intent(this, MainActivity2::class.java)  //Initialize Intent
        //startActivity(intent_page2)       // uncomment to jump directly to page2

        //Initialize Buttons
        current_location_btn = findViewById<Button>(R.id.current_location_button)
        call_shuttle_btn = findViewById<Button>(R.id.call_shuttle_button)
        preis_textview = findViewById<TextView>(R.id.preis_textView)
        zeit_textview = findViewById<TextView>(R.id.fahrzeit_textview)
        luggage_options = findViewById<ImageView>(R.id.luggage_options)
        // Initialize the AutocompleteSupportFragment
        start_textview = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        start_textview.setHint("Start suchen")
        start_textview.getView()?.setBackgroundColor(Color.argb(80, 220, 220, 220))
        ziel_textview = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment2) as AutocompleteSupportFragment
        ziel_textview.setHint("Ziel suchen")
        ziel_textview.getView()?.setBackgroundColor(Color.argb(80, 220, 220, 220))

        // Specify the types of place data to return.
        start_textview.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
        ziel_textview.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
        start_textview.setOnPlaceSelectedListener(object : PlaceSelectionListener  {
            override fun onPlaceSelected(place: Place) {

                val address : String? =  place.address
                val latlng : LatLng? = place.latLng
                val id : String? = place.id
                val name : String? = place.name


                //Get info about the selected place.
                Log.d("PLACES", "Place: ${place.name}, ${place.id}")
                Log.d("PLACES", "Place: ${place.address}, ${place.latLng}")

                start = place.name.toString()
                start_marker = MarkerOptions().position(latlng!!).title("Start")
                draw_marker_on_map(false, start_marker)
                draw_route()
                calculate_preis_and_zeit()

            }

            override fun onError(status: Status) {
                //Handle the error.
                Log.i(TAG, "An error occurred: $status")
            }
        })

        ziel_textview.setOnPlaceSelectedListener(object : PlaceSelectionListener  {
            override fun onPlaceSelected(place: Place) {

                val address : String? =  place.address
                val latlng : LatLng? = place.latLng
                val id : String? = place.id
                val name : String? = place.name


                //Get info about the selected place.
                Log.d("PLACES", "Place: ${place.name}, ${place.id}")
                Log.d("PLACES", "Place: ${place.address}, ${place.latLng}")

                ziel = place.name.toString()
                ziel_marker = MarkerOptions().position(latlng!!).title("Ziel")
                draw_marker_on_map(true, ziel_marker)
                draw_route()
                calculate_preis_and_zeit()

            }

            override fun onError(status: Status) {
                //Handle the error.
                Log.i(TAG, "An error occurred: $status")
            }
        })

        popup_menu()

        current_location_btn.setOnClickListener{
            start_textview.setText("Aktuelle Position")
            val current_location = LatLng(47.66876, 9.16962)
            start_marker = MarkerOptions().position(current_location).title("Start")
            draw_marker_on_map(false, start_marker)
            start = "Aktuelle Position"
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current_location, 15f))
            draw_route()
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
    }

    fun draw_route(){
        if (this::start_marker.isInitialized and this::ziel_marker.isInitialized) {
            val url = getDirectionsUrl(start_marker.position, ziel_marker.position)
            Log.d("AL", "$url")
            GetDirection(url).execute()
        }
        else
            return
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
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.position, 15f))
    }

    fun calculate_preis_and_zeit() {
        if (this::start_marker.isInitialized and this::ziel_marker.isInitialized) {
            // check if Start and Ziel have been set
            var distance = haversine_distance(start_marker, ziel_marker)
            var price = distance * 0.3
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
            var fahrzeit_min = (round((distance * fahrzeit_koeff)%60)).toInt().toString().padStart(2, '0')
            var fahrzeit_h = (round(distance * fahrzeit_koeff) / 60).toInt().toString().padStart(2, '0')
            preis_textview.text = "Preis: $price_rounded €"
            zeit_textview.text = "Fahrzeit: $fahrzeit_h h $fahrzeit_min"

            //set global variables
            preis_dbl = price
            preis = "$price_rounded €"
            fahrzeit = "$fahrzeit_h h $fahrzeit_min"
        }
        else{
            return
        }

    }

    fun popup_menu(){
        val popupMenu = PopupMenu(applicationContext, luggage_options)
        popupMenu.inflate(R.menu.popuplist_luggage)
        popupMenu.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.stroller -> {
                    Toast.makeText(applicationContext, "Kinderwagen ausgewählt", Toast.LENGTH_SHORT).show()
                    if(kinderwagen == false) {
                        it.icon.setTint(Color.argb(255, 23, 155, 255))
                        kinderwagen = true
                    }
                    else{
                        it.icon.setTint(Color.argb(255, 115, 115, 115))
                        kinderwagen = false
                    }
                    true
                }
                R.id.wheelchair -> {
                    Toast.makeText(applicationContext, "Rollstuhl ausgewählt", Toast.LENGTH_SHORT).show()
                    if(rollstuhl == false) {
                        it.icon.setTint(Color.argb(255, 23, 155, 255))
                        rollstuhl = true
                    }
                    else{
                        it.icon.setTint(Color.argb(255, 115, 115, 115))
                        rollstuhl = false
                    }
                    true
                }
                R.id.bicycle -> {
                    Toast.makeText(applicationContext, "Fahrrad ausgewählt", Toast.LENGTH_SHORT).show()
                    if(fahrrad == false) {
                        it.icon.setTint(Color.argb(255, 23, 155, 255))
                        fahrrad = true
                    }
                    else{
                        it.icon.setTint(Color.argb(255, 115, 115, 115))
                        fahrrad = false
                    }
                    true
                }
                else -> true
            }
        }

        luggage_options.setOnClickListener {
            try {
                val popup:Field = PopupMenu::class.java.getDeclaredField("mPopup")
                popup.isAccessible = true
                val menu_popup:Any? = popup.get(popupMenu)
                menu_popup!!.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java).invoke(menu_popup, true)
            }
            catch(e:Exception){
                e.printStackTrace()
            }
            finally {
                popupMenu.show()
            }

            true
        }
    }


    fun haversine_distance(mk1: MarkerOptions, mk2: MarkerOptions) : Double {
        var R = 6371 // Radius of the Earth in km
        var rlat1 = mk1.position.latitude * (Math.PI/180) // Convert degrees to radians
        var rlat2 = mk2.position.latitude * (Math.PI/180) // Convert degrees to radians
        var difflat = rlat2-rlat1 // Radian difference (latitudes)
        var difflon = (mk2.position.longitude-mk1.position.longitude) * (Math.PI/180) // Radian difference (longitudes)

        var d = 2 * R * asin(sqrt(sin(difflat/2) * sin(difflat/2) + cos(rlat1) * cos(rlat2)* sin(difflon/2)* sin(difflon/2)))
        return d
    }

    inner class GetDirection(val url: String) : AsyncTask<Void, Void, List<List<LatLng>>>(){
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
            global_polyline = lineoption
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


fun Activity.hideSoftKeyboard(){
    (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).apply {
        hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }
}

