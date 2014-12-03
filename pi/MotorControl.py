import RPi.GPIO as GPIO, time, os
import bluetooth


server_sock=bluetooth.BluetoothSocket( bluetooth.RFCOMM )

server_sock.bind(("", bluetooth.PORT_ANY))
server_sock.listen(1)

port = server_sock.getsockname()[1]

print "listening on port %d" % port

uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ee"

bluetooth.advertise_service(server_sock, "SampleServer", uuid)#, service_classes=[bluetooth.SERIAL_PORT_CLASS], profiles=[bluetooth.SERIAL_PORT_PROFILE])

client_sock,address = server_sock.accept()
print "Accepted from ",address

#server_sock.send("this is a test.")
#print("We sent. Recv now.")

print("About to recv.")
print(client_sock.recv(1024))


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
