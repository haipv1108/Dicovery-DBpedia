package com.brine.discovery.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.brine.discovery.AppController;
import com.brine.discovery.R;
import com.brine.discovery.model.TypeUri;
import com.brine.discovery.util.Config;
import com.brine.discovery.util.Utils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

import static android.R.style.TextAppearance_Material_Body1;
import static android.R.style.TextAppearance_Material_Body2;

public class DetailsActivity extends AppCompatActivity {
    private static final String TAG = DetailsActivity.class.getCanonicalName();
    public static final String DATA = "uri";
    private static final int NUMBER_REQUEST = 3;

    private CollapsingToolbarLayout collapsingToolbar;
    private ImageView mImgDetails;
    private LinearLayout mLinearTypeCategory, mLinearContentDetaisl;
    private TextView mTvDescriptionLabel, mTvYoutube2Label,
            mTvYoutube1Label, mTvSoundcloudLabel, mTvFmMusicLabel;
    private ExpandableTextView mTvDescriptionDetails;
    private RecyclerView mRecycleYoutube2, mRecycleYoutube1, mRecycleSoundCloud,
            mRecycleFmMusic;

    private String mUri;

    private int countRequestCompleted = 0;

    private interface RequestCallBack{
        void startRequestToServer();
        void requestCompleted();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        initUI();
        init();
        mUri = getIntent().getStringExtra(DATA);
        loadData();
    }

