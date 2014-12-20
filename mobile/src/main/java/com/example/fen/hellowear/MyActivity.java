package com.example.fen.hellowear;

import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;


public class MyActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,  MessageApi.MessageListener {

    private static final String PRESNTATION_URL = "http://present-control.herokuapp.com/";

//    getCurrentAddress gives you json with ip and port of the server
//    private static final String GET_CURRENT_ADDRESS_URL = "http://present-control.herokuapp.com/getCurrentAddress";

    private GoogleApiClient mGoogleApiClient;

    private static final String TAG = "MainActivity/Phone";
    private boolean mResolvingError;

    private static final int REQUEST_RESOLVE_ERROR = 1000;
    SocketIO socket = null;
    private Button rightButton;
    private Button leftButton;
    private Button topButton;
    private Button bottomButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        rightButton = (Button) findViewById(R.id.button_right);
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    swipeRight();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        leftButton = (Button) findViewById(R.id.button_left);
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    swipeLeft();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        topButton = (Button) findViewById(R.id.button_top);
        topButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    swipeUp();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        bottomButton = (Button) findViewById(R.id.button_bottom);
        bottomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    swipeDown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public void swipeRight() throws IOException {
        Log.wtf("SWIPE RIGHT","SWIPE RIGHT");

        new AsyncTaskParseJson().execute( PRESNTATION_URL +"right_myppt/");
    }
    public void swipeLeft() throws IOException {
        Log.wtf("SWIPE LEFT","SWIPE LEFT");
        new AsyncTaskParseJson().execute(PRESNTATION_URL+"left_myppt/");
    }
    public void swipeUp() throws IOException {
        Log.wtf("SWIPE UP","SWIPE UP");
        new AsyncTaskParseJson().execute(PRESNTATION_URL+"up_myppt/");
    }
    public void swipeDown() throws IOException {
        Log.wtf("SWIPE DOWN","SWIPE DOWN");
        new AsyncTaskParseJson().execute(PRESNTATION_URL+"down_myppt/");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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
    public void onConnected(Bundle bundle) {
        Log.wtf(TAG, "Google API Client was connected");
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
    }
    @Override
    public void onConnectionSuspended(int i) {
        Log.wtf(TAG, "Connection to Google API client was suspended");
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                String message = new String(messageEvent.getData());
//                outputView.setText(message);

                if (message.equals("up")) {
                    try {
                        swipeUp();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (message.equals("down")) {
                    try {
                        swipeDown();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (message.equals("left")) {
                    try {
                        swipeLeft();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (message.equals("right")) {
                    try {
                        swipeRight();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                Log.wtf(TAG,"message from client:"+messageEvent.toString());
            }
        });
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (mResolvingError) {
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                mGoogleApiClient.connect();
            }
        } else {
            Log.e(TAG, "Connection to Google API client has failed");
            mResolvingError = false;
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
        }
    }

    // TODO: be able to connect to server socket and interchange socket messages with it:
    public void runSocketConnection(String socketAddress) {
        try {
            socket = new SocketIO(socketAddress);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        socket.connect(new IOCallback() {
            @Override
            public void onMessage(JSONObject json, IOAcknowledge ack) {
                try {
                    System.out.println("Server said:" + json.toString(2));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMessage(String data, IOAcknowledge ack) {
                System.out.println("Server said: " + data);
            }

            @Override
            public void onError(SocketIOException socketIOException) {
                System.out.println("an Error occured");
                socketIOException.printStackTrace();
            }

            @Override
            public void onDisconnect() {
                System.out.println("Connection terminated.");
            }

            @Override
            public void onConnect() {
                System.out.println("Connection established");
            }

            @Override
            public void on(String event, IOAcknowledge ack, Object... args) {
                System.out.println("Server triggered event '" + event + "'");
            }
        });
    }


   // TODO maybe parse some HTTP replies from the presentation to show notes/slides in the app as well
    public class AsyncTaskParseJson extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... arg0) {

            JsonParser jParser = new JsonParser();
            JSONObject json = jParser.getJSONFromUrl(arg0[0]);

            if (json!=null) {
                // maybe parse reply somehow, but for now just sending request to move slides
            }

            return null;
        }

        @Override
        protected void onPostExecute(String strFromDoInBg) {
        }
    }

}


