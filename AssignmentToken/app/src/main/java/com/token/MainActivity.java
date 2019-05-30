package com.token;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.token.database.AppDatabase;
import com.token.model.Data;
import com.token.util.Constants;

import java.io.IOException;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public native byte[] generateOtp(String key);

    static {
        System.loadLibrary("otpjni");
    }
    private Button btnScanKey;
    private Button btnDeleteData;
    private TextView tvFriendlyName;
    private TextView tvOtp;
    private AppDatabase db;
    private Boolean isVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.initViews();
        this.checkDatabase();
    }
    private void initViews() {
        tvFriendlyName = findViewById(R.id.txt_friendly);
        tvOtp = findViewById(R.id.txt_otp);
        btnScanKey = findViewById(R.id.btn_get_key);
        btnDeleteData = findViewById(R.id.btn_delete);
        btnScanKey.setOnClickListener(this);
        btnDeleteData.setOnClickListener(this);
    }

    private void checkDatabase(){
        db = AppDatabase.getAppDatabase(this);
        Data data  = db.dataDao().getData();
        if(data!=null){
            btnScanKey.setVisibility(View.INVISIBLE);
            setDataVisible(data.getLabel(),getOtpValue(data.getKey()));
        }else{
            tvFriendlyName.setText(R.string.click_get_key);
            tvOtp.setText("");
            tvOtp.setVisibility(View.INVISIBLE);
            btnScanKey.setVisibility(View.VISIBLE);
            btnDeleteData.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_get_key:{
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                        startActivityForResult(new Intent(MainActivity.this, ScannerBarcodeActivity.class), Constants.REQUEST_CODE_SCAN);
                } else {
                    ActivityCompat.requestPermissions(this, new
                            String[]{Manifest.permission.CAMERA}, Constants.REQUEST_CAMERA_PERMISSION);
                    return;
                }
            }break;
            case R.id.btn_delete:{
                AppDatabase db = AppDatabase.getAppDatabase(this);
                        db.dataDao().deleteAll();
                btnScanKey.setVisibility(View.VISIBLE);
                tvFriendlyName.setText(R.string.click_get_key);

                tvOtp.setText("");
                tvOtp.setVisibility(View.INVISIBLE);
                btnDeleteData.setVisibility(View.INVISIBLE);
            }break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Constants.REQUEST_CODE_SCAN && resultCode==RESULT_OK){
            Data qrCodeData = (Data) data.getSerializableExtra(Constants.DATA_STRING_QR_CODE);
            Data d = new Data(qrCodeData.getKey(),qrCodeData.getLabel());
            db.dataDao().insert(d);
            btnDeleteData.setVisibility(View.VISIBLE);
            btnScanKey.setVisibility(View.INVISIBLE);
            setDataVisible(qrCodeData.getLabel(),qrCodeData.getKey());
        }else{
            Toast.makeText(getApplicationContext(),R.string.read_cancelled,Toast.LENGTH_SHORT).show();
        }
    }

    private void setDataVisible(String label, String key){
        tvFriendlyName.setVisibility(View.VISIBLE);
        tvFriendlyName.setText(label);
        tvOtp.setVisibility(View.VISIBLE);
        tvOtp.setText(getOtpValue(key));
    }

    private String getOtpValue(String key){
        byte[] otpGenerated = generateOtp(key);
        String str = String.format("%02x",otpGenerated[10]&0x7F);
        str += String.format("%02x",otpGenerated[11]);
        str += String.format("%02x",otpGenerated[12]);
        str += String.format("%02x",otpGenerated[13]);
        Long v = Long.parseLong(str.toUpperCase(),16);
        return String.format("%010d",v).substring(4);
    }


    @Override
    protected void onResume() {
        super.onResume();
        isVisible = true;
        this.updateOtp();
    }

    @Override
    protected void onStop() {
        super.onStop();
        isVisible = false;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.REQUEST_CAMERA_PERMISSION: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(new Intent(MainActivity.this, ScannerBarcodeActivity.class), Constants.REQUEST_CODE_SCAN);
                } else {
                    Toast.makeText(this, getString(R.string.request_denied),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private void updateOtp(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(isVisible){
                    if(tvOtp.getVisibility()==View.VISIBLE){
                        Calendar calendar = Calendar.getInstance();
                        if(calendar.get(Calendar.SECOND)==0
                                || calendar.get(Calendar.SECOND)==30){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    checkDatabase();
                                }
                            });
                        }
                        try {
                            Thread.sleep(Constants.ONE_SECOND);
                        }catch (Exception e){}
                    }
                }
            }
        }).start();
    }

}
