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
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity { // implements FC_AsyncTask.CallBackTask {

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
        //final TextView indicaterMessage =findViewById(R.id.tvMessage);

        // 1秒ごとに予定燃料消費量を再計算・表示するデーモンスレッド
        final Handler hdlrFuelClock = new Handler();
        final Runnable runFuelClock = new Runnable() {
            double dEstLPM = 0;
            double dFullTank;
            double dEstFuel;
            TextView indicaterEstFuel = findViewById(R.id.tvEstFuel);
            SimpleDateFormat sdf_HHmm = new SimpleDateFormat("HH:mm");
            SimpleDateFormat sdf_HHmmss = new SimpleDateFormat("HH:mm:ss");
            Date dateSatrtTime = new Date();
            Date dateFinishTime = new Date();
            TextView indicaterTimeNow = findViewById(R.id.tvTimeNow);

            @Override
            public void run() {
                // UIからstart/finish timeを取得
                String sStartTime = inputStartTime.getText().toString();
                String sFinishTime = inputFinishTime.getText().toString();
                try {
                    dateSatrtTime = sdf_HHmm.parse(sStartTime);
                    dateFinishTime = sdf_HHmm.parse(sFinishTime);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // UIから満タン容量(L)を取得
                String sFullTank = inputFullTank.getText().toString();
                dFullTank = Double.parseDouble(sFullTank);
                // 現在時刻を表示
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"));
                indicaterTimeNow.setText(sdf_HHmmss.format(cal.getTime()));
                // 予定燃料消費量を再計算・表示
                long lTimeNowTZoffset = cal.get(Calendar.ZONE_OFFSET);
                long lTimeNow = (cal.get(Calendar.HOUR_OF_DAY) * 3600 + cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.SECOND)) * 1000; // GMT + TimeZoneOffset, 1970/01/01
                long lStartTime = dateSatrtTime.getTime() + lTimeNowTZoffset; // GMT + TimeZoneOffset, 1970/01/01
                long lFinishTime = dateFinishTime.getTime() + lTimeNowTZoffset; // GMT + TimeZoneOffset, 1970/01/01
                if (lTimeNow > lStartTime) {
                    dEstLPM = dFullTank / (lFinishTime - lStartTime);
                    dEstFuel = dEstLPM * (lTimeNow - lStartTime);
                }else{
                    dEstFuel = 0;
                }
                indicaterEstFuel.setText(String.format(Locale.JAPAN, "%1$.2f", dEstFuel));
                hdlrFuelClock.postDelayed(this, 1000);
            }
        };
        hdlrFuelClock.post(runFuelClock);

        // 端末アプリでの設定値変更を示すセマフォ
        final Boolean[] semaEtStartTimeChanged = {Boolean.FALSE};
        final Boolean[] semaEtFinishTimeChanged = {Boolean.FALSE};
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
                String s1 = s.toString();
                String s2 = spPrefs.getString("StartTime", "00:00");
                if(!s1.equals(s2)) {
                    editorPrefs.putString("StartTime", s1);
                    editorPrefs.apply();
                    semaEtStartTimeChanged[0] = Boolean.TRUE;
                }
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
                String s1 = s.toString();
                String s2 = spPrefs.getString("FinishTime", "00:00");
                if(!s1.equals(s2)){
                    editorPrefs.putString("FinishTime", s1);
                    editorPrefs.apply();
                    semaEtFinishTimeChanged[0] = Boolean.TRUE;
                }
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

        // 定期的にGAS Web APIからstart/finish timeを取得
        final Handler handlerTimerTask = new Handler();
        final Context contextMainActivity = this; // 非同期スレッドからのUIアクセス用

        Timer timerWebAccess = new Timer(true);
        timerWebAccess.schedule(new TimerTask(){
            @Override
            public void run() {
                handlerTimerTask.post( new Runnable() {
                    public void run() {
                        // 端末アプリでスタート／フィニッシュ時刻が変更されたらWebにも反映する
                        String sParams = "";
                        if(semaEtStartTimeChanged[0]){
                            sParams = sParams + "&statime="+inputStartTime.getText().toString();
                            semaEtStartTimeChanged[0] = Boolean.FALSE;
                        }
                        if(semaEtFinishTimeChanged[0]){
                            sParams = sParams + "&fintime="+inputFinishTime.getText().toString();
                            semaEtFinishTimeChanged[0] = Boolean.FALSE;
                        }
                        // 非同期タスクでwebにアクセス
                        FC_AsyncTask task = new FC_AsyncTask(contextMainActivity);
                        task.execute("https://script.google.com/macros/s/AKfycbyvsoRq0HqbxcX_GXUgJdRclrwiiJ8GHcNMLzeEpMPuBN001Zs/exec?param=getparam"+sParams);
                    }
                });
            }
        },0,60000); //0秒後から60秒間隔で実行

    }//onCreate()
}
