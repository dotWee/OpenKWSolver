package de.dotwee.openkwsolver.Tools;

import android.os.AsyncTask;
import android.util.Log;
import android.webkit.URLUtil;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class DownloadContentTask extends AsyncTask<String, Void, String> {
    protected String doInBackground(String... urls) {
        Log.i("DownloadContentTask", "input: " + urls[0]); // log input
        String output = "";

        if (URLUtil.isValidUrl(urls[0])) {
            try {

                HttpClient mHttpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(urls[0]);
                HttpResponse response = mHttpClient.execute(httpGet);
                output = EntityUtils.toString(response.getEntity(), "UTF-8");

            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("DownloadContentTask", "output: " + output); // log output
        } else output = "";
        return output;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }

}