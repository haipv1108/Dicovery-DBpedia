package com.brine.discovery.util;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static com.brine.discovery.util.Config.BASE_URL_DBPEDIA;
import static com.brine.discovery.util.Config.RESULT_JSON_TYPE;

/**
 * Created by phamhai on 08/02/2017.
 */

public class Utils {
    private static final String TAG = Utils.class.getCanonicalName();

    public static String createUrlKeywordSearch(String queryString){
        String url = Config.LOOKUP_DBPEDIA + "QueryClass=&MaxHits=30&QueryString=" + queryString;
        showLog("Lookup url: " + url);
        return url;
    }

    public static String createUrlDbpedia(String query){
        String url = "";
        try {
            url = BASE_URL_DBPEDIA + URLEncoder.encode(query, "UTF-8") + RESULT_JSON_TYPE;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        showLog(url);
        return url;
    }

    private static void showLog(String message){
        Log.d(TAG, message);
    }
}
