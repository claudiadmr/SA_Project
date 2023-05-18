package com.example.driveclassifier

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
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.driveclassifier.models.UserModel
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {
    private val LOCATION_PERMISSION_CODE = 1
    private val MIN_TIME_BETWEEN_UPDATES = 1000L //1 seconds
    private val MIN_DISTANCE_CHANGE_FOR_UPDATES = 10f //10 meters
    private val databaseURL =
        "https://drivesafe-384814-default-rtdb.europe-west1.firebasedatabase.app/"

    private lateinit var locationManager: LocationManager
    private var name = ""
    private var helper: DBHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        helper = DBHelper(applicationContext)

        // Get a reference to the name EditText view
        val nameEditText = findViewById<EditText>(R.id.editTextTextPersonName2)

        // Check if the name is already saved in SharedPreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        name = sharedPreferences.getString("name", "") ?: ""
        Log.d("INFO", "Name: $name")


        var showTrips = findViewById<Button>(R.id.btn_trips)
        showTrips.setOnClickListener {
            val intent = Intent(this@MainActivity, MapActivity::class.java)
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
                    writeToFirebaseDatabase(data)
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
        txt_vlc.text = speed.toInt().toString()
        if(speed != 0f){
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val current = LocalDateTime.now().format(formatter)
            saveDataSQLite(lat, long, speed, current)
        }
    }

    private fun hideView() {
        findViewById<TextView>(R.id.textView).visibility = View.INVISIBLE
        findViewById<TextView>(R.id.textView2).visibility = View.INVISIBLE
        findViewById<TextView>(R.id.textView3).visibility = View.INVISIBLE
        findViewById<TextView>(R.id.txt_lat).visibility = View.INVISIBLE
        findViewById<TextView>(R.id.txt_long).visibility = View.INVISIBLE
        findViewById<TextView>(R.id.txt_vlc).visibility = View.INVISIBLE
        findViewById<Button>(R.id.btn_save_data).visibility = View.INVISIBLE
        findViewById<TextView>(R.id.txt_name).visibility = View.VISIBLE
    }

    private fun showView() {
        findViewById<TextView>(R.id.textView).visibility = View.VISIBLE
        findViewById<TextView>(R.id.textView2).visibility = View.VISIBLE
        findViewById<TextView>(R.id.textView3).visibility = View.VISIBLE
        findViewById<TextView>(R.id.txt_lat).visibility = View.VISIBLE
        findViewById<TextView>(R.id.txt_long).visibility = View.VISIBLE
        findViewById<TextView>(R.id.txt_vlc).visibility = View.VISIBLE
        findViewById<TextView>(R.id.txt_name).visibility = View.VISIBLE
        findViewById<Button>(R.id.btn_save_data).visibility = View.VISIBLE
        findViewById<Button>(R.id.btn_save).visibility = View.GONE
        findViewById<EditText>(R.id.editTextTextPersonName2).visibility = View.GONE
    }

    /*private fun convertLocation(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addressList = geocoder.getFromLocation(latitude, longitude, 1)
        val address = addressList?.get(0)
        Log.d("TAG", address.toString());
    }*/



    private fun writeToFirebaseDatabase(data: List<UserModel>) {
        val database = FirebaseDatabase.getInstance(databaseURL)
        val myRef = database.getReference(name)
        val tasks = mutableListOf<Task<Void>>()
        for (userModel in data) {
            val key = myRef.push().key ?: continue
            tasks.add(myRef.child(key).setValue(userModel))
        }
        var counts = 0;
        var countf = 0;
        Tasks.whenAll(tasks)
            .addOnSuccessListener {
                Log.d(TAG, "Data was successfully written to Firebase Realtime Database")
                counts++
            }
            .addOnFailureListener {
                Log.e(TAG, "Failed to write data to Firebase Realtime Database", it)
                countf++
            }
        if(counts !=0){
            Toast.makeText(this, "Data saved succefully!", Toast.LENGTH_SHORT).show()
        }
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
