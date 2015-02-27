package de.dotwee.openkwsolver.Tools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    String LOG_TAG = "DownloadImageTask";
    ImageView bmImage;

    public DownloadImageTask(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Log.d(LOG_TAG, "input URL: " + urldisplay);

        Bitmap captchaBitmap = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            captchaBitmap = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return captchaBitmap;
    }

    protected void onPostExecute(Bitmap result) {
        bmImage.setImageBitmap(result);

        if (result == null) Log.i(LOG_TAG, "Bitmap empty!");
        // else Log.i(LOG_TAG, "Captcha Bitmap: " + bmImage);
    }
}