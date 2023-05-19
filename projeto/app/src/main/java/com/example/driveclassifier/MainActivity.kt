package com.example.driveclassifier

import android.app.ProgressDialog
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.driveclassifier.models.UserModel
import com.google.gson.Gson
import okhttp3.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val LOCATION_PERMISSION_CODE = 1
    private val MIN_TIME_BETWEEN_UPDATES = 1000L //1 seconds
    private val MIN_DISTANCE_CHANGE_FOR_UPDATES = 10f //10 meters
    private lateinit var locationManager: LocationManager
    private var name = ""
    private var helper: DBHelper? = null
    private val url = "https://api-drive-safe.onrender.com/trip/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        helper = DBHelper(applicationContext)

        // Get a reference to the name EditText view
        val nameEditText = findViewById<EditText>(R.id.editTextTextPersonName2)

        // Check if the name is already saved in SharedPreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        name = sharedPreferences.getString("name", "") ?: ""
        Log.d("INFO", "Name: $name")


        var showTrips = findViewById<Button>(R.id.btn_trips)
        showTrips.setOnClickListener {
            val intent = Intent(this@MainActivity, ListActivity::class.java)
            startActivity(intent)
        }


        // If the name is not already saved, show the name EditText and save button
        if (name.isBlank()) {
            hideView()
            findViewById<Button>(R.id.btn_save).setOnClickListener {
                // Get the name from the EditText view and save it to SharedPreferences
                name = nameEditText.text.toString().trim()
                if (name.isNotBlank()) {
                    sharedPreferences.edit().putString("name", name).apply()
                    findViewById<TextView>(R.id.txt_name).text = name
                    // Hide the name EditText and save button and show the other views
                    showView()
                }
            }
        } else {
            // If the name is already saved, show the other views
            nameEditText.setText(name)
            findViewById<TextView>(R.id.txt_name).text = name
            showView()
            findViewById<Button>(R.id.btn_save_data).setOnClickListener{
                var data = readDataSQLite()
                if(!data.isEmpty()){
                    writeToURL(data, this)
                    deleteDataInDB()

                }else{
                    Toast.makeText(this, "No data to save!", Toast.LENGTH_LONG).show()
                }

            }
        }

        // Initialize the location manager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Check if the app has location permission
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request location permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_CODE
            )
        } else {
            getLocation(locationManager)
        }
    }



    private fun getLocation(locationManager: LocationManager) {
        // Request location updates
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_BETWEEN_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                locationListener
            )
        }
    }

    // Set up a location listener to receive location updates
    private val locationListener = object : LocationListener {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onLocationChanged(location: Location) {
            // This method is called when the user's location changes
            updateLocation(location)
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

        override fun onProviderEnabled(provider: String) {}

        override fun onProviderDisabled(provider: String) {}
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateLocation(location: Location) {
        val lat = location.latitude
        var long = location.longitude
        var speed = location.speed * 3.6f // convert speed to km/h
        val txt_lat = findViewById<TextView>(R.id.txt_lat)
        val txt_long = findViewById<TextView>(R.id.txt_long)
        val txt_vlc = findViewById<TextView>(R.id.txt_vlc)
        txt_lat.text = lat.toString()
        txt_long.text = long.toString()
        txt_vlc.text = speed.toInt().toString() + "km/h"
        if(speed != 0f){
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val current = LocalDateTime.now().format(formatter)
            saveDataSQLite(lat, long, speed, current)
        }
    }

    private fun hideView() {
        findViewById<LinearLayout>(R.id.layout_location).visibility = View.INVISIBLE
        findViewById<LinearLayout>(R.id.layout_btn).visibility = View.INVISIBLE
        findViewById<LinearLayout>(R.id.layout_name).visibility = View.VISIBLE

    }

    private fun showView() {

        findViewById<LinearLayout>(R.id.layout_location).visibility = View.VISIBLE
        findViewById<LinearLayout>(R.id.layout_btn).visibility = View.VISIBLE
        findViewById<LinearLayout>(R.id.layout_name).visibility = View.GONE
    }

    private fun writeToURL(data: List<UserModel>, context: Context) {
        val client = OkHttpClient.Builder().build()

        val jsonMediaType = "application/json".toMediaType()
        val requestBody = data.toJson().toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url(url+name)
            .post(requestBody)
            .build()

        // Show loading indicator
        val progressDialog = ProgressDialog(context)
        progressDialog.setMessage("Enviar Dados...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                // Hide loading indicator
                progressDialog.dismiss()

                if (response.isSuccessful) {
                    // Handle successful response
                    val responseBody = response.body?.string()
                    Handler(Looper.getMainLooper()).post {
                        Log.d(TAG, "Data was successfully sent to the server. Response: $responseBody")
                        showToast(context, "Viagem guardada com sucesso!")
                    }
                } else {
                    // Handle unsuccessful response
                    Handler(Looper.getMainLooper()).post {
                        Log.e(TAG, "Failed to send data to the server. Response code: ${response.code}")
                        showToast(context, "Falha ao enviar viagem!")
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                // Hide loading indicator
                progressDialog.dismiss()

                // Handle network failure
                Handler(Looper.getMainLooper()).post {
                    Log.e(TAG, "Failed to send data to the server. Error: ${e.message}")
                    showToast(context, "Falha ao enviar viagem!")
                }
            }
        })
    }

    private fun saveDataSQLite(latitude: Double, longitude: Double, speed: Float, date: String) {
        var db = helper?.readableDatabase
        var cv = ContentValues()
        cv.put("LATITUDE", latitude)
        cv.put("LONGITUDE", longitude)
        cv.put("SPEED", speed)
        cv.put("DATE", date)

        if (db != null) {
            db.insert("location", null, cv)
        }
    }

    private fun readDataSQLite(): List<UserModel> {
        val results = mutableListOf<UserModel>()
        helper?.readableDatabase?.use { db ->
            db.rawQuery("SELECT LONGITUDE, LATITUDE, SPEED, DATE FROM location", null)?.use { rs ->
                while (rs.moveToNext()) {
                    val userModel = UserModel(
                        lat = rs.getDouble(rs.getColumnIndexOrThrow("LATITUDE")),
                        lang = rs.getDouble(rs.getColumnIndexOrThrow("LONGITUDE")),
                        speed = rs.getFloat(rs.getColumnIndexOrThrow("SPEED")),
                        date = rs.getString(rs.getColumnIndexOrThrow("DATE"))
                    )
                    results.add(userModel)
                }
            }
        }
        return results
    }

    private fun deleteDataInDB() {
        var db = helper?.readableDatabase
        db?.execSQL("delete from location");
    }


    private fun List<UserModel>.toJson(): String {
        val gson = Gson()
        return gson.toJson(this)
    }
    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                // You can start retrieving the user's location here
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show()
                getLocation(locationManager)
            } else {
                // Permission denied
                // Handle the case where the user denied location permission
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
