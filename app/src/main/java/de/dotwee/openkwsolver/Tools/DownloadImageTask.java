/*
 *             DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
 *                     Version 2, December 2004
 *
 *  Copyright (C) 2015 Lukas "dotwee" Wolfsteiner <lukas@wolfsteiner.de>
 *
 *  Everyone is permitted to copy and distribute verbatim or modified
 *  copies of this license document, and changing it is allowed as long
 *  as the name is changed.
 *
 *             DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
 *    TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
 *
 *   0. You just DO WHAT THE FUCK YOU WANT TO.
 *
 */

package de.dotwee.openkwsolver.Tools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
	private static final String LOG_TAG = "DownloadImageTask";
	ImageView bmImage;

    public DownloadImageTask(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    protected Bitmap doInBackground(String... urls) {
	    Log.i(LOG_TAG, "doInBackground: INPUT / " + urls[0]);

        try {
	        InputStream in = new java.net.URL(urls[0]).openStream();
	        return BitmapFactory.decodeStream(in);
        } catch (Exception e) {
	        Log.d(LOG_TAG, "doInBackground: EXCEPTION / " + e);
        }
	    return null;
    }

    protected void onPostExecute(Bitmap result) {
        bmImage.setImageBitmap(result);

	    if (result == null) {
		    Log.w(LOG_TAG, "onPostExecute: Bitmap empty!");
	    }
    }
}