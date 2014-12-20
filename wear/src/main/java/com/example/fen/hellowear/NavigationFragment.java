package com.example.fen.hellowear;

import android.app.Fragment;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import java.util.Timer;
import java.util.TimerTask;

public class NavigationFragment extends Fragment implements SensorEventListener {

    /** How long to keep the screen on when no activity is happening **/
    private static final long SCREEN_ON_TIMEOUT_MS = 20000; // in milliseconds

    /** an up-down movement that takes more than this will not be registered as such **/
    private static final long TIME_THRESHOLD_NS = 2000000000; // in nanoseconds (= 2sec)

    /**
     * Earth gravity is around 9.8 m/s^2 but user may not completely direct his/her hand vertical
     * during the exercise so we leave some room. Basically if the x-component of gravity, as
     * measured by the Gravity sensor, changes with a variation (delta) > GRAVITY_THRESHOLD,
     * we consider that a successful count.
     */
    private static final float GRAVITY_THRESHOLD = 7.0f;
    private static final String TAG = "NavigationFragment";


    private Button startButton;

    private Sensor mSensor;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private Handler mHandler;
    private long mLastTime;
    private boolean mUp;
    private SensorManager mSensorManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.navigation_layout, container, false);

        startButton = (Button) view.findViewById(R.id.start_magic);

        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

//        setCounter(Utils.getCounterFromPreference(getActivity()));
        mHandler = new Handler();
//        startAnimation();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mSensorManager.registerListener(this, mSensor,
                SensorManager.SENSOR_DELAY_NORMAL)) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.wtf(TAG, "Successfully registered for the sensor updates");
            }
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        detectJump(event.values[0], event.timestamp);
    }

    private void detectJump(float xValue, long timestamp) {
        if ((Math.abs(xValue) > GRAVITY_THRESHOLD)) {
            if(timestamp - mLastTime < TIME_THRESHOLD_NS && mUp != (xValue > 0)) {
                Log.wtf("detectJump", "Juuuump!");
//                onJumpDetected(!mUp);
                renewTimer();
            }
            mUp = xValue > 0;
            mLastTime = timestamp;
        }
    }

    private void renewTimer() {
        if (null != mTimer) {
            mTimer.cancel();
        }
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.wtf(TAG,
                            "Removing the FLAG_KEEP_SCREEN_ON flag to allow going to background");
                }
                resetFlag();
            }
        };
        mTimer = new Timer();
        mTimer.schedule(mTimerTask, SCREEN_ON_TIMEOUT_MS);
    }

    private void resetFlag() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.wtf(TAG, "Resetting FLAG_KEEP_SCREEN_ON flag to allow going to background");
                }
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                getActivity().finish();
            }
        });
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
