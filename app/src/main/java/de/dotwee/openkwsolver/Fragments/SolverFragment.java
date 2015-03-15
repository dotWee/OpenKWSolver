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

import de.dotwee.openkwsolver.MainActivity;
import de.dotwee.openkwsolver.R;
import de.dotwee.openkwsolver.Tools.DownloadImageTask;

public class SolverFragment extends Fragment {
    public static final String URL_9WK = "http://www.9kw.eu:80/index.cgi";
    public static final String URL_PARAMETER_CAPTCHA_SHOW = "?action=usercaptchashow";
    public static final String URL_PARAMETER_SOURCE = "&source=androidopenkws";
	private static final String LOG_TAG = "SolverFragment";

    Thread BalanceUpdate;

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
        final Button buttonPull = (Button) view.findViewById(R.id.buttonPull);
        final ImageView imageViewCaptcha = (ImageView) view.findViewById(R.id.imageViewCaptcha);
        imageViewCaptcha.getLayoutParams().height = Integer.parseInt(prefs.getString("pref_layout_size", "200"));

        final EditText editTextAnswer = (EditText) view.findViewById(R.id.editTextAnswer);

        // fix edittext width
        editTextAnswer.setMaxWidth(editTextAnswer.getWidth());

        // start showing balance if network and apikey is available
	    if (MainActivity.isNetworkAvailable(getActivity())) {
		    if (!MainActivity.getApiKey(getActivity()).equals("")) {
			    Log.i(LOG_TAG, "onCreated: start balance thread");
		    }
		    balanceThread();
	    } else {
		    Log.w(LOG_TAG, "No network available");
		    Toast.makeText(getActivity(), "No network available!", Toast.LENGTH_SHORT).show();
	    }

        buttonPull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
	            Log.i(LOG_TAG, "onClickPull: Click recognized");
	            if (MainActivity.isNetworkAvailable(getActivity())) {

		            String CaptchaID = MainActivity.requestCaptchaID(getActivity(), // needed Context
				            prefs.getBoolean("pref_automation_loop", false), // Loop: false / true
				            2); // 2 = Normal

                    Boolean currentCapt = false;
		            currentCapt = pullCaptchaPicture(CaptchaID);

                    final ProgressBar ProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
                    buttonPull.setEnabled(false);

		            Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
		            if (MainActivity.isVibrateEnabled(getActivity())) {
		                if (currentCapt) {
			                vibrator.vibrate(500);
		                }
	                }

                    final int[] i = {0};
                    final CountDownTimer CountDownTimer;
                    CountDownTimer = new CountDownTimer(26000, 1000) {

                        @Override
                        public void onTick(long millisUntilFinished) {
                            i[0]++;
                            ProgressBar.setProgress(i[0]);
                        }

                        @Override
                        public void onFinish() {
                        }
                    };

                    CountDownTimer.start();
                    Button buttonSend = (Button) view.findViewById(R.id.buttonSend);
                    final String finalCaptchaID = CaptchaID;
                    buttonSend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v1) {
                            String CaptchaAnswer = editTextAnswer.getText().toString();
                            if (!CaptchaAnswer.equalsIgnoreCase("")) {
	                            MainActivity.sendCaptchaByID(getActivity(), finalCaptchaID, CaptchaAnswer, false);

                                CountDownTimer.cancel();
                                Log.i("OnClickSend", "Timer killed");
                                ProgressBar.setProgress(0);

                                imageViewCaptcha.setImageDrawable(null);
                                editTextAnswer.setText(null);

	                            if (MainActivity.isLoopEnabled(getActivity())) {
		                            Log.i("OnClickSend", "Loop-Mode");
                                    buttonPull.performClick();
                                } else buttonPull.setEnabled(true);
                            } else Toast.makeText(getActivity(),
                                    R.string.main_toast_emptyanswer, Toast.LENGTH_LONG).show();
                        }
                    });

                    Button buttonSkip = (Button) view.findViewById(R.id.buttonSkip);
                    buttonSkip.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v1) {
                            Log.i("OnClickSkip", "Click recognized");
                            editTextAnswer.setText(null);
                            MainActivity.skipCaptchaByID(
		                            getActivity(), MainActivity.getApiKey(getActivity()));

                            CountDownTimer.cancel();
                            ProgressBar.setProgress(0);

                            ImageView ImageView = (ImageView) view.findViewById(R.id.imageViewCaptcha);
                            ImageView.setImageDrawable(null);

                            buttonPull.setEnabled(true);
                        }
                    });

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

        if (BalanceUpdate.isAlive())
            BalanceUpdate.interrupt();
    }

    // Pull Captcha picture and display it
    public boolean pullCaptchaPicture(String CaptchaID) {
        String CaptchaPictureURL = (URL_9WK + URL_PARAMETER_CAPTCHA_SHOW +
                URL_PARAMETER_SOURCE + MainActivity.getExternalParameter(getActivity(), 2) + "&id=" + CaptchaID + MainActivity.getApiKey(getActivity()));

        Log.i("pullCaptchaPicture", "URL: " + CaptchaPictureURL);
        if (getView() != null) {
            ImageView ImageV = (ImageView) getView().findViewById(R.id.imageViewCaptcha);
            try {
	            Bitmap returnBit = null;
	            returnBit = new DownloadImageTask(ImageV).execute(CaptchaPictureURL).get(3000, TimeUnit.MILLISECONDS);
	            if (returnBit != null) return true; // true = new image
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
            }
        }

        return false;
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
                                TextView textViewBalance = (TextView) getView()
                                        .findViewById(R.id.textViewBalance);
                                textViewBalance.setText(MainActivity.getBalance(getActivity()));
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
