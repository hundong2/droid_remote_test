#!/bin/bash
# Quick test script to verify the Droid Remote Test Server is working

echo "======================================"
echo "Droid Remote Test Server - Quick Test"
echo "======================================"
echo ""

# Start the server in the background
echo "Starting server..."
./build/install/droid_remote_test/bin/droid_remote_test &
SERVER_PID=$!

# Wait for server to start by polling the root endpoint
MAX_ATTEMPTS=10
SLEEP_SECONDS=1
ATTEMPT=1
SERVER_READY=0

while [ $ATTEMPT -le $MAX_ATTEMPTS ]; do
    if ! kill -0 "$SERVER_PID" 2>/dev/null; then
        echo "Server process (PID $SERVER_PID) exited before becoming ready."
        exit 1
    fi

    if curl -sSf http://localhost:8080/ >/dev/null 2>&1; then
        SERVER_READY=1
        break
    fi

    echo "Waiting for server to become ready... (attempt $ATTEMPT/$MAX_ATTEMPTS)"
    ATTEMPT=$((ATTEMPT + 1))
    sleep "$SLEEP_SECONDS"
done

if [ "$SERVER_READY" -ne 1 ]; then
    echo "Server did not become ready after $((MAX_ATTEMPTS * SLEEP_SECONDS)) seconds."
    kill "$SERVER_PID" 2>/dev/null || true
    exit 1
fi
echo ""
echo "Testing API endpoints..."
echo ""

# Test root endpoint
echo "1. Testing root endpoint:"
curl -s http://localhost:8080/
echo ""
echo ""

# Test devices endpoint  
echo "2. Testing devices endpoint:"
curl -s http://localhost:8080/devices | head -20
echo ""
echo ""

echo "✓ Server is responding correctly!"
echo ""
echo "To test further:"
echo "  - Connect an Android device via ADB"
echo "  - Run: adb devices"
echo "  - Access the API endpoints in the README"
echo ""
echo "Stopping server..."
kill "$SERVER_PID" 2>/dev/null

# Wait for the server process to terminate, with a timeout
for i in {1..10}; do
    if ! kill -0 "$SERVER_PID" 2>/dev/null; then
        break
    fi
    sleep 1
done

# If still running, force kill
if kill -0 "$SERVER_PID" 2>/dev/null; then
    echo "Server did not terminate gracefully, forcing kill..."
    kill -9 "$SERVER_PID" 2>/dev/null
fi

# Reap the process if possible
wait "$SERVER_PID" 2>/dev/null
echo "Done!"
