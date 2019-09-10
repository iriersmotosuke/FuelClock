package com.example.fuelclock;

import androidx.appcompat.app.AppCompatActivity;

import android.icu.text.SimpleDateFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextClock;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ハンドラを生成
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            // 予定燃料消費率を計算
            double dEstLPM = 0;
            TextView indicaterEstFuel = findViewById(R.id.tvEstFuel);
            @Override
            public void run() {
                indicaterEstFuel.setText(String.format("%1$.2f", dEstLPM));
                dEstLPM+=0.1;
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);

        // 時刻表示フォーマットを設定
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        /* 予定燃料消費率を計算
        double dEstLPM = 0.3;*/

        // 現在時刻を取得
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+9") );
        Date dateTimeNow = new Date();
        dateTimeNow.setTime(cal.getTimeInMillis());

        // 現在時刻を表示
        TextView indicaterTimeNow = findViewById(R.id.tvTimeNow);
        indicaterTimeNow.setText(sdf.format(dateTimeNow));

        /* 予定燃料消費を表示
        TextView indicaterEstFuel = findViewById(R.id.tvEstFuel);
        indicaterEstFuel.setText(String.format("%1$.2f", dEstLPM));*/

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
