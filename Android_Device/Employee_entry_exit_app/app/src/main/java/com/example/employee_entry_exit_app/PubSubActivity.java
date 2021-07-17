package com.example.employee_entry_exit_app;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.regions.Regions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class PubSubActivity extends Activity {
    static int l = 0;
    static final String LOG_TAG = PubSubActivity.class.getCanonicalName();
    //ArrayList<String> choices = new ArrayList<String>(3);
    List<String> choices = new ArrayList<String>();
    // --- Constants to modify per your configuration ---
    boolean flag = false;
    // Customer specific IoT endpoint
    // AWS Iot CLI describe-endpoint call returns: XXXXXXXXXX.iot.<region>.amazonaws.com,
    private static final String CUSTOMER_SPECIFIC_ENDPOINT = "XXXXXXXXXX.iot.<region>.amazonaws.com";

    // Cognito pool ID. For this app, pool needs to be unauthenticated pool with
    // AWS IoT permissions.
    private static final String COGNITO_POOL_ID = "cognito_pool_id";

    // Region of AWS IoT
    private static final Regions MY_REGION = Regions.<region>;
    Timer timerObj = new Timer();
    //EditText txtSubscribe;
    //EditText txtTopic;
    //EditText txtMessage;
    public static Activity activityReference = null;
    //TextView tvLastMessage;
    TextView tvClientId;
    TextView tvStatus;

    //Button btnConnect;
    Button btnPublish2;
    Button btnPublish;
    //Button btnDisconnect;

    AWSIotMqttManager mqttManager;
    String clientId;

    CognitoCachingCredentialsProvider credentialsProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //txtSubscribe = (EditText) findViewById(R.id.txtSubscribe);
        //txtTopic = (EditText) findViewById(R.id.txtTopic);
        //txtMessage = (EditText) findViewById(R.id.txtMessage);

        // tvLastMessage = (TextView) findViewById(R.id.tvLastMessage);
        tvClientId = (TextView) findViewById(R.id.tvClientId);
        tvStatus = (TextView) findViewById(R.id.tvStatus);

        /*btnConnect = (Button) findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(connectClick);
        btnConnect.setEnabled(false);*/

        // btnSubscribe = (Button) findViewById(R.id.btnSubscribe);
        //btnSubscribe.setOnClickListener(subscribeClick);

        btnPublish = (Button) findViewById(R.id.btnPublish);
        btnPublish.setOnClickListener(publishClick);

        btnPublish2 = (Button) findViewById(R.id.btnPublish2);
        btnPublish2.setOnClickListener(publishClick2);

        /*btnDisconnect = (Button) findViewById(R.id.btnDisconnect);
        btnDisconnect.setOnClickListener(disconnectClick);*/

        // MQTT client IDs are required to be unique per AWS IoT account.
        // This UUID is "practically unique" but does not _guarantee_
        // uniqueness.
        clientId = UUID.randomUUID().toString();
        tvClientId.setText(clientId);

        // Initialize the AWS Cognito credentials provider
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(), // context
                COGNITO_POOL_ID, // Identity Pool ID
                MY_REGION // Region
        );
        activityReference = this;
        // MQTT Client
        mqttManager = new AWSIotMqttManager(clientId, CUSTOMER_SPECIFIC_ENDPOINT);

        // The following block uses a Cognito credentials provider for authentication with AWS IoT.
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnConnect.setEnabled(true);
                    }
                });
            }
        }).start();*/
        try {
            mqttManager.connect(credentialsProvider, new AWSIotMqttClientStatusCallback() {
                @Override
                public void onStatusChanged(final AWSIotMqttClientStatus status, final Throwable throwable) {
                    Log.d(LOG_TAG, "Status = " + String.valueOf(status));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (status == AWSIotMqttClientStatus.Connecting) {
                                tvStatus.setText("Connecting...");

                            } else if (status == AWSIotMqttClientStatus.Connected) {
                                tvStatus.setText("Connected");

                            } else if (status == AWSIotMqttClientStatus.Reconnecting) {
                                if (throwable != null) {
                                    Log.e(LOG_TAG, "Connection error.", throwable);
                                }
                                tvStatus.setText("Reconnecting");
                            } else if (status == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.ConnectionLost) {
                                if (throwable != null) {
                                    Log.e(LOG_TAG, "Connection error.", throwable);
                                    throwable.printStackTrace();
                                }
                                tvStatus.setText("Disconnected");
                            } else {
                                tvStatus.setText("Disconnected");

                            }
                        }
                    });
                }
            });
        } catch (final Exception e) {
            Log.e(LOG_TAG, "Connection error.", e);
            tvStatus.setText("Error! " + e.getMessage());
        }
    }

    /*View.OnClickListener connectClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Log.d(LOG_TAG, "clientId = " + clientId);

            try {
                mqttManager.connect(credentialsProvider, new AWSIotMqttClientStatusCallback() {
                    @Override
                    public void onStatusChanged(final AWSIotMqttClientStatus status, final Throwable throwable) {
                        Log.d(LOG_TAG, "Status = " + String.valueOf(status));

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (status == AWSIotMqttClientStatus.Connecting) {
                                    tvStatus.setText("Connecting...");

                                } else if (status == AWSIotMqttClientStatus.Connected) {
                                    tvStatus.setText("Connected");

                                } else if (status == AWSIotMqttClientStatus.Reconnecting) {
                                    if (throwable != null) {
                                        Log.e(LOG_TAG, "Connection error.", throwable);
                                    }
                                    tvStatus.setText("Reconnecting");
                                } else if (status == AWSIotMqttClientStatus.ConnectionLost) {
                                    if (throwable != null) {
                                        Log.e(LOG_TAG, "Connection error.", throwable);
                                        throwable.printStackTrace();
                                    }
                                    tvStatus.setText("Disconnected");
                                } else {
                                    tvStatus.setText("Disconnected");

                                }
                            }
                        });
                    }
                });
            } catch (final Exception e) {
                Log.e(LOG_TAG, "Connection error.", e);
                tvStatus.setText("Error! " + e.getMessage());
            }
        }
    };*/

   /* View.OnClickListener subscribeClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            final String topic = txtSubscribe.getText().toString();

            Log.d(LOG_TAG, "topic = " + topic);

            try {
                mqttManager.subscribeToTopic(topic, AWSIotMqttQos.QOS0,
                        new AWSIotMqttNewMessageCallback() {
                            @Override
                            public void onMessageArrived(final String topic, final byte[] data) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            String message = new String(data, "UTF-8");
                                            Log.d(LOG_TAG, "Message arrived:");
                                            Log.d(LOG_TAG, "   Topic: " + topic);
                                            Log.d(LOG_TAG, " Message: " + message);

                                            tvLastMessage.setText(message);

                                        } catch (UnsupportedEncodingException e) {
                                            Log.e(LOG_TAG, "Message encoding error.", e);
                                        }
                                    }
                                });
                            }
                        });
            } catch (Exception e) {
                Log.e(LOG_TAG, "Subscription error.", e);
            }
        }
    };*/

    View.OnClickListener publishClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            String topic = "entry_exit";
            //final String topic = txtTopic.getText().toString();
            //final String msg = txtMessage.getText().toString();
            final String currentTime = Calendar.getInstance().getTime().toString();
            JSONObject obj = new JSONObject();
            try {
                obj.put("datetime", currentTime);
                obj.put("id", "Employee1");
                obj.put("Status", "Entry");
            } catch (Exception e) {
                Log.e(LOG_TAG, "JSON Create Error", e);
            }
            try {
                l=0;
                choices.clear();
                mqttManager.publishString(obj.toString(), topic, AWSIotMqttQos.QOS0);
                AlertDialog.Builder coffee = new AlertDialog.Builder(activityReference);
                coffee.setTitle("Coffee Break")
                        .setMessage("Do you want to have a coffee?");
                coffee.setPositiveButton("Yes", (dialog, which) -> {
                    choices.add("Yes");
                        if (l<=1) {
                            final String topic1= "Actions";
                            final String currentTime1 = Calendar.getInstance().getTime().toString();
                            JSONObject abc = new JSONObject();
                                try {
                                    abc.put("datetime", currentTime1);
                                    abc.put("id", "Employee1");
                                    abc.put("Status", "Normal Coffee");
                                    mqttManager.publishString(abc.toString(), topic1, AWSIotMqttQos.QOS0);
                                }
                                catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                Log.i("tag", "Normal Coffee");

                        } else {
                            final String topic1= "Actions";
                            final String currentTime1 = Calendar.getInstance().getTime().toString();
                            JSONObject abc = new JSONObject();
                            try {
                                abc.put("datetime", currentTime1);
                                abc.put("id", "Employee1");
                                abc.put("Status", "Strong Coffee");
                                mqttManager.publishString(abc.toString(), topic1, AWSIotMqttQos.QOS0);
                            }
                            catch (JSONException e) {
                                e.printStackTrace();
                            }

                            Log.i("tag", "Strong Coffee");
                        }
                        choices.clear();
                        timerObj.cancel();
                    dialog.dismiss();
                });
                coffee.setNegativeButton("No", (dialog, which) -> {
                    choices.add("No");
                    l++;
                    dialog.dismiss();
                });
                AlertDialog alert = coffee.create();
                TimerTask timerTaskObj = new TimerTask() {
                    @Override
                    public void run() {
                        //perform your action here
                        PubSubActivity.this.runOnUiThread(new Runnable() {
                            public void run() {

                                try {
                                    System.out.println(l);
                                    alert.show();
                                    if(l>=3){
                                        l=0;
                                        System.out.println("All No's");
                                        choices.clear();
                                        timerObj.cancel();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    }
                };
                timerObj.schedule(timerTaskObj, 10000, 10000);

                /*new CountDownTimer(50000,10000){
                    @Override
                    public void onTick(long l) {

                        for(int j=0; j <= 2; j++){
                            if(choices.get(j)=="yes"){
                                if(j<=1){
                                    Log.i(LOG_TAG, "Normal Coffee");
                                }
                                else {
                                    Log.i(LOG_TAG, "Strong Coffee");
                                }
                                flag = true;
                            }
                        }
                        if(flag==true || l>=2){
                            finish();
                        }
                    }


                    @Override
                    public void onFinish() {

                    }
                }.start();*/




            } catch (Exception e) {
                Log.e(LOG_TAG, "Publish Entry error.", e);
            }

        }
    };



    View.OnClickListener publishClick2 = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            final String topic = "entry_exit";
            //final String topic = txtTopic.getText().toString();
            //final String msg = txtMessage.getText().toString();
            final String currentTime = Calendar.getInstance().getTime().toString();
            JSONObject obj = new JSONObject();
            try {
                obj.put("datetime", currentTime);
                obj.put("id", "Employee1");
                obj.put("Status", "Exit");
            } catch (Exception e) {
                Log.e(LOG_TAG, "JSON Create Error", e);
            }
            try {
                mqttManager.publishString(obj.toString(), topic, AWSIotMqttQos.QOS0);
                choices.clear();
                l=0;
            } catch (Exception e) {
                Log.e(LOG_TAG, "Publish Exit error.", e);
            }

        }
    };

    /*View.OnClickListener disconnectClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            try {
                mqttManager.disconnect();
            } catch (Exception e) {
                Log.e(LOG_TAG, "Disconnect error.", e);
            }

        }
    };*/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mqttManager.disconnect();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Disconnect error.", e);
        }
    }
   /* private class Task extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            l=0;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            flag = false;

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    l++;
                    new AlertDialog.Builder(activityReference)
                            .setTitle("Coffee Break")
                            .setMessage("Do you want to have a coffee?")
                            .setPositiveButton(android.R.string.yes, Logic("yes"))
                            .setNegativeButton(android.R.string.no, Logic("no"))
                            .setIcon(android.R.drawable.ic_dialog_alert).create().show();
                    for(int j=0; j <= 2; j++){
                        if(choices.get(j)=="yes"){
                            if(j<=1){
                                Log.i(LOG_TAG, "Normal Coffee");
                                flag = true;
                            }
                            else {
                                Log.i(LOG_TAG, "Strong Coffee");
                                flag = true;
                            }
                        }
                    }
                    if(flag==true || l>=2){
                        finish();
                    }
                }
            }, 12000);
            return null;
        }

        @Override
        protected void onPostExecute(Void s) {

        }
    }

    public DialogInterface.OnClickListener Logic(String s){
        choices.add(s);
        return null;
    }*/
}