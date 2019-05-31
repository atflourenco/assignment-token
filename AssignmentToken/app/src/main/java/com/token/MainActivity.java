package com.token;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.token.database.AppDatabase;
import com.token.model.Data;
import com.token.util.Constants;
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
    private Boolean updatePassword = false;
    private String password = "";

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
            tvFriendlyName.setVisibility(View.INVISIBLE);
            tvOtp.setVisibility(View.INVISIBLE);
            this.showInputPassword();
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
            Data d = new Data(qrCodeData.getKey(),qrCodeData.getLabel(),password);
            db.dataDao().insert(d);
            btnDeleteData.setVisibility(View.VISIBLE);
            btnScanKey.setVisibility(View.INVISIBLE);
            tvOtp.setVisibility(View.INVISIBLE);
            tvOtp.setVisibility(View.VISIBLE);
            updatePassword = true;
            showInputPassword();
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
                    }
                    try {
                        Thread.sleep(Constants.ONE_SECOND);
                    }catch (Exception e){}
                }
            }
        }).start();
    }

    private void showInputPassword(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.type_password);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.text_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                password = input.getText().toString();
                if(updatePassword){
                    Data d = db.dataDao().getData();
                    d.setPassword(password);
                    db.dataDao().update(d);
                    setDataVisible(d.getLabel(),getOtpValue(d.getKey()));
                    updatePassword = false;
                }else{
                    Data d = db.dataDao().getData();
                    if(!d.getPassword().equals(password)){
                        Toast.makeText(MainActivity.this,R.string.invalid_password,Toast.LENGTH_SHORT).show();
                        finish();
                    }else{
                        btnDeleteData.setVisibility(View.VISIBLE);
                        btnScanKey.setVisibility(View.INVISIBLE);
                        setDataVisible(d.getLabel(),d.getKey());
                    }
                }
            }
        });
        builder.setNegativeButton(R.string.text_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
            }
        });
        builder.show();
    }
}
