package com.dotwee.openkwsolver;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
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

import java.io.BufferedReader;
import java.io.File;
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

        File dir = getFilesDir();
        File dfile = new File(dir, "debug.txt");
        File sfile = new File(dir, "selfonly.txt");
        boolean ddeleted = dfile.delete();
        boolean sdeleted = sfile.delete();

        if (ddeleted == true) {
            Log.i("onCreate", "Debugfile deleted");
        }

        if (sdeleted == true) {
            Log.i("onCreate", "Selfonlyfile deleted");
        }
        
        final Button buttonPull = (Button) findViewById(R.id.buttonPull);
        buttonPull.setText(getString(R.string.start));
        buttonPull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("OnClickPull", "Click recognized");
                final String CaptchaID = requestCaptchaID();
                if (CaptchaID.matches(regex)) {

                    buttonPull.setEnabled(false);

                    final TextView TextViewCurrent = (TextView) findViewById(R.id.textViewCurrent);
                    TextViewCurrent.setText(getString(R.string.current_captchaid) + CaptchaID);

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

                    CountDownTimer.start();
                    Log.i("OnClickPull", "Timer started");

                    Button buttonSend = (Button) findViewById(R.id.buttonSend);
                    buttonSend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EditText EditTextCaptchaAnswer = (EditText) findViewById(R.id.editTextAnswer);
                            String CaptchaAnswer = EditTextCaptchaAnswer.getText().toString();
                            sendCaptchaAnswer(CaptchaAnswer, CaptchaID);

                            CountDownTimer.cancel();
                            Log.i("OnClickSend", "Timer killed");
                            ProgressBar.setProgress(0);

                            ImageView ImageView = (ImageView) findViewById(R.id.imageViewCaptcha);
                            ImageView.setImageDrawable(null);
                            EditTextCaptchaAnswer.setText(null);
                            TextViewCurrent.setText(null);

                            buttonPull.setEnabled(true);

                            Toast.makeText(getApplicationContext(), getString(R.string.next_captcha_arrives_soon), Toast.LENGTH_SHORT).show();
                            Handler autoPull = new Handler();
                            autoPull.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Log.i("OnClickPull", "Auto-pull next Captcha");
                                    buttonPull.performClick();
                                }
                            }, 3000); // three sec delay
                        }
                    });

                    Button buttonSkip = (Button) findViewById(R.id.buttonSkip);
                    buttonSkip.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.i("OnClickSkip", "Click recognized");
                            EditText EditTextCaptchaAnswer = (EditText) findViewById(R.id.editTextAnswer);
                            EditTextCaptchaAnswer.setText(null);

                            skipCaptcha(CaptchaID);

                            CountDownTimer.cancel();
                            ProgressBar.setProgress(0);

                            ImageView ImageView = (ImageView) findViewById(R.id.imageViewCaptcha);
                            ImageView.setImageDrawable(null);

                            buttonPull.setEnabled(true);

                            Toast.makeText(getApplicationContext(), getString(R.string.next_captcha_arrives_soon), Toast.LENGTH_SHORT).show();
                            Handler autoPull = new Handler();
                            autoPull.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    buttonPull.performClick();
                                    Log.i("OnClickSkip", "Auto-pull next Captcha");
                                }
                            }, 3000); // three sec delay
                        }
                    });
                } else
                    Log.i("OnClickPull", "Error with ID: " + CaptchaID);
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
                            Log.i("StatusUpdate", "Serverstatus updated");
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
        Log.i("onCreateOptionsMenu", "Return: " + true);
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
            case R.id.action_debug:
                String sType = "debug";
                if (item.isChecked()) {
                    item.setChecked(false);
                    writeState(sType, false);
                } else {
                    item.setChecked(true);
                    writeState(sType, true);
                }
                return true;
            case R.id.action_selfonly:
                String aType = "selfonly";
                if (item.isChecked()) {
                    item.setChecked(false);
                    writeState(aType, false);
                } else {
                    item.setChecked(true);
                    writeState(aType, true);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // CaptchaID anfragen
    public String requestCaptchaID() {
        String CaptchaURL = (kwCoreurl + actionCaptchanewok + pullKey() + actionSource + actionConfirm + actionNocaptcha + readState("selfonly") + readState("debug"));
        Log.i("requestCaptchaID", "URL: " + CaptchaURL);
        String CaptchaID = null;

        try {
            CaptchaID = new DownloadContentTask().execute(CaptchaURL).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        if (CaptchaID == null) {
            Log.i("requestCaptchaID", "CaptchaID = null");
        } else Log.i("requestCaptchaID", "Received ID: " + CaptchaID);
        return CaptchaID;
    }

    // Send Captcha answer
    public void sendCaptchaAnswer(String CaptchaAnswer, String CaptchaID) {

        Log.i("sendCaptchaAnswer", "Received answer: " + CaptchaAnswer);
        Log.i("sendCaptchaAnswer", "Received ID: " + CaptchaID);
        
        String CaptchaURL = (kwCoreurl + actionAnswer + actionSource + readState("debug") + "&antwort=" + CaptchaAnswer + "&id=" + CaptchaID + pullKey());
        // remove Spaces
        CaptchaURL = CaptchaURL.replaceAll(" ", "%20");
        Log.i("sendCaptchaAnswer", "URL: " + CaptchaURL);

        String Status = null;

        try {
            Status = new DownloadContentTask().execute(CaptchaURL).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        Log.i("sendCaptchaAnswer", "Code: " + Status);

    }

    // pull Captcha picture and display it
    public String pullCaptchaPicture(String CaptchaID) {
        String CaptchaPictureURL = (kwCoreurl + actionShow + actionSource + readState("debug") + "&id=" + CaptchaID + pullKey());
        Log.i("pullCaptchaPicture", "URL: " + CaptchaPictureURL);
        ImageView ImageV = (ImageView) findViewById(R.id.imageViewCaptcha);
        new DownloadImageTask(ImageV).execute(CaptchaPictureURL);

        if (ImageV.getDrawable() == null) {
            Log.i("pullCaptchaPicture", "After Downloadtask: UNUSED");
        } else Log.i("pullCaptchaPicture", "After Downloadtask: USED");
        
        return CaptchaPictureURL;

    }

    // skip Captcha
    public void skipCaptcha(String CaptchaID) {
        String CaptchaSkipURL = (kwCoreurl + actionSkipcaptcha + "&id=" + CaptchaID + pullKey() + actionSource + readState("debug"));
        Log.i("skipCaptcha", "URL: " + CaptchaSkipURL);
        String Code = null;

        try {
            Code = new DownloadContentTask().execute(CaptchaSkipURL).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        Log.i("skipCaptcha", "Result: " + Code);
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

        Log.i("pullStatus", "Output: " + tServercheck);

        assert tServercheck != null;
        Matcher mQueue = pQueue.matcher(tServercheck);
        if (mQueue.find()) {
            TextView TextViewQueue = (TextView) findViewById(R.id.textViewQueue);
            TextViewQueue.setText(null);
            TextViewQueue.setText(getString(R.string.captchas_in_queue) + mQueue.group(1));
            Log.i("pullStatus", "Queue: " + mQueue.group(1));
        }

        Matcher mWorker = pWorker.matcher(tServercheck);
        if (mWorker.find()) {
            TextView TextViewWorker = (TextView) findViewById(R.id.textViewWorker);
            TextViewWorker.setText(null);
            TextViewWorker.setText(getString(R.string.workers) + mWorker.group(1));
            Log.i("pullStatus", "Worker: " + mWorker.group(1));
        }
    }

    // Dialog for API key
    public void DialogAPI() {
        AlertDialog.Builder AskDialog = new AlertDialog.Builder(this);

        AskDialog.setTitle("API-Key");
        AskDialog.setMessage(getString(R.string.enter_captcha_here));

        final String filename = "apikey.txt";
        final EditText input_key = new EditText(this);

        AskDialog.setView(input_key);
        AskDialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
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
                    Log.i("DialogAPI", "Saving API-Key successful");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    save.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(), getString(R.string.apikey_now_saved), Toast.LENGTH_SHORT).show();
            }
        });

        AskDialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i("DialogAPI", "Canceled Dialog");
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
                String receiveString;
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                read = ("&apikey=" + stringBuilder.toString());
                Log.i("pullKey", "Readed key: " + stringBuilder.toString());
            }

        } catch (FileNotFoundException e) {
            Log.i("pullKey", "Couldn't read key. Ask for it.");
            DialogAPI();
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.i("pullKey", "Return: " + read);
        return read;

    }

    public void writeState(String Type, Boolean State) {
        String FILENAME = (Type + ".txt");
        // true = aktiviert

        File dir = getFilesDir();
        File file = new File(dir, FILENAME);
        boolean deleted = file.delete();
        Log.i("writeState", "File deleted: " + deleted);

        OutputStreamWriter save = null;
        try {
            save = new OutputStreamWriter(openFileOutput(FILENAME, MODE_APPEND));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            assert save != null;
            save.write(String.valueOf(State));
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("writeState", "Couldn't write file.");
        }
        try {
            save.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readState(String Type) {
        String FILENAME = (Type + ".txt");
        String out = "";
        String ret = "";

        try {
            InputStream inputStream = openFileInput(FILENAME);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString;
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null)
                    stringBuilder.append(receiveString);

                inputStream.close();
                ret = stringBuilder.toString();
                Log.i("readState", "File-Output: " + ret);
            }
        } catch (IOException ignored) {
        }

        if (Type.equals("debug")) {
            if (ret.equals("true")) out = "&debug=1";
        }

        if (Type.equals("selfonly")) if (ret.equals("true")) out = "&selfonly=1";
        else out = "";

        Log.i("readState", "After Check: " + out);
        return out;
    }
}