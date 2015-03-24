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

public class SolverFragment extends Fragment {
    public static final String URL_9WK = "http://www.9kw.eu:80/index.cgi";
    public static final String URL_PARAMETER_CAPTCHA_SHOW = "?action=usercaptchashow";
    public static final String URL_PARAMETER_SOURCE = "&source=androidopenkws";
	private static final String LOG_TAG = "SolverFragment";

	// main widgets
	public static TextView textViewCaptchaDesc;
	public static TextView textViewCaptcha;
	public static ImageView imageViewCaptcha;
	public static Button buttonPull;
	public static Button buttonSkip;
	public static Button buttonSend;
	private Boolean CURRENT_CAPTCHA;
	private TextView textViewBalance;
	private EditText editTextAnswer;
	private ProgressBar progressBar;
	private Vibrator vibrator;
	private Thread BalanceUpdate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_solver, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
	    Log.i(LOG_TAG, "onViewCreated");

        // init prefs
	    final SharedPreferences prefs = PreferenceManager
			    .getDefaultSharedPreferences(getActivity());

        // declare main widgets
	    buttonPull = (Button) view.findViewById(R.id.buttonPull);
	    buttonSkip = (Button) view.findViewById(R.id.buttonSkip);
	    buttonSend = (Button) view.findViewById(R.id.buttonSend);

	    textViewCaptchaDesc = (TextView) view.findViewById(R.id.textViewDescID);
	    textViewCaptcha = (TextView) view.findViewById(R.id.textViewID);

	    imageViewCaptcha = (ImageView) view.findViewById(R.id.imageViewCaptcha);
	    imageViewCaptcha.getLayoutParams().height = Integer.parseInt(prefs.getString("pref_layout_size", "200"));

	    editTextAnswer = (EditText) view.findViewById(R.id.editTextAnswer);
	    progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

	    // hide captchaid textviews if disabled
	    if (!StaticHelpers.isCaptchaIDEnabled(getActivity())) {
		    textViewCaptchaDesc.setVisibility(View.GONE);
	    }
	    textViewCaptcha.setVisibility(View.GONE);

        // fix edittext width
        editTextAnswer.setMaxWidth(editTextAnswer.getWidth());

        // start showing balance if network and apikey is available
	    if (StaticHelpers.isNetworkAvailable(getActivity())) {
		    if (StaticHelpers.getApiKey(getActivity()) != null) {
			    if (StaticHelpers.isAutoBalanceEnabled(getActivity())) {
				    Log.i(LOG_TAG, "onCreated: start balance thread");
				    balanceThread();
			    }
		    } else {
			    Toast.makeText(getActivity(), "Set a API-Key to start", Toast.LENGTH_SHORT).show();
		    }
	    } else {
		    Log.w(LOG_TAG, "No network available");
		    Toast.makeText(getActivity(), "No network available!", Toast.LENGTH_SHORT).show();
	    }

	    // is no api key is set, disable the start button
	    if (StaticHelpers.getApiKey(getActivity()) == null) {
		    if (getView() != null) {
			    buttonSkip.setEnabled(false);
			    buttonSend.setEnabled(false);
			    buttonPull.setEnabled(false);
		    }
	    }

        buttonPull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
	            Log.i(LOG_TAG, "onClickPull: Click recognized");
	            if (StaticHelpers.isNetworkAvailable(getActivity())) {
		            updateBalance();
		            final String CaptchaID = StaticHelpers.requestCaptchaID(getActivity(), // needed Context
				            prefs.getBoolean("pref_automation_loop", false), // Loop: false / true
				            2); // 2 = Normal
		            textViewCaptcha.setText(CaptchaID);

		            // request captcha image
		            CURRENT_CAPTCHA = pullCaptchaPicture(CaptchaID);
		            buttonPull.setEnabled(false);

		            final int[] i = {0};
		            final CountDownTimer CountDownTimer;
		            CountDownTimer = new CountDownTimer(30000, 1000) {

			            @Override
			            public void onTick(long millisUntilFinished) {
				            i[0]++;
				            progressBar.setProgress(i[0]);
			            }

			            @Override
			            public void onFinish() {
				            StaticHelpers.skipCaptchaByID(getActivity(), CaptchaID);
			            }
		            };

		            buttonSkip = (Button) view.findViewById(R.id.buttonSkip);
		            buttonSkip.setOnClickListener(new View.OnClickListener() {
			            @Override
			            public void onClick(View v1) {
				            Log.i("OnClickSkip", "Click recognized");
				            editTextAnswer.setText(null);
				            StaticHelpers.skipCaptchaByID(
						            getActivity(), StaticHelpers.getApiKey(getActivity()));

				            CountDownTimer.cancel();
				            progressBar.setProgress(0);

				            imageViewCaptcha.setImageDrawable(null);

				            buttonPull.setEnabled(true);
			            }
		            });

		            if (CURRENT_CAPTCHA == true) {


			            vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
			            if (StaticHelpers.isVibrateEnabled(getActivity())) {
				            vibrator.vibrate(500);
			            }

			            CountDownTimer.start();
			            buttonSend = (Button) view.findViewById(R.id.buttonSend);
			            buttonSend.setOnClickListener(new View.OnClickListener() {
				            @Override
				            public void onClick(View v1) {
					            String CaptchaAnswer = editTextAnswer.getText().toString();
					            if (!CaptchaAnswer.equalsIgnoreCase("")) {
						            StaticHelpers.sendCaptchaByID(getActivity(), CaptchaID, CaptchaAnswer, false);

						            CountDownTimer.cancel();
						            Log.i("OnClickSend", "Timer killed");
						            progressBar.setProgress(0);

						            imageViewCaptcha.setImageDrawable(null);
						            editTextAnswer.setText(null);

						            if (StaticHelpers.isLoopEnabled(getActivity())) {
							            Log.i("OnClickSend", "Loop-Mode");
							            buttonPull.performClick();
						            } else {
							            buttonPull.setEnabled(true);
						            }
					            } else {
						            Toast.makeText(getActivity(),
								            R.string.main_toast_emptyanswer, Toast.LENGTH_LONG).show();
					            }
				            }
			            });
		            } else {
			            buttonSkip.performClick();
		            }

	            } else {
		            Log.w(LOG_TAG, "onClickPull: Click without network");
		            Toast.makeText(getActivity(), "No network available!", Toast.LENGTH_SHORT).show();
	            }
            }
        });
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

	// Pull Captcha picture and display it
	public boolean pullCaptchaPicture(String CaptchaID) {
        String CaptchaPictureURL = (URL_9WK + URL_PARAMETER_CAPTCHA_SHOW +
		        URL_PARAMETER_SOURCE + StaticHelpers.getExternalParameter(getActivity(), 2) +
		        "&id=" + CaptchaID + StaticHelpers.getApiKey(getActivity()));

        Log.i("pullCaptchaPicture", "URL: " + CaptchaPictureURL);
        if (getView() != null) {
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
		if (getView() != null) {
			textViewBalance = (TextView) getView().findViewById(R.id.textViewBA);
			textViewBalance.setText(StaticHelpers.getBalance(getActivity()));
		}
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
                            if (getView() != null) {
	                            textViewBalance = (TextView) getView()
			                            .findViewById(R.id.textViewBA);
	                            textViewBalance.setText(StaticHelpers.getBalance(getActivity()));
                            }
                        }
                    });
                }

            }
        };

        // check if thread isn't already running.
        if (BalanceUpdate.isAlive())
            BalanceUpdate.interrupt();

            // if not, start it
        else BalanceUpdate.start();
    }
}
