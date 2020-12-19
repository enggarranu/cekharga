package com.ranumedia.cekharga

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.postgresql.util.PSQLException
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.SQLTimeoutException
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    var connection: Connection? = null
    val p = "5432"
    val h = "34.101.83.253"
    val d = "cekharga-db"
    var pass = ""
    var user = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

//    override fun onPause() {
//        super.onPause()
//
//        if (connection?.isClosed == false) {
//            connection?.close()
//        }
//    }

    fun getDBConnection() {
        connection = null
        if (input_username.toString() == null || input_username.toString().length == 0) {
            input_username.setError("input username")
            return
        }
        thread {
            try {
                user = input_username.text.toString()
                pass = input_password.text.toString()

                this.connection = DriverManager.getConnection(
                    "jdbc:postgresql://${h}:${p}/${d}",
                    user.toString(),
                    pass.toString()
                )

                runOnUiThread {
                    Toast.makeText(this, "Connected on getConnection", Toast.LENGTH_LONG).show()
                }
            } catch (e: PSQLException) {
                runOnUiThread {
                    Toast.makeText(this, "Worng Username / Password", Toast.LENGTH_LONG).show()
                }
                Log.e(this::class.toString(), e.message, e)
            } catch (e: SQLException) {
                runOnUiThread {
                    Toast.makeText(this, "Failed to Connecttttttttttttttttt", Toast.LENGTH_LONG)
                        .show()
                }

                Log.e(this::class.toString(), e.message, e)
            } catch (e: SQLTimeoutException) {
                runOnUiThread {
                    Toast.makeText(this, "Connection timeout", Toast.LENGTH_LONG).show()
                }

                Log.e(this::class.toString(), e.message, e)
            }
        }
    }

    fun onClickConnect(v: View) {
        getDBConnection()
        var supermarket_array: Array<String> = emptyArray()
        try {
            Thread.sleep(1000)
            if (connection == null || connection?.isClosed == true) {
                Toast.makeText(this, "Failed To Connect on if get Connection", Toast.LENGTH_SHORT)
                    .show()
                return
            }
            thread {
                val listSupermarketNames = arrayListOf<String>()
                try {
                    println("cccccccccccccccccccccccccccccccccccccccc" + connection)
                    connection!!.createStatement().use { s ->
                        s.executeQuery("SELECT name FROM supermarket order by 1").use {
                            while (it.next()) {
                                val supermarket_name = it.getString("name")
                                listSupermarketNames.add(supermarket_name)
                            }
                        }
                    }

                } catch (e: SQLException) {
                    runOnUiThread {
                        Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
                    }

                    Log.e(this::class.toString(), e.message, e)
                }
                supermarket_array = listSupermarketNames.toTypedArray()
            }
            Thread.sleep(500)
            if (supermarket_array.isNotEmpty()) {
                supermarket_array.forEach { println(it) }
            } else {
                println("supermarket_array empty :")
                supermarket_array.forEach { println(it) }
                Thread.sleep(1000)
                println("sleep 1000ms and reprint array")
                supermarket_array.forEach { println(it) }
            }

            runOnUiThread {
                Toast.makeText(this, "Connected onClickConnect", Toast.LENGTH_LONG).show()
            }

            val intent = Intent(this, InquiryActivity::class.java).apply {
                putExtra("PASSWORD", pass.toString())
                putExtra("USERNAME", user.toString())
                putExtra("PORT", p.toString())
                putExtra("HOST", h.toString())
                putExtra("DATABASE", d.toString())
                putExtra("SUPERMARKET_ARRAY", supermarket_array)
            }
            startActivity(intent)
        } catch (e: SQLException) {
            runOnUiThread {
                Toast.makeText(this, "Failed to Connect on GetConnection", Toast.LENGTH_SHORT)
                    .show()
            }

            Log.e(this::class.toString(), e.message, e)
        } catch (e: SQLTimeoutException) {
            runOnUiThread {
                Toast.makeText(this, "Connection timeout", Toast.LENGTH_SHORT).show()
            }

            Log.e(this::class.toString(), e.message, e)
        }
    }
}
