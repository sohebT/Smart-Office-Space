# Smart-Office-Space
This is a smart office project deployed on multiple devices (Raspberry Pi, Laptop and Android Device) and the AWS Cloud.
The scripts in Raspberry Pi folder are run to publish sensor values to the cloud and get actions to perform from the cloud.
![raspberry_screenshot](https://github.com/manthan410/Smart-Office-Space/assets/94113767/6c0ff4e4-77b3-4e32-8da4-12fd9ae9ebe0)

The script in AWS_Cloud is run on AWS Lambda which invokes the PDDL solver to generate the AI Plan based on the values received from Raspberry Pi. The script generates a set of action to perform from the Plan and publishes it to the Actions topic which is subscribed to by Raspberry Pi for performing those actions and a machine (laptop) for visualization to display those values.

The android app running on user's android device is used by every employee. It helps to track the entry and exit of an employee and inform them of coffee breaks. Based on when the employee chooses to take the break either a strong or normal coffee is selected by the app and this information is again published to the "Action" topic for Raspberry pi to perform this specific action.

"Visual" folder includes the script to display sensor value from the topic "Sensor_Data" to which Raspberry Pi published a=sensor data and to display actuator states as published by the Lamda to the Actions topic.

Also in all these scripts the ARN endpoint is replaced by xxxxxxxxxxxx so use your own ARN endpoint along with the AWS region which is replaced by <region> or Region in the given code.

The overiew for the architecture diagram for the project is shown below:
![block_page-0001](https://github.com/manthan410/Smart-Office-Space/assets/94113767/81fe78f6-c15e-492b-b984-c29d1f1941b1)

