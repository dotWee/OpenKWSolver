package com.dotwee.openkwsolver;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

    // double-back to close the app
    private static long back_pressed;
    String debug = "0"; // 1 = wahr
    String regex = "^[0-9]+ .+";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonStartAndPullCaptcha;
        Button buttonServerCheck;

        buttonStartAndPullCaptcha = (Button) findViewById(R.id.buttonStartAndPullCaptcha);
        buttonServerCheck = (Button) findViewById(R.id.buttonServerCheck);

        buttonServerCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String tServercheck = null;
                String tBalance = getString(R.string.balance);
                String tWorkers = (getString(R.string.workers));
                String tInQueue = (getString(R.string.captchas_in_queue));

                Pattern pworker = Pattern.compile("worker=(\\d+)");
                Pattern pqueue = Pattern.compile("queue=(\\d+)");

                try {
                    tServercheck = new DownloadContentTask().execute("http://www.9kw.eu:80/index.cgi?action=userservercheck&source=androidopenkws").get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                Matcher mworker = pworker.matcher(tServercheck);
                if (mworker.find()) {
                    TextView TextViewWorkers = (TextView) findViewById(R.id.textViewWorkers);
                    TextViewWorkers.setText(tWorkers + mworker.group(1));
                    // Toast.makeText(getApplicationContext(), TextWorkers + mworker.group(1), Toast.LENGTH_SHORT).show();
                }

                Matcher mqueue = pqueue.matcher(tServercheck);
                if (mqueue.find()) {
                    TextView TextViewInQueue = (TextView) findViewById(R.id.textViewInQueue);
                    TextViewInQueue.setText(tInQueue + mqueue.group(1));
                    // Toast.makeText(getApplicationContext(), TextInQueue + mqueue.group(1), Toast.LENGTH_SHORT).show();
                }

                TextView TextViewBalance = (TextView) findViewById(R.id.textViewBalance);
                TextViewBalance.setText(tBalance + pullBalanceCount());

            }
        });

        buttonStartAndPullCaptcha.setText(getString(R.string.start));
        buttonStartAndPullCaptcha.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                clearImageView();
                clearCaptchaAnswer();
                clearCurrentCaptchaID();

                final String CaptchaID = pullCaptchaIDonlyText();
                String TextCaptchaID = (getString(R.string.current_captcha_id));

                TextView TextViewCurrentCaptchaID = (TextView) findViewById(R.id.textViewCurrentCaptchaID);
                TextViewCurrentCaptchaID.setText(CaptchaID);

                TextView TextViewCurrentCaptchaText = (TextView) findViewById(R.id.textViewCurrentCaptchaIDText);
                TextViewCurrentCaptchaText.setText(TextCaptchaID);

                if (!CaptchaID.matches(regex)) {

                    pullCaptchaPicture(CaptchaID);

                    // TODO Progressbar Countdown

                    Button buttonSendAnswer = (Button) findViewById(R.id.buttonSendAnswer);
                    buttonSendAnswer.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            EditText EditTextCaptchaAnswer = (EditText) findViewById(R.id.editTextCaptchaAnswer);
                            String CaptchaAnswer = EditTextCaptchaAnswer.getText().toString();
                            sendCaptchaAnswer(CaptchaAnswer, CaptchaID);

                            clearImageView();
                            clearCaptchaAnswer();
                            clearCurrentCaptchaID();
                        }
                    });

                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.error_with_captcha) + CaptchaID, Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // TODO Dynamische und automatisch aktuallisierende TextView in ActtionBar zur Guthabenanzeige

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_enter_apikey:
                DialogAskForAPI();
                return true;
            case R.id.action_stop_fetching:
                super.onBackPressed();
                Toast.makeText(getApplicationContext(), getString(R.string.restart_app_to_continue), Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_show_balance:
                TextView action_show_balance = (TextView) findViewById(R.id.action_show_balance);
                action_show_balance.setText(getString(R.string.balance) + pullBalanceCount());
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // ask for API-Key in dialog
    public void DialogAskForAPI() {

        File mayexisting_apikeytxt = new File("apikey.txt");
        if (mayexisting_apikeytxt.exists()) {

            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle("API-Key");
            alert.setMessage(getString(R.string.dialog_enter_here_apikey));

            final EditText input_APIKey;
            final String apikeytxt = "apikey.txt";

            input_APIKey = new EditText(this);
            input_APIKey.setText(pullKeyFromFile());
            alert.setView(input_APIKey);

            alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int whichButton) {
                    // API-Key als apikey.txt speichern
                    OutputStreamWriter out = null;
                    try {
                        out = new OutputStreamWriter(openFileOutput(apikeytxt, MODE_APPEND));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    String apikey = input_APIKey.getText().toString();
                    try {
                        out.write(apikey);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getApplicationContext(), "Your API-Key is now saved!", Toast.LENGTH_SHORT).show();
                }
            });
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });

            alert.show();
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle("API-Key");
            alert.setMessage("Enter here your API-Key");

            final EditText input_APIKey;
            final String apikeytxt = "apikey.txt";

            input_APIKey = new EditText(this);
            alert.setView(input_APIKey);

            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int whichButton) {
                    // API-Key als apikey.txt speichern
                    OutputStreamWriter out = null;
                    try {
                        out = new OutputStreamWriter(openFileOutput(apikeytxt, MODE_APPEND));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    String apikey = input_APIKey.getText().toString();
                    try {
                        out.write(apikey);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getApplicationContext(), "Your API-Key is now saved!", Toast.LENGTH_SHORT).show();
                }
            });
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });

            alert.show();
        }

    }

    public void onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis()) super.onBackPressed();
        else
            Toast.makeText(getBaseContext(), getString(R.string.press_again_to_close), Toast.LENGTH_SHORT).show();
        back_pressed = System.currentTimeMillis();
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
            Toast.makeText(getApplicationContext(), getString(R.string.warning_couldnt_find_apikey), Toast.LENGTH_LONG).show();
            DialogAskForAPI();
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return apikey;
    }

    // request CaptchaID
    public String pullCaptchaIDonlyText() {

        String CaptchaIDURL = ("http://www.9kw.eu:80/index.cgi?action=usercaptchanew&source=androidopenkws&mouse=0&confirm=0&nocaptcha=1&debug=" + debug + "&apikey=" + pullKeyFromFile());
        String CaptchaID = null;

        try {
            CaptchaID = new DownloadContentTask().execute(CaptchaIDURL).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return CaptchaID;
    }

    // request Captcha and display it on ImageView
    public String pullCaptchaPicture(String CaptchaID) {

        String CaptchaPictureURL = ("http://www.9kw.eu:80/index.cgi?action=usercaptchashow&source=androidopenkws&debug=" + debug + "&base64=0&id=" + CaptchaID + "&apikey=" + pullKeyFromFile());
        new DownloadImageTask((ImageView) findViewById(R.id.imageViewReceivedCaptcha)).execute(CaptchaPictureURL);



        return CaptchaPictureURL;


    }

    // cleanup ImageView
    public void clearImageView() {
        ImageView ImageView = (ImageView) findViewById(R.id.imageViewReceivedCaptcha);
        ImageView.setImageDrawable(null);
    }

    // cleanup CurrentCaptchaID
    public void clearCurrentCaptchaID() {
        TextView TextViewID = (TextView) findViewById(R.id.textViewCurrentCaptchaID);
        TextViewID.setText(null);

        TextView TextViewText = (TextView) findViewById(R.id.textViewCurrentCaptchaIDText);
        TextViewText.setText(null);
    }

    // request balance
    public String pullBalanceCount() {

        String Balance = null;
        String BalanceURL = ("http://www.9kw.eu:80/index.cgi?action=usercaptchaguthaben&source=androidopenkws&apikey=" + pullKeyFromFile());

        try {
            Balance = new DownloadContentTask().execute(BalanceURL).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return Balance;

    }

    // cleanup TextViewCaptchaAnswer
    public void clearCaptchaAnswer() {
        EditText EditTextAnswer = (EditText) findViewById(R.id.editTextCaptchaAnswer);
        EditTextAnswer.setText(null);
    }

    // send the Captcha answer
    public void sendCaptchaAnswer(String CaptchaAnswer, String CurrentCaptchaID) {

        String CaptchaAnswerURL = ("http://www.9kw.eu:80/index.cgi?action=usercaptchacorrect&source=androidopenkws&debug=" + debug + "&antwort=" + CaptchaAnswer + "&id=" + CurrentCaptchaID + "&apikey=" + pullKeyFromFile());

        // Spaces aren't allowed in URLs
        CaptchaAnswerURL = CaptchaAnswerURL.replaceAll(" ", "%20");

        String AnswerStatus = null;
        try {
            AnswerStatus = new DownloadContentTask().execute(CaptchaAnswerURL).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        Toast.makeText(getApplicationContext(), getString(R.string.code) + AnswerStatus, Toast.LENGTH_SHORT).show();
    }


}