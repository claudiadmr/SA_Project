package com.example.driveclassifier

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Text
import java.io.IOException

class ProfileActivity : AppCompatActivity() {
    private var name = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        name = sharedPreferences.getString("name", "") ?: ""

        setContentView(R.layout.activity_profile)
        var toolbar = findViewById<Toolbar>(R.id.toolbar2) // Add this line
        setSupportActionBar(toolbar) // Add this line

        // Set the navigation icon click listener
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        findViewById<TextView>(R.id.txt_name).text = name
        getProfileData{ data ->
            if (data != null) {
                // Process the data
                println(data)

                val aggressiveTrips = data.getInt("aggressive_trips")
                val behavior = data.getString("behavior")
                val cautiousTrips = data.getInt("cautious_trips")
                val moderateTrips = data.getInt("moderate_trips")
                val totalTrips = data.getInt("total_trips")
                val type = findViewById<TextView>(R.id.txt_driver_type)
                val behaviorText = when (behavior) {
                    "AGGRESSIVE" -> "Agressivo"
                    "MODERATE" -> "Moderado"
                    else -> "Cuidadoso"
                }
                type.text = behaviorText

                val nTrips = findViewById<TextView>(R.id.txt_n_trips)
                nTrips.text = totalTrips.toString()
            } else {
                // Handle error or failure
                println("Failed to fetch data.")
            }
        }


        var showTrips = findViewById<Button>(R.id.btn_trips)
        showTrips.setOnClickListener {
            val intent = Intent(this@ProfileActivity, ListActivity::class.java)
            startActivity(intent)
        }
    }

    fun getProfileData( callback: (JSONObject?) -> Unit) {
        val contentLayout = findViewById<RelativeLayout>(R.id.contentLayout)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar2)
        val progressOverlay = findViewById<View>(R.id.progressOverlay2)

        runOnUiThread {
            contentLayout.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
            progressOverlay.visibility = View.VISIBLE
        }

        val url = "https://api-drive-safe.onrender.com/driver/$name"

        val client = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()

                runOnUiThread {
                    progressBar.visibility = View.GONE
                    progressOverlay.visibility = View.GONE
                    contentLayout.visibility = View.VISIBLE
                }

                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()

                if (response.isSuccessful && responseData != null) {
                    val json = JSONObject(responseData)

                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        progressOverlay.visibility = View.GONE
                        contentLayout.visibility = View.VISIBLE
                    }

                    callback(json)
                } else {
                    println("Request failed: ${response.code}")

                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        progressOverlay.visibility = View.GONE
                        contentLayout.visibility = View.VISIBLE
                    }

                    callback(null)
                }
            }
        })
    }


}