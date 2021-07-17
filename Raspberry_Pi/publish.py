# importing libraries
import paho.mqtt.client as paho
import os
import socket
import ssl
import random
from datetime import datetime
import string
import json
from time import sleep
 
connflag = False
 
def on_connect(client, userdata, flags, rc):                # func for making connection
    global connflag
    print ("Connected to AWS")
    connflag = True
    print("Connection returned result: " + str(rc) )
 
def on_message(client, userdata, msg):                      # Func for Sending msg
    print(msg.topic+" "+str(msg.payload))
 
mqttc = paho.Client()                                       # mqttc object
mqttc.on_connect = on_connect                               # assign on_connect func
mqttc.on_message = on_message                               # assign on_message func
#### Change following parameters #### 
awshost = "xxxxxxxxxxxxx-ats.iot.enter_your_region.amazonaws.com"      # Endpoint
awsport = 8883                                              # Port no.   
#clientId = "sensor_upload"                                     # Thing_Name
#thingName = "Rpi"                                    # Thing_Name
caPath = "/home/pi/AmazonRootCA1.pem"                                      # Root_CA_Certificate_Name
certPath = "/home/pi/certificate.pem.crt"                            # <Thing_Name>.cert.pem
keyPath = "/home/pi/private.pem.key"                          # <Thing_Name>.private.key
 
mqttc.tls_set(caPath, certfile=certPath, keyfile=keyPath, cert_reqs=ssl.CERT_REQUIRED, tls_version=ssl.PROTOCOL_TLSv1_2, ciphers=None)  # pass parameters
 
mqttc.connect(awshost, awsport, keepalive=60)               # connect to aws server
 
mqttc.loop_start()                                          # Start the loop
 
while 1==1:
    sleep(5)
    if connflag == True:
        now = datetime.now()
        dt_string=now.strftime("%d/%m/%Y %H:%M:%S")
        temperature_value = random.randint(-10,45)
        humidity_value = random.randint(63, 80)
        light_value = random.randint(0, 100)
        paylodmsg0="{"
        paylodmsg1 = "\"datetime\": \""
        paylodmsg2 = "\", \"Temperature\":"
        paylodmsg3 = ", \"Humidity\":"
        paylodmsg4= ", \"Light_Intensity\":"
        paylodmsg5= "}"
        paylodmsg = "{} {} {} {} {} {} {} {} {} {}".format(paylodmsg0, paylodmsg1, dt_string, paylodmsg2, temperature_value, paylodmsg3, humidity_value, paylodmsg4, light_value, paylodmsg5)
        paylodmsg = json.dumps(paylodmsg) 
        paylodmsg_json = json.loads(paylodmsg)       
        mqttc.publish("Sensor_Data", paylodmsg_json , qos=1)        # topic: Sensor_Data # Publishing sensor values
        print("msg sent: Data sent" ) # Print sent sensor msg on console


        print(paylodmsg_json)
    else:
        print("waiting for connection...")  