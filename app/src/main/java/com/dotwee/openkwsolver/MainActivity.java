package com.dotwee.openkwsolver;

import android.app.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
    String debug = "0"; // 1 = wahr
    String regex = "^[0-9]+ .+";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);
        pullKeyFromFile();

        final Button buttonBeginn = (Button) findViewById(R.id.buttonLeft);

        buttonBeginn.setText("Beginn");
        buttonBeginn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                clearImageViewAndEditText(); // First of all, clear the ImageView and EditText (just to be save)

                final String CaptchaID = pullOnlyTextCaptchaID();

                if (!CaptchaID.matches(regex)) {
                    pullCaptchaPicture(CaptchaID); // Pull the Captcha picture and display it

                    // TODO Progressbar Countdown

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

        File MaybeExistingKey = new File("apikey.txt");
        if (MaybeExistingKey.exists()) {

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
                Toast.makeText(getApplicationContext(), "Housten, couldn't find API-Key. You may want to enter it again.", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else DialogKey();

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
    public String pullOnlyTextCaptchaID() {
        String CaptchaIDURL = (coreurl + "?action=usercaptchanew" + actionsource + "&confirm=1&nocaptcha=1&debug=" + debug + "&apikey=" + pullKeyFromFile());
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
        String CaptchaPictureURL = (coreurl + "?action=usercaptchashow" + actionsource + "&debug=" + debug + "&base64=0&id=" + CaptchaID + "&apikey=" + pullKeyFromFile());
        new DownloadImageTask((ImageView) findViewById(R.id.imageViewCaptcha)).execute(CaptchaPictureURL);

        return CaptchaPictureURL;

    }

    // send the Captcha answer
    public void sendCaptchaAnswerText(String CaptchaAnswer, String CaptchaID) {

        String CaptchaAnswerURL = (coreurl + "?action=usercaptchacorrect" + actionsource + "&debug=" + debug + "&antwort=" + CaptchaAnswer + "&id=" + CaptchaID + "&apikey=" + pullKeyFromFile());

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

    // dialog for api key
    public void DialogKey() {
        File MaybeExistingKey = new File("apikey.txt");
        if (MaybeExistingKey.exists()) {
            // ask kindly for the key
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle("API-Key");
            alert.setMessage("Please enter a working key here:");

            final String keyfilename = "apikey.txt";
            final EditText input_key = new EditText(this);
            input_key.setText(pullKeyFromFile());

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
        } else {
            // ask kindly for the key
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle("API-Key");
            alert.setMessage("To use this application, you need an API-Key from 9kw.eu. Please enter it here:");

            final String keyfilename = "apikey.txt";
            final EditText input_key = new EditText(this);
            input_key.setText(null);

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
    }

}