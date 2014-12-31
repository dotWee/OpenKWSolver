package com.dotwee.openkwsolver;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity {

    String kwCoreurl = "http://www.9kw.eu:80/index.cgi";

    String actionCaptchanewok = "?action=usercaptchanew";
    String actionSource = "&source=androidopenkws";
    String actionConfirm = "&confirm=1";
    String actionNocaptcha = "&nocaptcha=1"; // TODO check if no captcha
    String actionAnswer = "?action=usercaptchacorrect";
    String actionShow = "?action=usercaptchashow";
    String actionSkipcaptcha = "?action=usercaptchaskip";
    String actionServercheck = "?action=userservercheck";

    String regex = "^[0-9]+ .+";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button buttonPull = (Button) findViewById(R.id.buttonPull);
        buttonPull.setText("Start");
        buttonPull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String CaptchaID = requestCaptchaID();

                if (CaptchaID.matches(regex)) {

                    buttonPull.setEnabled(false);

                    final TextView TextViewCurrent = (TextView) findViewById(R.id.textViewCurrent);
                    TextViewCurrent.setText("Current CaptchaID: " + CaptchaID);

                    final ProgressBar ProgressBar = (ProgressBar) findViewById(R.id.progressBar);
                    pullCaptchaPicture(CaptchaID);
                    final int[] i = {0};
                    final CountDownTimer CountDownTimer;
                    CountDownTimer = new CountDownTimer(30000, 1000) {

                        @Override
                        public void onTick(long millisUntilFinished) {
                            i[0]++;
                            ProgressBar.setProgress(i[0]);
                        }

                        @Override
                        public void onFinish() {

                        }
                    };

                    if (!CaptchaID.matches(regex)) {
                        CountDownTimer.start();
                    }


                    Button buttonSend = (Button) findViewById(R.id.buttonSend);
                    buttonSend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EditText EditTextCaptchaAnswer = (EditText) findViewById(R.id.editTextAnswer);
                            String CaptchaAnswer = EditTextCaptchaAnswer.getText().toString();
                            sendCaptchaAnswer(CaptchaAnswer, CaptchaID);

                            CountDownTimer.cancel();
                            ProgressBar.setProgress(0);

                            ImageView ImageView = (ImageView) findViewById(R.id.imageViewCaptcha);
                            ImageView.setImageDrawable(null);
                            EditTextCaptchaAnswer.setText(null);
                            TextViewCurrent.setText(null);

                            buttonPull.setEnabled(true);

                            Toast.makeText(getApplicationContext(), "Next Captcha arrives in 3 secs", Toast.LENGTH_SHORT).show();
                            Handler autoPull = new Handler();
                            autoPull.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    buttonPull.performClick();
                                }
                            }, 3000); // three sec delay
                        }
                    });

                    Button buttonSkip = (Button) findViewById(R.id.buttonSkip);
                    buttonSkip.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EditText EditTextCaptchaAnswer = (EditText) findViewById(R.id.editTextAnswer);
                            EditTextCaptchaAnswer.setText(null);

                            skipCaptcha(CaptchaID);

                            CountDownTimer.cancel();
                            ProgressBar.setProgress(0);

                            ImageView ImageView = (ImageView) findViewById(R.id.imageViewCaptcha);
                            ImageView.setImageDrawable(null);

                            buttonPull.setEnabled(true);

                            Toast.makeText(getApplicationContext(), "Next Captcha arrives in 3 secs", Toast.LENGTH_SHORT).show();
                            Handler autoPull = new Handler();
                            autoPull.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    buttonPull.performClick();
                                }
                            }, 3000); // three sec delay
                        }
                    });
                } else
                    Toast.makeText(getApplicationContext(), "Code: " + CaptchaID, Toast.LENGTH_SHORT).show();
            }
        });

        // TODO TextView Serverstatus
        Thread StatusUpdate = new Thread() {
            @Override
            public void run() {
                while (!isInterrupted()) {
                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pullStatus();
                        }
                    });
                }

            }
        };

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_enter_api:
                DialogAPI();
                return true;
            case R.id.action_stop:
                finish();
                System.exit(0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // CaptchaID anfragen
    public String requestCaptchaID() {
        String CaptchaURL = (kwCoreurl + actionCaptchanewok + pullKey() + actionSource + actionConfirm + actionNocaptcha + checkSelfOnlyCheckbox() + checkDebugCheckbox());
        String CaptchaID = null;

        try {
            CaptchaID = new DownloadContentTask().execute(CaptchaURL).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        if (CaptchaID.matches("NO CAPTCHA")) {
            Toast.makeText(getApplicationContext(), "Code: " + CaptchaID + " AVAILABLE", Toast.LENGTH_SHORT).show();
        }

        if (CaptchaID.matches(regex)) {
            return CaptchaID;
        }
        return CaptchaID;
    }

    // Send Captcha answer
    public void sendCaptchaAnswer(String CaptchaAnswer, String CaptchaID) {
        String CaptchaURL = (kwCoreurl + actionAnswer + actionSource + checkDebugCheckbox() + "&antwort=" + CaptchaAnswer + "&id=" + CaptchaID + pullKey());

        // remove Spaces
        CaptchaURL = CaptchaURL.replaceAll(" ", "%20");

        String Status = null;

        try {
            Status = new DownloadContentTask().execute(CaptchaURL).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        assert Status != null;
        if (Status.matches("OK")) {
            Toast.makeText(getApplicationContext(), "Code: " + Status, Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(getApplicationContext(), "Code: " + Status, Toast.LENGTH_SHORT).show();

    }

    // pull Captcha picture and display it
    public String pullCaptchaPicture(String CaptchaID) {
        String CaptchaPictureURL = (kwCoreurl + actionShow + actionSource + checkDebugCheckbox() + "&id=" + CaptchaID + pullKey());
        new DownloadImageTask((ImageView) findViewById(R.id.imageViewCaptcha)).execute(CaptchaPictureURL);

        return CaptchaPictureURL;

    }

    // read DebugBox TODO add Checkbox to menu
    public String checkDebugCheckbox() {
        CheckBox DebugBox = (CheckBox) findViewById(R.id.DebugBox);
        String result;

        if ((DebugBox).isChecked()) {
            result = "&debug=1"; // 1 = wahr
        } else {
            result = "&debug=0";
        }
        return result;
    }

    // read SelfOnlyBox TODO add Checkbox to menu
    public String checkSelfOnlyCheckbox() {
        CheckBox SelfOnlyBox = (CheckBox) findViewById(R.id.SelfOnlyBox);
        String result;

        if ((SelfOnlyBox).isChecked()) {
            result = "&selfonly=1"; // 1 = wahr
        } else {
            result = "";
        }
        return result;
    }

    // skip Captcha
    public void skipCaptcha(String CaptchaID) {
        String CaptchaSkipURL = (kwCoreurl + actionSkipcaptcha + "&id=" + CaptchaID + pullKey() + actionSource + checkDebugCheckbox());
        String Code = null;

        try {
            Code = new DownloadContentTask().execute(CaptchaSkipURL).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        Toast.makeText(getApplicationContext(), "Skipped: " + Code, Toast.LENGTH_SHORT).show();
    }

    // pull Serverstatus // TODO put maybe in asynctask
    public void pullStatus() {
        Pattern pQueue = Pattern.compile("queue=(\\d+)");
        Pattern pWorker = Pattern.compile("worker=(\\d+)");
        String tServercheck = null;

        try {
            tServercheck = new DownloadContentTask().execute(kwCoreurl + actionServercheck + actionSource).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        assert tServercheck != null;
        Matcher mQueue = pQueue.matcher(tServercheck);
        if (mQueue.find()) {
            TextView TextViewQueue = (TextView) findViewById(R.id.textViewQueue);
            TextViewQueue.setText(null);
            TextViewQueue.setText("Captchas in queue: " + mQueue.group(1));
        }

        Matcher mWorker = pWorker.matcher(tServercheck);
        if (mWorker.find()) {
            TextView TextViewWorker = (TextView) findViewById(R.id.textViewWorker);
            TextViewWorker.setText(null);
            TextViewWorker.setText("Workers: " + mWorker.group(1));
        }
    }

    // Dialog for API key
    public void DialogAPI() {
        AlertDialog.Builder AskDialog = new AlertDialog.Builder(this);

        AskDialog.setTitle("API-Key");
        AskDialog.setMessage("Please enter a working key here:");

        final String filename = "apikey.txt";
        final EditText input_key = new EditText(this);

        AskDialog.setView(input_key);
        AskDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                OutputStreamWriter save = null;
                try {
                    save = new OutputStreamWriter(openFileOutput(filename, MODE_APPEND));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                String apikey = input_key.getText().toString();
                try {
                    assert save != null;
                    save.write(apikey);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    save.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(), "Your API-Key is now saved.", Toast.LENGTH_SHORT).show();
            }
        });

        AskDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AskDialog.show();
    }

    // read API-Key from Dialog
    private String pullKey() {
        String read = null;

        try {

            InputStream inputStream = openFileInput("apikey.txt");

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                read = ("&apikey=" + stringBuilder.toString());
            }

        } catch (FileNotFoundException e) {
            DialogAPI();
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return read;

    }

}