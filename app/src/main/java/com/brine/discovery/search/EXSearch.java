package com.brine.discovery.search;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.brine.discovery.AppController;
import com.brine.discovery.activity.MainActivity;
import com.brine.discovery.activity.RecommendActivity;
import com.brine.discovery.model.Recommend;
import com.brine.discovery.util.Config;
import com.brine.discovery.util.DbpediaConstant;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

import java.util.Arrays;
import java.util.List;

/**
 * Created by phamhai on 29/03/2017.
 */

public class EXSearch {
    private static final String TAG = EXSearch.class.getCanonicalName();
    private static final int DEFAULT_TIMEOUT = 20 * 1000;

    private Context mContext;
    private List<Recommend> mRecommends;
    private ProgressDialog mProgressDialog;
    private Callback mCallback;

    public interface Callback{
        void showResultRecommend(String response);
    }

    public EXSearch(Context _context, List<Recommend> _recommends, Callback _callback){
        this.mContext = _context;
        this.mRecommends = _recommends;
        this.mCallback = _callback;

        search();
    }

    private void search(){
        AppController.getInstance().setUriDecovery(mRecommends);
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.show();

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        for(Recommend param : mRecommends){
            params.add("nodes[]", param.getUri());
        }
        showLog("Params: " + params.toString());

        final long startTime = System.currentTimeMillis();

        client.post(Config.DISCOVERYHUB_RECOMMEND_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                showLog(response);
                long elapsedTime = System.currentTimeMillis() - startTime;
                showLogAndToast("Time request: " + elapsedTime/1000 + "s");
                parser(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                showLogAndToast("Error request to server. Try again!");
                mProgressDialog.dismiss();
            }
        });
    }

    private void parser(String response){
        if(response == null) return;
        try {
            JSONObject json = new JSONObject(response);
            String id = json.getString("id");
            List<String> splitId = Arrays.asList(id.split("/"));
            String key = splitId.get(splitId.size() - 1);
            getDataRecomendation(key);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }

    private void getDataRecomendation(String key){
        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(DEFAULT_TIMEOUT);
        String url = "http://api.discoveryhub.co/recommendations/" + key;
        showLog("RECOMMEND API: " + url);
        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                mProgressDialog.dismiss();
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    if(jsonArray.length() == 1 &&
                            jsonArray.getJSONObject(0).getJSONArray("results").length() == 1){
                        showLogAndToast("No results. Try again!");
                    }else if(checkRusultContent(response)){
                        mCallback.showResultRecommend(response);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                showLogAndToast("Error request to server. Try again!");
                mProgressDialog.dismiss();
            }
        });
    }

    private boolean checkRusultContent(String response){
        try {
            JSONArray jsonArray = new JSONArray(response);
            for(int i = 0; i < jsonArray.length(); i++){
                String uri = jsonArray.getJSONObject(i).getString("uri");
                if(DbpediaConstant.isContext(uri)){
                    return true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
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

