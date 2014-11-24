#Test comment!


import RPi.GPIO as GPIO, time, os

GPIO.setmode(GPIO.BCM)

delayTime = .007

def setup():
  GPIO.setup(23, GPIO.OUT) #dir
  GPIO.setup(24, GPIO.OUT) #step probe
  GPIO.setup(25, GPIO.OUT) #always high

def run():
  GPIO.output(25, GPIO.HIGH)
  while True:
    cycle()

def cycle():
  for x in range(0,1000):
    cwStep()

def ccwStep():
  GPIO.output(23, GPIO.LOW)
  step()

def cwStep():
  GPIO.output(23, GPIO.HIGH)
  step()
  
def step():
  GPIO.output(24, GPIO.HIGH)
  time.sleep(delayTime)
  GPIO.output(24, GPIO.LOW)
  time.sleep(delayTime)

setup()
run()
GPIO.cleanup()
