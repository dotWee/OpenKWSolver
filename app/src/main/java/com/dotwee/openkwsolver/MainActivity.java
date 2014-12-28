package com.dotwee.openkwsolver;

import android.app.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void openTextActivity(View view) {
        Intent intent = new Intent(MainActivity.this, TextActivity.class);
        startActivity(intent);
    }

    public void openClickActivity(View view) {
        Intent intent = new Intent(MainActivity.this, ClickActivity.class);
        startActivity(intent);
    }

    public void openConfirmActivity(View view) {
        Intent intent = new Intent(MainActivity.this, ConfirmActivity.class);
        startActivity(intent);
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
            input_key.setText(pullKeyFromFile()); // TODO read the key from file

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