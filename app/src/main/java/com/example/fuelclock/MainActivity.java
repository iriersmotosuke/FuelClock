package com.example.fuelclock;

import androidx.appcompat.app.AppCompatActivity;

import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1秒ごとに予定燃料消費量を再計算・表示するデーモンスレッド
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            double dEstLPM = 0;
            double dFullTank;
            TextView indicaterEstFuel = findViewById(R.id.tvEstFuel);
            EditText inputStartTime = findViewById(R.id.etStartTime);
            EditText inputFinishTime = findViewById(R.id.etFinishTime);
            EditText inputFullTank = findViewById(R.id.etFullTank);
            Date dateTimeNow = new Date();
            SimpleDateFormat sdf_HHmm = new SimpleDateFormat("HH:mm");
            SimpleDateFormat sdf_HHmmss = new SimpleDateFormat("HH:mm:ss");
            Date dateSatrtTime = new Date();
            Date dateFinishTime = new Date();

            @Override
            public void run() {
                // スタート・フィニッシュ時刻を取得
                String sStartTime = inputStartTime.getText().toString();
                String sFinishTime = inputFinishTime.getText().toString();
                try {
                    dateSatrtTime = sdf_HHmm.parse(sStartTime);
                    dateFinishTime = sdf_HHmm.parse(sFinishTime);
                } catch (Exception e) {
                    dateSatrtTime.setTime(0);
                    dateFinishTime.setTime(0);
                }
                String sFullTank = inputFullTank.getText().toString();
                dFullTank = Double.parseDouble(sFullTank);

                // 現在時刻を取得
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"));
                dateTimeNow.setTime(cal.get(Calendar.HOUR) * 3600000 + cal.get(Calendar.MINUTE) * 60000 + cal.get(Calendar.SECOND) * 1000);

                // 現在時刻を表示
                TextView indicaterTimeNow = findViewById(R.id.tvTimeNow);
                indicaterTimeNow.setText(sdf_HHmmss.format(dateTimeNow));

                // 予定燃料消費量を再計算・表示
                dEstLPM+=0.1;
                indicaterEstFuel.setText(String.format("%1$.2f", dEstLPM));
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);

        // スタート時刻をクリックすると入力モードに入る
        EditText inputStartTime = findViewById(R.id.etStartTime);
        inputStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setFocusable(true);
                view.setFocusableInTouchMode(true);
                view.requestFocus();
            }
        });

        // フィニッシュ時刻をクリックすると入力モードに入る
        EditText inputFinishTime = findViewById(R.id.etFinishTime);
        inputFinishTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setFocusable(true);
                view.setFocusableInTouchMode(true);
                view.requestFocus();
            }
        });



    }
}
