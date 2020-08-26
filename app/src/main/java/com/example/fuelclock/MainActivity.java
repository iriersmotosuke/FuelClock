package com.example.fuelclock;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // 設定値を読み込む
        final SharedPreferences spPrefs = getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editorPrefs = spPrefs.edit();
        String sStartTime = spPrefs.getString("StartTime", "13:00:00");
        String sDuration = spPrefs.getString("Duration", "2:30:00");
        String sFullTank = spPrefs.getString("FullTank", "40.00");
        String sAveLapTime = spPrefs.getString("AveLapTime", "00:01:16");
        final EditText inputStartTime = findViewById(R.id.etStartTime);
        final EditText inputDuration = findViewById(R.id.etDuration);
        final EditText inputFullTank = findViewById(R.id.etFullTank);
        final EditText inputAveLapTime = findViewById(R.id.etAveLapTime);
        inputStartTime.setText(sStartTime);
        inputDuration.setText(sDuration);
        inputFullTank.setText(sFullTank);
        inputAveLapTime.setText(sAveLapTime);

        // Version表示
        TextView tvVersionName = findViewById(R.id.tvVersionName);
        tvVersionName.setText(new StringBuilder().append("v.").append(getPackageAppVersion()));

        // 1秒ごとに予定燃料消費量・残り周回数を再計算・表示するデーモンスレッド
        final Handler hdlrFuelClock = new Handler();
        final Runnable runFuelClock = new Runnable() {
            double dEstLPM = 0;
            double dFullTank;
            double dEstFuel;
            double dLapsToGo;
            TextView indicaterEstFuel = findViewById(R.id.tvEstFuel);
            // SimpleDateFormat sdf_HHmm = new SimpleDateFormat("HH:mm");
            SimpleDateFormat sdf_HHmmss = new SimpleDateFormat("HH:mm:ss");
            Date dateSatrtTime = new Date();
            Date dateDuration = new Date();
            Date dateAveLapTime = new Date();
            TextView indicaterTimeNow = findViewById(R.id.tvTimeNow);
            TextView indicaterLapsToGo = findViewById(R.id.tvLapsToGo);

            @Override
            public void run() {
                // UIからスタート時刻, フィニッシュ時刻, ラップタイム(平均)を取得
                String sStartTime = inputStartTime.getText().toString();
                String sDuration = inputDuration.getText().toString();
                String sAveLapTime = inputAveLapTime.getText().toString();
                try {
                    dateSatrtTime = sdf_HHmmss.parse(sStartTime);
                    dateDuration = sdf_HHmmss.parse(sDuration);
                    dateAveLapTime = sdf_HHmmss.parse(sAveLapTime);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // UIから満タン容量(L)を取得
                String sFullTank = inputFullTank.getText().toString();
                if(sFullTank.matches("(^0|^[1-9]+[0-9]*)\\.?[0-9]*$")) {
                    dFullTank = Double.parseDouble(sFullTank);
                }
                // 現在時刻を取得
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"));
                // 予定燃料消費量・残り周回数を再計算・表示
                long lTimeNowTZoffset = cal.get(Calendar.ZONE_OFFSET);
                long lTimeNow = (cal.get(Calendar.HOUR_OF_DAY) * 3600 + cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.SECOND)) * 1000; // GMT + TimeZoneOffset, 1970/01/01
                long lStartTime = dateSatrtTime.getTime() + lTimeNowTZoffset; // スタート時刻　GMT + TimeZoneOffset, 1970/01/01
                long lDuration = dateDuration.getTime() + lTimeNowTZoffset;// レース時間
                long lAveLapTime = dateAveLapTime.getTime() + lTimeNowTZoffset;// 平均ラップタイム
                if (lTimeNow > lStartTime) {
                    dEstLPM = dFullTank / (lDuration);
                    dEstFuel = dEstLPM * (lTimeNow - lStartTime);
                }else{
                    dEstFuel = 0;
                }
                indicaterEstFuel.setText(String.format(Locale.JAPAN, "%1$.2f", dEstFuel));
                // 残り時間を再計算・表示
                long lTimeToGo;
                if(lTimeNow < lStartTime){
                     lTimeToGo = lDuration;
                }else if (lTimeNow < lStartTime + lDuration){
                     lTimeToGo = lStartTime + lDuration - lTimeNow;
                }else{
                     lTimeToGo = 0;
                }
                indicaterTimeNow.setText(sdf_HHmmss.format(lTimeToGo - lTimeNowTZoffset));
                // 残り周回数(予定)を再計算・表示
                if (lTimeToGo > 0) {
                    dLapsToGo = Math.ceil((double)lTimeToGo / (double)lAveLapTime);
                }else{
                    dLapsToGo = 0;
                }
                indicaterLapsToGo.setText(String.format(Locale.JAPAN, "%1$.0f", dLapsToGo));
                // 1秒間隔
                hdlrFuelClock.postDelayed(this, 1000);
            }
        };
        hdlrFuelClock.post(runFuelClock);

        // 端末アプリでの設定値変更を示すセマフォ
        final Boolean[] semaEtStartTimeChanged = {Boolean.FALSE};
        final Boolean[] semaEtDurationChanged = {Boolean.FALSE};
        // スタート時刻をクリックすると入力モードに入る
        inputStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setFocusable(true);
                view.setFocusableInTouchMode(true);
                view.requestFocus();
                semaEtStartTimeChanged[0] = Boolean.TRUE;
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
                String s2 = spPrefs.getString("StartTime", "00:00:00");
                if(!s1.equals(s2)) {
                    editorPrefs.putString("StartTime", s1);
                    editorPrefs.apply();
                    // semaEtStartTimeChanged[0] = Boolean.TRUE;
                }
            }
        });

        // フィニッシュ時刻をクリックすると入力モードに入る
        inputDuration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setFocusable(true);
                view.setFocusableInTouchMode(true);
                view.requestFocus();
                semaEtDurationChanged[0] = Boolean.TRUE;
            }
        });
        // 値を変更したら保存する
        inputDuration.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count){
            }
            @Override
            public void afterTextChanged(Editable s) {
                String s1 = s.toString();
                String s2 = spPrefs.getString("Duration", "00:00:00");
                if(!s1.equals(s2)){
                    editorPrefs.putString("Duration", s1);
                    editorPrefs.apply();
                    // semaEtDurationChanged[0] = Boolean.TRUE;
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

        // ラップタイム(平均)をクリックすると入力モードに入る
        inputAveLapTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setFocusable(true);
                view.setFocusableInTouchMode(true);
                view.requestFocus();
            }
        });
        // 値を変更したら保存する
        inputAveLapTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count){
            }
            @Override
            public void afterTextChanged(Editable s) {
                String s1 = s.toString();
                String s2 = spPrefs.getString("AveLapTime", "00:00:00");
                if(!s1.equals(s2)){
                    editorPrefs.putString("AveLapTime", s1);
                    editorPrefs.apply();
                }
            }
        });

        // 定期的にGAS Web APIからstart/Race timeを取得
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
                        if(semaEtDurationChanged[0]){
                            sParams = sParams + "&duration="+inputDuration.getText().toString();
                            semaEtDurationChanged[0] = Boolean.FALSE;
                        }
                        // 非同期タスクでwebにアクセス
                        FC_AsyncTask task = new FC_AsyncTask(contextMainActivity);
                        task.execute("https://script.google.com/macros/s/AKfycbyvsoRq0HqbxcX_GXUgJdRclrwiiJ8GHcNMLzeEpMPuBN001Zs/exec?param=getparam"+sParams);
                    }
                });
            }
        },0,60000); //0秒後から60秒間隔で実行

    }//onCreate()

    private String getPackageAppVersion() {

        String version = null;
        try {
            String packageName = getPackageName();
            PackageInfo packageInfo = getPackageManager().getPackageInfo(packageName, 0);
            version = packageInfo.versionName;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return version;
    }
}
