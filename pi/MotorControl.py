import RPi.GPIO as GPIO, time, os
import bluetooth


server_sock=bluetooth.BluetoothSocket( bluetooth.RFCOMM )

server_sock.bind(("", bluetooth.PORT_ANY))
server_sock.listen(1)

print ("listening on port ")

#uuid = "11111111-2222-3333-4444-555555555555"

#bluetooth.advertise_service(server_sock, "SampleServer", service_classes=[bluetooth.SERIAL_PORT_CLASS], profiles=[bluetooth.SERIAL_PORT_PROFILE])
target_name = "SAMSUNG-SM-G900A"
target_address = None
nearby_devices = bluetooth.discover_devices()
for bdaddr in nearby_devices:
  if target_name == bluetooth.lookup_name( bdaddr ):
    target_address = bdaddr
    break
if target_address is not None:
  client_sock,address = server_sock.accept()
  print ("Accepted from ",address)
else:
  print ("device 'SAMSUNG-SM-G900A' not found")


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
