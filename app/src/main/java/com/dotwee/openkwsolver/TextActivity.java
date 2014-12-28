package com.dotwee.openkwsolver;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;


public class TextActivity extends ActionBarActivity {

    String coreurl = "http://www.9kw.eu:80/index.cgi";
    String actionsource = "&source=androidopenkws";
    String debug = "1"; // 1 = wahr
    String regex = "^[0-9]+ .+";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);

        Button buttonBeginn = (Button) findViewById(R.id.buttonLeft);

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
            Toast.makeText(getApplicationContext(), "Housten, couldn't find API-Key. You may want to enter it again.", Toast.LENGTH_LONG).show();
            ;
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
    public String pullOnlyTextCaptchaID() {
        String CaptchaIDURL = (coreurl + "?action=usercaptchanew" + actionsource + "&mouse=0&confirm=0&nocaptcha=1&debug=" + debug + "&apikey=" + pullKeyFromFile());
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
}
