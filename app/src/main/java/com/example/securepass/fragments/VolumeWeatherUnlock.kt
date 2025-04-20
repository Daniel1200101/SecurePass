import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.securepass.GpsLocationProvider
import com.example.securepass.R
import com.example.securepass.WeatherResponse
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class VolumeWeatherUnlock : Fragment() {

    private var onStepCompleted: (() -> Unit)? = null
    private lateinit var gpsProvider: GpsLocationProvider

    companion object {
        fun newInstance(onStepCompleted: () -> Unit): VolumeWeatherUnlock {
            val fragment = VolumeWeatherUnlock()
            fragment.onStepCompleted = onStepCompleted
            return fragment
        }

        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_volume_weather_unlock, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val infoText = view.findViewById<TextView>(R.id.weatherInfoText)
        val checkButton = view.findViewById<Button>(R.id.checkVolumeButton)

        gpsProvider = GpsLocationProvider(requireContext(), requireActivity())

        if (!gpsProvider.hasLocationPermission()) {
            gpsProvider.requestLocationPermission(LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        fetchLocationAndWeather(infoText, checkButton)
    }

    private fun fetchLocationAndWeather(infoText: TextView, checkButton: Button) {
        gpsProvider.getLastKnownLocation { location ->
            if (location != null) {
                val lat = location.latitude
                val lon = location.longitude
                Log.d("Location", "Latitude: $lat, Longitude: $lon")
                fetchCurrentTemperature(lat, lon) { temperature ->
                    if (temperature == null) {
                        Toast.makeText(requireContext(), "Failed to get weather data", Toast.LENGTH_SHORT).show()
                        return@fetchCurrentTemperature
                    }

                    infoText.text = "Current temperature: $temperature°C"

                    checkButton.setOnClickListener {
                        val audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
                        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                        val volumePercent = (currentVolume.toDouble() / maxVolume.toDouble()) * 100

                        val unlock = if (temperature < 25) {
                            volumePercent.toInt() == 0
                        } else {
                            volumePercent.toInt() == 100
                        }

                        if (unlock) {
                            Toast.makeText(requireContext(), "Unlocked!", Toast.LENGTH_SHORT).show()
                            onStepCompleted?.invoke()
                        } else {
                            Toast.makeText(requireContext(), "Wrong volume. Try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Failed to get location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchCurrentTemperature(lat: Double, lon: Double, onResult: (Double?) -> Unit) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(WeatherApiService::class.java)
        val apiKey = "df2b24c39fa99a69880968d6f5ab9374" // Replace with your actual API key

        val call = api.getCurrentWeather(lat, lon, apiKey)
        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val temp = response.body()?.main?.temp
                    onResult(temp)
                } else {
                    onResult(null)
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                onResult(null)
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val infoText = view?.findViewById<TextView>(R.id.weatherInfoText)
                val checkButton = view?.findViewById<Button>(R.id.checkVolumeButton)
                if (infoText != null && checkButton != null) {
                    fetchLocationAndWeather(infoText, checkButton)
                }
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
