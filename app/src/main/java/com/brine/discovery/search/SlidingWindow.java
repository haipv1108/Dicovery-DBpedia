package com.brine.discovery.search;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.brine.discovery.model.SLDResult;
import com.brine.discovery.util.Config;
import com.brine.discovery.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

/**
 * Created by phamhai on 29/03/2017.
 */

public class SlidingWindow {
    private static final String TAG = SlidingWindow.class.getCanonicalName();

    private Context mContext;
    private String mKeywordSearch;

    public SlidingWindow(Context _context, String _keyword){
        this.mContext = _context;
        this.mKeywordSearch = _keyword;
        search();
    }

    private void search(){
        List<String> phrases = splitKeywordToPhrase(mKeywordSearch);
        new SLDWindowTop(mContext).execute(phrases);
        new SLDWindowRecommend(mContext).execute(phrases);
    }

    private List<String> splitKeywordToPhrase(String keywords){
        List<String> phrases = new ArrayList<>();
        List<String> listWord = Arrays.asList(keywords.split(" "));

        int lengthWords = listWord.size();
        for (int i = 0; i < lengthWords; i++) {
            if (i + 2 < lengthWords) {
                String pharse = listWord.get(i) + " " +
                        listWord.get(i + 1) + " " + listWord.get(i + 2);
                if(!phrases.contains(pharse))
                    phrases.add(pharse);
            }
            if (i + 1 < lengthWords) {
                String pharse = listWord.get(i) + " " + listWord.get(i + 1);
                if(!phrases.contains(pharse))
                    phrases.add(pharse);
            }
            if(!phrases.contains(listWord.get(i)))
                phrases.add(listWord.get(i));
        }
        showLog("Phrases: " + phrases.toString());
        return phrases;
    }

    private class SLDWindowTop extends AsyncTask<List<String>, Void, List<String>> {
        ProgressDialog progressDialog;
        Context context;
        long startTime;

        public SLDWindowTop(Context _context){
            this.context = _context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setMessage("Loading...");
            progressDialog.show();
            startTime = System.currentTimeMillis();
        }

        @Override
        protected List<String> doInBackground(List<String>... lists) {
            List<String> uris = new ArrayList<>();
            List<String> pharses = lists[0];
            HttpClient client = new DefaultHttpClient();
            for(String pharse : pharses){
                if(isStopWord(pharse)) continue;
                String url = Utils.createUrlSearchAccuracySLD(pharse);
                HttpGet request = new HttpGet(url);
                HttpResponse response;
                try {
                    response = client.execute(request);
                    Log.d("FUCK", EntityUtils.toString(response.getEntity()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return uris;
        }

        @Override
        protected void onPostExecute(List<String> uris) {
            super.onPostExecute(uris);
            progressDialog.dismiss();
            long elapsedTime = System.currentTimeMillis() - startTime;
            showLogAndToast("Top recommend time: " + elapsedTime/1000 + "s");
        }
    }

    private boolean isStopWord(String word) {
        List<String> listStopWord = Arrays.asList(Config.STOP_WORD);
        return listStopWord.contains(word.toLowerCase());
    }

    private class SLDWindowRecommend extends AsyncTask<List<String>, Void, List<SLDResult>>{
        private ProgressDialog progressDialog;
        private Context context;
        long startTime;

        public SLDWindowRecommend(Context _context){
            this.context = _context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setMessage("Loading");
            progressDialog.show();
            startTime = System.currentTimeMillis();
        }

        @Override
        protected List<SLDResult> doInBackground(List<String>... lists) {
            List<SLDResult> sldResults = new ArrayList<>();
            List<String> pharses = lists[0];
            HttpClient client = new DefaultHttpClient();
            for(String pharse : pharses){
                if(isStopWord(pharse)) continue;
                String url = Utils.createUrlSearchExpandSLD(pharse);
                HttpGet request = new HttpGet(url);
                HttpResponse httpResponse;
                try {
                    httpResponse = client.execute(request);
                    String response = EntityUtils.toString(httpResponse.getEntity());
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray data = jsonObject.getJSONObject("results").getJSONArray("bindings");
                    if(data.length() == 0){
                        showLog("No result");
                    }else{
                        for(int i = 0; i < data.length(); i++){
                            JSONObject element = data.getJSONObject(i);
                            String uri = element.getJSONObject("s").getString("value");
                            boolean isContained = false;
                            for(SLDResult sldResult : sldResults){
                                if(sldResult.getUri().equals(uri)){
                                    isContained = true;
                                    break;
                                }
                            }
                            if(!isContained){
                                String label = element.getJSONObject("label").getString("value");
                                String description = element.getJSONObject("description").getString("value");
                                String thumb = element.getJSONObject("thumb").getString("value");
                                SLDResult result = new SLDResult(uri, label, description, thumb);
                                sldResults.add(result);
                            }
                        }
                    }

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
            return sldResults;
        }

        @Override
        protected void onPostExecute(List<SLDResult> sldResults) {
            super.onPostExecute(sldResults);
            progressDialog.dismiss();
            long elapsedTime = System.currentTimeMillis() - startTime;
            showLogAndToast("Recommend time: " + elapsedTime/1000 + "s");
        }
    }

    private void showLog(String message){
        if(message != null){
            Log.d(TAG, message);
        }
    }

    private void showLogAndToast(String message){
        showLog(message);
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }
}
