#!/usr/bin/env python3
"""
Example client script for Droid Remote Test Server
This demonstrates how to use the API endpoints
"""

import requests
import json
import websocket

# Server configuration
BASE_URL = "http://localhost:8080"

def get_devices():
    """Get list of connected devices"""
    response = requests.get(f"{BASE_URL}/devices")
    print("Connected Devices:")
    print(json.dumps(response.json(), indent=2))
    return response.json()

def get_recent_logs(serial, lines=100):
    """Get recent logcat entries"""
    response = requests.get(f"{BASE_URL}/logcat/{serial}/recent?lines={lines}")
    print(f"\nRecent Logs ({lines} lines):")
    print(response.text[:1000])  # Print first 1000 characters
    
def execute_command(serial, command):
    """Execute shell command on device"""
    response = requests.post(
        f"{BASE_URL}/command/{serial}",
        json={"command": command}
    )
    result = response.json()
    print(f"\nCommand: {command}")
    print(f"Success: {result['success']}")
    print(f"Output: {result['output'][:500]}")  # Print first 500 characters
    
def tap_screen(serial, x, y):
    """Tap screen at coordinates"""
    response = requests.post(
        f"{BASE_URL}/command/{serial}/tap",
        data={"x": x, "y": y}
    )
    result = response.json()
    print(f"\nTapped at ({x}, {y})")
    print(f"Success: {result['success']}")

def send_text(serial, text):
    """Send text input to device"""
    response = requests.post(
        f"{BASE_URL}/command/{serial}/input",
        data={"text": text}
    )
    result = response.json()
    print(f"\nSent text: {text}")
    print(f"Success: {result['success']}")

def listen_logcat_stream(serial):
    """Listen to real-time logcat stream via WebSocket"""
    ws_url = f"ws://localhost:8080/logcat/{serial}"
    
    def on_message(ws, message):
        print(f"LOG: {message}")
    
    def on_error(ws, error):
        print(f"ERROR: {error}")
    
    def on_close(ws, close_status_code, close_msg):
        print("WebSocket closed")
    
    def on_open(ws):
        print(f"Connected to logcat stream for {serial}")
    
    ws = websocket.WebSocketApp(
        ws_url,
        on_message=on_message,
        on_error=on_error,
        on_close=on_close,
        on_open=on_open
    )
    
    # Run for 10 seconds
    import threading
    wst = threading.Thread(target=ws.run_forever)
    wst.daemon = True
    wst.start()
    import time
    time.sleep(10)
    ws.close()

if __name__ == "__main__":
    # Get devices
    devices = get_devices()
    
    if devices:
        serial = devices[0]["serial"]
        print(f"\nUsing device: {serial}")
        
        # Get recent logs
        get_recent_logs(serial, lines=50)
        
        # Execute some commands
        execute_command(serial, "pm list packages | grep chrome")
        execute_command(serial, "dumpsys battery")
        
        # Note: Uncomment these if you want to test interaction
        # tap_screen(serial, 500, 1000)
        # send_text(serial, "Hello")
        
        # Uncomment to listen to real-time logs
        # listen_logcat_stream(serial)
    else:
        print("No devices connected!")
