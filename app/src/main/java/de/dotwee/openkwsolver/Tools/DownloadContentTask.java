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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

/**
 * Created by Lukas on 15.03.2015
 * for project OpenKWSolver.
 */

public class DownloadContentTask extends AsyncTask<String, Void, String> {
	private static final String LOG_TAG = "DownloadContentTask";

	@Override
	protected String doInBackground(String... params) {
		Log.i(LOG_TAG, "doInBackground: INPUT ARRAY / " + Arrays.toString(params)); // log input array
		String output = "";

		try {
			URL inURL = new URL(params[0]);

			// Action to handle requestCaptchaID
			if (params.length == 2) {
				int timeToNextCaptcha = Integer.parseInt(params[1]);
				if (URLUtil.isValidUrl(params[0])) {
					while (true) {
						if (timeToNextCaptcha != 0) {
							Log.i(LOG_TAG, "doInBackground: SLEEP /" + timeToNextCaptcha);
							Thread.sleep(250 + timeToNextCaptcha);
						}

						URLConnection connection = inURL.openConnection();
						BufferedReader bufferedReader = new BufferedReader(
								new InputStreamReader(connection.getInputStream()));
						output = bufferedReader.readLine();
						bufferedReader.close();

						if (output != null) {
							Log.i(LOG_TAG, "doInBackground: NEW CAPTCHA / " + output);
							break;
						} else {
							Log.i(LOG_TAG, "doInBackground: EMPTY CAPTCHA ");
						}
					}
				}
			}

			// Action to handle everything else
			else {
				URLConnection connection = inURL.openConnection();
				BufferedReader bufferedReader = new BufferedReader(
						new InputStreamReader(connection.getInputStream()));
				output = bufferedReader.readLine();
				bufferedReader.close();
			}

		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}


		Log.i(LOG_TAG, "doInBackground: RETURN / " + output);
		return output;
	}
}