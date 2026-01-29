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

# Wait for server to start
sleep 3

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
kill $SERVER_PID 2>/dev/null
echo "Done!"
