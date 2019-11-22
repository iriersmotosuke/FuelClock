package com.example.fuelclock;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.EditText;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import com.example.fuelclock.MainActivity;
import com.example.fuelclock.R;
import org.json.JSONException;
import org.json.JSONObject;

public class FC_AsyncTask extends AsyncTask<String, Void, String> {
    // private TextView indicaterMessage;
    private EditText inputStartTime;
    private EditText inputFinishTime;
    private TextView indicaterMessage;


    public FC_AsyncTask(Context context) { // コンストラクタ：UI スレッドで処理
        super();
        MainActivity mainActivity = (MainActivity)context;
        // indicaterMessage = (TextView)mainActivity.findViewById(R.id.tvMessage);
        inputStartTime = (EditText) mainActivity.findViewById(R.id.etStartTime);
        inputFinishTime = (EditText) mainActivity.findViewById(R.id.etFinishTime);
        indicaterMessage = (TextView) mainActivity.findViewById(R.id.tvMessage);
    }

    @Override
    protected String doInBackground(String... params) { // background処理スレッド：UIビューは操作できない
        StringBuilder sb = new StringBuilder(); // 文字列操作用
        InputStream inputStream = null; // finally 内で利用するため try の前に宣言しておく
        HttpsURLConnection connection = null;
        try { // HTTP GET リクエスト送信
            URL url = new URL(params[0]);
            connection = (HttpsURLConnection)url.openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.setRequestMethod("GET");
            connection.connect();
            // レスポンスコードの確認
            int responseCode = connection.getResponseCode();
            if(responseCode != HttpsURLConnection.HTTP_OK) {
                throw new IOException("HTTP responseCode: " + responseCode);
            }
            // input streamを文字列化
            inputStream = connection.getInputStream();
            if(inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(connection != null) {
                connection.disconnect();
            }
        }
        return sb.toString();
    }

    @Override
    protected void onPostExecute(String result) { // ポスト処理：UI スレッドで実行される
        String sStartTime = "";
        String sFinishTime =  "";
        String sMessage = "";

        try { // JSONのパース
            JSONObject json = new JSONObject(result.toString());
            sStartTime = json.getString("start");
            sFinishTime = json.getString("finish");
            sMessage = json.getString("message");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // 取得したstart/finish timeを画面の入力ボックスにセットする
        if(sStartTime != "" && sFinishTime != "") {
            inputStartTime.setText(sStartTime);
            inputFinishTime.setText(sFinishTime);
            indicaterMessage.setText(sMessage);
        }
    }
}