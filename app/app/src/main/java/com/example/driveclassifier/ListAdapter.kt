package com.example.driveclassifier

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.driveclassifier.models.TripModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ListAdapter(private val context: Context, private val arrayList: List<TripModel>) : BaseAdapter(){

    private lateinit var serialNum: TextView
    private lateinit var name: TextView
    private lateinit var contactNum: TextView
    override fun getCount(): Int {
        return arrayList.size
    }
    override fun getItem(position: Int): Any {
        return position
    }
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        convertView = LayoutInflater.from(context).inflate(R.layout.list_item_trip, parent, false)
        val title = convertView.findViewById<TextView>(R.id.title)
        val startDate = convertView.findViewById<TextView>(R.id.startDateDate)
        val startHour = convertView.findViewById<TextView>(R.id.startDateHour)
        val endHour = convertView.findViewById<TextView>(R.id.endDateHour)
        val duration = convertView.findViewById<TextView>(R.id.duration)
        val vlc_a = convertView.findViewById<TextView>(R.id.txt_vlc_a)
        val vlc_m = convertView.findViewById<TextView>(R.id.txt_vlc_m)
        val vlc_n = convertView.findViewById<TextView>(R.id.txt_vlc_n)

        val trip = arrayList[position]

        title.text = "Viagem " + trip.nameTrip
        startDate.text = trip.startDate
        vlc_n.text = "1km-10km: " +trip.normalDriving.toString()+'%'
        vlc_m.text = "11km-20km: " + trip.moderateDriving.toString()+'%'
        vlc_a.text = ">20km: " + trip.aggressiveDriving.toString()+'%'

        val startTime = extractTimeFromDateTime(trip.startDate)
        val endTime = extractTimeFromDateTime(trip.endDate)

        startHour.text = startTime
        endHour.text = endTime
        duration.text = trip.duration.toString()+ "min"

        convertView.setOnClickListener {
            val fragment = MapsFragment()
            val bundle = Bundle()
            bundle.putSerializable("trip", trip)
            fragment.arguments = bundle

            val fragmentManager = (context as AppCompatActivity).supportFragmentManager
            fragmentManager.beginTransaction()
                .replace(R.id.frame_layout, fragment)
                .addToBackStack(null)
                .commit()
        }

        return convertView
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun extractTimeFromDateTime(dateTime: String): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val dateTimeObj = LocalDateTime.parse(dateTime, formatter)
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        return dateTimeObj.format(timeFormatter)
    }
}