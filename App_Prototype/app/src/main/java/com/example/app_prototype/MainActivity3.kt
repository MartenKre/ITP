package com.example.app_prototype

import android.app.Dialog
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.ContactsContract
import android.util.Log
import android.view.MotionEvent
import kotlin.math.round
import android.view.View
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import android.view.View.OnTouchListener
import android.widget.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.Exception

//import com.growingio.android.sdk.track.providers.ConfigurationProvider.core


class MainActivity3 : AppCompatActivity() {

    lateinit var shuttle_slider: SeekBar
    lateinit var textView_remaining_time: TextView
    lateinit var textView_start_location: TextView
    lateinit var textView_ziel_location: TextView
    lateinit var textView_start_time: TextView
    lateinit var textView_ziel_time: TextView
    lateinit var textView_header: TextView
    lateinit var textView_zwischenstopps: TextView
    lateinit var button_change_stop: Button
    lateinit var imgview_wheelchair: ImageView
    lateinit var imgview_bicycle: ImageView
    lateinit var imgview_stroller: ImageView
    lateinit var dialog3: Dialog_Window_3
    lateinit var timer: CountDownTimer
    lateinit var current_time:LocalDateTime
    var fahrzeit: Int = 0 // fahrzeit in minutes
    var number_stops = 1
    var offset_ms = 0   // offset in ms
    var dialog3_loading = false
    companion object {
        var passed_time = 0 // passed time des timers in Sekunden
        var polyline_list = mutableListOf<PolylineOptions>()
        val Stopp1_coord = LatLng(47.71512161982119, 9.067548798840004)
        val Stopp1_name = "Allensbach"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)

        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        //remove whitespaces from fahrzeit string
        var sentence = MainActivity.fahrzeit
        sentence = sentence.replace("\\s".toRegex(), "")
        //split farhzeit string in h and min
        val fahrzeit_h_m : List<String> = sentence.split("h")
        //calculate fahrzeit as int
        fahrzeit = fahrzeit_h_m[0].toInt()*60 + fahrzeit_h_m[1].toInt()

