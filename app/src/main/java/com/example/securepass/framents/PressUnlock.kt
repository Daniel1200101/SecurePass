import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.ScaleAnimation
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.securepass.R
import java.util.*

class PressUnlock : Fragment() {

    private lateinit var buttonLeft: ImageButton
    private lateinit var buttonUp: ImageButton
    private lateinit var buttonRight: ImageButton
    private lateinit var buttonDown: ImageButton
    private lateinit var buttonReset: View

    private val shakeSequence = mutableListOf<String>()
    private var requiredPattern: List<String> = listOf()

    private var onStepCompleted: (() -> Unit)? = null

    companion object {
        fun newInstance(onStepCompleted: () -> Unit): PressUnlock {
            val fragment = PressUnlock()
            fragment.onStepCompleted = onStepCompleted
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_press_unlock, container, false)

        // Initialize arrow buttons as ImageButtons
        buttonLeft = view.findViewById(R.id.buttonLeft)
        buttonUp = view.findViewById(R.id.buttonUp)
        buttonRight = view.findViewById(R.id.buttonRight)
        buttonDown = view.findViewById(R.id.buttonDown)
        buttonReset = view.findViewById(R.id.buttonReset)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up button click listeners
        buttonLeft.setOnClickListener { onButtonClicked("L", buttonLeft) }
        buttonUp.setOnClickListener { onButtonClicked("U", buttonUp) }
        buttonRight.setOnClickListener { onButtonClicked("R", buttonRight) }
        buttonDown.setOnClickListener { onButtonClicked("D", buttonDown) }
        buttonReset.setOnClickListener {
            shakeSequence.clear()
            Toast.makeText(requireContext(), "Pattern reset.", Toast.LENGTH_SHORT).show()
        }
        // Generate the required shake pattern based on the current time
        requiredPattern = getCurrentTimeShakePattern()
    }

    private fun onButtonClicked(direction: String, button: ImageButton) {
        shakeSequence.add(direction)
        vibrate()
        animateButtonClick(button)

        if (shakeSequence.size == requiredPattern.size && shakeSequence == requiredPattern) {
            Toast.makeText(requireContext(), "Unlocked!", Toast.LENGTH_SHORT).show()
            onStepCompleted?.invoke()
            shakeSequence.clear()
        }
    }

    private fun vibrate() {
        val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(50)
        }
    }

    private fun animateButtonClick(button: ImageButton) {
        val scaleDown = ScaleAnimation(
            1f, 0.9f, 1f, 0.9f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f
        )
        scaleDown.duration = 100
        scaleDown.fillAfter = true

        val scaleUp = ScaleAnimation(
            0.9f, 1f, 0.9f, 1f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f
        )
        scaleUp.duration = 100

        button.startAnimation(scaleDown)
        scaleDown.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
            override fun onAnimationStart(animation: android.view.animation.Animation?) {}
            override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                button.startAnimation(scaleUp)
            }
            override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
        })
    }

    private fun getCurrentTimeShakePattern(): List<String> {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val pattern = mutableListOf<String>()

        fun appendShakes(count: Int, direction: String) {
            repeat(count) {
                pattern.add(direction)
            }
        }

        val hourStr = hour.toString().padStart(2, '0')
        val minuteStr = minute.toString().padStart(2, '0')

        appendShakes(Character.getNumericValue(hourStr[0]), "L")
        appendShakes(Character.getNumericValue(hourStr[1]), "R")
        appendShakes(Character.getNumericValue(minuteStr[0]), "U")
        appendShakes(Character.getNumericValue(minuteStr[1]), "D")

        return pattern
    }
}
