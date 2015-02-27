package de.dotwee.openkwsolver;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.dotwee.openkwsolver.Tools.DownloadContentTask;
import de.dotwee.openkwsolver.Tools.DownloadImageTask;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        
        
        final Button buttonPull = (Button) findViewById(R.id.buttonPull);
        final Button buttonBalance = (Button) findViewById(R.id.buttonBalance);
        final ImageView ImageViewCaptcha = (ImageView) findViewById(R.id.imageViewCaptcha);
        final EditText EditTextCaptchaAnswer = (EditText) findViewById(R.id.editTextAnswer);
        EditTextCaptchaAnswer.setMaxWidth(EditTextCaptchaAnswer.getWidth());
        final Boolean loopMode = prefs.getBoolean("loop", false);

        String checkAPI = pullKey();
        if (checkAPI != null) {
            balanceThread();
            buttonBalance.setEnabled(false);
            buttonBalance.setVisibility(View.VISIBLE);
        }

        if (isNetworkAvailable()) {
            servercheckThread();
        }

        buttonPull.setText(getString(R.string.start));
        buttonPull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("OnClickPull", "Click recognized");
                if (isNetworkAvailable()) {
                    String CaptchaID = null;

                    if (loopMode) {
                        while (loopMode) {
                            CaptchaID = requestCaptchaID();
                            if (!CaptchaID.equals("")) break;
                        }
                    } else CaptchaID = requestCaptchaID();
                    
                    if (!CaptchaID.equalsIgnoreCase("")) {
                        Boolean currentCapt = false;
                        currentCapt = pullCaptchaPicture(CaptchaID);
                        
                        
                        final ProgressBar ProgressBar = (ProgressBar) findViewById(R.id.progressBar);
                        buttonPull.setEnabled(false);


                        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        if (currentCapt) vibrator.vibrate(500);

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

                        final Boolean finalCurrentCapt = currentCapt;
                        CountDownTimer.start();
                        buttonPull.performClick();

                        Button buttonSend = (Button) findViewById(R.id.buttonSend);
                        final String finalCaptchaID = CaptchaID;
                        buttonSend.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String CaptchaAnswer = EditTextCaptchaAnswer.getText().toString();
                                sendCaptchaAnswer(CaptchaAnswer, finalCaptchaID);

                                CountDownTimer.cancel();
                                Log.i("OnClickSend", "Timer killed");
                                ProgressBar.setProgress(0);

                                ImageViewCaptcha.setImageDrawable(null);
                                EditTextCaptchaAnswer.setText(null);

                                TextView TextViewCurrent;
                                TextViewCurrent = (TextView) findViewById(R.id.textViewCurrent);
                                TextViewCurrent.setText(null);

                                if (loopMode) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.next_captcha_arrives_soon), Toast.LENGTH_SHORT).show();
                                    Log.i("OnClickSend", "Loop-Mode");
                                    buttonPull.performClick();
                                } else buttonPull.setEnabled(true);
                            }
                        });

                        Button buttonSkip = (Button) findViewById(R.id.buttonSkip);
                        buttonSkip.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.i("OnClickSkip", "Click recognized");
                                EditText EditTextCaptchaAnswer = (EditText) findViewById(R.id.editTextAnswer);
                                EditTextCaptchaAnswer.setText(null);

                                skipCaptcha(finalCaptchaID);

                                TextView TextViewCurrent = (TextView) findViewById(R.id.textViewCurrent);
                                TextViewCurrent.setText(null);

                                CountDownTimer.cancel();
                                ProgressBar.setProgress(0);

                                ImageView ImageView = (ImageView) findViewById(R.id.imageViewCaptcha);
                                ImageView.setImageDrawable(null);

                                buttonPull.setEnabled(true);
                                Toast.makeText(getApplicationContext(), getString(R.string.next_captcha_arrives_soon), Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else Log.i("OnClickPull", "Error with ID: " + CaptchaID);
                } else {
                    DialogNetwork();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        Log.i("onCreateOptionsMenu", "Return: " + true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, PreferenceActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_stop:
                finish();
                System.exit(0);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Request CaptchaID
    public String requestCaptchaID() {
        String CaptchaURL = (SourceConfig.URL + SourceConfig.URL_PARAMETER_CAPTCHA_NEW + pullKey() + SourceConfig.URL_PARAMETER_SOURCE + SourceConfig.URL_PARAMETER_TYPE_CONFIRM + SourceConfig.URL_PARAMETER_NOCAPTCHA + readState("selfonly") + readState("debug"));
        Log.i("requestCaptchaID", "URL: " + CaptchaURL);
        String CaptchaID = "";

        try {
            CaptchaID = new DownloadContentTask().execute(CaptchaURL).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        if (CaptchaID.equalsIgnoreCase("")) {
            Log.i("requestCaptchaID", "CaptchaID is empty");
        } else Log.i("requestCaptchaID", "Received ID: " + CaptchaID);

        TextView TextViewCurrent = (TextView) findViewById(R.id.textViewCurrent);
        TextViewCurrent.setText(CaptchaID);

        return CaptchaID;
    }

    // Send Captcha answer
    public void sendCaptchaAnswer(String CaptchaAnswer, String CaptchaID) {

        Log.i("sendCaptchaAnswer", "Received answer: " + CaptchaAnswer);
        Log.i("sendCaptchaAnswer", "Received ID: " + CaptchaID);

        String CaptchaURL = (SourceConfig.URL + SourceConfig.URL_PARAMETER_CAPTCHA_ANSWER + SourceConfig.URL_PARAMETER_SOURCE + readState("debug") + "&antwort=" + CaptchaAnswer + "&id=" + CaptchaID + pullKey());
        // remove Spaces
        CaptchaURL = CaptchaURL.replaceAll(" ", "%20");
        Log.i("sendCaptchaAnswer", "Answer-URL: " + CaptchaURL);

        String Status = null;

        try {
            Status = new DownloadContentTask().execute(CaptchaURL).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        Log.i("sendCaptchaAnswer", "Code: " + Status);

    }

    // Pull Captcha picture and display it
    public boolean pullCaptchaPicture(String CaptchaID) {
        String CaptchaPictureURL = (SourceConfig.URL + SourceConfig.URL_PARAMETER_CAPTCHA_SHOW + SourceConfig.URL_PARAMETER_SOURCE + readState("debug") + "&id=" + CaptchaID + pullKey());
        Log.i("pullCaptchaPicture", "URL: " + CaptchaPictureURL);
        ImageView ImageV = (ImageView) findViewById(R.id.imageViewCaptcha);
        try {
            Bitmap returnBit = new DownloadImageTask(ImageV).execute(CaptchaPictureURL).get();
            if (returnBit != null) return true; // true = new image
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Skip Captcha
    public void skipCaptcha(String CaptchaID) {
        String CaptchaSkipURL = (SourceConfig.URL + SourceConfig.URL_PARAMETER_CAPTCHA_SKIP + "&id=" + CaptchaID + pullKey() + SourceConfig.URL_PARAMETER_SOURCE + readState("debug"));
        Log.i("skipCaptcha", "URL: " + CaptchaSkipURL);
        String r = null;

        try {
            r = new DownloadContentTask().execute(CaptchaSkipURL).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        Log.i("skipCaptcha", "Result: " + r);
    }

    // Read API-Key from Dialog
    private String pullKey() {
        String r;
        SharedPreferences pref_apikey = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        String k = pref_apikey.getString("apikey", null);
        Log.i("pullKey", "Readed key: " + k);

        if (k != null) {
            r = ("&apikey=" + k);
            return r;
        } else return "";
    }

    // Read written states
    public String readState(String rType) {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        boolean b = prefs.getBoolean(rType, false);

        Log.i("readState", "input Type: " + rType);
        Log.i("readState", "input State: " + b);

        String r = "";

        if (rType.equalsIgnoreCase("debug")) {
            if (b) {
                r = readState("debug");
            }
        }

        if (rType.equalsIgnoreCase("selfonly")) {
            if (b) {
                r = "&selfonly=1";
            }
        } else {
            r = "";
            Log.i("readState", "No Type discovered");
        }

        Log.i("readState", "Return: " + r);
        return r;
    }

    // Check if network is available
    private boolean isNetworkAvailable() {
        Log.i("isNetworkAvailable", "Called");
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // Notify the user about now working network
    public void DialogNetwork() {
        Log.i("DialogNetwork", "Called");
        AlertDialog.Builder Dialog = new AlertDialog.Builder(this);

        Dialog.setTitle("No network available");
        Dialog.setMessage("Please connect to the internet!");

        Dialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i("DialogNetwork", "OK");
            }
        });

        Dialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Button buttonBalance = (Button) findViewById(R.id.buttonBalance);
                            Log.i("balanceThread", "Called");

                            String BalanceURL = (SourceConfig.URL + SourceConfig.URL_PARAMETER_SERVER_BALANCE + SourceConfig.URL_PARAMETER_SOURCE + pullKey());
                            Log.i("balanceThread", "BalanceURL: " + BalanceURL);

                            String tBalance = null;

                            try {
                                tBalance = new DownloadContentTask().execute(BalanceURL).get();
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }
                            Log.i("balanceThread", "Balance: " + tBalance);
                            buttonBalance.setText(tBalance);

                        }
                    });
                }

            }
        };

        // check if thread isn't already running.
        if (BalanceUpdate.isAlive()) {
            BalanceUpdate.stop();
            Log.i("balanceThread", "stopped");
        }

        // if not, start it
        else {
            BalanceUpdate.start();
            Log.i("balanceThread", "started");
        }
    }

    // ServercheckThread: Update the server-stats every 5 seconds
    public void servercheckThread() {
        final Thread ServercheckUpdate;
        ServercheckUpdate = new Thread() {

            @Override
            public void run() {
                while (!isInterrupted()) {
                    try {
                        Thread.sleep(5000); // 5000ms = 5s
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i("servercheckThread", "Called");
                            Pattern pQueue = Pattern.compile("queue=(\\d+)");
                            Pattern pWorker = Pattern.compile("worker=(\\d+)");
                            String tServercheck = null;

                            try {
                                tServercheck = new DownloadContentTask().execute(SourceConfig.URL + SourceConfig.URL_PARAMETER_SERVER_CHECK + SourceConfig.URL_PARAMETER_SOURCE).get();
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }

                            assert tServercheck != null;
                            Matcher mQueue = pQueue.matcher(tServercheck);
                            if (mQueue.find()) {
                                TextView TextViewQueue = (TextView) findViewById(R.id.textViewQueue);
                                TextViewQueue.setText(null);
                                TextViewQueue.setText(getString(R.string.captchas_in_queue) + mQueue.group(1));
                                Log.i("servercheckThread", "Queue: " + mQueue.group(1));
                            }

                            Matcher mWorker = pWorker.matcher(tServercheck);
                            if (mWorker.find()) {
                                TextView TextViewWorker = (TextView) findViewById(R.id.textViewWorker);
                                TextViewWorker.setText(null);
                                TextViewWorker.setText(getString(R.string.workers) + mWorker.group(1));
                                Log.i("servercheckThread", "Workers: " + mWorker.group(1));
                            }
                        }
                    });
                }

            }
        };

        // check if thread isn't already running.
        if (ServercheckUpdate.isAlive()) {
            ServercheckUpdate.stop();
            Log.i("servercheckThread", "stopped");
        }

        // if not, start it
        else {
            ServercheckUpdate.start();
            Log.i("servercheckThread", "started");
        }
    }
}