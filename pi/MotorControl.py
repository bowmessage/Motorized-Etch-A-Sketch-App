import RPi.GPIO as GPIO, time, os, bluetooth, threading

GPIO.setmode(GPIO.BCM)

stepDelay = .0001
curX = 0
curY = 0

def setup():
  GPIO.setup(14, GPIO.OUT) #dir
  GPIO.setup(15, GPIO.OUT) #step probe
  GPIO.setup(23, GPIO.OUT) #dir
  GPIO.setup(24, GPIO.OUT) #step probe

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

  return client_sock

def communicate(socket):
  buffer = ""
  while True:
    chunk = socket.recv(65535)
    if chunk == "":
      return
    buffer += chunk

    start = buffer.find('[')
    end = buffer.find(']')

    while start != -1 and end != -1:
      draw(buffer[start+1:end])
      buffer = buffer[end+1:]

      start = buffer.find('[')
      end = buffer.find(']')


def draw(points_string):
  #eg 1.35,234,352,3.2
  print("Drawing: ",points_string)
  points = points_string.split(",")
  for i in xrange(0,len(points),2):
    x = int(float(points[i]))
    y = int(float(points[i+1]))
    moveTo(x,y)



def moveTo(x,y):
  global curX
  global curY
  print("moving to:",x,y)

  horiz_thread = None
  vert_thread = None

  slope = float(y)/float(x)

  if x < curX:
    horiz_thread = Thread(target=left, args=(curX - x, .001))
  else:
    horiz_thread = Thread(target=right, args=(x - curX, .001))

  if y < curY:
    vert_thread = Thread(target=up, args(curY - y, .001))
  else:
    vert_thread = Thread(target=down, args(y - curY, .001))

  horiz_thread.start()
  vert_thread.start()

  horiz_thread.join()
  vert_thread.join()

  curX = x
  curY = y




def ccwStep(vert, num, betweenDelay):
  if vert:
    GPIO.output(14, GPIO.LOW)
  else:
    GPIO.output(23, GPIO.LOW)
  for x in range(0,num):
    step(vert)
    time.sleep(betweenDelay)

def cwStep(vert, num, betweenDelay):
  if vert:
    GPIO.output(14, GPIO.HIGH)
  else:
    GPIO.output(23, GPIO.HIGH)
  for x in range(0,num):
    step(vert)
    time.sleep(betweenDelay)


def right(num, betweenDelay):
  cwStep(True,num,betweenDelay)

def left(num, betweenDelay):
  ccwStep(True,num,betweenDelay)

def up(num, betweenDelay):
  ccwStep(False,num,betweenDelay)
def down(num, betweenDelay):
  cwStep(False,num,betweenDelay)


def step(vert):
  global stepDelay
  pin = 15 if vert else 24

  GPIO.output(pin, GPIO.HIGH)
  time.sleep(stepDelay)
  GPIO.output(pin, GPIO.LOW)
  time.sleep(stepDelay)


setup()

communicate(advertise())



s = 0
for z in xrange(700,700,50):
  down(z,s)
  left(z,s)
  up(z,s)
  right(z,s)


GPIO.cleanup()
