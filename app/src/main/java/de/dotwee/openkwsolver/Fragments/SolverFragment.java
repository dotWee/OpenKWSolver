/*
 *             DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
 *                     Version 2, December 2004
 *
 *  Copyright (C) 2015 Lukas "dotwee" Wolfsteiner <lukas@wolfsteiner.de>
 *
 *  Everyone is permitted to copy and distribute verbatim or modified
 *  copies of this license document, and changing it is allowed as long
 *  as the name is changed.
 *
 *             DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
 *    TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
 *
 *   0. You just DO WHAT THE FUCK YOU WANT TO.
 *
 */

package de.dotwee.openkwsolver.Fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.github.clans.fab.FloatingActionButton;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import de.dotwee.openkwsolver.R;
import de.dotwee.openkwsolver.Tools.StaticHelpers;

public class SolverFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {
    public static final String URL_9WK = "http://www.9kw.eu:80/index.cgi";
    public static final String URL_PARAMETER_CAPTCHA_SHOW = "?action=usercaptchashow";
    public static final String URL_PARAMETER_SOURCE = "&source=androidopenkws";
    private static final String LOG_TAG = "SolverFragment";

    // main widgets
    public static TextView textViewCaptchaDesc, textViewCaptcha, textViewBalance;
    public static FloatingActionButton floatingActionButton;
    public static Button buttonPull, buttonSkip, buttonSend;
    public static ImageView imageViewCaptcha;
    public static EditText editTextAnswer;
    public static Thread balanceThread;

    private boolean _stateRequestCompleted = false, _stateStartCompleted = false, _stateSolveCompleted = false, _stateContinueCompleted = false, _stateEndCompleted = false;
    private String _captchaID, _captchaURLImage, _captchaAnswer;
    private CountDownTimer _countDownTimer;
    private SharedPreferences _sharedPreferences;
    @SuppressWarnings("FieldNotUsedInToString")
    private ProgressBar progressBar;
    private Vibrator vibrator;
    private Context baseContext;
    private Target imageTarget;
    private View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        baseContext = getActivity().getBaseContext();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_solver, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;

