import RPi.GPIO as GPIO, time, os, bluetooth, threading

GPIO.setmode(GPIO.BCM)

stepDelay = .0001
curX = 0
curY = 0

def advertise():
  server_sock=bluetooth.BluetoothSocket( bluetooth.RFCOMM )

  server_sock.bind(("", bluetooth.PORT_ANY))
  server_sock.listen(1)

  port = server_sock.getsockname()[1]

  print "listening on port %d" % port

  uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ee"

  bluetooth.advertise_service(server_sock, "SampleServer", uuid)#, service_classes=[bluetooth.SERIAL_PORT_CLASS], profiles=[bluetooth.SERIAL_PORT_PROFILE])

  client_sock,address = server_sock.accept()
  print "Accepted from ",address

def communicate(socket):
  buffer = ""
  chunk = recv(65535)
  if chunk == "":
    return
  buffer += chunk

  start = buffer.find('[')
  end = buffer.find(']')

  while start != -1 and end != -1:
    draw(buffer[start+1:end])
    buffer = buffer[end+1:]


def draw(points_string):
  #eg 1.35,234,352,3.2
  print("Drawing: ",points_string)
  points = points_string.split(",")
  for i in xrange(0,len(points_string),2):
    x = int(float(points[i]))
    y = int(float(points[i+1]))
    moveTo(x,y)



def moveTo(x,y):
  global curX
  global curY

def setup():
  GPIO.setup(14, GPIO.OUT) #dir
  GPIO.setup(15, GPIO.OUT) #step probe
  GPIO.setup(23, GPIO.OUT) #dir
  GPIO.setup(24, GPIO.OUT) #step probe

def ccwStep(vert, betweenDelay):
  if vert:
    GPIO.output(14, GPIO.LOW)
  else:
    GPIO.output(23, GPIO.LOW)
  step()
  time.sleep(betweenDelay)

def cwStep():
  GPIO.output(23, GPIO.HIGH)
  step()



def step(vert):
  global stepDelay
  pin = 15 if vert else 24

  GPIO.output(pin, GPIO.HIGH)
  time.sleep(stepDelay)
  GPIO.output(pin, GPIO.LOW)
  time.sleep(stepDelay)

setup()
run()
GPIO.cleanup()