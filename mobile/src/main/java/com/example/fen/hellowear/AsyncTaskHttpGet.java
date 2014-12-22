package com.example.fen.hellowear;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

public class AsyncTaskHttpGet extends AsyncTask<String, String, String> {

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected String doInBackground(String... params) {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(params[0]);
        Log.wtf("AsyncTaskHttpGet", "httpGet url="+params[0]);
        try {
            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
