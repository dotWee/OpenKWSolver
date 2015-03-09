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

import de.dotwee.openkwsolver.R;
import de.dotwee.openkwsolver.Tools.DownloadContentTask;
import de.dotwee.openkwsolver.Tools.DownloadImageTask;

/**
 * Created by Lukas on 08.03.2015.
 */
public class SolverFragment extends Fragment {
    public static final String URL_9WK = "http://www.9kw.eu:80/index.cgi";
    public static final String URL_PARAMETER_NOCAPTCHA = "&nocaptcha=1";
    public static final String URL_PARAMETER_CAPTCHA_NEW = "?action=usercaptchanew";
    public static final String URL_PARAMETER_CAPTCHA_SHOW = "?action=usercaptchashow";
    public static final String URL_PARAMETER_CAPTCHA_SKIP = "?action=usercaptchaskip";
    public static final String URL_PARAMETER_CAPTCHA_ANSWER = "?action=usercaptchacorrect";
    public static final String URL_PARAMETER_SOURCE = "&source=androidopenkws";
    public static final String URL_PARAMETER_TYPE_CONFIRM = ""; // &confirm=1
    public static final String URL_PARAMETER_SERVER_CHECK = "?action=userservercheck";
    public static final String URL_PARAMETER_SERVER_BALANCE = "?action=usercaptchaguthaben";
    private final static String LOG_TAG = "SolverFragment";
    private Context context = getActivity();

    public SolverFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_solver, container, true);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // declare main widgets
        final Button buttonPull = (Button) view.findViewById(R.id.buttonPull);
        final Button buttonSkip = (Button) view.findViewById(R.id.buttonSkip);
        final Button buttonSend = (Button) view.findViewById(R.id.buttonSend);
        final TextView textViewBalance = (TextView) view.findViewById(R.id.textViewBalance);
        final ImageView imageViewCaptcha = (ImageView) view.findViewById(R.id.imageViewCaptcha);
        final EditText editTextAnswer = (EditText) view.findViewById(R.id.editTextAnswer);

        // fix edittext width
        editTextAnswer.setMaxWidth(editTextAnswer.getWidth());

        // init prefs
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getActivity());

        final Boolean prefLoop = prefs.getBoolean("pref_automation_loop", false);

        Boolean prefNotification = prefs.getBoolean("pref_notification", false);
        Boolean prefSound = prefs.getBoolean("pref_notification_sound", false);
        final Boolean prefVibrate = prefs.getBoolean("pref_notification_vibrate", false);

        // start showing balance if network and apikey is available
        if (isNetworkAvailable()) {
            if (!pullKey().equals("")) balanceThread();
        }

        buttonPull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i("OnClickPull", "Click recognized");
                if (isNetworkAvailable()) {

                    SharedPreferences prefs = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());

                    String CaptchaID = null; // = MainActivity.requestCaptchaID(pullKey(), readState(), prefs.getBoolean("pref_automation_loop", false));

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
                    buttonSend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
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
                        }
                    });

                    Button buttonSkip = (Button) view.findViewById(R.id.buttonSkip);
                    buttonSkip.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.i("OnClickSkip", "Click recognized");
                            editTextAnswer.setText(null);
                            skipCaptcha(finalCaptchaID);

                            CountDownTimer.cancel();
                            ProgressBar.setProgress(0);

                            ImageView ImageView = (ImageView) view.findViewById(R.id.imageViewCaptcha);
                            ImageView.setImageDrawable(null);

                            buttonPull.setEnabled(true);
                        }
                    });

                } else {
                    DialogNetwork();
                }
            }
        });
    }

    // Request CaptchaID
    /*
    public String requestCaptchaID() {
        String CaptchaURL = (URL_9WK + URL_PARAMETER_CAPTCHA_NEW +
                pullKey() + URL_PARAMETER_SOURCE + URL_PARAMETER_TYPE_CONFIRM +
                URL_PARAMETER_NOCAPTCHA + readState());

        Log.i("requestCaptchaID", "URL: " + CaptchaURL);
        String CaptchaID = "";

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getActivity());

        Boolean prefLoop = prefs.getBoolean("pref_automation_loop", false);
        if (prefLoop) {
            try {
                CaptchaID = new DownloadContentTask().execute(CaptchaURL, "captchaid").get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            try {
                CaptchaID = new DownloadContentTask().execute(CaptchaURL, "").get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        if (CaptchaID.equalsIgnoreCase("")) {
            Log.i("requestCaptchaID", "CaptchaID is empty");
        } else Log.i("requestCaptchaID", "Received ID: " + CaptchaID);

        return CaptchaID;
    }
    */

    // Send Captcha answer
    public void sendCaptchaAnswer(String CaptchaAnswer, String CaptchaID) {

        Log.i("sendCaptchaAnswer", "Received answer: " + CaptchaAnswer);
        Log.i("sendCaptchaAnswer", "Received ID: " + CaptchaID);

        String CaptchaURL = (URL_9WK + URL_PARAMETER_CAPTCHA_ANSWER +
                URL_PARAMETER_SOURCE + readState() + "&antwort=" +
                CaptchaAnswer + "&id=" + CaptchaID + pullKey());

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
                URL_PARAMETER_SOURCE + readState() + "&id=" + CaptchaID + pullKey());

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

    // Skip Captcha
    public void skipCaptcha(String CaptchaID) {
        String CaptchaSkipURL = (URL_9WK + URL_PARAMETER_CAPTCHA_SKIP + "&id=" +
                CaptchaID + pullKey() + URL_PARAMETER_SOURCE + readState());

        Log.i("skipCaptcha", "URL: " + CaptchaSkipURL);
        String r = null;

        try {
            r = new DownloadContentTask().execute(CaptchaSkipURL, "").get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        Log.i("skipCaptcha", "Result: " + r);
    }

    // Read API-Key from Dialog
    private String pullKey() {
        String r;
        SharedPreferences pref_apikey = PreferenceManager
                .getDefaultSharedPreferences(getActivity());

        String k = pref_apikey.getString("pref_api_key", null);
        Log.i("pullKey", "Readed key: " + k);

        if (k != null) {
            r = ("&apikey=" + k);
            return r;
        } else return "";
    }

    // Read written states
    public String readState() {

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getActivity());

        Boolean prefSelfonly = prefs.getBoolean("pref_api_selfonly", false);
        Boolean prefDebug = prefs.getBoolean("pref_api_debug", false);
        String s = "";
        String d = "";

        if (prefSelfonly) s = "&selfonly=1";
        if (prefDebug) d = "&debug=1";

        return s + d;
    }

    // Check if network is available
    private boolean isNetworkAvailable() {
        Log.i("isNetworkAvailable", "Called");
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // Notify the user about not working network
    public void DialogNetwork() {
        Log.i("DialogNetwork", "Called");
        AlertDialog.Builder Dialog = new AlertDialog.Builder(getActivity());

        Dialog.setTitle("No network available");
        Dialog.setMessage("Please connect to the internet!");

        Dialog.setPositiveButton(getString(R.string.action_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i("DialogNetwork", "OK");
            }
        });

        Dialog.setNegativeButton(getString(R.string.action_close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i("DialogNetwork", "Canceled");
            }
        });
        Dialog.show();
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
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView textViewBalance;

                            String tBalance = null;
                            String BalanceURL = (URL_9WK + URL_PARAMETER_SERVER_BALANCE +
                                    URL_PARAMETER_SOURCE + pullKey());
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
