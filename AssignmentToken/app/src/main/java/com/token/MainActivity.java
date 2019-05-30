package com.token;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.token.model.QRCodeData;
import com.token.util.Constants;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
    private Button btnScanKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());
        this.initViews();
    }
    private void initViews() {
        btnScanKey = findViewById(R.id.btn_get_key);
        btnScanKey.setOnClickListener(this);
    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_get_key:{
                startActivityForResult(new Intent(MainActivity.this, ScannerBarcodeActivity.class), Constants.REQUEST_CODE_SCAN);
            }break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Constants.REQUEST_CODE_SCAN && resultCode==RESULT_OK){
            QRCodeData qrCodeData = (QRCodeData) data.getSerializableExtra(Constants.DATA_STRING_QR_CODE);
            Toast.makeText(getApplicationContext(),qrCodeData.toString(),Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(getApplicationContext(),R.string.reader_canceled,Toast.LENGTH_SHORT).show();
        }
    }
}
