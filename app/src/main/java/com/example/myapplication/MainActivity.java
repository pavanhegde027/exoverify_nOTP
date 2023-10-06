package com.example.myapplication;

import android.content.pm.PackageManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.exotel.verification.ConfigBuilder;
import com.exotel.verification.ExotelVerification;
import com.exotel.verification.Timer;
import com.exotel.verification.TimerListener;
import com.exotel.verification.VerificationListener;
import com.exotel.verification.contracts.Config;
import com.exotel.verification.contracts.VerificationFailed;
import com.exotel.verification.contracts.VerificationStart;
import com.exotel.verification.contracts.VerificationSuccess;
import com.exotel.verification.exceptions.ConfigBuilderException;
import com.exotel.verification.exceptions.PermissionNotGrantedException;
import com.exotel.verification.exceptions.VerificationAlreadyInProgressException;

public class MainActivity extends AppCompatActivity {

    ExotelVerification eVerification;
    private final String LOGGING_TAG = "VerificatrixDemoApp";
    private final String accountSid = "stipe1";
    private final String NotpAppId = "344715cf13f044c1aa79d9aef36358e1";
    private final String appSecret = "bajocodejiha";
    Button verifyButton;
    EditText numberEt;
    TextView secondsTv;

    class VerifyListener implements VerificationListener {
        public void onVerificationStarted(VerificationStart verificationStart){
            secondsTv.setVisibility(View.VISIBLE);
            Toast.makeText(MainActivity.this,"Verification Started",Toast.LENGTH_SHORT).show();
        }
        public void onVerificationSuccess(VerificationSuccess verificationSuccess) {
            secondsTv.setVisibility(View.GONE);
            System.out.println(verificationSuccess);
            Toast.makeText(MainActivity.this, "Verification Success", Toast.LENGTH_SHORT).show();
        }
        public void onVerificationFailed(VerificationFailed verificationFailed){
            secondsTv.setVisibility(View.GONE);
            Toast.makeText(MainActivity.this, verificationFailed.getErrorMessage(), Toast.LENGTH_SHORT).show();
            Log.i(LOGGING_TAG, "Verification Failed: "+verificationFailed.getRequestID()+ " "+verificationFailed.getErrorCode()+" "+verificationFailed.getErrorMessage()+" "+verificationFailed.getMiscData() );
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyButton = findViewById(R.id.verify_butt);
        numberEt = findViewById(R.id.number);
        secondsTv = findViewById(R.id.seconds);

        try {
            initializeVerification();
        }
        catch (Exception e) {
            Log.e(LOGGING_TAG, "onCreate: Exception occured " + e.getMessage());
        }

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    eVerification.startVerification(new VerifyListener(),numberEt.getText().toString(),10);
                } catch (VerificationAlreadyInProgressException e) {
                    Log.e(LOGGING_TAG, "Exception: " + e.getMessage());
                }
            }
        });

        Timer customTimer = new Timer();

        customTimer.setTimerListener(new TimerListener(){
            @Override
            public void getTimerTick(long time){
                secondsTv.setText("Please expect the verification call in "+String.valueOf(time/1000)+" seconds.");
            }
        });


    }

    private void initializeVerification() {
        try {
            Config config = new ConfigBuilder(NotpAppId, appSecret, accountSid, getApplicationContext()).Build();
            eVerification = new ExotelVerification(config);
        } catch (PermissionNotGrantedException vPNGE) {
            Log.d(LOGGING_TAG, "initializeVerification: permission not granted exception: " + vPNGE.getPermission());
            askForPermission(vPNGE.getPermission(), 1);

            //Try initializing again after 3 seconds
            (new android.os.Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    initializeVerification();
                }
            }, 3000);

        } catch (ConfigBuilderException cBE) {
            Log.d(LOGGING_TAG, "initializeVerification: ClientBuilder Exception!");
        }
    }

    private void askForPermission(String permission, Integer requestCode) {
        //if the user denied it perviously
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {
                //just asking them again for now
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
            }
        } else {
            //permission already given
        }
    }
}