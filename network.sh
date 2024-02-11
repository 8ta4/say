#!/usr/bin/expect -f

# Keep the script running indefinitely
set timeout -1

# Start scutil
spawn scutil

# Subscribe to State:/Network/Global/IPv4 changes
send "n.add State:/Network/Global/IPv4\r"
send "n.watch\r"

expect