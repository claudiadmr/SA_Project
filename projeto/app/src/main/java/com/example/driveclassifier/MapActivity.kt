package com.example.driveclassifier

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import com.example.driveclassifier.models.TripModel

class MapActivity : AppCompatActivity(), GetDataTask.OnDataFetchedListener {
    private lateinit var listView: ListView
    private var tripList: List<TripModel>? = null
    var adapter: ListAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        listView = findViewById(R.id.listView)

        fetchData()
    }
    private fun fetchData() {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val getDataTask = GetDataTask(sharedPreferences, this)
        getDataTask.execute()
    }

    override fun onDataFetched(trips: List<TripModel>) {
        tripList = trips
        displayData()
    }

    override fun onFetchError(error: String) {
        // Handle the fetch error here
        // You can display an error message or perform any other error handling
    }

    private fun displayData() {
        if (tripList != null) {
            adapter = ListAdapter(this, tripList!!)
            listView.adapter = adapter
        }
    }
}