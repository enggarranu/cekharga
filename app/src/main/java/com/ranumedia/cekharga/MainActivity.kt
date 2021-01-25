package com.ranumedia.cekharga

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_inquiry.*
import kotlinx.android.synthetic.main.activity_main.*
import org.postgresql.util.PSQLException
import java.math.BigInteger
import java.security.MessageDigest
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.SQLTimeoutException
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    val db = Firebase.firestore
    var username: String = ""
    var password: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun md5(input:String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
    }

    fun onClickConnect(v: View) {

        username = input_username.text.toString()
        password = input_password.text.toString()

        if (username == null || username.length == 0) {
            Toast.makeText(this, "Username cannot be blank", Toast.LENGTH_LONG).show()
            return
        }

        if (password == null || password.length == 0) {
            Toast.makeText(this, "Password cannot be blank", Toast.LENGTH_LONG).show()
            return
        }

        val docRef = db.collection("users").document(username)
        // Source can be CACHE, SERVER, or DEFAULT.
        val source = Source.DEFAULT

        docRef.get(source).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Document found in the offline cache
                val document = task.result
                if (document != null) {
                    var password_db = document.data?.get("password")
                    if (md5(password) == password_db) {
                        println("berhasil login")
                        Toast.makeText(this, "Login Success!", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, InquiryActivity::class.java).apply {}
                        startActivity(intent)
                    }
                    else {
                        Toast.makeText(this, "Worng Username and Password!", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Log.d(this::class.toString(), "Cached get failed: ", task.exception)
            }
        }
    }
}
