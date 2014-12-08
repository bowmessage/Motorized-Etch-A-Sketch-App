import RPi.GPIO as GPIO, time, os, bluetooth, threading

GPIO.setmode(GPIO.BCM)

stepDelay = .0001
betweenSteps = .001
curX = 0
curY = 0

class BluetoothDisconnectException(Exception):
  pass

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
      raise BluetoothDisconnectException("The phone has disconnected.")
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



def moveTo(x,y,override=False):
  global curX
  global curY
  global betweenSteps

  if not override:
    if x < 0:
      x = 0
    if y < 0:
      y = 0


  print("moving to:",x,y)

  horiz_thread = None
  vert_thread = None

  xdiff = float(abs(x-curX))
  ydiff = float(abs(y-curY))

  slope = 9999 #infinity
  if xdiff != 0:
    slope = float(ydiff)/float(xdiff)


  xBetween = betweenSteps
  yBetween = betweenSteps

  if slope < 1 and slope > 0:
    yBetween = betweenSteps * 1/slope + ((xdiff-ydiff) * stepDelay)/ydiff
  elif slope > 1 and xdiff != 0:
    xBetween = betweenSteps * slope + ((ydiff-xdiff) * stepDelay)/xdiff

  print("xBetween",xBetween,"yBetween",yBetween)
  

  if x < curX:
    horiz_thread = threading.Thread(target=left, args=(curX - x, xBetween))
  else:
    horiz_thread = threading.Thread(target=right, args=(x - curX, xBetween))

  if y < curY:
    vert_thread = threading.Thread(target=up, args=(curY - y, yBetween))
  else:
    vert_thread = threading.Thread(target=down, args=(y - curY, yBetween))

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



#moveTo(-100,-100,True)

try:

  while True:

    try:
      communicate(advertise())
    except (bluetooth.BluetoothError, BluetoothDisconnectException):
      moveTo(0,0)

except (KeyboardInterrupt, SystemExit):

  moveTo(0,0)
  GPIO.cleanup()



s = 0
for z in xrange(700,700,50):
  down(z,s)
  left(z,s)
  up(z,s)
  right(z,s)


