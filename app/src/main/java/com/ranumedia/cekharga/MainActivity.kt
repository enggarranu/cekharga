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

//         Create an ArrayAdapter
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.city_list, android.R.layout.simple_spinner_item
        )

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Apply the adapter to the spinner
        spinner.adapter = adapter
    }

//    override fun onPause() {
//        super.onPause()
//
//        if (connection?.isClosed == false) {
//            connection?.close()
//        }
//    }

    fun getSupermarket(): ArrayList<String> {
        var supermarket_names: ArrayList<String> = ArrayList()
        if (connection == null || connection?.isClosed == true) {
            Toast.makeText(this, "Connection closed Get Supermarket", Toast.LENGTH_LONG)
                .show()
        }
        thread {
            try {
                println("cccccccccccccccccccccccccccccccccccccccc" + connection)
                connection!!.createStatement().use { s ->
                    s.executeQuery("SELECT name FROM supermarket").use {

                        while (it.next()) {
                            val supermarket_name = it.getString("name")
                            supermarket_names.add(supermarket_name)
                        }
                    }
                }
            } catch (e: SQLException) {
                runOnUiThread {
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
                }

                Log.e(this::class.toString(), e.message, e)
            }
        }
        return supermarket_names
    }

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
                    Toast.makeText(this, "Failed to Connecttttttttttttttttt", Toast.LENGTH_LONG).show()
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
        try {
            Thread.sleep(1000)
            if (connection == null || connection?.isClosed == true) {
                Toast.makeText(this, "Failed To Connect on if get Connection", Toast.LENGTH_SHORT).show()
                return
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
                putExtra("SUPERMARKET", spinner.selectedItem.toString().toLowerCase())
            }
            startActivity(intent)


        } catch (e: SQLException) {
            runOnUiThread {
                Toast.makeText(this, "Failed to Connect on GetConnection", Toast.LENGTH_SHORT).show()
            }

            Log.e(this::class.toString(), e.message, e)
        } catch (e: SQLTimeoutException) {
            runOnUiThread {
                Toast.makeText(this, "Connection timeout", Toast.LENGTH_SHORT).show()
            }

            Log.e(this::class.toString(), e.message, e)
        }
    }

//    fun onClickCreate(v: View) {
//        if (connection == null || connection?.isClosed == true) {
//            Toast.makeText(this, "Connection closed", Toast.LENGTH_LONG).show()
//            return
//        }
//
//        thread {
//            try {
//                connection!!.createStatement().use {
//                    it.execute("CREATE TABLE IF NOT EXISTS fruits (id SERIAL, name VARCHAR(30))")
//                    it.execute("INSERT INTO fruits (name) VALUES ('apple'), ('orange'), ('grape')")
//                }
//
//                runOnUiThread {
//                    Toast.makeText(this, "Created", Toast.LENGTH_LONG).show()
//                }
//            } catch (e: SQLException) {
//                runOnUiThread {
//                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
//                }
//
//                Log.e(this::class.toString(), e.message, e)
//            } catch (e: SQLTimeoutException) {
//                runOnUiThread {
//                    Toast.makeText(this, "Timeout", Toast.LENGTH_LONG).show()
//                }
//
//                Log.e(this::class.toString(), e.message, e)
//            }
//        }
//    }

//    fun onClickSelect(v: View) {
//        if (connection == null || connection?.isClosed == true) {
//            Toast.makeText(this, "Connection closed", Toast.LENGTH_LONG).show()
//            return
//        }
//
//        thread {
//            try {
//                connection!!.createStatement().use { s ->
//                    s.executeQuery("SELECT * FROM fruits").use {
//                        var r = ""
//
//                        while (it.next()) {
//                            val id = it.getInt("id")
//                            val name = it.getString("name")
//
//                            r += "${id}: ${name}\n"
//                        }
//
//                        runOnUiThread { result.text = r }
//                    }
//                }
//            } catch (e: SQLException) {
//                runOnUiThread {
//                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
//                }
//
//                Log.e(this::class.toString(), e.message, e)
//            }
//        }
//    }
}
