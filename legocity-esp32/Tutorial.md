# Micropython for ESP32

> [!NOTE]
> Dette er en dump av notater fra Kristian om hvordan du kan bruke micropython på esp32. Lolin sine esp32 som vi har, komer ferdig installert med micropython.


- Kan brukes på visse esp32-modeller
- IDE:
	- uPyCraft https://randomnerdtutorials.com/install-upycraft-ide-mac-os-x-instructions/
	- Thonny
- mpremote https://docs.micropython.org/en/latest/reference/mpremote.html
	- CLI to interact with the device
	- ```
	  pip install --user mpremote --break-system-packages
	  # Run:
	  ~/Library/Python/3.12/bin/mpremote
	  ```
- https://docs.micropython.org/en/latest/esp8266/tutorial/index.html#esp8266-tutorial
	- > After a fresh install and boot the device configures itself as a WiFi access point (AP) that you can connect to. The ESSID is of the form **MicroPython-xxxxxx** where the x’s are replaced with part of the MAC address of your device (so will be the same every time, and most likely different for all ESP8266 chips). The password for the WiFi is **micropythoN** (note the upper-case N). Its IP address will be **192.168.4.1** once you connect to its network.
	- REPL baudrate `115200`
	- REPL HOWTO Mac:
		- `ls /dev/tty.*`
		  logseq.order-list-type:: number
		- Koble i ESP32 med USB-C data cable
		  logseq.order-list-type:: number
		- Trykk "Allow" når mac spør om å koble til accessory
		  logseq.order-list-type:: number
		- `ls /dev/tty.*` . Se hva som er nytt siden sist.
		  logseq.order-list-type:: number
			- F.eks. `/dev/tty.usbmodem1234561`
			  logseq.order-list-type:: number
		- `screen /dev/tty.usbmodem1234561 115200`
		  logseq.order-list-type:: number
		- For å lukke:
		  logseq.order-list-type:: number
			- Detach: <kbd>ctrl</kbd> + <kbd>a</kbd>, <kbd>d</kbd>
			  logseq.order-list-type:: number
				- Resume: `screen -R`
				  logseq.order-list-type:: number
			- Exit/kill: Ctrl + a, Ctrl + \
			  logseq.order-list-type:: number
	- Test at NeoPixel LED fungerer:
	  ```python
	  import machine, neopixel
	  np = neopixel.NeoPixel(machine.Pin(38), 1)
	  # G R B
	  np[0] = (64, 0, 0)
	  np.write()
	  ```
	- Send strøm på en pin:
	  ```python
	  import machine
	  pin = machine.Pin(10, machine.Pin.OUT)
	  pin.on()
	  pin.off()
	  ```
	- Test at knapp fungerer
	  ```python
	  button = machine.Pin(0, machine.Pin.IN)
	  button.value()
	  # Hold nede knappen 0 og kjør:
	  button.value()
	  ```
- > There are two files that are treated specially by the ESP8266 when it starts up: `boot.py` and main.py. The boot.py script is executed first (if it exists) and then once it completes the `main.py` script is executed. You can create these files yourself and populate them with the code that you want to run when the device starts up.
- ## Network
	- ```python
	  import network
	  # For connecting to a router:
	  sta_if = network.WLAN(network.STA_IF)
	  # For hosting a wifi so others connect to me:
	  ap_if = network.WLAN(network.AP_IF)
	  ```
	- ```
	  # Returns False if wifi is disabled
	  sta_if.active()
	  ```
	- Koble på WiFi: 
	  skriv dette i `boot.py`
	  ```python
	  def do_connect():
	      import network
	      sta_if = network.WLAN(network.STA_IF)
	      if not sta_if.isconnected():
	          print('connecting to network...')
	          sta_if.active(True)
	          sta_if.connect('<ssid>', '<key>')
	          while not sta_if.isconnected():
	              pass
	      print('network config:', sta_if.ifconfig())
	  ```
