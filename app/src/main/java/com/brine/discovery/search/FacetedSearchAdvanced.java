package com.brine.discovery.search;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.brine.discovery.AppController;
import com.brine.discovery.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by phamhai on 29/03/2017.
 */

public class FacetedSearchAdvanced {
    private static final String TAG = FacetedSearchAdvanced.class.getCanonicalName();

    private Context mContext;
    private String mKeywordSearch;
    private Callback mCallback;

    public interface Callback{
        void runFacetedSearch(String keywordSearch, String optionSearch);
    }

    public FacetedSearchAdvanced(Context _context, String _keyword, Callback _callback){
        this.mContext = _context;
        this.mKeywordSearch = _keyword;
        mCallback = _callback;

        search();
    }

    private void search(){
        String url = Utils.createUrlGetTypes(mKeywordSearch);

        final ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
                Map<String, Integer> entitiesMap = new HashMap<>();
                List<String> splitKeyword = Arrays.asList(mKeywordSearch.replace("actor", "person").split(" "));
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray data = jsonObject.getJSONObject("results").getJSONArray("bindings");
                    for(int i = 0; i < data.length(); i++){
                        JSONObject element = data.getJSONObject(i);
                        String uri = element.getJSONObject("c1").getString("value");
                        List<String> splitUri = Arrays.asList(uri.split("/"));
                        String label = splitUri.get(splitUri.size()-1);
                        label = label.replace("_", " ");
                        for(int index = 0; index < splitKeyword.size(); index++){
                            int percent = splitKeyword.get(index).length() * 8/10;
                            if((splitKeyword.get(index).toLowerCase().contains(label.toLowerCase())
                                    && percent <= label.length()) || label.toLowerCase().contains(splitKeyword.get(index))){
                                entitiesMap.put(uri, index);
                            }
                        }
                    }
                    if(entitiesMap.size() == 0){
                        mCallback.runFacetedSearch(mKeywordSearch, "");
                    }else if(entitiesMap.size() == 1){
                        String uri = (String) entitiesMap.keySet().toArray()[0];
                        String option = " ?s1 a " + "<" + uri + "> .\n" ;
                        showLog("1. OPTION SEARCH: " + option);
                        mCallback.runFacetedSearch(mKeywordSearch, option);
                    }else if(entitiesMap.size() >1){
                        List<Integer> listweight = new ArrayList<Integer>(entitiesMap.values());
                        int minValue = listweight.get(0);
                        for(int i = 1; i < listweight.size(); i++){
                            if(listweight.get(i) < minValue){
                                minValue = i;
                            }
                        }
                        String uri = (String) entitiesMap.keySet().toArray()[minValue];
                        String option = " ?s1 a " + "<" + uri + "> .\n" ;
                        showLog("2. OPTION SEARCH: " + option);
                        mCallback.runFacetedSearch(mKeywordSearch, option);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
                showLogAndToast("Error: " + error.getMessage());
            }
        });
        AppController.getInstance().addToRequestQueue(request, "advanced_fs");
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
