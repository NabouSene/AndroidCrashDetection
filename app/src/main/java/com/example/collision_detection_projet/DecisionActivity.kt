package com.example.collision_detection_projet

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit


class DecisionActivity : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var detect: TextView
    private lateinit var btn_red: Button
    private lateinit var btn_green: Button
    private lateinit var mqttClient: MqttAndroidClient
    lateinit var intente: Intent
    // TAG
    companion object {
        const val TAG = "AndroidMqttClient"
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_decision)





        progressBar = findViewById(R.id.progress_bar)
        progressText = findViewById(R.id.progress_text)
        detect = findViewById(R.id.detect_text)

        detect.setText("Car crash detected!")

        btn_green = findViewById(R.id.btn_green)
        btn_red = findViewById(R.id.btn_red)

        btn_green.setOnClickListener{
            Toast.makeText(this@DecisionActivity, "You're OK!", Toast.LENGTH_SHORT).show()
            val intention = Intent(this@DecisionActivity, MainActivity::class.java)
            startActivity(intention)
        }

        btn_red.setOnClickListener{
            Toast.makeText(this@DecisionActivity, "Emergency", Toast.LENGTH_SHORT).show()
            connect(applicationContext)


        }


        object : CountDownTimer(30000, 1000) {
            // Callback function, fired on regular interval
            override fun onTick(millisUntilFinished: Long) {
                progressText.setText("" + millisUntilFinished / 1000 +"s")
                progressBar.setProgress(progressBar.max - (millisUntilFinished / 1000).toInt())
            }

            // Callback function, fired
            // when the time is up
            override fun onFinish() {
                progressText.setText("done!")
                //Envoyer les données au serveur

                //se connecter d'abord au broker


                Toast.makeText(applicationContext, "Une alerte a été transférée aux services d'urgence", Toast.LENGTH_SHORT).show()


            }

        }.start()


    }

    private fun connect(context: Context) {
        val serverURI = "tcp://192.168.122.1:1883"
        mqttClient = MqttAndroidClient(context, serverURI, "kotlin-client")
        mqttClient.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.d(TAG, "Receive message: ${message.toString()} from topic: $topic")
            }

            override fun connectionLost(cause: Throwable?) {
                Log.d(TAG, "Connection lost ${cause.toString()}")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {

            }
        })
        val options = MqttConnectOptions()
        try {
            mqttClient.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("alors", "Connection success")
                    Log.d("nana", "${mqttClient}")
                    subscribe("Alerte", 1)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d("alors", "Connection failure")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
            Log.d("Excepteuh", "t'es tombée ma chère!")
        }

    }

    private fun subscribe(topic: String, qos: Int = 1) {

        //on récupère les données de collision
        intente = intent
        val firstName = intente.getStringExtra("FIRST_NAME")
        val lastName = intente.getStringExtra("LAST_NAME")
        val matricule = intente.getStringExtra("MATRICULE")
        val email = intente.getStringExtra("EMAIL")
        val age = intente.getIntExtra("AGE", 5)
        val longitude = intente.getFloatExtra("LONGITUDE", 2F)
        val latitude = intente.getFloatExtra("LATITUDE", 2F)


        val rootObject = JSONObject()
        rootObject.put("nom", lastName)
        rootObject.put("prenom", firstName)
        rootObject.put("age", age)
        rootObject.put("email", email)
        rootObject.put("long", longitude)
        rootObject.put("lat", latitude)
        rootObject.put("Severity", "High")
        rootObject.put("matricule", matricule)

        try {

            mqttClient.subscribe(topic, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Subscribed to $topic")
                    publish("Alerte", rootObject.toString())
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to subscribe $topic")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun publish(topic: String, msg: String, qos: Int = 1, retained: Boolean = false) {

        try {
            val message = MqttMessage()
            message.payload = msg.toByteArray()
            message.qos = qos
            message.isRetained = retained
            mqttClient.publish(topic, message, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "$msg published to $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to publish $msg to $topic")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

}