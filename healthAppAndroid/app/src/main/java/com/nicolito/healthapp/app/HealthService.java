package com.nicolito.healthapp.app;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import android.text.format.Time;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jose on 7/15/14.
 */
public class HealthService extends Service {
    public enum ConnectionState { DISCONNECTED, CONNECTING, CONNECTED }

    private static final String TAG = "HealthService";
    private static boolean sStarted;

    private EHealthListener mEhealthListener;

    public static boolean isRunning() {
        return sStarted;
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!sStarted) {
            mEhealthListener = new EHealthListener();
            mEhealthListener.start();
            sStarted = true;
        } else {
            Toast.makeText(this, "Already listening", Toast.LENGTH_LONG).show();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        if (mEhealthListener != null) {
            mEhealthListener.mDatagramSocket.close();
            if(mEhealthListener.isRunning()) {
                mEhealthListener.stopLoop();
            }
        }

        sStarted = false;

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class EHealthListener extends Thread {
        private DatagramSocket mDatagramSocket;
        private boolean stop = false;

        private int last_bpm = -1;
        private int last_sp02 = -1;

        @Override
        public void run() {
            try {
                Log.i(TAG, "Listening Started");
                mDatagramSocket = new DatagramSocket(12345);
                byte[] buffer = new byte[1000];

                Time now = new Time();

                int last_second = -1;

                while(!stop) {

                    now.setToNow();

                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    mDatagramSocket.receive(packet);

                    byte[] data = packet.getData();
                    //Log.i(TAG, new String(data));
                    parseResult(new String(data));

                    if(last_bpm >= 0 && last_sp02 >= 0 && now.second % 2 == 0 && last_second != now.second) {
                        last_second = now.second;
                        Log.i(TAG,"Sending last data "+ now.second);
                        new MyAsyncTask().execute(last_bpm, last_sp02);
                    }

                }


            } catch (IOException e) {
                //e.printStackTrace();
            }

            Log.i(TAG, "Listening Stopped");
        }

        public boolean isRunning() {
            return !stop;
        }

        public void stopLoop(){
            stop = true;
        }

        private void parseResult(String data) {

            String s = data.split("\n")[0];
            String[] values = s.split("#");
            try {
                if (values.length > 11) {
                    int airflow = Integer.parseInt(values[0]);
                    int ecg = Integer.parseInt(values[1]);
                    int systolic = Integer.parseInt(values[2]);
                    int diastolic = Integer.parseInt(values[3]);
                    int glucose = Integer.parseInt(values[4]);
                    int temperature = Integer.parseInt(values[5]);
                    int bpm = Integer.parseInt(values[6]);
                    int sp02 = Integer.parseInt(values[7]);
                    int conductance = Integer.parseInt(values[8]);
                    int resistance = Integer.parseInt(values[9]);
                    int airflow2 = Integer.parseInt(values[10]);
                    int position = Integer.parseInt(values[11]);

                    Log.i(TAG, "BPM: " + bpm);
                    Log.i(TAG, "SPO2: " + sp02);

                    last_bpm = bpm;
                    last_sp02 = sp02;


                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

        }

        private class MyAsyncTask extends AsyncTask<Integer, Integer, Double> {
            @Override
            protected Double doInBackground(Integer... params) {

                int bpm = params[0];
                int sp02 = params[1];

                HttpClient httpclient = new DefaultHttpClient();
                HttpPost request = new HttpPost("http://healthnode.herokuapp.com/api/measures");
                StringEntity body;

                try {
                    body = new StringEntity("{\"bpm\":" + bpm + ",\"spo2\":" + sp02 + "} ");
                    request.addHeader("content-type", "application/json;charset=UTF-8");
                    request.setEntity(body);
                    HttpResponse response = httpclient.execute(request);

                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");

                    Log.i(TAG, responseString);

                } catch (Exception e) {
                    Log.e(TAG,"Post error",e);
                    Log.e(TAG,e.getMessage());
                }


                return null;
            }

            protected void onPostExecute(Double result){
            }

            protected void onProgressUpdate(Integer... progress){
            }
        }

    }



}
