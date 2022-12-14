package com.example.collision_detection_projet

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlin.math.abs
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity() {
    private lateinit var textview : TextView
    private lateinit var textview1 : TextView
    private lateinit var accelerometerSensor: Sensor
    private var isAccelerometerSensorAvailable: Boolean = false
    private var itIsNotFirstTime: Boolean = false
    var lastX: Float = 0f
    var lastY: Float = 0f
    var lastZ: Float = 0f
    var shakeTreeshold: Float = 5f
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var textviewLocation: TextView
    private lateinit var textviewLatitude: TextView
    private lateinit var textviewLongitude: TextView
    private lateinit var intente: Intent
    lateinit var extras: Bundle
    var longitudE by Delegates.notNull<Float>()
    var latitudE by Delegates.notNull<Float>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //----------Partie Localisation  -------------------------
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        Log.d("fusedLocationContent", "$fusedLocationProviderClient")
        textviewLocation = findViewById(R.id.textviewLocation)
        textviewLocation.setText("Location")
        textviewLatitude = findViewById(R.id.textviewLatitude)
        textviewLongitude = findViewById(R.id.textviewLongitude)

        getCurrentLocation()

        //----------Partie Acceleromètre-------------------------
        //déclaration d'un sensor manager

        var sm : SensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        var list = sm.getSensorList(Sensor.TYPE_ACCELEROMETER)
        val vibrator : Vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if(sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) !=null){
            accelerometerSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            isAccelerometerSensorAvailable = true
        } else {
            Toast.makeText(this, "Accelerometer sensor is not available", Toast.LENGTH_SHORT).show()
            isAccelerometerSensorAvailable = true
        }

        //on met un event listener (pour vérifier si la position a changé)
        var se = object : SensorEventListener{
            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
            }

            override fun onSensorChanged(sensorEvent: SensorEvent?) {
                textview1 = findViewById(R.id.textview1)
                textview1.setText("Accelerometer")
                var values:FloatArray? = sensorEvent?.values
                var x = values?.get(0)
                var y = values?.get(1)
                var z = values?.get(2)

                textview = findViewById(R.id.textview)
                textview.setText("X=$x\nY=$y\nZ=$z")

                if (itIsNotFirstTime)
                {
                    var xDiff = abs(lastX - x!!)
                    var yDiff = abs(lastY - y!!)
                    var zDiff = abs(lastZ - z!!)
                    //Log.d("Difference", "${xDiff}, ${yDiff}; $zDiff")
                    if ((xDiff > shakeTreeshold && yDiff > shakeTreeshold) ||
                        (xDiff > shakeTreeshold && zDiff > shakeTreeshold) ||
                        (yDiff > shakeTreeshold && zDiff > shakeTreeshold)) {
                        Log.d("MakeShake", "shake detected")


                        /*if (Build.VERSION.SDK_INT >= 26) {
                            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            vibrator.vibrate(500)

                        }*/
                        //On récupère les données d'enregistrement
                        intente = intent
                        //extras = intente.extras!!
                        val firstName = intente.getStringExtra("FIRST_NAME")
                        val lastName = intente.getStringExtra("LAST_NAME")
                        val matricule = intente.getStringExtra("MATRICULE")
                        val email = intente.getStringExtra("EMAIL")
                        val age = intente.getIntExtra("AGE", 5)

                        Log.d("clickmain", "${firstName}, ${lastName}, ${age}, ${email}, ${matricule}, ${longitudE}, ${latitudE}")


                        val intention = Intent(this@MainActivity, DecisionActivity::class.java)
                        intention.putExtra("FIRST_NAME", firstName)
                        intention.putExtra("LAST_NAME", lastName)
                        intention.putExtra("AGE", age)
                        intention.putExtra("EMAIL", email)
                        intention.putExtra("MATRICULE", matricule)
                        intention.putExtra("LONGITUDE", longitudE)
                        intention.putExtra("LATITUDE", latitudE)
                        startActivity(intention)

                    }
                }

                if (x != null) {
                    lastX = x
                }
                if (y != null) {
                    lastY = y
                }
                if (z != null) {
                    lastZ = z
                }
                itIsNotFirstTime = true

            }
        }
        sm.registerListener(se, list.get(0), SensorManager.SENSOR_DELAY_NORMAL)


    }


    //Cette fonction permets de vérifier si l'utilisateur a déjà donné les permissions ou non

    private fun getCurrentLocation() {
        if (checkPermissions()){
            if (isLocationEnabled())
            {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions()
                    return
                }

                fusedLocationProviderClient.lastLocation.addOnCompleteListener(this){ task->
                    val location: Location?=task.result
                    if(location==null){
                        Toast.makeText(this, "Location not received", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        Toast.makeText(this, "Location success", Toast.LENGTH_SHORT).show()
                        textviewLatitude.text = "Longitude: "+location.longitude
                        textviewLongitude.text = "Latitude: "+location.latitude

                        longitudE = location.longitude.toFloat()
                        latitudE = location.latitude.toFloat()

                    }
                    Log.d("localisation", "$location")
                }

            } else {
                //on ouvre les paramètres
                Toast.makeText(this, "Turn on location", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }


        }
        else {
            //demander les permisssions

            requestPermissions()
        }

    }
    private fun isLocationEnabled():Boolean
    {
        val locationManager: LocationManager =getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }


    private fun requestPermissions(){
        ActivityCompat.requestPermissions(
            this, arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_REQUEST_ACCESS_LOCATION
        )
    }
    companion object{
        private const val PERMISSION_REQUEST_ACCESS_LOCATION=100
    }

    private fun checkPermissions():Boolean
    {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
        {
            return true
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_ACCESS_LOCATION)
        {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(applicationContext, "Granted", Toast.LENGTH_SHORT).show()
                getCurrentLocation()
            }
            else{
                Toast.makeText(applicationContext, "Denied", Toast.LENGTH_SHORT).show()
            }

        }
    }

}

