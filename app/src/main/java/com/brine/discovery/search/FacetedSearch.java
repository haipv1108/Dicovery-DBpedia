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
import com.brine.discovery.adapter.FSAdapter;
import com.brine.discovery.model.FSResult;
import com.brine.discovery.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

/**
 * Created by phamhai on 29/03/2017.
 */

public class FacetedSearch {
    private static final String TAG = FacetedSearch.class.getCanonicalName();

    private Context mContext;
    private String mKeywordSearch;
    private String optionSearch;
    private FSAdapter mFSAdapter;
    private List<FSResult> mFSResults;
    private CallBack mCallback;

    public interface CallBack{
        void hideFSFilterCallback();
        void showFSFilterCallback();
        void FSCompleted();
    }

    public FacetedSearch(Context _context, String _keyword, String _option,
                         List<FSResult> _listData, FSAdapter _adapter, CallBack _callback){
        this.mContext = _context;
        this.mKeywordSearch = _keyword;
        this.optionSearch = _option;
        this.mFSResults = _listData;
        this.mFSAdapter = _adapter;
        this.mCallback = _callback;

        search();
    }

    private void search(){
        mFSResults.clear();
        mFSAdapter.notifyDataSetChanged();
        mCallback.hideFSFilterCallback();

        String url = Utils.createUrlFacetedSearch(mKeywordSearch, optionSearch);

        final ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
                parse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
                showLog("Error: " + error.getMessage());
            }
        });
        AppController.getInstance().addToRequestQueue(stringRequest, "factedsearch");
    }

    private void parse(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray data = jsonObject.getJSONObject("results").getJSONArray("bindings");
            if(data.length() == 0){
                showLogAndToast("No results");
            }else{
                for(int i = 0; i < data.length(); i++){
                    JSONObject element = data.getJSONObject(i);
                    String uri = element.getJSONObject("c1").getString("value");

                    List<String> splitUri = Arrays.asList(uri.split("/"));
                    String localName = splitUri.get(splitUri.size()-1);
                    String label = localName.replace("_", " ");
                    String description = element.getJSONObject("c2").getString("value");

                    FSResult result = new FSResult(uri, label, description, null);
                    updateFSResult(result);
                }
                if(mFSResults.size() == 0){
                    showLogAndToast("No results");
                }else{
                    searchImage();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateFSResult(FSResult result){
        if(!isContained(result.getUri()) && isDbpediaSource(result.getUri())){
            mFSResults.add(result);
            mFSAdapter.notifyDataSetChanged();
            mCallback.showFSFilterCallback();
        }
    }

    private boolean isContained(String uri){
        for(FSResult result : mFSResults){
            if(result.getUri().equals(uri)){
                return true;
            }
        }
        return false;
    }

    private boolean isDbpediaSource(String uri){
        if(uri.contains("dbpedia.org")){
            return true;
        }
        return false;
    }

    private void searchImage(){
        if(mFSResults.size() == 0){
            return;
        }
        String query = "SELECT ?uri ?img\n" +
                "WHERE\n" +
                "{\n" +
                "?uri <http://dbpedia.org/ontology/thumbnail> ?img .\n" +
                "FILTER (";
        for(int i = 0; i < mFSResults.size() - 1; i++){
            query += " ?uri = <" + mFSResults.get(i).getUri() + "> || ";
        }
        query += " ?uri = <" + mFSResults.get(mFSResults.size() - 1) + "> ) }";
        String url = Utils.createUrlDbpedia(query);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray data = jsonObject.getJSONObject("results").getJSONArray("bindings");
                            if(data.length() == 0){
                                showLog("No result");
                            }else{
                                for(int i = 0; i < data.length(); i++){
                                    JSONObject element = data.getJSONObject(i);
                                    String uri = element.getJSONObject("uri").getString("value");
                                    String thumb = element.getJSONObject("img").getString("value");
                                    insertImage(uri, thumb);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },  new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showLog("Error: " + error.getMessage());
            }
        });
        AppController.getInstance().addToRequestQueue(request, "factedsearch");
    }

    private void insertImage(String uri, String thumb){
        for(FSResult fs : mFSResults){
            if(fs.getUri().equals(uri)){
                fs.setThumbnail(thumb);
                mFSAdapter.notifyDataSetChanged();
                return;
            }
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
