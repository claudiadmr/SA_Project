package com.example.driveclassifier
import android.content.SharedPreferences
import android.os.AsyncTask
import com.example.driveclassifier.models.LocationModel
import com.example.driveclassifier.models.TripModel
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class GetDataTask(private val sharedPreferences: SharedPreferences, private val listener: OnDataFetchedListener) : AsyncTask<Void, Void, List<TripModel>>() {

    interface OnDataFetchedListener {
        fun onDataFetched(trips: List<TripModel>)
        fun onFetchError(error: String)
    }

    override fun doInBackground(vararg params: Void): List<TripModel>? {
        val name = sharedPreferences.getString("name", "") ?: ""

        val url = URL("https://api-drive-safe.onrender.com/trip/$name")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        try {
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val bufferedReader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?

                while (bufferedReader.readLine().also { line = it } != null) {
                    response.append(line)
                }

                bufferedReader.close()
                return parseJson(response.toString())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            listener.onFetchError(e.message ?: "Unknown error occurred.")
        } finally {
            connection.disconnect()
        }

        return null
    }

    override fun onPostExecute(result: List<TripModel>?) {
        if (result != null) {
            listener.onDataFetched(result)
        } else {
            // Handle the case where the request failed
            listener.onFetchError("Failed to fetch data.")
        }
    }

    private fun parseJson(json: String): List<TripModel> {
        val tripList = mutableListOf<TripModel>()

        try {
            val jsonArray = JSONArray(json)

            for (i in jsonArray.length() - 1 downTo 0) {
                val tripObject = jsonArray.getJSONObject(i)
                val duration = tripObject.getInt("duration")
                val endDate = tripObject.getString("end_date")
                val nameTrip = tripObject.getString("nametrip")
                val startDate = tripObject.getString("start_date")
                val aggressiveDriving = tripObject.getDouble("aggressive_driving")
                val moderateDriving = tripObject.getDouble("moderate_driving")
                val normalDriving = tripObject.getDouble("normal_driving")
                val locationArray = tripObject.getJSONArray("locations")
                val locationList = mutableListOf<LocationModel>()

                for (j in 0 until locationArray.length()) {
                    val locationObject = locationArray.getJSONObject(j)
                    val lang = locationObject.getDouble("lang")
                    val lat = locationObject.getDouble("lat")
                    val roadUse = locationObject.getString("roadUse")
                    val speed = locationObject.getDouble("speed")
                    val speedDiff = locationObject.getDouble("speed_dif")


                    // Handle the case where speedLimit is null
                    val speedLimit = if (!locationObject.isNull("speedLimit")) {
                        locationObject.getDouble("speedLimit")
                    } else {
                        // Assign a default value or handle it accordingly
                        0.0
                    }

                    val location = LocationModel(lang, lat, roadUse, speed, speedLimit, speedDiff)
                    locationList.add(location)
                }

                val trip = TripModel(duration, endDate, locationList, nameTrip, startDate, aggressiveDriving, moderateDriving, normalDriving)
                tripList.add(trip)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return tripList
    }
}

