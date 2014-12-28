package com.dotwee.openkwsolver;

import android.app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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

}