# Droid Remote Test - API Examples

## Quick Start

### 1. Start the Server
```bash
./gradlew run
```

The server will start on `http://localhost:8080`

### 2. List Connected Devices
```bash
curl http://localhost:8080/devices
```

**Response:**
```json
[
  {
    "serial": "emulator-5554",
    "state": "DEVICE",
    "model": "sdk_gphone64_arm64",
    "product": "sdk_gphone64_arm64",
    "device": "emu64a"
  }
]
```

### 3. Real-time Logcat Streaming (WebSocket)

**JavaScript Example:**
```javascript
const ws = new WebSocket('ws://localhost:8080/logcat/emulator-5554');

ws.onmessage = (event) => {
  console.log('Log:', event.data);
};

ws.onopen = () => {
  console.log('Connected to logcat stream');
};

ws.onerror = (error) => {
  console.error('WebSocket error:', error);
};
```

**Python Example:**
```python
import websocket

def on_message(ws, message):
    print(f"Log: {message}")

def on_error(ws, error):
    print(f"Error: {error}")

def on_open(ws):
    print("Connected to logcat stream")

ws = websocket.WebSocketApp(
    "ws://localhost:8080/logcat/emulator-5554",
    on_message=on_message,
    on_error=on_error,
    on_open=on_open
)

ws.run_forever()
```

### 4. Get Recent Logs (HTTP)
```bash
# Get last 100 lines
curl http://localhost:8080/logcat/emulator-5554/recent?lines=100
```

### 5. Execute Shell Command
```bash
curl -X POST http://localhost:8080/command/emulator-5554 \
  -H "Content-Type: application/json" \
  -d '{"command": "pm list packages"}'
```

**Response:**
```json
{
  "success": true,
  "output": "package:com.android.chrome\npackage:com.android.settings\n...",
  "error": null
}
```

### 6. Send Text Input
```bash
curl -X POST http://localhost:8080/command/emulator-5554/input \
  -d "text=Hello World"
```

### 7. Tap Screen
```bash
curl -X POST http://localhost:8080/command/emulator-5554/tap \
  -d "x=500&y=1000"
```

### 8. Swipe Gesture
```bash
curl -X POST http://localhost:8080/command/emulator-5554/swipe \
  -d "x1=500&y1=1000&x2=500&y2=500&duration=300"
```

### 9. Send Key Event
```bash
# Send BACK button (keycode 4)
curl -X POST http://localhost:8080/command/emulator-5554/keyevent \
  -d "keycode=4"
```

**Common Keycodes:**
- 3: HOME
- 4: BACK
- 26: POWER
- 82: MENU
- 24: VOLUME_UP
- 25: VOLUME_DOWN

### 10. Install APK
```bash
curl -X POST http://localhost:8080/command/emulator-5554/install \
  -d "apk_path=/path/to/app.apk"
```

### 11. Uninstall App
```bash
curl -X POST http://localhost:8080/command/emulator-5554/uninstall \
  -d "package=com.example.app"
```

### 12. Get Device Properties
```bash
curl http://localhost:8080/devices/emulator-5554/properties
```

**Response (partial):**
```json
{
  "ro.product.model": "sdk_gphone64_arm64",
  "ro.product.manufacturer": "Google",
  "ro.build.version.release": "13",
  "ro.build.version.sdk": "33",
  ...
}
```

### 13. Clear Logcat
```bash
curl -X POST http://localhost:8080/logcat/emulator-5554/clear
```

## Security Notes

- All inputs are validated to prevent command injection
- Device serials must match expected format
- Numeric parameters are validated
- Text inputs are escaped before shell execution
- Package names must follow Java naming conventions
- WebSocket frames are limited to 1MB
- Logcat lines are capped at 10,000 per request
- CORS is restricted to localhost only

## Error Handling

All endpoints return consistent JSON error responses:

```json
{
  "error": "Error message description"
}
```

HTTP status codes are used appropriately:
- 200: Success
- 400: Bad Request (invalid input)
- 500: Internal Server Error (command failed)
