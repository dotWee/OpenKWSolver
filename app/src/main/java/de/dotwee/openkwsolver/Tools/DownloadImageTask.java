/*
 * Copyright 2015 Lukas "dotwee" Wolfsteiner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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