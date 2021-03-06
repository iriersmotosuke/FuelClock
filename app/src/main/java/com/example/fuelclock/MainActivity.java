package com.example.fuelclock;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static java.util.Locale.JAPAN;

public class MainActivity extends AppCompatActivity {

    private static final String PREF_FILE_NAME = "com.example.fuelclock.FuelClockPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 設定値を読み込む
        final SharedPreferences spPrefs = getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editorPrefs = spPrefs.edit();
        String sStartTime = spPrefs.getString("StartTime", "13:00");
        String sFinishTime = spPrefs.getString("FinishTime", "15:30");
        String sFullTank = spPrefs.getString("FullTank", "40.00");
        final EditText inputStartTime = findViewById(R.id.etStartTime);
        final EditText inputFinishTime = findViewById(R.id.etFinishTime);
        final EditText inputFullTank = findViewById(R.id.etFullTank);
        inputStartTime.setText(sStartTime);
        inputFinishTime.setText(sFinishTime);
        inputFullTank.setText(sFullTank);
        final TextView indicaterMessage =findViewById(R.id.tvMessage);

        // 1秒ごとに予定燃料消費量を再計算・表示するデーモンスレッド
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            double dEstLPM = 0;
            double dFullTank;
            TextView indicaterEstFuel = findViewById(R.id.tvEstFuel);
            SimpleDateFormat sdf_HHmm = new SimpleDateFormat("HH:mm");
            SimpleDateFormat sdf_HHmmss = new SimpleDateFormat("HH:mm:ss");
            Date dateSatrtTime = new Date();
            Date dateFinishTime = new Date();
            TextView indicaterTimeNow = findViewById(R.id.tvTimeNow);

            @Override
            public void run() {
                indicaterMessage.setText("");
                // スタート・フィニッシュ時刻を取得
                String sStartTime = inputStartTime.getText().toString();
                String sFinishTime = inputFinishTime.getText().toString();
                try {
                    dateSatrtTime = sdf_HHmm.parse(sStartTime);
                    dateFinishTime = sdf_HHmm.parse(sFinishTime);
                } catch (Exception e) {
                    indicaterMessage.setText(R.string.err_format_HHmm);
                }
                // 満タン容量(L)を取得
                String sFullTank = inputFullTank.getText().toString();
                dFullTank = Double.parseDouble(sFullTank);

                // 現在時刻を表示
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"));// 現在時刻を取得
                indicaterTimeNow.setText(sdf_HHmmss.format(cal.getTime()));

                // 予定燃料消費量を再計算・表示
                long lTimeNowHour = cal.get(Calendar.HOUR_OF_DAY); // GMT + TimeZoneOffset, Today
                long lTimeNowMin = cal.get(Calendar.MINUTE);
                long lTimeNowSec = cal.get(Calendar.SECOND);
                long lTimeNowTZoffset = cal.get(Calendar.ZONE_OFFSET);
                long lTimeNow = (lTimeNowHour * 3600 + lTimeNowMin * 60 + lTimeNowSec) * 1000; // GMT + TimeZoneOffset, 1970/01/01
                long lStartTime = dateSatrtTime.getTime() + lTimeNowTZoffset; // GMT + TimeZoneOffset, 1970/01/01
                long lFinishTime = dateFinishTime.getTime() + lTimeNowTZoffset; // GMT + TimeZoneOffset, 1970/01/01
                dEstLPM = dFullTank / (lFinishTime - lStartTime);
                double dEstFuel = dEstLPM * (lTimeNow - lStartTime);
                indicaterEstFuel.setText(String.format(Locale.JAPAN, "%1$.2f", dEstFuel));
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);

        // スタート時刻をクリックすると入力モードに入る
        inputStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setFocusable(true);
                view.setFocusableInTouchMode(true);
                view.requestFocus();
            }
        });
        // 値を変更したら保存する
        inputStartTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count){
            }
            @Override
            public void afterTextChanged(Editable s) {
                editorPrefs.putString("StartTime",s.toString());
                editorPrefs.apply();
            }
        });

        // フィニッシュ時刻をクリックすると入力モードに入る
        inputFinishTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setFocusable(true);
                view.setFocusableInTouchMode(true);
                view.requestFocus();
            }
        });
        // 値を変更したら保存する
        inputFinishTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count){
            }
            @Override
            public void afterTextChanged(Editable s) {
                editorPrefs.putString("FinishTime",s.toString());
                editorPrefs.apply();
            }
        });

        // 満タン容量(L)をクリックすると入力モードに入る
        inputFullTank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setFocusable(true);
                view.setFocusableInTouchMode(true);
                view.requestFocus();
            }
        });
        // 値を変更したら保存する
        inputFullTank.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count){
            }
            @Override
            public void afterTextChanged(Editable s) {
                editorPrefs.putString("FullTank",s.toString());
                editorPrefs.apply();
            }
        });

    }
}
