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

package de.dotwee.openkwsolver.Fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.dotwee.openkwsolver.R;
import de.dotwee.openkwsolver.Tools.DownloadImageTask;
import de.dotwee.openkwsolver.Tools.StaticHelpers;

public class SolverFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {
	public static final String URL_9WK = "http://www.9kw.eu:80/index.cgi";
	public static final String URL_PARAMETER_CAPTCHA_SHOW = "?action=usercaptchashow";
	public static final String URL_PARAMETER_SOURCE = "&source=androidopenkws";
	private static final String LOG_TAG = "SolverFragment";

	// main widgets
	public static TextView textViewCaptchaDesc, textViewCaptcha, textViewBalance;
	public static Button buttonPull, buttonSkip, buttonSend;
	public static ImageView imageViewCaptcha;
	private CountDownTimer countDownTimer;
	private SharedPreferences prefs;
	private Boolean isCurrentCaptcha;
	private EditText editTextAnswer;
	private ProgressBar progressBar;
	private Vibrator vibrator;
	private Thread BalanceUpdate;
	private Context baseContext;
	private View view;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		baseContext = getActivity().getBaseContext();
		return inflater.inflate(R.layout.fragment_solver, container, false);
	}

	@Override
	public void onViewCreated(final View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		this.view = view;

		// declare main widgets and services
		initWidgets();

		// start showing balance if network and apikey is available
		if (StaticHelpers.isNetworkAvailable(baseContext)) {
			if (StaticHelpers.getApiKey(baseContext) != null) {
				if (StaticHelpers.isAutoBalanceEnabled(baseContext)) {
					balanceThread();
				} else {
					Toast.makeText(baseContext, "Set a API-Key to start", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(baseContext, "No network available!", Toast.LENGTH_SHORT).show();
			}
		}

		buttonPull.setOnClickListener(this);
		buttonPull.setOnLongClickListener(this);

		buttonSend.setOnClickListener(this);
		buttonSend.setOnLongClickListener(this);

		buttonSkip.setOnClickListener(this);
		buttonSkip.setOnLongClickListener(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (BalanceUpdate != null) {
			if (BalanceUpdate.isAlive()) {
				BalanceUpdate.interrupt();
			}
		}
	}

	private void initWidgets() {
		buttonPull = (Button) view.findViewById(R.id.buttonPull);
		buttonSkip = (Button) view.findViewById(R.id.buttonSkip);
		buttonSkip.setEnabled(false);

		buttonSend = (Button) view.findViewById(R.id.buttonSend);
		buttonSend.setEnabled(false);

		vibrator = (Vibrator) baseContext.getSystemService(Context.VIBRATOR_SERVICE);
		imageViewCaptcha = (ImageView) view.findViewById(R.id.imageViewCaptcha);
		textViewCaptchaDesc = (TextView) view.findViewById(R.id.textViewDescID);
		textViewCaptcha = (TextView) view.findViewById(R.id.textViewID);
		textViewBalance = (TextView) view.findViewById(R.id.textViewBA);

		editTextAnswer = (EditText) view.findViewById(R.id.editTextAnswer);
		progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

		// hide captchaid textviews if disabled
		if (!StaticHelpers.isCaptchaIDEnabled(baseContext)) {
			textViewCaptchaDesc.setVisibility(View.GONE);
			textViewCaptcha.setVisibility(View.GONE);
		}

		// fix edittext width
		editTextAnswer.setMaxWidth(editTextAnswer.getWidth());
	}

	private void resetWidgets() {
		imageViewCaptcha.setImageDrawable(null);
		editTextAnswer.setText(null);
		progressBar.setProgress(0);
		countDownTimer.cancel();
	}

	// Pull Captcha picture and display it
	public boolean pullCaptchaPicture(String CaptchaID) {
		String CaptchaPictureURL = (URL_9WK + URL_PARAMETER_CAPTCHA_SHOW + URL_PARAMETER_SOURCE + StaticHelpers.getExternalParameter(baseContext, 2) + "&id=" + CaptchaID + StaticHelpers.getApiKey(baseContext));

		Log.i("pullCaptchaPicture", "URL: " + CaptchaPictureURL);
		if (view != null) {
			try {
				Bitmap returnBit = new DownloadImageTask(imageViewCaptcha).execute(CaptchaPictureURL).get(5000, TimeUnit.MILLISECONDS);
				if (returnBit != null) {
					return true; // true = new image
				}
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				e.printStackTrace();
			}
		}

		return false;
	}

	public void updateBalance() {
		textViewBalance.setText(StaticHelpers.getBalance(baseContext));
	}

	// BalanceThread: Update the balance every 5 seconds
	public void balanceThread() {
		BalanceUpdate = new Thread() {

			@Override
			public void run() {
				while (!isInterrupted()) {
					try {
						Thread.sleep(5000); // 5000ms = 5s
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (view != null) {
								textViewBalance.setText(StaticHelpers.getBalance(getActivity()));
							}
						}
					});
				}

			}
		};

		// check if thread isn't already running.
		if (BalanceUpdate.isAlive()) {
			BalanceUpdate.interrupt();
		}

		// if not, start it
		else {
			BalanceUpdate.start();
		}
	}

	public boolean captchaInWork() {
		return isCurrentCaptcha;
	}

	public void notifyUser() {
		if (StaticHelpers.isVibrateEnabled(baseContext)) {
			vibrator.vibrate(500);
		}
	}

	@Override
	public void onClick(View v) {
		int viewId = v.getId();
		String CaptchaID = null;

		if (viewId == R.id.buttonPull) {
			if (StaticHelpers.getApiKey(baseContext) != null) {
				buttonSend.setEnabled(true);
				buttonSkip.setEnabled(true);

				Log.i(LOG_TAG, "onClickPull: Click recognized");
				if (StaticHelpers.isNetworkAvailable(baseContext)) {
					updateBalance();
					CaptchaID = StaticHelpers.requestCaptchaID(baseContext, // needed Context
							prefs.getBoolean("pref_automation_loop", false), // Loop: false / true
							2); // 2 = Normal
					textViewCaptcha.setText(CaptchaID);

					// request captcha image
					isCurrentCaptcha = pullCaptchaPicture(CaptchaID);
					if (CaptchaID != null) {
						notifyUser();
					}
					buttonPull.setEnabled(false);

					final int[] i = {0};
					countDownTimer = new CountDownTimer(30000, 1000) {
						@Override
						public void onTick(long millisUntilFinished) {
							i[0]++;
							progressBar.setProgress(i[0]);
						}

						@Override
						public void onFinish() {
							// todo global
							// StaticHelpers.skipCaptchaByID(baseContext, CaptchaID);
						}
					};

					if (isCurrentCaptcha) {
						countDownTimer.start();
					} else {
						buttonSkip.performClick();
					}
				} else {
					Toast.makeText(baseContext, "No network available!", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(baseContext, "Set an API-Key first!", Toast.LENGTH_LONG).show();
			}
		} else if (viewId == R.id.buttonSend) {
			String answer = getCaptchaAnswer();
			if (!answer.equalsIgnoreCase("")) {
				StaticHelpers.sendCaptchaByID(baseContext, CaptchaID, answer, false);
				resetWidgets();
				if (StaticHelpers.isLoopEnabled(baseContext)) {
					Log.i("OnClickSend", "Loop-Mode");
					buttonPull.performClick();
				} else {
					buttonPull.setEnabled(true);
				}
			} else {
				Toast.makeText(baseContext, R.string.main_toast_emptyanswer, Toast.LENGTH_LONG).show();
			}
		} else if (viewId == R.id.buttonSkip) {
			StaticHelpers.skipCaptchaByID(baseContext, StaticHelpers.getApiKey(baseContext));
			buttonPull.setEnabled(true);
			resetWidgets();
		}
	}

	private String getCaptchaAnswer() {
		String tmp = editTextAnswer.getText().toString();
		editTextAnswer.setText(null);
		return tmp;
	}

	private void startTimer() {

	}

	@Override
	public boolean onLongClick(View v) {
		onClick(v);
		return false;
	}
}
