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

import android.content.Context;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class StaticHelpers {
	public static final String URL_9WK = "http://www.9kw.eu:80/index.cgi";
	public static final String URL_PARAMETER_NOCAPTCHA = "&nocaptcha=1";
	public static final String URL_PARAMETER_CAPTCHA_NEW = "?action=usercaptchanew";
	public static final String URL_PARAMETER_SERVER_BALANCE = "?action=usercaptchaguthaben";
	public static final String URL_PARAMETER_SOURCE = "&source=openkwsolver";
	public static final String URL_PARAMETER_CAPTCHA_SKIP = "?action=usercaptchaskip";
	public static final String URL_PARAMETER_CAPTCHA_ANSWER = "?action=usercaptchacorrect";
	private static final String LOG_TAG = "StaticHelper";

	public static String requestCaptchaID(Context context, Boolean LOOP, int TYPE) {
		String CAPTCHA_URL = (URL_9WK + URL_PARAMETER_CAPTCHA_NEW + getApiKey(context) +
				URL_PARAMETER_SOURCE + getExternalParameter(context, TYPE) + URL_PARAMETER_NOCAPTCHA);

		Log.i(LOG_TAG, "requestCaptchaID: CAPTCHA_URL / " + CAPTCHA_URL);
		String CAPTCHA_ID = "";
		if (LOOP) {
			try {
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
				String timeToNextCaptcha = prefs.getString("pref_automation_loop_time", "0");
				Log.i(LOG_TAG, "requestCaptchaID with loop: CAPTCHA_TIME / " + timeToNextCaptcha);
				CAPTCHA_ID = new DownloadContentTask().execute(CAPTCHA_URL, timeToNextCaptcha).get(3000 + Integer.parseInt(timeToNextCaptcha), TimeUnit.MILLISECONDS);
				Log.i(LOG_TAG, "requestCaptchaID with loop: RETURN / " + CAPTCHA_ID);
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			} catch (TimeoutException e) {
				e.printStackTrace();
				Log.w(LOG_TAG, "requestCaptchaID: Timeout!");
			}
		} else {
			try {
				CAPTCHA_ID = new DownloadContentTask().execute(CAPTCHA_URL).get(3000, TimeUnit.MILLISECONDS);
				Log.i(LOG_TAG, "requestCaptchaID: RETURN / " + CAPTCHA_ID);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				Log.w(LOG_TAG, "requestCaptchaID: EXCEPTION / " + e);
				e.printStackTrace();
			}
		}

		if (isSoundEnabled(context)) {
			RingtoneManager.getRingtone(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).play();
		}

		return CAPTCHA_ID;
	}

	public static void skipCaptchaByID(Context context, String CAPTCHA_ID) {
		String CAPTCHA_URL = (URL_9WK + URL_PARAMETER_CAPTCHA_SKIP + "&id=" + CAPTCHA_ID +
				getApiKey(context) + URL_PARAMETER_SOURCE);
		Log.i(LOG_TAG, "skipCaptchaByID: URL / " + CAPTCHA_URL);

		try {
			String res = new DownloadContentTask().execute(CAPTCHA_URL).get(3000, TimeUnit.MILLISECONDS);
			Log.i(LOG_TAG, "skipCaptchaByID: RETURN / " + res);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			Log.w(LOG_TAG, "skipCaptchaByID: EXCEPTION / " + e);
			e.printStackTrace();
		}
	}

	public static void sendCaptchaByID(Context context, String CAPTCHA_ID, String CAPTCHA_ANSWER, Boolean CONFIRM) {
		String CaptchaURL;

		if (CONFIRM) {
			CaptchaURL = (URL_9WK + URL_PARAMETER_CAPTCHA_ANSWER +
					URL_PARAMETER_SOURCE + getExternalParameter(context, 0) + CAPTCHA_ANSWER +
					"&id=" + CAPTCHA_ID + getApiKey(context));

		} else {
			CaptchaURL = (URL_9WK + URL_PARAMETER_CAPTCHA_ANSWER +
					URL_PARAMETER_SOURCE + getExternalParameter(context, 2) + "&antwort=" +
					CAPTCHA_ANSWER + "&id=" + CAPTCHA_ID + getApiKey(context));
		}

		Log.i(LOG_TAG, "sendCaptchaByID: URL / " + CaptchaURL);

		try {
			String s = new DownloadContentTask().execute(CaptchaURL.replaceAll(" ", "%20")).get(3000, TimeUnit.MILLISECONDS);
			if (s != null) {
				Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
				Log.i(LOG_TAG, "sendCaptchaByID: RETURN / " + s);
			}
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			Log.i(LOG_TAG, "sendCaptchaByID: EXCEPTION / " + e);
			e.printStackTrace();
		}
	}

	public static String getApiKey(Context context) {
		SharedPreferences pref_apikey = PreferenceManager
				.getDefaultSharedPreferences(context);
		String apikey = pref_apikey.getString("pref_api_key", null);
		Log.i(LOG_TAG, "getApiKey: " + apikey);
		if (apikey != null) {
			return "&apikey=" + apikey;
		} else {
			return null;
		}
	}

	public static String getExternalParameter(Context context, int type) {
		String s = "", d = "", t = "";
		String CONFIRM = "&confirm=";
		String CLICK = "&mouse=";

		Log.i(LOG_TAG, "getExteranalParameter: TYPE / " + type);

		switch (type) {
			case 0: // confirm
				t = CONFIRM + "1" + CLICK + "0";
				break;

			case 1: // Todo click
				t = CONFIRM + "0" + CLICK + "1";
				break;

			case 2: // none
				t = CONFIRM + "0" + CLICK + "0";
				break;
		}

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		Boolean prefSelfonly = prefs.getBoolean("pref_api_selfonly", false);
		Boolean prefDebug = prefs.getBoolean("pref_api_debug", false);
		if (prefSelfonly) {
			s = "&selfonly=1";
		}
		if (prefDebug) {
			d = "&debug=1";
		}
		Log.i(LOG_TAG, "getExternalParameter: RETURN / " + s + d + t);
		return s + d + t;
	}

	public static String getBalance(Context context) {
		String URL_BALANCE = (URL_9WK + URL_PARAMETER_SERVER_BALANCE +
				URL_PARAMETER_SOURCE + getApiKey(context));
		Log.i(LOG_TAG, "getBalance: URL / " + URL_BALANCE);

		try {
			String s = new DownloadContentTask().execute(URL_BALANCE).get(3000, TimeUnit.MILLISECONDS);
			if (s.contains("html")) {
				return "Return-Error";
			}
			Log.i(LOG_TAG, "getBalance: RETURN / " + s);
			return s;
		} catch (InterruptedException | ExecutionException ignored) {
			Log.w(LOG_TAG, "getBalance: EXCEPTION / " + ignored);
		} catch (TimeoutException e) {
			Log.w(LOG_TAG, "getBalance: TIMEOUT / " + e);
			e.printStackTrace();
		}

		return "";
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivityManager
				= (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		Boolean b = activeNetworkInfo != null && activeNetworkInfo.isConnected();
		Log.i(LOG_TAG, "isNetworkAvailable: RETURN / " + b);
		return b;
	}

	public static boolean isVibrateEnabled(Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		Boolean b = prefs.getBoolean("pref_notification_vibrate", true);
		Log.i(LOG_TAG, "isVibrateEnabled: RETURN / " + b);
		return b;
	}

	public static boolean isLoopEnabled(Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		Boolean b = prefs.getBoolean("pref_automation_loop", true);
		Log.i(LOG_TAG, "isLoopEnabled: RETURN / " + b);
		return b;
	}

	public static boolean isAutoBalanceEnabled(Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		Boolean b = prefs.getBoolean("pref_automation_balance", false);
		Log.i(LOG_TAG, "isAutoBalanceEnabled: RETURN / " + b);
		return b;
	}

	public static boolean isSoundEnabled(Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		Boolean b = prefs.getBoolean("pref_notification_sound", true);
		Log.i(LOG_TAG, "isSoundEnabled: RETURN / " + b);
		return b;
	}

	public static boolean isCaptchaIDEnabled(Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		Boolean b = prefs.getBoolean("pref_layout_captchaid", false);
		Log.i(LOG_TAG, "isCaptchaIDEnabled: RETURN / " + b);
		return b;
	}
}
