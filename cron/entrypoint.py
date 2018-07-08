#!/usr/local/bin/python

import os
import time
import signal
import subprocess
import sys

#Gracefully exit when called.
def signal_handler(signal, frame):
        sys.exit(0)

#Add signal handler for sigint
signal.signal(signal.SIGINT, signal_handler)

#Default delay
defaultdelay = 300
delay = None

#Check environmental variable is set, show error and default
if os.getenv('DELAY') is None:
	print("Environmental variable 'DELAY' not set, using default.")
	delay =  defaultdelay
else:
	#Convert variable to a float
	try:
		delay = float(os.getenv('DELAY'))
	except (ValueError, TypeError) as e:
		#Show error and default
		print("Delay not a number, using default.")
		delay = defaultdelay

#Loop until killed
while True:
	#Run cache and wait until finished
	try:
		subprocess.check_call(['python','/app/cache.py'])
	except subprocess.CalledProcessError as e:
		print("command '{}' return with error (code {}): {}".format(e.cmd, e.returncode, e.output))
	#Sleep for delay
	print("Sleeping for %s seconds" %delay)
	time.sleep(delay)
