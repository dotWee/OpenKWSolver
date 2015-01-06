package com.dotwee.openkwsolver;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class DownloadContentTask extends AsyncTask<String, Void, String> {
    protected String doInBackground(String... urls) {
        Log.i("DownloadContentTaskInput", urls[0]); // log input
        HttpResponse response = null;
        HttpGet httpGet = null;
        HttpClient mHttpClient = null;
        String output = "";

        try {
            if (mHttpClient == null) {
                mHttpClient = new DefaultHttpClient();
            }

            httpGet = new HttpGet(urls[0]);

            response = mHttpClient.execute(httpGet);
            output = EntityUtils.toString(response.getEntity(), "UTF-8");


        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("DownloadContentTaskOutput", output); // log output
        return output;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }

}