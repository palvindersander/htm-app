package com.htm.twilio;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.accessibilityservice.GestureDescription;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final String NUM = "01797462034";
    public static final String NUMUK = "+441797462034";
    private FusedLocationProviderClient fusedLocationClient;
    private static MainActivity ins;
    private EditText phoneNumber;
    private EditText smsMessage;
    private Button sendSMS;
    private TextView smsHistory;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkForSmsPermission();
        ins = this;
        phoneNumber = findViewById(R.id.editText);
        smsMessage = findViewById(R.id.editText2);
        sendSMS = findViewById(R.id.sendSMS);
        smsHistory = findViewById(R.id.textView4);
        phoneNumber.setText(NUM);
        Button clearButton = findViewById(R.id.button);
        smsHistory.setMovementMethod(new ScrollingMovementMethod());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        sendSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSMS(smsMessage.getText().toString());
                sendLocSMS();
            }
        });
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                smsHistory.setText("");
            }
        });
    }

    //for other class access
    public static MainActivity getInstace() {
        return ins;
    }

    //add text to screen
    public void addText(String s, boolean b, ArrayList<String> messages) {
        if (smsHistory != null) {
            if (b) {
                String line  = s;
                String newLine = ((String[])line.split(":"))[2];
                smsHistory.setText(smsHistory.getText().toString() + "YOU:  " + newLine + "\n");
            } else {
                //show msgs
                //strip upto 37
                String message = "";
                for (String msg : messages){
                    message += msg;
                }
                message = message.substring(37);
                message = message.substring(0,message.length()-1);
                String[] lines = message.split("=");
                int i = 0;
                for (String line : lines){
                    if (i==0) {
                        smsHistory.setText(smsHistory.getText().toString() + "TRAVELSMS:  " + line + "\n");
                    }
                    else {
                        smsHistory.setText(smsHistory.getText().toString() + line + "\n");
                    }
                    i++;
                }
            }
        }
    }

    //send location sms
    private Double[] sendLocSMS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        final Double[] loc = new Double[2];
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            loc[0] = location.getLongitude();
                            loc[1] = location.getLatitude();
                            simpleSMS newSMS = new simpleSMS(smsMessage.getText().toString(),phoneNumber.getText().toString());
                            newSMS.setLocation(loc);
                            String textMessage = newSMS.getLocText();
                            if (newSMS.checkMessage(textMessage)) {
                                sendSMS(textMessage);
                            }else {
                                Toast.makeText(MainActivity.this, "please follow the correct SMS format: 'x to y'", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            simpleSMS newSMS = new simpleSMS(smsMessage.getText().toString(),phoneNumber.getText().toString());
                            if (newSMS.checkInput(smsMessage.getText().toString())) {
                                sendSMS(smsMessage.getText().toString());
                            }else {
                                Toast.makeText(MainActivity.this, "please follow the correct SMS format: 'x to y'", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
        return loc;
    }

    //check sms permission
    private void checkForSmsPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    MY_PERMISSIONS_REQUEST_SEND_SMS);
        } else {
            // Permission already granted.
        }
    }

    //check and get send sms permission
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (permissions[0].equalsIgnoreCase(Manifest.permission.SEND_SMS)
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    // Permission denied.
                }
            }
        }
    }

    //initialises sms sending
    private void sendSMS(String message) {
        String phoneNumber = this.phoneNumber.getText().toString();
        if (message == null) {
            String textMessage = this.smsMessage.getText().toString();
            simpleSMS newSMS = new simpleSMS(textMessage, phoneNumber);
            if (newSMS.checkInput(textMessage)) {
                sender(phoneNumber, textMessage);
                addText(textMessage, true, null);
            }
        }
        else {
            simpleSMS newSMS = new simpleSMS(message, phoneNumber);
            if (newSMS.checkMessage(message)) {
                sender(phoneNumber,message);
                addText(message, true, null);
            }
        }
    }

    //sends sms
    private void sender( String number, String message) {
        try {
            SmsManager smgr = SmsManager.getDefault();
            smgr.sendTextMessage(number, null, message, null, null);
            Toast.makeText(MainActivity.this, "SMS Sent Successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "SMS Failed to Send, Please try again", Toast.LENGTH_SHORT).show();
            System.out.println(e);
        }
    }

}
