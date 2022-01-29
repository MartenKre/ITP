package com.example.app_prototype

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.ContactsContract
import android.view.MotionEvent
import kotlin.math.round
import android.view.View
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import android.view.View.OnTouchListener
import android.widget.*

//import com.growingio.android.sdk.track.providers.ConfigurationProvider.core


class MainActivity3 : AppCompatActivity() {

    lateinit var shuttle_slider: SeekBar
    lateinit var textView_remaining_time: TextView
    lateinit var textView_start_location: TextView
    lateinit var textView_ziel_location: TextView
    lateinit var textView_start_time: TextView
    lateinit var textView_ziel_time: TextView
    lateinit var textView_header: TextView
    lateinit var button_change_stop: Button
    lateinit var imgview_wheelchair: ImageView
    lateinit var imgview_bicycle: ImageView
    lateinit var imgview_stroller: ImageView
    var fahrzeit: Int = 0 // fahrzeit in minutes
    var number_stops = 1

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
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("HH:mm")  //yyyy-MM-dd HH:mm:ss.SSS
            currentDateTime = current.format(formatter)
            val currenttime_h_m : List<String> = currentDateTime.split(":")
            var targetDateTime_h = ((currenttime_h_m[0].toInt() + fahrzeit_h_m[0].toInt() + (currenttime_h_m[1].toInt() + fahrzeit_h_m[1].toInt())/60) % 24).toString().padStart(2, '0')
            var targetDateTime_m = ((currenttime_h_m[1].toInt() + fahrzeit_h_m[1].toInt())%60).toString().padStart(2, '0')
            targetDateTime = "$targetDateTime_h:$targetDateTime_m"

        } else {
            //TODO("VERSION.SDK_INT < O")
        }
        var dialog2 = Dialog_Window_2(this)      //initialize dialog window
        var dialog3 = Dialog_Window_3(this)

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
            else
                dialog3.show(supportFragmentManager, "customDialog3")
        }

        textView_ziel_location.text = "${MainActivity.ziel}"
        textView_start_location.text = "${MainActivity.start}"
        textView_start_time.text = currentDateTime
        textView_ziel_time.text = targetDateTime

        update_seekbar()
    }

    fun update_seekbar(){
        val timer = object: CountDownTimer((fahrzeit * 60 * 1000).toLong(), 10) {
            override fun onTick(millisUntilFinished: Long) {
                //adapt textview (remaining time)
                var minutes_until_finished = (millisUntilFinished/60000).toDouble()
                var fahrzeit_min = (round((minutes_until_finished)%60)).toInt().toString().padStart(2, '0')
                var fahrzeit_h = (round(minutes_until_finished) / 60).toInt().toString().padStart(2, '0')
                textView_remaining_time.text = "Verbleibende Fahrzeit: $fahrzeit_h h $fahrzeit_min"

                //adapt seekbar value
                shuttle_slider.progress = (fahrzeit * 60 *1000 - millisUntilFinished).toInt()
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

    override fun onBackPressed() {
        //super.onBackPressed()
        return
    }
}
