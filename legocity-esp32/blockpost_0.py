import machine
import ssd1306
import json

# legocity/blockpost/0/state
# { "type": "TRAIN_PASSAGE" }
# { "type": "STATUS", "value": "READY" }

# legocity/blockpost/0/cmd
# { "type": "DISPLAY", "value": "Stop!" } 
# { "type": "BARRIER", "value": "UP"    }
# { "type": "BARRIER", "value": "DOWN"  }

# ------------------------------------------------------------ Setup

state_topic = b'legocity/blockpost/0/state'
cmd_topic   = b'legocity/blockpost/0/cmd'
barrier     = "DOWN"
btn         = 1

i2c     = machine.SoftI2C(sda=machine.Pin(9), scl=machine.Pin(10))
display = ssd1306.SSD1306_I2C(64, 32, i2c)
button  = machine.Pin(0, machine.Pin.IN)
pwm     = machine.PWM(machine.Pin(15), freq=50) # freq is Hz

# ------------------------------------------------------------- MQTT

def connect_and_subscribe():
  	global client_id, mqtt_server, topic_sub
  	client = MQTTClient(client_id, mqtt_server, port=mqtt_port, user=mqtt_user, password=mqtt_pass)
  	client.set_callback(handle_message)
  	client.connect()
  	client.subscribe(cmd_topic)
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

# ------------------------------------------------------------ Servo

def deg_to_sleep(deg):
	# 1.228 s sleep at 60 duty is 360deg
    return deg * (1.228 / 360)

def barrier_up():
	pwm.duty(94)
	time.sleep(deg_to_sleep(90))
	pwm.duty(0)

def barrier_down(): 
	pwm.duty(60)
	time.sleep(deg_to_sleep(90))
	pwm.duty(0)

# ------------------------------------------------------------- Logic

def handle_message(topic, msg):

	print((topic, msg))
  	if topic == b'notification' and msg == b'received':
		print('ESP received hello message')
 	
	if topic == cmd_topic:
		try:
			payload = json.loads(msg.decode('ascii'))

			if payload["type"] == "BARRIER":	
				update_barrier(payload)

			if payload["type"] == "DISPLAY":
				update_display(payload)

		except Exception as e:

			payload = msg.decode('ascii')
			if payload == "UP":
				payload = {"type": "BARRIER", "value": "UP"}
				update_barrier(payload)
			elif payload == "DOWN":
				payload = {"type": "BARRIER", "value": "DOWN"}
				update_barrier(payload)
			else:
				print("Failed to deserialize command payload " + str(msg), e)

def update_barrier(payload):

	global barrier

	if payload["value"] == "UP" and barrier == "DOWN":
		barrier_up()
		barrier = "UP"

	if payload["value"] == "DOWN" and barrier == "UP":
		barrier_down()
		barrier = "DOWN"

def update_display(payload):		
	texts = payload["value"].split("\n")
	i = 0
	display.fill(0)
	for text in texts:
		display.text(text, 0, i * 10, 1)
		i += 1
	display.show()

def update_button_state():
	state = 1 - button.value()
	if state != btn and state > 0:
		client.publish(state_topic, json.dumps({ "type": "TRAIN_PASSAGE" }))
		time.sleep(0.25)

	return state

# ----------------------------------------------------------- Loop

client.publish(state_topic, json.dumps({ "type": "STATUS", "value": "READY" }))

while True:	
	client.check_msg()
	btn = update_button_state()