        lateinit var currentDateTime: String
        lateinit var targetDateTime: String
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            current_time  = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("HH:mm")  //yyyy-MM-dd HH:mm:ss.SSS
            currentDateTime = current_time.format(formatter)
            val currenttime_h_m : List<String> = currentDateTime.split(":")
            var targetDateTime_h = ((currenttime_h_m[0].toInt() + fahrzeit_h_m[0].toInt() + (currenttime_h_m[1].toInt() + fahrzeit_h_m[1].toInt())/60) % 24).toString().padStart(2, '0')
            var targetDateTime_m = ((currenttime_h_m[1].toInt() + fahrzeit_h_m[1].toInt())%60).toString().padStart(2, '0')
            targetDateTime = "$targetDateTime_h:$targetDateTime_m"

        } else {
            //TODO("VERSION.SDK_INT < O")
        }
        var dialog2 = Dialog_Window_2(this)      //initialize dialog window
        dialog3 = Dialog_Window_3(this)

        create_route(MainActivity.start_coord, Stopp1_coord, Color.RED)
        create_route(Stopp1_coord, MainActivity.ziel_coord, Color.MAGENTA)

        shuttle_slider = findViewById<SeekBar>(R.id.seekBar)
        shuttle_slider.setMax(0)
        shuttle_slider.setMax(fahrzeit*60*1000)
        shuttle_slider.setOnTouchListener(OnTouchListener { v, event -> true })     // Disable Seekbar Touchevent (without disabling seekbar)
        textView_header = findViewById<TextView>(R.id.textView4)
        textView_remaining_time = findViewById<TextView>(R.id.textView5)
        textView_start_location = findViewById<TextView>(R.id.textView8)
        textView_ziel_location = findViewById<TextView>(R.id.textView9)
        textView_start_time = findViewById<TextView>(R.id.textView10)
        textView_ziel_time = findViewById<TextView>(R.id.textView11)
        textView_zwischenstopps = findViewById<TextView>(R.id.textView15)
        button_change_stop = findViewById<Button>(R.id.button)
        imgview_wheelchair = findViewById<ImageView>(R.id.imageView13)
        imgview_bicycle = findViewById<ImageView>(R.id.imageView14)
        imgview_stroller = findViewById<ImageView>(R.id.imageView15)

        if (MainActivity.rollstuhl)
            imgview_wheelchair.setColorFilter(Color.argb(255, 23, 155, 255))
        if (MainActivity.fahrrad)
            imgview_bicycle.setColorFilter(Color.argb(255, 23, 155, 255))
        if (MainActivity.kinderwagen)
            imgview_stroller.setColorFilter(Color.argb(255, 23, 155, 255))

        button_change_stop.setOnClickListener(){
            if (number_stops == 0)
                dialog2.show(supportFragmentManager, "customDialog2")
            else{
                if(dialog3_loading == false){
                    dialog3_loading = true
                    dialog3.show(supportFragmentManager, "customDialog3")
                }
            }

        }

        textView_ziel_location.text = "${MainActivity.ziel}"
        textView_start_location.text = "${MainActivity.start}"
        textView_start_time.text = currentDateTime
        textView_ziel_time.text = targetDateTime

        update_seekbar()
    }

    fun update_seekbar(){
        timer = object: CountDownTimer((fahrzeit * 60 * 1000).toLong(), 10) {
            override fun onTick(millisUntilFinished: Long) {
                //adapt textview (remaining time)
                var minutes_until_finished = (millisUntilFinished/60000).toDouble()
                var fahrzeit_min = (round((minutes_until_finished)%60)).toInt().toString().padStart(2, '0')
                var fahrzeit_h = (round(minutes_until_finished) / 60).toInt().toString().padStart(2, '0')
                textView_remaining_time.text = "Verbleibende Fahrzeit: $fahrzeit_h h $fahrzeit_min"

                //adapt seekbar value
                shuttle_slider.progress = (fahrzeit * 60 *1000 - millisUntilFinished + offset_ms).toInt()


                //update passed time global var
                var current_time = (((fahrzeit * 60 * 1000).toLong() - millisUntilFinished) / 1000).toInt()
                if (passed_time != current_time) {
                    passed_time = current_time
                    if (dialog3.isVisible) {
                        dialog3.time_updated()
                    }
                }
            }

            override fun onFinish() {
                shuttle_slider.progress = (fahrzeit * 60 *1000)
                button_change_stop.isEnabled = false
                textView_remaining_time.text = "Bitte steigen Sie hier aus"
                textView_header.text = "Das Shuttle hat das Ziel erreicht"
                val toast = Toast.makeText(this@MainActivity3, "Das Shuttle hat das Ziel erreicht", Toast.LENGTH_SHORT)
                toast.show()
            }
        }
        timer.start()
    }

    fun new_target_locatiopn_selected(){
        timer.cancel()
        textView_zwischenstopps.text = "Anzahl Zwischenstopps: $number_stops"
        textView_ziel_time.text = calc_target_time()
        offset_ms = passed_time*1000
        update_seekbar()
        if(number_stops == 0)
        {
            textView_ziel_location.text = Stopp1_name
        }
    }

    fun calc_target_time(): String
    {
        var sentence = MainActivity.fahrzeit
        sentence = sentence.replace("\\s".toRegex(), "")
        //split farhzeit string in h and min
        val fahrzeit_h_m : List<String> = sentence.split("h")
        //calculate fahrzeit as int
        fahrzeit = fahrzeit_h_m[0].toInt()*60 + fahrzeit_h_m[1].toInt()
        shuttle_slider.setMax(fahrzeit*60*1000+ passed_time*1000)
        lateinit var currentDateTime: String
        lateinit var targetDateTime: String
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val formatter = DateTimeFormatter.ofPattern("HH:mm")  //yyyy-MM-dd HH:mm:ss.SSS
            currentDateTime = current_time.format(formatter)
            val currenttime_h_m : List<String> = currentDateTime.split(":")
            var targetDateTime_h = ((currenttime_h_m[0].toInt() + fahrzeit_h_m[0].toInt() + (currenttime_h_m[1].toInt() + fahrzeit_h_m[1].toInt())/60) % 24).toString().padStart(2, '0')
            var targetDateTime_m = ((currenttime_h_m[1].toInt() + fahrzeit_h_m[1].toInt())%60).toString().padStart(2, '0')
            targetDateTime = "$targetDateTime_h:$targetDateTime_m"
            return targetDateTime
        }
        else
            return ""
    }

    override fun onBackPressed() {
        //super.onBackPressed()
        return
    }

    fun create_route(start_coord: LatLng, ziel_coord: LatLng, color: Int){
        var url = getDirectionsUrl(start_coord, ziel_coord)
        GetDirection(url, color).execute()
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
            polyline_list.add(lineoption)
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

    fun getDirectionsUrl(origin: LatLng, dest: LatLng) : String{
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&sensor=false&mode=driving&key=AIzaSyADgx8m94egOCMWAhUlDhFG_dwiG9CSre8"
    }
}
