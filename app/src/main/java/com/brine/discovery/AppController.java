package com.brine.discovery;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;
import com.brine.discovery.model.Recommend;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by phamhai on 08/02/2017.
 */

public class AppController extends Application {
    private static final String TAG = AppController.class.getCanonicalName();

    private static AppController sInstance;
    private RequestQueue mRequestQueue;
    private List<Recommend> mCurrentUriDecovery;
    private List<Recommend> mPrevUriDecovery;

    @Override
    public void onCreate() {
        super.onCreate();
        mCurrentUriDecovery = new ArrayList<>();
        mPrevUriDecovery = new ArrayList<>();
        sInstance = this;
    }

    public static synchronized AppController getInstance() {
        return sInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        VolleyLog.d("Adding request to queue: %s", req.getUrl());
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        Log.d("AppController", "Cancel pending request: " + tag);
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public void setUriDecovery(List<Recommend> inputUris){
        mPrevUriDecovery.clear();
        mPrevUriDecovery.addAll(mCurrentUriDecovery);

        mCurrentUriDecovery.clear();
        mCurrentUriDecovery.addAll(inputUris);
        Log.d(TAG, "URIAppController Prex " + mPrevUriDecovery.toString());
        Log.d(TAG, "URIAppController Curr " + mCurrentUriDecovery.toString());
    }

    public List<Recommend> getCurrentUriDecovery(){
        if(mPrevUriDecovery.isEmpty()){
            return mCurrentUriDecovery;
        }
        List<Recommend> currents = new ArrayList<>();
        for(Recommend node : mCurrentUriDecovery){
            boolean isContained = false;
            for(Recommend node1 : mPrevUriDecovery){
                if(node.getUri().toLowerCase().equals(node1.getUri().toLowerCase())){
                   isContained = true;
                }
            }
            if(!isContained){
                currents.add(node);
            }
        }
        return currents;
    }

    public List<String> getFromUriDecovery(){
        return convertToListString(mCurrentUriDecovery);
    }

    private List<String> convertToListString(List<Recommend> recommends){
        List<String> inputUris = new ArrayList<>();
        for(Recommend node : recommends){
            inputUris.add(node.getUri());
        }
        return inputUris;
    }
}
