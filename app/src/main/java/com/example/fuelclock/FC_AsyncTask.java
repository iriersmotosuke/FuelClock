package com.example.fuelclock;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import com.example.fuelclock.MainActivity;
import com.example.fuelclock.R;


public class FC_AsyncTask extends AsyncTask<String, Void, String> {

    // UI スレッドから操作するビュー
    private TextView textView;

    public FC_AsyncTask(Context context) {
        // 本メソッドは UI スレッドで処理されます。
        super();
        MainActivity mainActivity = (MainActivity)context;
        textView = (TextView)mainActivity.findViewById(R.id.tvMessage);
    }

    @Override
    protected String doInBackground(String... params) {

        // 本メソッドは background 用のスレッドで処理されます。
        // そのため、UI のビューを操作してはいけません。

        // Java の文字列連結では StringBuilder を利用します。
        // https://www.qoosky.io/techs/05a157a3e0
        StringBuilder sb = new StringBuilder();

        // finally 内で利用するため try の前に宣言します。
        InputStream inputStream = null;
        HttpsURLConnection connection = null;

        try {
            // URL 文字列をセットします。
            URL url = new URL(params[0]);
            connection = (HttpsURLConnection)url.openConnection();
            connection.setConnectTimeout(3000); // タイムアウト 3 秒
            connection.setReadTimeout(3000);

            // GET リクエストの実行
            connection.setRequestMethod("GET");
            connection.connect();

            // レスポンスコードの確認します。
            int responseCode = connection.getResponseCode();
            if(responseCode != HttpsURLConnection.HTTP_OK) {
                throw new IOException("HTTP responseCode: " + responseCode);
            }

            // 文字列化します。
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
        // 本メソッドは UI スレッドで処理されるため、ビューを操作できます。
        textView.setText(result);
    }
}