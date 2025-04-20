package com.example.securepass.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.securepass.CanvasView
import com.example.securepass.R
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.Calendar
import kotlin.math.abs

class HoursAndMinutes: Fragment() {
    private lateinit var canvasView: CanvasView
    private var onStepCompleted: (() -> Unit)? = null

    companion object {
        fun newInstance(onStepCompleted: () -> Unit): HoursAndMinutes {
            val fragment = HoursAndMinutes()
            fragment.onStepCompleted = onStepCompleted
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_hours_minutes, container, false)

        canvasView = view.findViewById(R.id.canvasView)
        val btnRecognize = view.findViewById<Button>(R.id.btnRecognize)
        val btnClear = view.findViewById<Button>(R.id.btnClear)

        btnRecognize.setOnClickListener { recognizeText() }
        btnClear.setOnClickListener { canvasView.clearCanvas() }

        return view
    }

    private fun recognizeText() {
        val bitmap: Bitmap = canvasView.getBitmap()
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { result ->
                val recognizedText = result.text.trim()
                if (recognizedText.isNotEmpty()) {
                    checkUnlock(recognizedText)
                } else {
                    showToast("No text recognized! Try again.")
                }
            }
            .addOnFailureListener {
                showToast("Recognition failed: ${it.message}")
            }
    }

    private fun checkUnlock(recognizedText: String) {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val hourFirstDigit = currentHour / 10
        val hourSecondDigit = currentHour % 10
        val minuteFirstDigit = currentMinute / 10
        val minuteSecondDigit = currentMinute % 10

        val passcodeFirstDigit = abs(hourFirstDigit - hourSecondDigit)
        val passcodeSecondDigit = abs(minuteFirstDigit - minuteSecondDigit)

        val generatedPasscode = "$passcodeFirstDigit$passcodeSecondDigit"
        val recognizedDigits = recognizedText.filter { it.isDigit() }

        if (recognizedDigits == generatedPasscode) {
            showToast("✅ Unlock Successful!")
            onStepCompleted?.invoke()  // ✅ Move to next step
        } else {
            showToast("❌ Wrong code!")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}

