package com.example.wangkuan.zidingyiyuan;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {


    private GuaView gv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gv = (GuaView) findViewById(R.id.st_guaguaka);
        gv.setOnGuaGuaKaCompletedListener(new GuaView.onGuaGuaKaCompletedListener() {
            @Override
            public void complete(String message) {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });

    }
}
