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

    // UI スレッドから操作するビュー
    private TextView indicaterMessage;
    private EditText inputStartTime;
    private EditText inputFinishTime;

    private CallBackTask callBackTask;

    public FC_AsyncTask(Context context) {
        // 本メソッドは UI スレッドで処理されます。
        super();
        MainActivity mainActivity = (MainActivity)context;
        indicaterMessage = (TextView)mainActivity.findViewById(R.id.tvMessage);
        inputStartTime = (EditText) mainActivity.findViewById(R.id.etStartTime);
        inputFinishTime = (EditText) mainActivity.findViewById(R.id.etFinishTime);
    }

    @Override
    protected String doInBackground(String... params) {
        // background処理スレッド：UIビューは操作できない。

        // Java の文字列連結では StringBuilder を利用。
        // https://www.qoosky.io/techs/05a157a3e0
        StringBuilder sb = new StringBuilder();

        // finally 内で利用するため try の前に宣言しておく。
        InputStream inputStream = null;
        HttpsURLConnection connection = null;

        try {
            // URL 文字列をセット。
            URL url = new URL(params[0]);
            connection = (HttpsURLConnection)url.openConnection();
            connection.setConnectTimeout(3000); // タイムアウト 3 秒
            connection.setReadTimeout(3000);

            // GET リクエストの実行
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
    protected void onPostExecute(String result) {
        // UI スレッドで処理される。ビューを操作できる。
        // indicaterMessage.setText(result);
        String sStartTime = "";
        String sFinishTime =  "";

        // JSONをパースする
        try {
            JSONObject json = new JSONObject(result.toString());
            sStartTime = json.getString("start");
            sFinishTime = json.getString("finish");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // 取得したstart/finish timeをセットする
        if(sStartTime != "" && sFinishTime != "") {
            inputStartTime.setText(sStartTime);
            inputFinishTime.setText(sFinishTime);
        }
        /*
        try {
            Thread.sleep(3600000);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
         */
        callBackTask.CallBack();
    }

    public void setOnCallBack(CallBackTask t_object)
    {
        callBackTask = t_object;
    }

    // コールバック用のインターフェース定義
    interface CallBackTask
    {
        void CallBack();
    }
}