    private void initUI(){
        collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        mImgDetails = (ImageView) findViewById(R.id.img_details);
        mLinearTypeCategory = (LinearLayout) findViewById(R.id.ln_type_category);
        mLinearContentDetaisl = (LinearLayout) findViewById(R.id.ln_content_details);

        mTvDescriptionLabel = (TextView) findViewById(R.id.tv_description_label);
        mTvDescriptionDetails = (ExpandableTextView) findViewById(R.id.expand_tv_description);
        mTvYoutube2Label = (TextView) findViewById(R.id.tv_2youtube_label);
        mTvYoutube1Label = (TextView) findViewById(R.id.tv_1youtube_label);
        mTvSoundcloudLabel = (TextView) findViewById(R.id.tv_soundcloud_label);
        mTvFmMusicLabel = (TextView) findViewById(R.id.tv_fmmusic_label);

        mRecycleYoutube2 = (RecyclerView) findViewById(R.id.recycle_2youtube);
        mRecycleYoutube1 = (RecyclerView) findViewById(R.id.recycle_1youtube);
        mRecycleSoundCloud = (RecyclerView) findViewById(R.id.recycle_soundcloud);
        mRecycleFmMusic = (RecyclerView) findViewById(R.id.recycle_fmmusic);

        Button btnGraph = (Button) findViewById(R.id.graph);
        btnGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DetailsActivity.this, GraphActivity.class);
                intent.putExtra(GraphActivity.RECOMMENDEDURI, mUri);
                startActivity(intent);
            }
        });
    }

    private void init(){

    }

    private RequestCallBack callBack = new RequestCallBack() {
        ProgressDialog progressDialog;
        @Override
        public void startRequestToServer() {
            progressDialog = new ProgressDialog(DetailsActivity.this);
            progressDialog.setMessage("Loading...");
            progressDialog.show();
        }

        @Override
        public void requestCompleted() {
            countRequestCompleted++;
            if(countRequestCompleted == NUMBER_REQUEST){
                progressDialog.dismiss();
            }
        }
    };

    private void loadData(){
        callBack.startRequestToServer();
        getDetailsInfo();
        getTypesInfo();
        getGraphDecovery();
        getCategoryInfo();
        getYoutubeData();
        getSoundCloudData();
        getFmMusicData();
        //TODO:
    }

    private void getDetailsInfo(){
        final List<String> fromUris = AppController.getInstance().getFromUriDecovery();
        String url = Utils.createUrlGetDetailsUri(mUri, fromUris);
        final StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        callBack.requestCompleted();
                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            JSONArray jsonArray = jsonObject.getJSONObject("results")
                                    .getJSONArray("bindings");
                            if(jsonArray.length() == 0) {
                                return;
                            }
                            JSONObject result = jsonArray.getJSONObject(0);
                            String label = result.getJSONObject("label").getString("value");
                            String description = result.getJSONObject("description").getString("value");
                            String image = result.getJSONObject("image").getString("value");
                            //TODO: get label of from uri
                            setDetailsInfo(label, description, image);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callBack.requestCompleted();
                    }
                });
        AppController.getInstance().addToRequestQueue(request, "details");
    }

    private void setDetailsInfo(String label, String description, String image){
        collapsingToolbar.setTitle(label);
        mTvDescriptionDetails.setText(description);
        if(image.contains("http")){
            String imageUrl = image.replace("http://", "https://");
            Picasso.with(this)
                    .load(imageUrl)
                    .error(R.drawable.details)
                    .into(mImgDetails);
        }
    }

    private void getTypesInfo(){
        final String url = Utils.createUrlGetTypeUri(mUri);
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        callBack.requestCompleted();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray jsonArray = jsonObject.getJSONObject("results")
                                    .getJSONArray("bindings");
                            if(jsonArray.length() == 0) {
                                mLinearTypeCategory.setVisibility(View.GONE);
                                return;
                            }
                            Map<TypeUri, List<TypeUri>> mapType = new HashMap<>();
                            for(int i = 0; i < jsonArray.length(); i++){
                                JSONObject result = jsonArray.getJSONObject(i);
                                String y = result.getJSONObject("y").getString("value");
                                String z = result.getJSONObject("z").getString("value");
                                String type = result.getJSONObject("type").getString("value");
                                String name = result.getJSONObject("name").getString("value");
                                TypeUri typeKey = new TypeUri(type, y);
                                TypeUri typeValue = new TypeUri(name, z);
                                addTypeDetails(mapType, typeKey, typeValue);
                            }
                            showTypeDetails(mapType);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callBack.requestCompleted();
                    }
                });
        AppController.getInstance().addToRequestQueue(request, "details");
    }

    private void addTypeDetails(Map<TypeUri, List<TypeUri>> mapType,
                                TypeUri typeKey, TypeUri typeValue){
        boolean isAdded = false;
        List<TypeUri> keys = new ArrayList<>(mapType.keySet());
        for(TypeUri key : keys){
            if(key.getUri().equals(typeKey.getUri())){
                List<TypeUri> values = mapType.get(key);
                values.add(typeValue);
                isAdded = true;
            }
        }
        if(!isAdded){
            List<TypeUri> values = new ArrayList<>();
            values.add(typeValue);
            mapType.put(typeKey, values);
        }
    }

    private void showTypeDetails(Map<TypeUri, List<TypeUri>> mapType){
        List<TypeUri> keys = new ArrayList<>(mapType.keySet());
        for(TypeUri key : keys){
            TextView tvLabel = new TextView(this);
            tvLabel.setText(firstUpperString(key.getLabel()));
            tvLabel.setTextAppearance(getApplicationContext(), TextAppearance_Material_Body2);

            mLinearTypeCategory.addView(tvLabel);

            List<TypeUri> values = mapType.get(key);
            for(int i = 0; i < values.size(); i++){
                final TypeUri value = values.get(i);
                TextView tvValue = new TextView(this);
                tvValue.setText((i + 1) + "." + value.getLabel());
                tvValue.setTextAppearance(getApplicationContext(), TextAppearance_Material_Body1);

                tvValue.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showLogAndToast(value.getUri());
                        //TODO: xu ly
                    }
                });

                mLinearTypeCategory.addView(tvValue);
            }

            TextView tvSpace = new TextView(this);
            tvSpace.setLines(1);
            mLinearTypeCategory.addView(tvSpace);
        }
    }

    private String firstUpperString(String word){
        String result = "";
        List<String> splitWord = Arrays.asList(word.split(" "));
        for(int i = 0; i < splitWord.size(); i++){
            String value = splitWord.get(i);
            String type = value.substring(0, 1).toUpperCase() + value.substring(1);
            result += " " + type;
        }
        return result.trim();
    }

    private void getGraphDecovery(){
        List<String> fromUris = AppController.getInstance().getFromUriDecovery();

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        for(String uri : fromUris){
            params.add("nodes[]", uri);
        }
        params.add("nodes[]", mUri);
        showLog("Params graph: " + params.toString());
        client.post(Config.DISCOVERYHUB_GRAPH_API, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                showLog("Response graph: " + response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    private void getCategoryInfo(){

    }

    private void getYoutubeData(){
        String currentUriLabel = getLabelFromUri(mUri);
        List<String> fromUris = AppController.getInstance().getFromUriDecovery();
        for(String from: fromUris){
            String fromUriLabel = getLabelFromUri(from);
            String keyword = currentUriLabel + " " + fromUriLabel;
            String keywordSearch = keyword.replace(" ", "+");
            String url = Utils.createUrlGetVideoYoutube(keywordSearch);
            StringRequest request = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            callBack.requestCompleted();
                            //TODO: view data
                            showLog("youtube data: " + response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            callBack.requestCompleted();
                        }
                    });
            AppController.getInstance().addToRequestQueue(request, "details");
        }
    }

    private String getLabelFromUri(String uri){
        List<String> splitUri = Arrays.asList(uri.split("/"));
        String label = splitUri.get(splitUri.size() - 1).replace("_", " ");
        return label;
    }

    private void getSoundCloudData(){
        String keyword = getLabelFromUri(mUri);
        String url = Utils.createUrlGetSoundCloud(keyword);
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        callBack.requestCompleted();
                        //TODO: view data
                        showLog("soundcloud data: " + response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callBack.requestCompleted();
                    }
                });
    }

    private void getFmMusicData(){

    }

    private void showLog(String message){
        if(message != null){
            Log.d(TAG, message);
        }
    }

    private void showLogAndToast(final String message){
        showLog(message);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
