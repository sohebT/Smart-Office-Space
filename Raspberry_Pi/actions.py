import paho.mqtt.client as paho
import os
import socket
import ssl
import json
from time import sleep
import RPi.GPIO as GPIO
from rpi_ws281x import *
import argparse

# LED strip configuration:
LED_COUNT      = 8      # Number of LED pixels.
LED_PIN        = 18      # GPIO pin connected to the pixels (18 uses PWM!).
LED_FREQ_HZ    = 800000  # LED signal frequency in hertz (usually 800khz)
LED_DMA        = 10      # DMA channel to use for generating signal (try 10)
LED_BRIGHTNESS = 255     # Set to 0 for darkest and 255 for brightest
LED_INVERT     = False   # True to invert the signal (when using NPN transistor level shift)
LED_CHANNEL    = 0       # set to '1' for GPIOs 13, 19, 41, 45 or 53

# Process arguments
parser = argparse.ArgumentParser()
parser.add_argument('-c', '--clear', action='store_true', help='clear the display on exit')
args = parser.parse_args()
# Create NeoPixel object with appropriate configuration.
strip = Adafruit_NeoPixel(LED_COUNT, LED_PIN, LED_FREQ_HZ, LED_DMA, LED_INVERT, LED_BRIGHTNESS, LED_CHANNEL)
# Intialize the library (must be called once before other functions).
strip.begin()
                  
GPIO.setmode(GPIO.BCM)
GPIO.setup(14,GPIO.OUT)
GPIO.setup(15,GPIO.OUT)
GPIO.setup(23,GPIO.OUT)
GPIO.setup(2,GPIO.OUT)
GPIO.setup(3,GPIO.OUT)
GPIO.setup(4,GPIO.OUT)
GPIO.setup(17,GPIO.OUT)
GPIO.output(2,GPIO.HIGH)
pwm=GPIO.PWM(17, 50)
pwm.start(0)
emp_count = set()
def SetAngle(angle):
	duty = (angle / 18) + 2
	GPIO.output(17, True)
	pwm.ChangeDutyCycle(duty)

    
def colorWipe(strip, color, wait_ms=5):
    for i in range(strip.numPixels()):
        strip.setPixelColor(i, color)
        strip.show()
    
def fan(speed):
    print("Fan :"+str(speed))
    
    if(str(speed)=='off'):
        GPIO.output(23,GPIO.LOW)
        GPIO.output(14,GPIO.LOW)
        GPIO.output(15,GPIO.LOW)      
    if(str(speed)=='low'):
        GPIO.output(23,GPIO.HIGH)
        GPIO.output(14,GPIO.LOW)
        GPIO.output(15,GPIO.LOW)
    if(str(speed)=='medium'):
        GPIO.output(23,GPIO.HIGH)
        GPIO.output(14,GPIO.HIGH)
        GPIO.output(15,GPIO.LOW)
    if(str(speed)=='high'):
        GPIO.output(23,GPIO.HIGH)
        GPIO.output(14,GPIO.HIGH)
        GPIO.output(15,GPIO.HIGH)
    
def servo(state):
    print("Servo :"+str(state))
    if(str(state)=='off'):
        SetAngle(0) 
        
    if(str(state)=='on'):
        SetAngle(180) 
      
def led(intensity):
    print("LED :"+str(intensity))
    int i = (255//100)*int(intensity)
    colorWipe(strip, Color(i, i, i))
	
def coffee(type):
    print("Coffee :"+str(type))
    if(str(type)=='Strong Coffee'):
        GPIO.output(2,GPIO.LOW)
        GPIO.output(4,GPIO.HIGH)
        sleep(3)
        GPIO.output(4,GPIO.LOW)
        GPIO.output(2,GPIO.HIGH)
    if(str(type)=='Normal Coffee'):
        GPIO.output(2,GPIO.LOW)
        GPIO.output(3,GPIO.HIGH)
        sleep(3)
        GPIO.output(3,GPIO.LOW)
        GPIO.output(2,GPIO.HIGH)

def on_connect(client, userdata, flags, rc):                # func for making connection
    print("Connection returned result: " + str(rc) )
    # Subscribing in on_connect() means that if we lose the connection and
    # reconnect then subscriptions will be renewed.
    client.subscribe("Actions" , 1 )                              # Subscribe to "Actions" topic
 
def on_message(client, userdata, msg):                      # Func for receiving msgs
    act = json.loads(str(msg.payload.decode("utf-8")))
    for key, value in act.items():
        if key == "Fan":
            fan(value)
        if key == "Servo":
            servo(value)
        if key == "LED":
            led(value)
        if key == "Status":
            coffee(value)

def on_connect_ctrl(client, userdata, flags, rc):
    client.subscribe("entry_exit", 1)

def on_message_ctrl(client, userdata, msg):
    entry_exit = json.loads(str(msg.payload.decode("utf-8")))
    if entry_exit["Status"] == "Entry":
        emp_count.add(entry_exit["id"])
    if entry_exit["Status"] == "Exit":
        emp_count.discard(entry_exit["id"])
        
        
    
mqttc_ctrl = paho.Client()
mqttc_ctrl.on_connect = on_connect_ctrl
mqttc_ctrl.on_message = on_message_ctrl


#For Android device topic entry_exit
ctrl_awshost = "xxxxxxxxxxx-ats.iot.enter_your_region.amazonaws.com"      # Endpoint
ctrl_awsport = 8883                                              # Port no.   
#clientId = "sensor_upload"                                     # Thing_Name
#thingName = "Rpi"                                    # Thing_Name
ctrl_caPath = "/home/pi/AmazonRootCA1.pem"                                      # Root_CA_Certificate_Name
ctrl_certPath = "/home/pi/certificate.pem.crt"                            # <Thing_Name>.cert.pem
ctrl_keyPath = "/home/pi/private.pem.key"                          # <Thing_Name>.private.key

mqttc = paho.Client()                                       # mqttc object
mqttc.on_connect = on_connect                               # assign on_connect func
mqttc.on_message = on_message                               # assign on_message func

#### For Actions topic #### 
awshost = "xxxxxxxxxxx-ats.iot.enter_your_region.amazonaws.com"      # Endpoint
awsport = 8883                                              # Port no.   
clientId = "sensor_upload"                                     # Thing_Name
thingName = "Rpi"                                    # Thing_Name
caPath = "/home/pi/AmazonRootCA1.pem"                                      # Root_CA_Certificate_Name
certPath = "/home/pi/certificate.pem.crt"                            # <Thing_Name>.cert.pem
keyPath = "/home/pi/private.pem.key"                          # <Thing_Name>.private.key
 
mqttc.tls_set(caPath, certfile=certPath, keyfile=keyPath, cert_reqs=ssl.CERT_REQUIRED, tls_version=ssl.PROTOCOL_TLSv1_2, ciphers=None)      

mqttc_ctrl.tls_set(ctrl_caPath, certfile=ctrl_certPath, keyfile=ctrl_keyPath, cert_reqs=ssl.CERT_REQUIRED, tls_version=ssl.PROTOCOL_TLSv1_2, ciphers=None) 

mqttc_ctrl.connect(ctrl_awshost, ctrl_awsport, keepalive=60)

mqttc_ctrl.loop_start()
while 1==1:
    mqttc.connect(awshost, awsport, keepalive=60)               # connect to aws server
    mqttc.loop_start()                                        # Start receiving in loop
    while len(emp_count) != 0:
        pass
    mqttc.loop_stop()
    fan("off")
    servo('off')
    led("0")