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

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import de.dotwee.openkwsolver.MainActivity;
import de.dotwee.openkwsolver.R;
import de.dotwee.openkwsolver.Tools.DownloadContentTask;
import de.dotwee.openkwsolver.Tools.DownloadImageTask;

/**
 * Created by Lukas on 08.03.2015.
 */
public class SolverFragment extends Fragment {
    public static final String URL_9WK = "http://www.9kw.eu:80/index.cgi";
    public static final String URL_PARAMETER_CAPTCHA_SHOW = "?action=usercaptchashow";
    public static final String URL_PARAMETER_CAPTCHA_ANSWER = "?action=usercaptchacorrect";
    public static final String URL_PARAMETER_SOURCE = "&source=androidopenkws";
    public static final String URL_PARAMETER_SERVER_BALANCE = "?action=usercaptchaguthaben";
    private static final String LOG_TAG = "SolverFragment";

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

        // declare main widgets
        final Button buttonPull = (Button) view.findViewById(R.id.buttonPull);
        final ImageView imageViewCaptcha = (ImageView) view.findViewById(R.id.imageViewCaptcha);
        final EditText editTextAnswer = (EditText) view.findViewById(R.id.editTextAnswer);

        // fix edittext width
        editTextAnswer.setMaxWidth(editTextAnswer.getWidth());

        // init prefs
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getActivity());

        final Boolean prefLoop = prefs.getBoolean("pref_automation_loop", false);
        final Boolean prefVibrate = prefs.getBoolean("pref_notification_vibrate", false);

        // start showing balance if network and apikey is available
        if (MainActivity.networkAvailable(getActivity()))
            if (!MainActivity.getApiKey(getActivity()).equals(""))
                balanceThread();

        buttonPull.setOnClickListener(v -> {

            Log.i("OnClickPull", "Click recognized");
            if (MainActivity.networkAvailable(getActivity())) {

                SharedPreferences prefs1 = PreferenceManager
                        .getDefaultSharedPreferences(getActivity());

                String CaptchaID = MainActivity.requestCaptchaID(getActivity(), prefs1.getBoolean("pref_automation_loop", false), 2);

                Boolean currentCapt = false;
                currentCapt = pullCaptchaPicture(CaptchaID);


                final ProgressBar ProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
                buttonPull.setEnabled(false);


                Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                if (prefVibrate)
                    if (currentCapt)
                        vibrator.vibrate(500);

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
                buttonSend.setOnClickListener(v1 -> {
                    String CaptchaAnswer = editTextAnswer.getText().toString();
                    if (!CaptchaAnswer.equalsIgnoreCase("")) {
                        sendCaptchaAnswer(CaptchaAnswer, finalCaptchaID);

                        CountDownTimer.cancel();
                        Log.i("OnClickSend", "Timer killed");
                        ProgressBar.setProgress(0);

                        imageViewCaptcha.setImageDrawable(null);
                        editTextAnswer.setText(null);

                        if (prefLoop) {
                            Log.i("OnClickSend", "Loop-Mode");
                            buttonPull.performClick();
                        } else buttonPull.setEnabled(true);
                    } else Toast.makeText(getActivity(),
                            R.string.main_toast_emptyanswer, Toast.LENGTH_LONG).show();
                });

                Button buttonSkip = (Button) view.findViewById(R.id.buttonSkip);
                buttonSkip.setOnClickListener(v1 -> {
                    Log.i("OnClickSkip", "Click recognized");
                    editTextAnswer.setText(null);
                    MainActivity.skipCaptchaByID(
                            getActivity(), MainActivity.getApiKey(getActivity()));

                    CountDownTimer.cancel();
                    ProgressBar.setProgress(0);

                    ImageView ImageView = (ImageView) view.findViewById(R.id.imageViewCaptcha);
                    ImageView.setImageDrawable(null);

                    buttonPull.setEnabled(true);
                });

            }
        });
    }

    // Send Captcha answer
    public void sendCaptchaAnswer(String CaptchaAnswer, String CaptchaID) {

        Log.i("sendCaptchaAnswer", "Received answer: " + CaptchaAnswer);
        Log.i("sendCaptchaAnswer", "Received ID: " + CaptchaID);

        String CaptchaURL = (URL_9WK + URL_PARAMETER_CAPTCHA_ANSWER +
                URL_PARAMETER_SOURCE + MainActivity.getExternalParameter(getActivity(), 2) + "&antwort=" +
                CaptchaAnswer + "&id=" + CaptchaID + MainActivity.getApiKey(getActivity()));

        // remove Spaces from URL
        CaptchaURL = CaptchaURL.replaceAll(" ", "%20");
        Log.i("sendCaptchaAnswer", "Answer-URL: " + CaptchaURL);

        String Status = null;

        try {
            Status = new DownloadContentTask().execute(CaptchaURL, "").get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        Log.i("sendCaptchaAnswer", "Code: " + Status);

    }

    // Pull Captcha picture and display it
    public boolean pullCaptchaPicture(String CaptchaID) {
        String CaptchaPictureURL = (URL_9WK + URL_PARAMETER_CAPTCHA_SHOW +
                URL_PARAMETER_SOURCE + MainActivity.getExternalParameter(getActivity(), 2) + "&id=" + CaptchaID + MainActivity.getApiKey(getActivity()));

        Log.i("pullCaptchaPicture", "URL: " + CaptchaPictureURL);
        if (getView() != null) {
            ImageView ImageV = (ImageView) getView().findViewById(R.id.imageViewCaptcha);
            try {
                Bitmap returnBit = new DownloadImageTask(ImageV).execute(CaptchaPictureURL).get();
                if (returnBit != null) return true; // true = new image
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    // BalanceThread: Update the balance every 5 seconds
    public void balanceThread() {
        final Thread BalanceUpdate;
        BalanceUpdate = new Thread() {

            @Override
            public void run() {
                while (!isInterrupted()) {
                    try {
                        Thread.sleep(5000); // 5000ms = 5s
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    getActivity().runOnUiThread(() -> {
                        TextView textViewBalance;

                        String tBalance = null;
                        String BalanceURL = (URL_9WK + URL_PARAMETER_SERVER_BALANCE +
                                URL_PARAMETER_SOURCE + MainActivity.getApiKey(getActivity()));
                        Log.i("balanceThread", "BalanceURL: " + BalanceURL);

                        try {
                            tBalance = new DownloadContentTask().execute(BalanceURL, "").get();
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }

                        if (getView() != null) {
                            textViewBalance = (TextView) getView()
                                    .findViewById(R.id.textViewBalance);

                            Log.i("balanceThread", "Balance: " + tBalance);
                            textViewBalance.setText(tBalance);
                        }
                    });
                }

            }
        };

        // check if thread isn't already running.
        if (BalanceUpdate.isAlive()) {
            BalanceUpdate.interrupt();
            Log.i("balanceThread", "stopped");
        }

        // if not, start it
        else {
            BalanceUpdate.start();
            Log.i("balanceThread", "started");
        }
    }
}
