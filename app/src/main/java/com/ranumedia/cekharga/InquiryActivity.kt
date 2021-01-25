package com.ranumedia.cekharga

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_inquiry.*
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import kotlin.concurrent.thread


class InquiryActivity : AppCompatActivity() {

    val db = Firebase.firestore

    var username: String = ""
    var password: String = ""
    var port: String = ""
    var host: String = ""
    var database: String = ""
    var i_id: String = ""
    var productName: String = ""
    var connection: Connection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inquiry)

        val rootRef = FirebaseFirestore.getInstance()
        val subjectsRef = rootRef.collection("supermarket")
        val spinner = findViewById<View>(R.id.spinner) as Spinner
        val subjects: MutableList<String?> = ArrayList()
        val adapter =
            ArrayAdapter(applicationContext, android.R.layout.simple_spinner_item, subjects)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        subjectsRef.get().addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
            if (task.isSuccessful) {
                for (document in task.result!!) {
                    val subject = document.getString("name")
                    subjects.add(subject)
                }
                adapter.notifyDataSetChanged()
            }
        })

        val inputBarcode: EditText = findViewById(R.id.item_id)
        inputBarcode.requestFocus()

        inputBarcode.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (event != null && event.keyCode === KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE) {
                select.performClick()
            }
            false
        })
    }

    fun onClickSelect(v: View) {
        if (item_id.text == null || item_id.text.length == 0) {
            Toast.makeText(this, "Mohon Masukkan ID atau Barcode Product", Toast.LENGTH_LONG).show()
            item_id.setError("mohon masukkan product id")
            return
        }

        val docRef = db.collection("items").document(item_id.text.toString())
        // Source can be CACHE, SERVER, or DEFAULT.
        val source = Source.DEFAULT

        docRef.get(source).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Document found in the offline cache
                val document = task.result
                if (document != null) {
                    var productname = document.data?.get("productname")
                    if (productname != null) {
                        println(productname)
                        result2.text = productname.toString()
                    } else {
                        android.widget.Toast.makeText(
                            this,
                            "Produk tidak ditemukan!",
                            android.widget.Toast.LENGTH_LONG
                        )
                            .show()
                        result2.setText("")
                    }
                }
            } else {
                Log.d(this::class.toString(), "Cached get failed: ", task.exception)
            }
        }
    }

//    fun onClickSelect(v: View) {
//
//        if (item_id.text == null || item_id.text.length == 0) {
//            Toast.makeText(this, "Mohon Masukkan ID atau Barcode Product", Toast.LENGTH_LONG).show()
//            item_id.setError("mohon masukkan product id")
//            return
//        }
//
//        if (connection == null || connection?.isClosed == true) {
//            Toast.makeText(this, "Connection closed, Please try again", Toast.LENGTH_LONG).show()
//            return
//        }
//
//        thread {
//            try {
//                i_id = item_id.text.toString()
////                var queryString : String = "SELECT * FROM fruits where id =" + i_id
//                var queryString =
//                    "select items.id,  items.name, supermarket.name as supermarket_name,  price.price, price.created_at " +
//                            "from items left join price on items.id = price.item_id " +
//                            "left join supermarket on price.supermarket_id = supermarket.id " +
//                            "where items.id = ${i_id} order by price.created_at desc limit 7;"
//                connection!!.createStatement().use { s ->
//                    s.executeQuery(queryString).use {
//                        var r = ""
//                        if (!it.isBeforeFirst()) {
//                            runOnUiThread {
//                                Toast.makeText(this, "Produk tidak ditemukan!", Toast.LENGTH_LONG)
//                                    .show()
//                            }
//                            val intent = Intent(this, UpdateActivity::class.java).apply {
//                                putExtra("PRODUCT_ID", i_id)
//                                putExtra(
//                                    "SUPERMARKET",
//                                    spinner.selectedItem.toString().toLowerCase()
//                                )
//                                putExtra("PASSWORD", password.toString())
//                                putExtra("USERNAME", username.toString())
//                                putExtra("PORT", port.toString())
//                                putExtra("HOST", host.toString())
//                                putExtra("DATABASE", database.toString())
//                                putExtra("PRODUCT_NAME", "")
//                            }
//                            runOnUiThread {result2.text = ""}
//                            startActivity(intent)
//                        } else {
//                            while (it.next()) {
//                                val id = it.getLong("id")
//                                val name = it.getString("name")
//                                val supermarket = if (it.getString("supermarket_name") != null) it.getString(
//                                    "supermarket_name"
//                                ) else "-"
//                                val price = it.getLong("price")
//
//                                val formatter: DecimalFormat =
//                                    NumberFormat.getInstance(Locale.US) as DecimalFormat
//                                formatter.applyPattern("#,###,###,###")
//                                val formattedStringPrice: String = formatter.format(price)
//
//                                val timestamp_s = if (it.getTimestamp("created_at") != null) it.getTimestamp(
//                                    "created_at"
//                                ) else "-"
//                                r += "Barcode Product : ${id}" +
//                                        "\nNamaProduct : ${name}" +
//                                        "\nSupermarket : ${supermarket}" +
//                                        "\nHarga : Rp. ${formattedStringPrice}" +
//                                        "\nTanggal Input : ${timestamp_s}\n\n"
//                                productName = name
//                            }
//                            runOnUiThread { result2.text = r }
//                        }
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

    fun onClickUpdateProduct(v: View) {
        val intent = Intent(this, UpdateActivity::class.java).apply {
            putExtra("PRODUCT_ID", i_id)
            putExtra("PRODUCT_NAME", productName)
            putExtra("SUPERMARKET", spinner.selectedItem.toString().toLowerCase())
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