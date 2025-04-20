import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.securepass.R
import java.util.*

class VoiceCommand : Fragment(R.layout.fragment_voice_command) {

    private lateinit var speechRecognizer: SpeechRecognizer
    private var onStepCompleted: (() -> Unit)? = null
    private lateinit var listenButton: Button
    private lateinit var progressBar: ProgressBar  // Declare ProgressBar
    private var speechStarted = false
    private var stopHandler: Handler? = null
    companion object {
        private const val RECORD_AUDIO_REQUEST_CODE = 100

        fun newInstance(onStepCompleted: () -> Unit): VoiceCommand {
            val fragment = VoiceCommand()
            fragment.onStepCompleted = onStepCompleted
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_voice_command, container, false)

        listenButton = view.findViewById(R.id.listenButton)
        progressBar = view.findViewById(R.id.voiceProgressBar)  // Initialize ProgressBar
        // Button click checks permission first
        listenButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    RECORD_AUDIO_REQUEST_CODE
                )
            } else {
                startListening()
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                progressBar.visibility = View.VISIBLE
            }
            override fun onBeginningOfSpeech() {
                speechStarted = true

                // Start the 5-second timer AFTER speech begins
                stopHandler = Handler(Looper.getMainLooper())
                stopHandler?.postDelayed({
                    speechRecognizer.stopListening()  // This is safe now
                }, 5000)
            }
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                progressBar.visibility = View.GONE
                listenButton.isEnabled = true
                listenButton.text = "Start Listening"
                stopHandler?.removeCallbacksAndMessages(null)
                speechStarted = false
            }
            override fun onError(error: Int) {
                progressBar.visibility = View.GONE
                listenButton.isEnabled = true
                listenButton.text = "Start Listening"
                stopHandler?.removeCallbacksAndMessages(null)
                speechStarted = false

                Toast.makeText(requireContext(), "Speech error: $error", Toast.LENGTH_SHORT).show()
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spokenText = matches?.firstOrNull()?.lowercase(Locale.getDefault())

                // Update the recognized text on the TextView
                view?.findViewById<TextView>(R.id.recognizedText)?.text = spokenText ?: "No speech recognized"

                // Check for the command "open login"
                if (spokenText?.contains("daniel") == true) {
                    onStepCompleted?.invoke()
                } else {
                    Toast.makeText(requireContext(), "Try again", Toast.LENGTH_SHORT).show()
                }
                listenButton.isEnabled = true
                listenButton.text = "Start Listening"
                progressBar.visibility = View.GONE
            }


            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun startListening() {
        listenButton.isEnabled = false
        listenButton.text = "Listening..."
        speechStarted = false
        stopHandler?.removeCallbacksAndMessages(null)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Say 'daniel' to continue")
        }

        speechRecognizer.startListening(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        speechRecognizer.destroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == RECORD_AUDIO_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startListening()
        } else {
            Toast.makeText(requireContext(), "Microphone permission is required", Toast.LENGTH_SHORT).show()
        }
    }
}
