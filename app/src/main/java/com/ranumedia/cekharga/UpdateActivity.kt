package com.ranumedia.cekharga

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_update.*
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.SQLTimeoutException
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import kotlin.concurrent.thread


class UpdateActivity : AppCompatActivity() {
    val inquiryActivity = InquiryActivity()
    var product_id: String = ""
    var supermarket: String = ""
    var connection: Connection? = null
    var username: String = ""
    var password: String = ""
    var port: String = ""
    var host: String = ""
    var database: String = ""
    var productName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update)

        item_id.setText(product_id)
        supermarket_txt.text = supermarket
        ProductName.setText(productName)

        price.addTextChangedListener(onTextChangedListener());
    }

    private fun onTextChangedListener(): TextWatcher? {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                price.removeTextChangedListener(this)
                try {
                    var originalString = s.toString()
                    val longval: Long
                    if (originalString.contains(",")) {
                        originalString = originalString.replace(",".toRegex(), "")
                    }
                    longval = originalString.toLong()
                    val formatter: DecimalFormat =
                        NumberFormat.getInstance(Locale.US) as DecimalFormat
                    formatter.applyPattern("#,###,###,###")
                    val formattedString: String = formatter.format(longval)

                    //setting text after format to EditText
                    price.setText(formattedString)
                    price.setSelection(price.text.length)
                } catch (nfe: java.lang.NumberFormatException) {
                    nfe.printStackTrace()
                }
                price.addTextChangedListener(this)
            }
        }
    }

    fun createDBConnection(
        username: String,
        password: String,
        port: String,
        host: String,
        database: String
    ): Connection? {
        thread {
            connection = DriverManager.getConnection(
                "jdbc:postgresql://${host}:${port}/${database}",
                username.toString(),
                password.toString()
            )
        }
        return connection
    }

    fun insertUpdatebtn(v: View) {

        var barcodeProduct: String = item_id.text.toString()
        var productName: String = ProductName.text.toString()
        var supermarket_id: Int = 0
        var price_v: Int = 0
        var price_s: String = price.text.toString()
        if (price_s.length == 0 ) {
            price_v = 0
        } else {

            price_v = price_s.replace(",","").toInt()
        }

        if (connection == null || connection?.isClosed == true) {
            Toast.makeText(this, "Connection closed", Toast.LENGTH_LONG).show()
            return
        }

        thread {
            try {
                var queryString =
                    "select id from supermarket where lower(name) = '${supermarket}'"
                connection!!.createStatement().use { s ->
                    s.executeQuery(queryString).use {
                        var r = ""
                        if (!it.isBeforeFirst()) {
                            runOnUiThread {
                                Toast.makeText(
                                    this,
                                    "supermarket tidak ditemukan",
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                            }
                        } else {
                            while (it.next()) {
                                supermarket_id = it.getInt("id")
                            }
                        }
                    }
                }

                connection!!.createStatement().use {
                    it.execute("INSERT INTO items (id, name) values (${barcodeProduct}, '${productName}') on conflict (id) do update set name = EXCLUDED.name")
                    it.execute("Insert into price (supermarket_id, item_id, price) values (${supermarket_id}, ${barcodeProduct}, ${price_v})")
                }

                runOnUiThread {
                    Toast.makeText(this, "Produk berhasil diinsert", Toast.LENGTH_LONG).show()
                }

                item_id.setText("")
                ProductName.setText("")
                price.setText("0")
                finish()

            } catch (e: SQLException) {
                runOnUiThread {
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
                }
                Log.e(this::class.toString(), e.message, e)
            } catch (e: SQLTimeoutException) {
                runOnUiThread {
                    Toast.makeText(this, "Timeout", Toast.LENGTH_LONG).show()
                }

                Log.e(this::class.toString(), e.message, e)
            }
        }
    }
}