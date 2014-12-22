package com.example.fen.hellowear;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.view.GridViewPager;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

public class MyActivity extends Activity implements SensorEventListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, MessageApi.MessageListener {


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
    private static final float GRAVITY_THRESHOLD = 6.5f;
//    private static final String TAG = "NavigationFragment";



    /** Request code for launching the Intent to resolve Google Play services errors. */
    private static final int REQUEST_RESOLVE_ERROR = 1000;


    private static final String TAG = "JJMainActivity";
    private static final String START_ACTIVITY_PATH = "/start/MainActivity";


    private GoogleApiClient mGoogleApiClient;
    private boolean mResolvingError;

    public int oldLineNum;
    public int oldColumNum;

    private Timer mTimer;
    private TimerTask mTimerTask;
    private Handler mHandler;
    private long mLastTime;
    private boolean mUp;
    private SensorManager mSensorManager;
    ImageView leftPagePoint;
    ImageView centerPagePoint;
    ImageView rightPagePoint;

    private Sensor mSensor;

    public MyActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        final Resources res = getResources();

        leftPagePoint = (ImageView) findViewById(R.id.indicator_left);
        centerPagePoint = (ImageView) findViewById(R.id.indicator_center);
        rightPagePoint = (ImageView) findViewById(R.id.indicator_right);



        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

//        final GridViewPager pager = (android.support.wearable.view.GridViewPager) findViewById(R.id.grid_pager);
        final HorizontalListPager pager = (HorizontalListPager) findViewById(R.id.grid_pager);
        pager.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                // Adjust page margins:
                //   A little extra horizontal spacing between pages looks a bit
                //   less crowded on a round display.
                final boolean round = insets.isRound();
                int rowMargin = res.getDimensionPixelOffset(R.dimen.page_row_margin);
                int colMargin = res.getDimensionPixelOffset(round ?
                        R.dimen.page_column_margin_round : R.dimen.page_column_margin);
                pager.setPageMargins(rowMargin, colMargin);

                // GridViewPager relies on insets to properly handle
                // layout for round displays. They must be explicitly
                // applied since this listener has taken them over.
                pager.onApplyWindowInsets(insets);
                return insets;
            }
        });

        pager.setAdapter(new SampleGridPagerAdapter(this, getFragmentManager()));
        pager.setOnPageChangeListener(new GridViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int curLineNum, int curColumnNum, float v, float v2, int i3, int i4) {
//                updateNavigationPoint(curColumnNum);
                if (curColumnNum < oldColumNum) {
                    new StartWearableActivityMessage("left").execute();
                } else if (curColumnNum > oldColumNum) {
                    new StartWearableActivityMessage("right").execute();
                }

                if (curLineNum < oldLineNum) {
                    new StartWearableActivityMessage("up").execute();
                } else if (curLineNum > oldLineNum) {
                    new StartWearableActivityMessage("down").execute();
                }

                Log.wtf("onPageSelected","curLineNum="+curLineNum);
                Log.wtf("onPageSelected","oldLineNum="+oldLineNum);
                Log.wtf("onPageSelected","curColumnNum="+curColumnNum);
                Log.wtf("onPageSelected","oldColumNum="+oldColumNum);


                oldLineNum = curLineNum;
                oldColumNum = curColumnNum;
            }

            @Override
            public void onPageSelected(int curLineNum, int curColumnNum) {
                updateNavigationPoint(curColumnNum);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

    }


    private void updateNavigationPoint(int col) {
        Log.wtf("updateNavigationPoint", "col="+col);
//        View root = ((Activity)mContext).getWindow().getDecorView().findViewById(android.R.id.content);

        switch(col) {
            case 0:
                leftPagePoint.setImageResource(R.drawable.full_10);
                centerPagePoint.setImageResource(R.drawable.empty_10);
                Log.wtf("updateNavigationPoint", "0 updated");
                break;

            case 1:
                centerPagePoint.setImageResource(R.drawable.full_10);
                leftPagePoint.setImageResource(R.drawable.empty_10);
                rightPagePoint.setImageResource(R.drawable.empty_10);
                Log.wtf("updateNavigationPoint", "1 updated");
                break;

            case 2:
                rightPagePoint.setImageResource(R.drawable.full_10);
                centerPagePoint.setImageResource(R.drawable.empty_10);
                Log.wtf("updateNavigationPoint", "2 updated");
                break;


        }
    }


    private class StartWearableActivityMessage extends AsyncTask<Void, Void, Void> {

        private final String message;

        public StartWearableActivityMessage(String message) {
            this.message = message;
        }

        @Override
        protected Void doInBackground(Void... args) {
            Collection<String> nodes = getNodes();
            for (String node : nodes) {
                sendStartActivityMessage(node, message);
            }
            return null;
        }
    }
    private void sendStartActivityMessage(String node, String message) {

//        Log.wtf(TAG, "sendStartActivityMessage "+node);

        Wearable.MessageApi.sendMessage(
                mGoogleApiClient, node, START_ACTIVITY_PATH, message.getBytes()).setResultCallback(
                new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                        if (!sendMessageResult.getStatus().isSuccess()) {
                            Log.wtf(TAG, "Failed to send message with status code: "
                                    + sendMessageResult.getStatus().getStatusCode());
                        } else {
//                            Log.wtf(TAG, "Success send"
//                                    + sendMessageResult.getStatus().getStatusCode());
                        }
                    }
                }
        );
    }

    private Collection<String> getNodes() {
        HashSet<String> results= new HashSet<String>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        for (Node node : nodes.getNodes()) {
            results.add(node.getId());
        }
        return results;
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (!mResolvingError) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        if (!mResolvingError) {
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.wtf(TAG,"message from phone:"+messageEvent.toString());
    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.wtf(TAG, "Google API Client was connected");
        mResolvingError = false;

        Wearable.MessageApi.addListener(mGoogleApiClient, this);
    }
    @Override
    public void onConnectionSuspended(int i) {
        Log.wtf(TAG, "Connection to Google API client was suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            Log.e(TAG, "Connection to Google API client has failed");
            mResolvingError = false;
//            mSendButton.setEnabled(false);
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
        }
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
            if (timestamp - mLastTime < TIME_THRESHOLD_NS && mUp != (xValue > 0)) {
                Log.wtf("detectJump", "Juuuump!");
//                onJumpDetected(!mUp);
                // hands go from down to top, hence

                if (mUp) {
                    new StartWearableActivityMessage("up").execute();
                } else {
                    new StartWearableActivityMessage("down").execute();
                }
                renewTimer();
            }
            mUp = xValue > 0;
            mLastTime = timestamp;
        }
    }

    /**
     * Called on detection of a successful down -> up or up -> down movement of hand.
     */
//    private void onJumpDetected(boolean up) {
//        // we only count a pair of up and down as one successful movement
//        if (up) {
//            new StartWearableActivityMessage("down").execute();
//        }
//
//    }

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
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                finish();
            }
        });
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
