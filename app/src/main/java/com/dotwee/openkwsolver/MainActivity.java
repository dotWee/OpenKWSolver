package com.dotwee.openkwsolver;

import android.app.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.concurrent.ExecutionException;

public class MainActivity extends Activity {

    String coreurl = "http://www.9kw.eu:80/index.cgi";
    String actionsource = "&source=androidopenkws";
    String regex = "^[0-9]+ .+";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pullKeyFromFile();

        Button buttonKey = (Button) findViewById(R.id.buttonKey);
        buttonKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogKey();
            }
        });

        final Button buttonBeginn = (Button) findViewById(R.id.buttonLeft);

        buttonBeginn.setText("Beginn");
        buttonBeginn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                clearImageViewAndEditText();

                final String CaptchaID = pullCaptchaID();

                buttonBeginn.setText("Skip");
                buttonBeginn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        skipCaptcha(CaptchaID);
                        buttonBeginn.performClick();
                    }
                });

                if (!CaptchaID.matches(regex)) {
                    pullCaptchaPicture(CaptchaID); // Pull the Captcha picture and display it

                    final ProgressBar pProgressBar;
                    final CountDownTimer pCountDownTimer;
                    final int[] i = {0};
                    pProgressBar = (ProgressBar) findViewById(R.id.progressBar);
                    pProgressBar.setProgress(i[0]);
                    pCountDownTimer = new CountDownTimer(30000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            i[0]++;
                            pProgressBar.setProgress(i[0]);
                        }

                        @Override
                        public void onFinish() {
                            buttonBeginn.performClick();
                            skipCaptcha(CaptchaID);
                        }
                    };
                    pCountDownTimer.start();

                    Button buttonSend = (Button) findViewById(R.id.buttonRight);
                    buttonSend.setText("Send");
                    buttonSend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EditText EditTextCaptchaAnswer = (EditText) findViewById(R.id.editTextAnswer);
                            String CaptchaAnswer = EditTextCaptchaAnswer.getText().toString();
                            sendCaptchaAnswerText(CaptchaAnswer, CaptchaID);
                            clearImageViewAndEditText();
                            buttonBeginn.setText("Continue");
                            buttonBeginn.performClick();
                            pCountDownTimer.cancel();
                        }
                    });

                } else
                    Toast.makeText(getApplicationContext(), "Housten, we got a problem with CaptchaID " + CaptchaID, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    // read the 9kw API-Key from previously saved file
    private String pullKeyFromFile() {
        String apikey = null;

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
                    apikey = stringBuilder.toString();
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        return apikey;
    }

    // clear ImageView and EditText (just to be save)
    public void clearImageViewAndEditText() {
        ImageView ImageView = (ImageView) findViewById(R.id.imageViewCaptcha);
        ImageView.setImageDrawable(null);

        EditText EditText = (EditText) findViewById(R.id.editTextAnswer);
        EditText.setText(null);
    }

    // request CaptchaID (only text)
    public String pullCaptchaID() {
        String CaptchaIDURL = (coreurl + "?action=usercaptchanew" + "&apikey=" + pullKeyFromFile() + actionsource + "&confirm=1&nocaptcha=1&debug=" + checkDebugCheckbox());
        String CaptchaID = null;

        try {
            CaptchaID = new DownloadContentTask().execute(CaptchaIDURL).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return CaptchaID;
    }

    // pull Captcha picture and display it
    public String pullCaptchaPicture(String CaptchaID) {
        String CaptchaPictureURL = (coreurl + "?action=usercaptchashow" + actionsource + "&debug=" + checkDebugCheckbox() + "&base64=0&id=" + CaptchaID + "&apikey=" + pullKeyFromFile());
        new DownloadImageTask((ImageView) findViewById(R.id.imageViewCaptcha)).execute(CaptchaPictureURL);
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(500);
        return CaptchaPictureURL;

    }

    // send the Captcha answer
    public void sendCaptchaAnswerText(String CaptchaAnswer, String CaptchaID) {

        String CaptchaAnswerURL = (coreurl + "?action=usercaptchacorrect" + actionsource + "&debug=" + checkDebugCheckbox() + "&antwort=" + CaptchaAnswer + "&id=" + CaptchaID + "&apikey=" + pullKeyFromFile());

        // spaces aren't allowed in URLs
        CaptchaAnswerURL = CaptchaAnswerURL.replaceAll(" ", "%20");

        String AnswerStatus = null;
        try {
            AnswerStatus = new DownloadContentTask().execute(CaptchaAnswerURL).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        Toast.makeText(getApplicationContext(), "Code: " + AnswerStatus, Toast.LENGTH_SHORT).show();
    }

    // skip Captcha
    public void skipCaptcha(String CaptchaID) {
        String CaptchaSkipURL = (coreurl + "?action=usercaptchaskip" + "&id=" + CaptchaID + "&apikey=" + pullKeyFromFile() + actionsource + "&debug=" + checkDebugCheckbox());
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

    // dialog for api key
    public void DialogKey() {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle("API-Key");
            alert.setMessage("Please enter a working key here:");

            final String keyfilename = "apikey.txt";
            final EditText input_key = new EditText(this);

            alert.setView(input_key);
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // save entered key as apikey.txt
                    OutputStreamWriter save = null;
                    try {
                        save = new OutputStreamWriter(openFileOutput(keyfilename, MODE_APPEND));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    String apikey = input_key.getText().toString();
                    try {
                        save.write(apikey);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        save.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getApplicationContext(), "Your API-Key is now saved!", Toast.LENGTH_SHORT).show();
                }
            });
    }

    // debug Checkbox listener
    public String checkDebugCheckbox() {
        CheckBox DebugBox = (CheckBox) findViewById(R.id.DebugBox);
        String debug;
        if (((CheckBox) DebugBox).isChecked()) {
            debug = "1";
        } else debug = "0";
        return debug;
    }

}