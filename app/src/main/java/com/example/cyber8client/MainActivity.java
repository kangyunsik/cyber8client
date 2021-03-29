package com.example.cyber8client;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    Button join_btn;                 // 회원가입 버튼
    Button login_btn;                // 로그인 버튼

    EditText id_edit;               // id 에디트
    EditText pw_edit;               // pw 에디트

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        id_edit = (EditText)findViewById(R.id.id_tv);
        pw_edit = (EditText)findViewById(R.id.pw_tv);
    }

    public void onClicked(View view) {
        NetworkTask networkTask = new NetworkTask();
        Thread thread = new Thread(networkTask);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void onClick_btn(View v) {
        switch (v.getId()) {
            case R.id.join_btn:     // 회원가입 버튼
                Intent intent = new Intent(MainActivity.this, JoinActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.login_btn:    // 로그인 버튼
                login();
            /*
                Intent intent2 = new Intent(main.this, login.class);
                startActivity(intent2);
                finish();
                break;
                */
        }
    }

    public void login() {
        Log.w("login","로그인 하는중");
        try {
            String id = id_edit.getText().toString();
            String pw = pw_edit.getText().toString();
            Log.w("앱에서 보낸값",id+", "+pw);
            CustomTask task = new CustomTask();
            String result = task.execute(id,pw).get();
            Log.w("받은값",result);

            Intent intent2 = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent2);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("앱에서 보낸값","error");
        }
    }


    class CustomTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        // doInBackground의 매개변수 값이 여러개일 경우를 위해 배열로
        protected String doInBackground(String[] strings) {
            try {
                String str;

                URL url = new URL("http://59.18.147.62:8080/login");  // 어떤 서버에 요청할지(localhost 안됨.)
                // ex) http://123.456.789.10:8080/hello/android
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");                              //데이터를 POST 방식으로 전송합니다.
                conn.setDoOutput(true);

                // 서버에 보낼 값 포함해 요청함.
                OutputStreamWriter osw;
                osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "id=" + strings[0] + "&pw=" + strings[1]; // GET방식으로 작성해 POST로 보냄 ex) "id=admin&pwd=1234";
                osw.write(sendMsg);                           // OutputStreamWriter에 담아 전송
                osw.flush();

                // jsp와 통신이 잘 되고, 서버에서 보낸 값 받음.
                if (conn.getResponseCode() == conn.HTTP_OK) {
                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(tmp);

                    StringBuffer buffer = new StringBuffer();
                    while ((str = reader.readLine()) != null) {
                        buffer.append(str);
                    }
                    receiveMsg = buffer.toString();
                } else {    // 통신이 실패한 이유를 찍기위한 로그
                    Log.i("통신 결과", conn.getResponseCode() + "에러");
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 서버에서 보낸 값을 리턴합니다.
            return receiveMsg;
        }
    }


    public class NetworkTask implements Runnable {       //rest api test
        String result = null;

        public void run() {
            try {
                // Open the connection
                URL url = new URL("http://59.18.147.62:8080/rest2?str=hello");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                InputStream is = conn.getInputStream();

                // Get the stream
                StringBuilder builder = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }

                // Set the result
                result = builder.toString();
                runOnUiThread(new Runnable() {
                    public void run() {
                        TextView tv = findViewById(R.id.tv);
                        tv.setText(result); // result getting test.
                    }
                });

            } catch (IOException e) {
                // Error calling the rest api
                Log.e("REST_API", "GET method failed: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}