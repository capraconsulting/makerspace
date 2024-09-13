import machine
import json
import pn532_i2c

# legocity/station/0/state
# { "type": "TRAIN_PRESENT", "value": "123" }
# { "type": "STATUS", "value": "READY" }

# ------------------------------------------------------------ Setup

state_topic = b'legocity/station/0/state'

i2c = machine.I2C(1, scl=machine.Pin(10), sda=machine.Pin(9))
pn532 = pn532_i2c.PN532_I2C(i2c)
pn532.SAM_configuration()

# ------------------------------------------------------------- MQTT

def connect_and_subscribe():
  	global client_id, mqtt_server, topic_sub
  	client = MQTTClient(client_id, mqtt_server, port=mqtt_port, user=mqtt_user, password=mqtt_pass)
  	client.connect()
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

# ----------------------------------------------------------- Loop

client.publish(state_topic, json.dumps({ "type": "STATUS", "value": "READY" }))

while True:	
	client.check_msg()

	# Read RFID
	uid = pn532.read_passive_target(timeout=20) # returns bytearray, or None on timeout
	
	if uid != None:
		pretty_uid = ":".join([hex(i) for i in uid]).replace("0x", "")
		print(pretty_uid)
		client.publish(state_topic, json.dumps({ "type": "TRAIN_PRESENT", "value": pretty_uid }))

	time.sleep(1)
