package com.ranumedia.cekharga

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_inquiry.*
import kotlinx.android.synthetic.main.activity_inquiry.item_id
import kotlinx.android.synthetic.main.activity_update.*
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import kotlin.concurrent.thread


class InquiryActivity : AppCompatActivity() {

    var username: String = ""
    var password: String = ""
    var port: String = ""
    var host: String = ""
    var database: String = ""
    var supermarket: String = ""
    var i_id: String = ""
    var productName: String = ""
    var connection: Connection? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inquiry)

        username = intent.getStringExtra("USERNAME")
        password = intent.getStringExtra("PASSWORD")
        port = intent.getStringExtra("PORT")
        host = intent.getStringExtra("HOST")
        database = intent.getStringExtra("DATABASE")
        supermarket = intent.getStringExtra("SUPERMARKET")
        connection = createDBConnection(username, password, port, host, database)

        val inputBarcode: EditText = findViewById(R.id.item_id)
        inputBarcode.requestFocus()

        inputBarcode.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (event != null && event.keyCode === KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE) {
                select.performClick()
            }
            false
        })
    }

    fun createDBConnection(
        username: String,
        password: String,
        port: String,
        host: String,
        database: String
    ): Connection? {
        thread {
            try {
                connection = DriverManager.getConnection(
                    "jdbc:postgresql://${host}:${port}/${database}",
                    username.toString(),
                    password.toString()
                )
            } catch (e: SQLException) {
                runOnUiThread {
                    Toast.makeText(this, "Failed to Connect", Toast.LENGTH_LONG).show()
                }
            }
        }
        return connection
    }

    fun onClickSelect(v: View) {

        if (item_id.text == null || item_id.text.length == 0) {
            Toast.makeText(this, "Mohon Masukkan ID atau Barcode Product", Toast.LENGTH_LONG).show()
            item_id.setError("mohon masukkan product id")
            return
        }

        if (connection == null || connection?.isClosed == true) {
            Toast.makeText(this, "Connection closed, Please try again", Toast.LENGTH_LONG).show()
            return
        }

        thread {
            try {
                i_id = item_id.text.toString()
//                var queryString : String = "SELECT * FROM fruits where id =" + i_id
                var queryString =
                    "select items.id,  items.name, supermarket.name as supermarket_name,  price.price, price.created_at " +
                            "from items left join price on items.id = price.item_id " +
                            "left join supermarket on price.supermarket_id = supermarket.id " +
                            "where items.id = ${i_id} order by price.created_at desc limit 7;"
                connection!!.createStatement().use { s ->
                    s.executeQuery(queryString).use {
                        var r = ""
                        if (!it.isBeforeFirst()) {
                            runOnUiThread {
                                Toast.makeText(this, "Produk tidak ditemukan!", Toast.LENGTH_LONG)
                                    .show()
                            }
                            val intent = Intent(this, UpdateActivity::class.java).apply {
                                putExtra("PRODUCT_ID", i_id)
                                putExtra("SUPERMARKET", supermarket)
                                putExtra("PASSWORD", password.toString())
                                putExtra("USERNAME", username.toString())
                                putExtra("PORT", port.toString())
                                putExtra("HOST", host.toString())
                                putExtra("DATABASE", database.toString())
                                putExtra("PRODUCT_NAME", "")
                            }
                            runOnUiThread {result2.text = ""}
                            startActivity(intent)
                        } else {
                            while (it.next()) {
                                val id = it.getLong("id")
                                val name = it.getString("name")
                                val supermarket = if (it.getString("supermarket_name") != null) it.getString(
                                    "supermarket_name"
                                ) else "-"
                                val price = it.getLong("price")

                                val formatter: DecimalFormat =
                                    NumberFormat.getInstance(Locale.US) as DecimalFormat
                                formatter.applyPattern("#,###,###,###")
                                val formattedStringPrice: String = formatter.format(price)

                                val timestamp_s = if (it.getTimestamp("created_at") != null) it.getTimestamp(
                                    "created_at"
                                ) else "-"
                                r += "Barcode Product : ${id}" +
                                        "\nNamaProduct : ${name}" +
                                        "\nSupermarket : ${supermarket}" +
                                        "\nHarga : Rp. ${formattedStringPrice}" +
                                        "\nTanggal Input : ${timestamp_s}\n\n"
                                productName = name
                            }
                            runOnUiThread { result2.text = r }
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
    }

    fun onClickBarcode(v: View) {
        item_id.setText("")
        result2.setText("")
        scanBarcode("PRODUCT_MODE")
    }

    private fun scanBarcode(mode: String) {
        try {
            //buat intent untuk memanggil fungsi scan pada aplikasi zxing
            val intent = Intent("com.google.zxing.client.android.SCAN")
            intent.putExtra("SCAN_MODE", mode) // "PRODUCT_MODE for bar codes
            startActivityForResult(intent, 1)
        } catch (e: Exception) {
            val marketUri = Uri.parse("market://details?id=com.google.zxing.client.android")
            val marketIntent = Intent(Intent.ACTION_VIEW, marketUri)
            startActivity(marketIntent)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //Tanggkap hasil dari scan
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                item_id.setText("")
                val contents = data?.getStringExtra("SCAN_RESULT")
                Toast.makeText(baseContext, "Hasil :$contents", Toast.LENGTH_SHORT).show()
                item_id.setText(contents)
                select.performClick()
            }
        }
    }

    fun onClickUpdateProduct(v: View){
        val intent = Intent(this, UpdateActivity::class.java).apply {
            putExtra("PRODUCT_ID", i_id)
            putExtra("PRODUCT_NAME", productName)
            putExtra("SUPERMARKET", supermarket)
            putExtra("PASSWORD", password.toString())
            putExtra("USERNAME", username.toString())
            putExtra("PORT", port.toString())
            putExtra("HOST", host.toString())
            putExtra("DATABASE", database.toString())
        }
        startActivity(intent)
    }


    fun item_idOnClick(view: View) {
        item_id.setText("")
    }
}