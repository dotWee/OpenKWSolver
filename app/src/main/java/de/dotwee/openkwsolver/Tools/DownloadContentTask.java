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

import android.os.AsyncTask;
import android.util.Log;
import android.webkit.URLUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

class DownloadContentTask extends AsyncTask<String, Void, String> {
	private static final String LOG_TAG = "DownloadContentTask";

	@Override
	protected String doInBackground(String... params) {
		Log.i(LOG_TAG, "doInBackground: INPUT ARRAY / " + Arrays.toString(params)); // log input array
		String output = null;

		try {
			URL mURL = new URL(params[0]);

			// Action to handle requestCaptchaID
			if (params.length == 2) {
				int timeToNextCaptcha = Integer.parseInt(params[1]);
				if (URLUtil.isValidUrl(params[0])) while (output == null) {
					if (timeToNextCaptcha != 0) {
						Log.i(LOG_TAG, "doInBackground: SLEEP /" + timeToNextCaptcha);
						Thread.sleep(250 + timeToNextCaptcha);
					}

					URLConnection connection = mURL.openConnection();
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					output = bufferedReader.readLine();
					bufferedReader.close();

					if (output != null) {
						Log.i(LOG_TAG, "doInBackground: NEW CAPTCHA / " + output);
					} else {
						Log.i(LOG_TAG, "doInBackground: EMPTY CAPTCHA ");
					}
				}
			}

			// Action to handle everything else
			else {
				URLConnection connection = mURL.openConnection();
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