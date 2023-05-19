package com.example.driveclassifier

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import com.example.driveclassifier.models.TripModel

class ListActivity : AppCompatActivity(), GetDataTask.OnDataFetchedListener {
    private lateinit var listView: ListView
    private lateinit var progressBar: ProgressBar
    private var tripList: List<TripModel>? = null
    var adapter: ListAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        var toolbar = findViewById<Toolbar>(R.id.toolbar) // Add this line
        setSupportActionBar(toolbar) // Add this line

        // Set the navigation icon click listener
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }


        progressBar = findViewById(R.id.progressBar)
        listView = findViewById(R.id.listView)

        fetchData()
    }
    private fun fetchData() {
        showProgressBar()
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val getDataTask = GetDataTask(sharedPreferences, this)
        getDataTask.execute()
    }

    override fun onDataFetched(trips: List<TripModel>) {
        tripList = trips
        hideProgressBar()
        displayData()
    }

    override fun onFetchError(error: String) {
        // Handle the fetch error here
        // You can display an error message or perform any other error handling
        hideProgressBar()
    }

    private fun displayData() {
        if (tripList != null) {
            adapter = ListAdapter(this, tripList!!)
            listView.adapter = adapter
        }
    }

    private fun showProgressBar() {
        progressBar.visibility = ProgressBar.VISIBLE
    }

    private fun hideProgressBar() {
        progressBar.visibility = ProgressBar.GONE
    }
}