package com.example.collision_detection_projet

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class RegistrationActivity : AppCompatActivity() {
    private lateinit var firstName: EditText
    private lateinit var lastName: EditText
    private lateinit var age: EditText
    private lateinit var matricule: EditText
    private lateinit var email: EditText
    private lateinit var btn_OK: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        firstName = findViewById(R.id.firstName)
        lastName = findViewById(R.id.lastName)
        age = findViewById(R.id.age)
        matricule = findViewById(R.id.matricule)
        email = findViewById(R.id.email)
        btn_OK = findViewById(R.id.signupbtn)

        btn_OK.setOnClickListener{
            val first_name = firstName.text.toString()
            val last_name = lastName.text.toString()
            val agE = age.text.toString().toInt()
            val emaiL = email.text.toString()
            val matriculE = matricule.text.toString()

            Log.d("clickage", "${first_name}, ${last_name}, ${agE}, ${emaiL}, ${matriculE}")
            val intention = Intent(this@RegistrationActivity, MainActivity::class.java)
            //var extras.putString("FIRST_NAME", first_name)
            intention.putExtra("FIRST_NAME", first_name)
            intention.putExtra("LAST_NAME", last_name)
            intention.putExtra("AGE", agE)
            intention.putExtra("EMAIL", emaiL)
            intention.putExtra("MATRICULE", matriculE)
            startActivity(intention)

        }




    }
}