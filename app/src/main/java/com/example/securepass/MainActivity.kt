package com.example.securepass

import PressUnlock
import VolumeWeatherUnlock
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.securepass.fragments.BluetoothUnlock
import com.example.securepass.fragments.HoursAndMinutes

class MainActivity : AppCompatActivity() {

    private var currentStep = 0
    private val totalSteps = 5
    private lateinit var stepViews: List<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        stepViews = listOf(
            findViewById(R.id.step1),
            findViewById(R.id.step2),
            findViewById(R.id.step3),
            findViewById(R.id.step4),
            findViewById(R.id.step5)
        )

        loadFragment(VoiceCommand.newInstance(::onStepCompleted))
    }

    private fun onStepCompleted() {
        // Mark current step as completed
        stepViews[currentStep].setBackgroundResource(R.drawable.circle_green)
        currentStep++

        if (currentStep < totalSteps) {
            // Load next fragment
            val nextFragment = when (currentStep) {
                1 -> HoursAndMinutes.newInstance(::onStepCompleted)
                2 -> PressUnlock.newInstance(::onStepCompleted)
                3 -> VolumeWeatherUnlock.newInstance(::onStepCompleted)
                4 -> BluetoothUnlock.newInstance(::onStepCompleted)
                else -> null
            }

            nextFragment?.let {
                loadFragment(it)
            }
        } else {
            // All steps completed
            Toast.makeText(this, "Login complete!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,  // enter
                R.anim.fade_out,        // exit
                R.anim.fade_in,         // popEnter (when going back)
                R.anim.slide_out_right  // popExit
            )
            .replace(R.id.loginFragmentContainer, fragment)
            .commit()
    }
}

