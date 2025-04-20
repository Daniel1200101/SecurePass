package com.example.securepass.framents

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.securepass.R

class BluetoothUnlock : Fragment() {

    private var onStepCompleted: (() -> Unit)? = null
    private lateinit var statusText: TextView
    private val targetDeviceName = "Baseus Bowie M2s" // 🔁 Replace with your headset's actual name

    companion object {
        fun newInstance(onStepCompleted: () -> Unit): BluetoothUnlock {
            val fragment = BluetoothUnlock()
            fragment.onStepCompleted = onStepCompleted
            return fragment
        }
    }

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothDevice.ACTION_ACL_CONNECTED) {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                if (device != null && hasBluetoothPermission()) {
                    try {
                        if (device.name == targetDeviceName) {
                            statusText.text = "Headset connected: ${device.name}"
                            Toast.makeText(requireContext(), "Unlocked!", Toast.LENGTH_SHORT).show()
                            onStepCompleted?.invoke()
                        }
                    } catch (e: SecurityException) {
                        statusText.text = "Permission denied to access device name"
                        Toast.makeText(requireContext(), "Bluetooth permission error", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    statusText.text = "Bluetooth permission not granted or device is null"
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_bluetooth_unlock, container, false)
        statusText = view.findViewById(R.id.statusText)
        return view
    }

    override fun onStart() {
        super.onStart()

        // Register the Bluetooth connected receiver
        val filter = IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED)
        requireContext().registerReceiver(bluetoothReceiver, filter)

        // Check if already connected
        checkIfAlreadyConnected()
    }

    override fun onStop() {
        super.onStop()
        requireContext().unregisterReceiver(bluetoothReceiver)
    }

    private fun checkIfAlreadyConnected() {
        val adapter = BluetoothAdapter.getDefaultAdapter()

        if (adapter == null) {
            statusText.text = "Bluetooth not supported"
            return
        }

        if (!adapter.isEnabled) {
            statusText.text = "Bluetooth is off"
            return
        }

        if (!hasBluetoothPermission()) {
            statusText.text = "Bluetooth permission not granted"
            return
        }

        try {
            val pairedDevices = adapter.bondedDevices
            pairedDevices?.forEach { device ->
                if (device.name == targetDeviceName) {
                    // Check if the device is connected, not just paired
                    if (adapter.getProfileConnectionState(BluetoothAdapter.STATE_CONNECTED) == BluetoothAdapter.STATE_CONNECTED) {
                        statusText.text = "Already connected to: ${device.name}"
                        Toast.makeText(requireContext(), "Unlocked!", Toast.LENGTH_SHORT).show()
                        onStepCompleted?.invoke()
                    } else {
                        statusText.text = "Device is paired but not connected"
                    }
                }
            }
        } catch (e: SecurityException) {
            statusText.text = "Failed to access Bluetooth device info"
        }
    }

    private fun hasBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
