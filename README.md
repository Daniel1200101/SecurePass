# 🔐 SecurePass – Multi-Step Unlock System

SecurePass is a unique multi-layered phone unlock system based on real-time context, sensor input, and user interaction. The app currently guides the user through **five unlock steps**, each using a different method to verify identity or awareness but later it should be secretly kept to the user.

Each successful step turns one of **five indicator lights** green. Upon completing all steps, the device or protected feature is **fully unlocked**.

# 5 steps:

### 1️⃣ Voice Command Unlock
- **Fragment**: `VoiceCommandFragment`
- **How it works**: The user must say a predefined keyword (e.g., `"daniel"`).
- **Tech used**: Android Speech Recognition.
- **On success**: Light 1 turns green ✅

---

### 2️⃣ Time-Based Button Pattern Unlock
- **Fragment**: `PressUnlockFragment`
- **How it works**: The user presses directional buttons based on the current time (e.g., 20:21 = 2 right, 0 left, 2 up, 1 down).
- **Tech used**: Button press detection + current time logic.
- **On success**: Light 2 turns green ✅

---

### 3️⃣ Volume & Weather Condition Unlock
- **Fragment**: `VolumeWeatherUnlock`
- **How it works**: 
  - Gets current location using GPS.
  - Fetches temperature using OpenWeatherMap API.
  - **Rule**:
    - If temp < 25°C → set volume to **0%**
    - If temp ≥ 25°C → set volume to **100%**
- **Tech used**: LocationManager, Retrofit API, AudioManager.
- **On success**: Light 3 turns green ✅

---

### 4️⃣ Bluetooth Device Unlock
- **Fragment**: `BluetoothUnlock`
- **How it works**: Unlocks when a specific Bluetooth headset (e.g., `"Baseus Bowie M2s"`) is connected.
- **Tech used**: BluetoothAdapter, BroadcastReceiver.
- **On success**: Light 4 turns green ✅

---

### 5️⃣ Handwritten Time-Derived Code Unlock
- **Fragment**: `HoursAndMinutes`
- **How it works**: 
  - User draws digits on canvas.
  - ML Kit OCR extracts handwritten text.
  - Passcode = difference between hour digits and minute digits (e.g., 14:32 → |1-4||3-2| → passcode: `31`)
- **Tech used**: ML Kit Text Recognition, CanvasView.
- **On success**: Light 5 turns green ✅



## 📦 Dependencies
- **Retrofit2** – Weather API
- **ML Kit** – OCR (Text Recognition)
- **Google Play Services** – Location
- **Bluetooth APIs**
- **SpeechRecognizer**

---

## ⚠️ Permissions Required
- `RECORD_AUDIO`
- `ACCESS_FINE_LOCATION`
- `BLUETOOTH_CONNECT` (Android 12+)
- `INTERNET`


