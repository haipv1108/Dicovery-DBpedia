package com.brine.discovery;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by phamhai on 08/02/2017.
 */

public class AppController extends Application {
    private static final String TAG = AppController.class.getCanonicalName();

    private static AppController sInstance;
    private RequestQueue mRequestQueue;
    private List<String> mCurrentUriDecovery;
    private List<String> mPrevUriDecovery;

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
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public void setUriDecovery(List<String> inputUris){
        mPrevUriDecovery.clear();
        mPrevUriDecovery.addAll(mCurrentUriDecovery);

        mCurrentUriDecovery.clear();
        mCurrentUriDecovery.addAll(inputUris);
        Log.d(TAG, "URIAppController Prex " + mPrevUriDecovery.toString());
        Log.d(TAG, "URIAppController Curr " + mCurrentUriDecovery.toString());
    }

    public List<String> getCurrentUriDecovery(){
        return mCurrentUriDecovery;
    }

    public List<String> getFromUriDecovery(){
        return mPrevUriDecovery;
    }
}