- ## MQTT
	- https://randomnerdtutorials.com/micropython-mqtt-esp32-esp8266/
		- Install `umqttsimple` by placing a new file `umqttsimple.py` onto the ESP32 with [https://raw.githubusercontent.com/RuiSantosdotme/ESP-MicroPython/master/code/MQTT/umqttsimple.py](https://raw.githubusercontent.com/RuiSantosdotme/ESP-MicroPython/master/code/MQTT/umqttsimple.py)
			- ```sh
			  wget https://raw.githubusercontent.com/RuiSantosdotme/ESP-MicroPython/master/code/MQTT/umqttsimple.py
			  
			  # : prefix means remote path
			  ~/Library/Python/3.12/bin/mpremote fs cp umqttsimple.py :
			  # Do `fs ls` to verify
			  ```
			- Alternatively https://github.com/micropython/micropython-lib/tree/master/micropython/umqtt.simple via `mpremote mip install umqtt.robust`
			- Alternatively https://github.com/peterhinch/micropython-mqtt/tree/master/mqtt_as
		- boot.py:
		  ```python
		  # Complete project details at https://RandomNerdTutorials.com
		  
		  import time
		  from umqttsimple import MQTTClient
		  import ubinascii
		  import machine
		  import micropython
		  import network
		  import esp
		  esp.osdebug(None)
		  import gc
		  gc.collect()
		  import neopixel
		  
		  led = neopixel.NeoPixel(machine.Pin(38), 1)
		  led[0] = (0, 128, 0)
		  led.write()
		  
		  ssid = 'legocity'
		  password = '' # ask
		  mqtt_server = '192.168.1.20'
		  mqtt_port = 1883
		  # No auth:
		  mqtt_user = None
		  mqtt_pass = None
		  
		  client_id = ubinascii.hexlify(machine.unique_id())
		  topic_sub = b'notification'
		  topic_pub = b'hello'
		  
		  last_message = 0
		  message_interval = 5
		  counter = 0
		  
		  station = network.WLAN(network.STA_IF)
		  
		  station.active(True)
		  station.connect(ssid, password)
		  
		  led[0] = (0, 0, 128)
		  led.write()
		  while station.isconnected() == False:
		    pass
		  
		  led[0] = (128, 0, 0)
		  led.write()
		  
		  print('Connection successful')
		  print(station.ifconfig())
		  
		  ```
		- main.py:
		  ```python
		  # Complete project details at https://RandomNerdTutorials.com
		  
		  def sub_cb(topic, msg):
		    print((topic, msg))
		    if topic == b'notification' and msg == b'received':
		      print('ESP received hello message')
		  
		  def connect_and_subscribe():
		    global client_id, mqtt_server, topic_sub
		    client = MQTTClient(client_id, mqtt_server, port=mqtt_port, user=mqtt_user, password=mqtt_pass)
		    client.set_callback(sub_cb)
		    client.connect()
		    client.subscribe(topic_sub)
		    print('Connected to %s MQTT broker, subscribed to %s topic' % (mqtt_server, topic_sub))
		    return client
		  
		  def restart_and_reconnect():
		    print('Failed to connect to MQTT broker. Reconnecting...')
		    time.sleep(10)
		    machine.reset()
		  
		  try:
		    client = connect_and_subscribe()
		  except OSError as e:
		    restart_and_reconnect()
		  
		  while True:
		    try:
		      client.check_msg()
		      if (time.time() - last_message) > message_interval:
		        msg = b'Hello #%d' % counter
		        client.publish(topic_pub, msg)
		        last_message = time.time()
		        counter += 1
		    except OSError as e:
		      restart_and_reconnect()
		  ```
- ## OLED display
	- https://docs.micropython.org/en/latest/esp8266/tutorial/ssd1306.html
		- For the tiny https://www.wemos.cc/en/latest/d1_mini_shield/oled_0_49.html
			- Color: Black/White
	- Install the [ssd1306](https://github.com/micropython/micropython-lib/tree/master/micropython/drivers/display/ssd1306) library: `mpremote mip install ssd1306`
	- ```python
	  import machine
	  import ssd1306
	  
	  i2c = machine.SoftI2C(sda=machine.Pin(9), scl=machine.Pin(10))
	  display = ssd1306.SSD1306_I2C(64, 32, i2c)
	  
	  # You can verify by using i2c.scan()
	  ```
	- Write text:
	  ```python
	  # Clear the screen
	  display.fill(0)
	  
	  # text, x, y, color (0 or 1)
	  display.text('Hello!', 0, 0, 1)
	  display.show()
	  ```
	- If the text is upside-down in the case, you can rotate it with `display.rotate(False)`
	- Control the display:
	  ```python
	  display.poweroff()     # power off the display, pixels persist in memory
	  display.poweron()      # power on the display, pixels redrawn
	  display.contrast(0)    # dim
	  display.contrast(255)  # bright
	  display.invert(1)      # display inverted
	  display.invert(0)      # display normal
	  display.rotate(True)   # rotate 180 degrees
	  display.rotate(False)  # rotate 0 degrees
	  display.show()         # write the contents of the FrameBuffer to display memory
	  ```
	- Other stuff:
	  ```python
	  display.fill(0)                         # fill entire screen with colour=0
	  display.pixel(0, 10)                    # get pixel at x=0, y=10
	  display.pixel(0, 10, 1)                 # set pixel at x=0, y=10 to colour=1
	  display.hline(0, 8, 4, 1)               # draw horizontal line x=0, y=8, width=4, colour=1
	  display.vline(0, 8, 4, 1)               # draw vertical line x=0, y=8, height=4, colour=1
	  display.line(0, 0, 127, 63, 1)          # draw a line from 0,0 to 127,63
	  display.rect(10, 10, 107, 43, 1)        # draw a rectangle outline 10,10 to 117,53, colour=1
	  display.fill_rect(10, 10, 107, 43, 1)   # draw a solid rectangle 10,10 to 117,53, colour=1
	  display.text('Hello World', 0, 0, 1)    # draw some text at x=0, y=0, colour=1
	  display.scroll(20, 0)                   # scroll 20 pixels to the right
	  
	  # draw another FrameBuffer on top of the current one at the given coordinates
	  import framebuf
	  fbuf = framebuf.FrameBuffer(bytearray(8 * 8 * 1), 8, 8, framebuf.MONO_VLSB)
	  fbuf.line(0, 0, 7, 7, 1)
	  display.blit(fbuf, 10, 10, 0)           # draw on top at x=10, y=10, key=0
	  display.show()
	  ```
	- Anything from [Framebuffer](http://docs.micropython.org/en/latest/pyboard/library/framebuf.html) can be called on the `display`.
- ## Servo
	- Adafruit mg90s https://www.adafruit.com/product/1143
		- All metal, 360 degree.
	- Guide https://www.upesy.com/blogs/tutorials/esp32-servo-motor-sg90-on-micropython
	- Wires:
		- The middle wire (orange or red) is +5V power
		- The brown or black wire is Ground.
		- The yellow wire is Control. Most servo motors can accept 3.3-volt logic as well as 5-volt logic.
	- Voltage : 4.8V to 6V DC
	- This is a 360 degree servo, so it is more like a motor. Use time to rotate by a specific amount.
	- ```python
	  import machine
	  import time
	  
	  class Servo():
	    def __init__(self, pin):
	      self.pwm = machine.PWM(machine.Pin(pin), freq=50) # freq is Hz
	      # duty from 0 to 1023. 512=50% active.
	    
	    def counterclockwise(self):
	      # 79 duty is min speed
	      # 120 duty is max speed
	      self.pwm.duty(120)
	    
	    def clockwise(self):
	      # 35 duty is max speed
	      # 75 duty is min speed
	      self.pwm.duty(35)
	    
	    def deg_to_sleep(deg):
	      # 1.228 s sleep at 60 duty is 360deg
	      return deg * (1.228 / 360)
	    
	    def half_counterclockwise(self): 
	      self.pwm.duty(94)
	      time.sleep(self.deg_to_sleep(180))
	      self.pwm.duty(0)
	      
	    def half_clockwise(self):
	      self.pwm.duty(60)
	      time.sleep(self.deg_to_sleep(180))
	      self.pwm.duty(0)
	    
	      
	    def stop(self)
	    	# duty of 76 or 0.
	      self.pwm.duty(0)
	    
	    def cleanup(self):
	    	self.pwm.deinit()
	    
	  ```
- ## Micro Switch
	- Docs:
		- Guide https://www.upesy.com/blogs/tutorials/micropython-gpio-pins-of-esp32-usage#
		- https://docs.micropython.org/en/latest/library/machine.Pin.html#machine.Pin.irq
		- Interrupt https://docs.micropython.org/en/latest/library/machine.Pin.html#machine.Pin.irq
	- The switch has 1 common pin, 1 normally closed (NC, connected), and 1 normally open (NO, disconnected).
		- ```text
		     */
		    /
		  /n
		  [COM   NO   NC]
		    |     |    |
		  ```
	- Prefer Pull Up resistors on digital inputs. The ESP32 has internal pullups, which MicroPython can enable for us.
		- The button must then connect the input `pin` to *ground*, since `pin` will be 3.3V.
	- Use interrupts on a pin to invoke a callback immediately, instead of polling the pin state:
	  ```python
	  import machine
	  import utime
	  
	  class Switch():
	    def __init__(self, pin):
	      if pin >= 34 and pin <= 39:
	        raise Exception("This pin does not have an internal resistor")
	      
	      self.pin = machine.Pin(pin, machine.Pin.IN, machine.Pin.PULL_UP)
	      self.last_debounce = 0
	    
	    def is_active(self):
	      # value is 1 when disconnected.
	      return self.pin.value() == 0
	    
	    def set_callback(self, cb):
	      '''cb should be a lambda or function which takes 1 parameter.
	      The parameter is the self.pin instance.
	      '''
	      self._cb = cb
	      # TODO debounce handler is not invoked properly?
	      pin.irq(handler=lambda p: self._debouncer(p), trigger=machine.Pin.IRQ_FALLING, wake= machine.SLEEP | machine.DEEPSLEEP)
	    
	    def _debouncer(self, pin):
	        print("Pin interrupt " + str(pin))
	        now = utime.ticks_ms()
	        if utime.ticks_diff(now, self.last_debounce) > 100:
	          self.last_debounce = now
	          self._cb(pin)
	    
	    def unset_callback(self):
	      pin.irq(handler=None)
	      self._cb = None
	  
	  # Example:
	  # Connect switch to pin 1 and GND. (Note, pin0 has a button on the board itself!)
	  # s = Switch(1)
	  # def callback(pin): print("Pin triggered: " + str(pin))
	  ```
	- Use `micropython.schedule` in the callback to run more heavy code.
- ## NFC Reader
	- Model: ELECHOUSE PN532 V3
	- Docs:
		- https://www.elechouse.com/elechouse/images/product/PN532_module_V3/PN532_%20Manual_V3.pdf
		- https://www.elechouse.com/product/pn532-nfc-rfid-module-v4/
	- Libs:
		- Official lib https://github.com/elechouse/PN532
		- https://github.com/luiz-brandao/micropython_pn532 (No I2C or SPI)
		- https://warlord0blog.wordpress.com/2021/10/09/esp32-and-nfc-over-i2c/ (Arduino, not MicroPython)
		- https://github.com/somervda/nfc-tester/tree/ed166545357cde894f40cee2de3e44fc15592dfe (**Best bet!**)
	- Remove the sticker on the switches and toggle 1 to up (I2C mode)
	- Connect pins:
		- VCC to ESP32 3v3
		- GND to ESP32 GND
		- SDA to ESP32 9
		- SCL to ESP32 10
	- Upload the `digitalio.py`, `adafruit_pn532.py` and `pn532_i2c.py` from https://github.com/somervda/nfc-tester/tree/ed166545357cde894f40cee2de3e44fc15592dfe
	- ```python
	  import machine
	  import pn532_i2c
	  
	  i2c = machine.I2C(1, scl=machine.Pin(10), sda=machine.Pin(9))
	  # devices = i2c.scan()
	  # It has id 36 in devices array
	  
	  pn532 = pn532_i2c.PN532_I2C(i2c)
	  # ic, ver, rev, support = pn532.get_firmware_version()
	  # print("Found PN532 with firmware version: {0}.{1}".format(ver, rev))
	  # prints v 1.6
	  
	  pn532.SAM_configuration()
	  uid = pn532.read_passive_target(timeout=10) # returns bytearray, or None on timeout
	  pretty_uid = ":".join([hex(i) for i in uid]).replace("0x", "")
	  
	  print(pretty_uid)
	  
	  ```
-
