import json
import boto3
import requests

s3 = boto3.resource('s3')
client = boto3.client('iot-data', region_name='enter_your_region')
bucket = s3.Bucket('enter_your_bucket_name')
def lambda_handler(event, context):
    # TODO implement
    try:
        domain = bucket.Object('domain_office.pddl').get()['Body'].read()
        for record in event['Records']:
            if record['eventName'] == 'INSERT':
                newImage = record['dynamodb']['NewImage']
                humid = int(newImage['Humidity']['N'])
                light = int(newImage['Light_Intensity']['N'])
                temperature = int(newImage['Temperature']['N'])
                
                #Fan-Action
                if temperature <= 10:
                    pb1 = bucket.Object('problemt1.pddl').get()['Body'].read()
                    data = {'domain': str(domain.decode('utf-8')),
                            'problem': str(pb1.decode('utf-8'))}
                    resp = requests.post('http://solver.planning.domains/solve',
                                            verify=False, json=data).json()
                    if str(resp['result']['plan'][0]['name']) == '(fan prev off)':
                        mode = 'off'
                elif 10 < temperature <= 25:
                    pb2 = bucket.Object('problemt2.pddl').get()['Body'].read()
                    data = {'domain': str(domain.decode('utf-8')),
                            'problem': str(pb2.decode('utf-8'))}
                    resp = requests.post('http://solver.planning.domains/solve',
                                            verify=False, json=data).json()
                    if str(resp['result']['plan'][0]['name']) == '(fan prev low)':
                      mode = 'low'
                elif 25 < temperature <= 30:
                    pb3 = bucket.Object('problemt3.pddl').get()['Body'].read()
                    data = {'domain': str(domain.decode('utf-8')),
                            'problem': str(pb3.decode('utf-8'))}
                    resp = requests.post('http://solver.planning.domains/solve',
                                            verify=False, json=data).json()
                    if str(resp['result']['plan'][0]['name']) == '(fan prev medium)':
                        mode = 'medium'
                elif temperature > 30:
                    pb4 = bucket.Object('problemt4.pddl').get()['Body'].read()
                    data = {'domain': str(domain.decode('utf-8')),
                            'problem': str(pb4.decode('utf-8'))}
                    resp = requests.post('http://solver.planning.domains/solve',
                                            verify=False, json=data).json()
                    if str(resp['result']['plan'][0]['name']) == '(fan prev high)':
                        mode = 'high'
                
                #Servo-Action
                if temperature <= 20:
                    state = 'on'
                elif temperature > 20:
                    state = 'off'
                    
                #LED-Action
                intensity = 100 - light
                
                response = client.publish(
                        topic='Actions',
                        qos=1,
                        payload=json.dumps({
                            "Fan":mode,
                            "Servo":state,
                            "LED":intensity,
                        })
                )
                
                return "Successfully sent the action to rpi"
                
    except Exception as e:
        print(e)
        return "Error"