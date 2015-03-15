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

import android.os.AsyncTask;
import android.util.Log;
import android.webkit.URLUtil;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by Lukas on 15.03.2015
 * for project OpenKWSolver.
 */

public class DownloadContentTask extends AsyncTask<String, Void, String> {
	private static final String LOG_TAG = "DownloadContentTask";

	protected String doInBackground(String... params) {
		Log.i(LOG_TAG, "doInBackground: INPUT ARRAY / " + Arrays.toString(params)); // log input array
		HttpClient mHttpClient = new DefaultHttpClient();
		String inURL = params[0];
		String output = "";

		// Action to handle requestCaptchaID
		if (params.length == 2) {
			int timeToNextCaptcha = Integer.parseInt(params[1]);
			if (URLUtil.isValidUrl(params[0])) {
				try {
					while (true) {
						try {
							if (timeToNextCaptcha != 0) {
								Log.i(LOG_TAG, "doInBackground: SLEEP /" + timeToNextCaptcha);
								Thread.sleep(timeToNextCaptcha);
							}
							HttpGet httpGet = new HttpGet(inURL);
							HttpResponse response = mHttpClient.execute(httpGet);
							output = EntityUtils.toString(response.getEntity(), "UTF-8");
							if (!output.equalsIgnoreCase("")) {
								Log.i(LOG_TAG, "doInBackground: NEW CAPTCHA / " + output);
								break;
							} else {
								Log.i(LOG_TAG, "doInBackground: EMPTY CAPTCHA ");
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		// Action to handle everything else
		else {
			if (URLUtil.isValidUrl(inURL)) {
				HttpGet httpGet = new HttpGet(params[0]);
				HttpResponse response = null;

				try {
					response = mHttpClient.execute(httpGet);
					output = EntityUtils.toString(response.getEntity(), "UTF-8");
				} catch (IOException ignored) {
				}
			}
		}

		Log.i(LOG_TAG, "doInBackground: RETURN / " + output);
		return output;
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
	}

}