        stateStart();
        stateReset();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (balanceThread != null) if (balanceThread.isAlive()) balanceThread.interrupt();
    }

    public void updateBalance() {
        textViewBalance.setText(StaticHelpers.getBalance(baseContext));
    }

    // BalanceThread: Update the balance every 5 seconds
    public void balanceThread() {
        balanceThread = new Thread() {

            @Override
            public void run() {
                while (!isInterrupted()) {
                    try {
                        Thread.sleep(5000); // 5000ms = 5s
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (view != null) {
                                    textViewBalance.setText(StaticHelpers.getBalance(getActivity()));
                                }
                            }
                        });
                    }
                }

            }
        };

        // check if thread isn't already running.
        if (balanceThread.isAlive()) {
            balanceThread.interrupt();
        }

        // if not, start it
        else {
            balanceThread.start();
        }
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        switch (viewId) {
            case R.id.buttonPull:
                if (StaticHelpers.isAutoBalanceEnabled(baseContext)) balanceThread();
                floatingActionButton.setEnabled(false);
                if (isReady()) stateRequest();
                break;

            case R.id.buttonSend:
                stateContinue(true);
                break;

            case R.id.buttonSkip:
                stateContinue(false);
                break;

            case R.id.fab:
                buttonPull.setEnabled(true);
                buttonPull.performClick();
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        onClick(v);
        return false;
    }

    private boolean isReady() {
        if (StaticHelpers.getApiKey(baseContext) != null) {
            if (StaticHelpers.isNetworkAvailable(baseContext)) {
                return true;
            } else toastBreak("No network available.");
        } else toastBreak("Set a API-Key to start.");
        return false;
    }

    private void toastBreak(String message) {
        Toast.makeText(baseContext, message, Toast.LENGTH_SHORT).show();
    }

    private void stateStart() {
        _stateStartCompleted = false;

        vibrator = (Vibrator) baseContext.getSystemService(Context.VIBRATOR_SERVICE);
        imageViewCaptcha = (ImageView) view.findViewById(R.id.imageViewCaptcha);
        textViewCaptchaDesc = (TextView) view.findViewById(R.id.textViewDescID);
        textViewCaptcha = (TextView) view.findViewById(R.id.textViewID);
        textViewBalance = (TextView) view.findViewById(R.id.textViewBA);

        buttonPull = (Button) view.findViewById(R.id.buttonPull);
        buttonSkip = (Button) view.findViewById(R.id.buttonSkip);
        buttonSend = (Button) view.findViewById(R.id.buttonSend);

        editTextAnswer = (EditText) view.findViewById(R.id.editTextAnswer);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        // hide captchaid textviews if disabled
        if (!StaticHelpers.isCaptchaIDEnabled(baseContext)) {
            textViewCaptchaDesc.setVisibility(View.GONE);
            textViewCaptcha.setVisibility(View.GONE);
        }

        if (!getResources().getBoolean(R.bool.isTablet)) {
            floatingActionButton = (FloatingActionButton) view.findViewById(R.id.fab);
            floatingActionButton.setOnClickListener(this);
            floatingActionButton.setOnLongClickListener(this);
        }

        // fix edittext width
        editTextAnswer.setMaxWidth(editTextAnswer.getWidth());

        // init listeners
        buttonPull.setOnClickListener(this);
        buttonPull.setOnLongClickListener(this);

        buttonSend.setOnClickListener(this);
        buttonSend.setOnLongClickListener(this);

        buttonSkip.setOnClickListener(this);
        buttonSkip.setOnLongClickListener(this);

        imageTarget = new com.squareup.picasso.Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
                imageViewCaptcha.setImageBitmap(bitmap);
                final int[] i = {0};

                _countDownTimer = new CountDownTimer(30000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        i[0]++;
                        progressBar.setProgress(i[0]);
                    }

                    @Override
                    public void onFinish() {
                        stateContinue(false);
                    }
                }.start();
            }

            @Override
            public void onBitmapFailed(Drawable drawable) {
                stateContinue(false);

                toastBreak("Error with Captcha. Reloading...");
            }

            @Override
            public void onPrepareLoad(Drawable drawable) {
                imageViewCaptcha.setImageDrawable(drawable);
                notifyUser();
            }
        };

        isReady();

        buttonPull.setEnabled(false);
        buttonSkip.setEnabled(false);
        buttonSend.setEnabled(false);

        _stateEndCompleted = true;
    }

    private void notifyUser() {
        if (StaticHelpers.isVibrateEnabled(baseContext)) vibrator.vibrate(500);
        if (StaticHelpers.isSoundEnabled(baseContext))
            RingtoneManager.getRingtone(baseContext, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).play();
    }

    private void stateRequest() {
        _stateRequestCompleted = false;

        if (StaticHelpers.isNetworkAvailable(baseContext)) {
            if (StaticHelpers.getApiKey(baseContext) != null) {
                _captchaID = StaticHelpers.requestCaptchaID(baseContext, _sharedPreferences.getBoolean("pref_automation_loop", false), 2);
                textViewCaptcha.setText(_captchaID);
                buttonPull.setEnabled(false);
                buttonSend.setEnabled(true);
                buttonSkip.setEnabled(true);
                updateBalance();

                _stateRequestCompleted = true;
                stateSolve();

            }
            toastBreak("Set a API-Key to start.");
        } else toastBreak("No network available!");
    }

    private void stateSolve() {
        _stateSolveCompleted = false;

        _captchaURLImage = (URL_9WK + URL_PARAMETER_CAPTCHA_SHOW + URL_PARAMETER_SOURCE +
                StaticHelpers.getExternalParameter(baseContext, 2) + "&id=" + _captchaID +
                StaticHelpers.getApiKey(baseContext));

        Picasso.with(baseContext)
                .load(_captchaURLImage)
                .placeholder(R.drawable.captcha_loading_animation)
                .resize(0, 200)
                .into(imageTarget);

        _stateSolveCompleted = true;
    }

    private void stateContinue(boolean send) {
        _stateContinueCompleted = false;

        _captchaAnswer = editTextAnswer.getText().toString();
        editTextAnswer.setText(null);

        if (send) {
            if (_captchaAnswer != null) {
                StaticHelpers.sendCaptchaByID(baseContext, _captchaID, _captchaAnswer, false);

                if (StaticHelpers.isLoopEnabled(baseContext)) {
                    Log.i("OnClickSend", "Loop-Mode");
                    buttonPull.performClick();
                } else {
                    buttonPull.setEnabled(true);
                }

                _stateContinueCompleted = true;
            } else Toast.makeText(baseContext, R.string.main_toast_emptyanswer, Toast.LENGTH_LONG).show();
        } else if (!send) {
            StaticHelpers.skipCaptchaByID(baseContext, StaticHelpers.getApiKey(baseContext));
            buttonPull.setEnabled(true);
            _stateContinueCompleted = true;
        }

        if (_stateContinueCompleted) stateReset();
    }

    private void stateReset() {
        _stateEndCompleted = false;

        imageViewCaptcha.setImageDrawable(null);
        textViewCaptcha.setText(null);
        editTextAnswer.setText(null);
        progressBar.setProgress(0);

        if (_countDownTimer != null) _countDownTimer.cancel();

        _captchaAnswer = null;
        _captchaID = null;

        _stateEndCompleted = true;
    }

    @Override
    public String toString() {
        return "SolverFragment{" +
                "_stateRequestCompleted=" + _stateRequestCompleted +
                ", _stateStartCompleted=" + _stateStartCompleted +
                ", _stateSolveCompleted=" + _stateSolveCompleted +
                ", _stateContinueCompleted=" + _stateContinueCompleted +
                ", _stateEndCompleted=" + _stateEndCompleted +
                ", _captchaID='" + _captchaID + '\'' +
                ", _captchaURLImage='" + _captchaURLImage + '\'' +
                ", _captchaAnswer='" + _captchaAnswer + '\'' +
                '}';
    }
}
