package com.brine.discovery.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.brine.discovery.AppController;
import com.brine.discovery.R;
import com.brine.discovery.adapter.SoundCloudAdapter;
import com.brine.discovery.adapter.YoutubeAdapter;
import com.brine.discovery.model.Recommend;
import com.brine.discovery.model.SCMusic;
import com.brine.discovery.model.TypeUri;
import com.brine.discovery.model.YoutubeVideo;
import com.brine.discovery.search.EXSearch;
import com.brine.discovery.util.Config;
import com.brine.discovery.util.Utils;
import com.brine.discovery.view.OnSwipeTouchListener;
import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.R.style.TextAppearance_Material_Body1;
import static android.R.style.TextAppearance_Material_Body2;

public class DetailsActivity extends AppCompatActivity
        implements YoutubeAdapter.YoutubeAdapterCallBack, SoundCloudAdapter.SCAdapterCallback, EXSearch.Callback{
    private static final String TAG = DetailsActivity.class.getCanonicalName();
    public static final String DATA = "uri";

    private CollapsingToolbarLayout mCollapsingToolbar;
    private ImageView mImgDetails;
    private LinearLayout mLinearTypeCategory, mLinearContentDetails, mLinearWrapDetails;
    private TextView mTvRecommendedGraph, mTvRunExploration;
    private ExpandableTextView mTvDescriptionDetails;

    private String mUri;
    private String message = "";

    private int countRequestCompleted = 0;
    private int maxRequest = 0;
    private boolean isTypeValue = false;

    private interface RequestCallBack{
        void startRequestToServer();
        void requestCompleted();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        initUI();
        mUri = getIntent().getStringExtra(DATA);
        maxRequest = calRequest();
        loadData();
    }

    private void initUI(){
        mCollapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        mImgDetails = (ImageView) findViewById(R.id.img_details);
        mLinearTypeCategory = (LinearLayout) findViewById(R.id.ln_type_category);
        mLinearContentDetails = (LinearLayout) findViewById(R.id.ln_content_details);
        mLinearWrapDetails = (LinearLayout) findViewById(R.id.liner_details);

        mTvDescriptionDetails = (ExpandableTextView) findViewById(R.id.expand_tv_description);
        mTvRecommendedGraph = (TextView) findViewById(R.id.tv_recommened_graph);
        mTvRecommendedGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showWhyRecommended();
            }
        });
        mTvRunExploration = (TextView) findViewById(R.id.tv_run_exploration);
        mTvRunExploration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runExploration();
            }
        });

        mLinearWrapDetails.setOnTouchListener(new OnSwipeTouchListener(this){
            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                hideTypeCategory();
            }

            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                showTypeCategory();
            }
        });
    }

    private void showWhyRecommended(){
        if(!message.isEmpty()){
            showDialogRelative(message);
            return;
        }
        List<String> originUris = AppController.getInstance().getFromUriDecovery();
        String currentUri = mUri;
        for(String originUri : originUris){
            String url = Utils.createUrlGetRelative(originUri, currentUri);
            final ProgressDialog progressDialog = new ProgressDialog(DetailsActivity.this);
            progressDialog.setMessage("Loading...");
            progressDialog.show();
            StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);

                        JSONArray jsonArray = jsonObject.getJSONObject("results").getJSONArray("bindings");
                        if(jsonArray.length() == 0) {
                            showLog("no result");
                            showGraphSearch();
                        }
                        JSONObject object = jsonArray.getJSONObject(0);
                        String label1 = object.getJSONObject("label1").getString("value");
                        String label2 = object.getJSONObject("label2").getString("value");

                        Map<String, List<String>> relativeEntities = new HashMap<>();
                        for(int i = 0; i < jsonArray.length(); i++){
                            JSONObject element = jsonArray.getJSONObject(i);
                            String k = element.getJSONObject("k").getString("value");
                            String catlabel = element.getJSONObject("catlabel").getString("value");
                            boolean isAdded = false;
                            for(Map.Entry<String, List<String>> entry : relativeEntities.entrySet()){
                                if(entry.getKey().equals(k)){
                                    entry.getValue().add(catlabel);
                                    isAdded = true;
                                }
                            }
                            if(!isAdded){
                                List<String> listValues = new ArrayList<>();
                                listValues.add(catlabel);
                                relativeEntities.put(k, listValues);
                            }

                        }
                        message = "\"" + label1 + "\"" + " and " + "\"" + label2 + "\"" + " have same \n";
                        for(Map.Entry<String, List<String>> entry : relativeEntities.entrySet()){
                            List<String> keySplit = Arrays.asList(entry.getKey().split("/"));
                            String key = keySplit.get(keySplit.size() -1);
                            if(!message.contains(":")){
                                message += key + ": ";
                            }else {
                                message += "and \n" + key + ": ";
                            }
                            List<String> listValues = entry.getValue();
                            for(int m = 0; m < listValues.size(); m++){
                                String cate = listValues.get(m);
                                message += cate + ", ";
                            }
                        }
                        message += "..." ;
                        progressDialog.dismiss();
                        showDialogRelative(message);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    if (volleyError.networkResponse == null) {
                        if (volleyError.getClass().equals(TimeoutError.class)) {
                            progressDialog.dismiss();
                            showGraphSearch();
                        }
                    }
                }
            });

            int socketTimeout = 4000;
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            request.setRetryPolicy(policy);
            AppController.getInstance().addToRequestQueue(request, "relative");
        }
    }

    private void showDialogRelative(String message){
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("View graph relative", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        showGraphSearch();
                    }
                })
                .show();
    }

    private void showGraphSearch(){
        Intent intent = new Intent(DetailsActivity.this, GraphActivity.class);
        intent.putExtra(GraphActivity.RECOMMENDEDURI, mUri);
        startActivity(intent);
    }

    private void runExploration(){
        List<Recommend> recommends = new ArrayList<>();
        Recommend recommend = new Recommend(mCollapsingToolbar.getTitle().toString(),
                mTvDescriptionDetails.getText().toString(), mUri, "");
        recommends.add(recommend);
        new EXSearch(DetailsActivity.this, recommends, DetailsActivity.this);
    }

    @Override
    public void showResultRecommend(String response) {
        Intent intent = new Intent(DetailsActivity.this, RecommendActivity.class);
        intent.putExtra(RecommendActivity.DATA, response);
        startActivity(intent);
    }

    private int calRequest(){
        List<String> fromUris = AppController.getInstance().getFromUriDecovery();
        int lengthFromUris = fromUris.size();
        int maxRequest = 2 + lengthFromUris + 1; //Details + type + from uris + soundcloud
        return maxRequest;
    }

    private void showTypeCategory(){
        if(isTypeValue){
            if(mLinearTypeCategory.getVisibility() == View.GONE){
                mLinearTypeCategory.setVisibility(View.VISIBLE);
            }
        }
    }

    private void hideTypeCategory(){
        if(isTypeValue){
            if(mLinearTypeCategory.getVisibility() == View.VISIBLE){
                mLinearTypeCategory.setVisibility(View.GONE);
            }
        }
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
            if(countRequestCompleted == maxRequest){
                progressDialog.dismiss();
            }
        }
    };

    private void loadData(){
        callBack.startRequestToServer();
        getDetailsInfo();
        getTypesInfo();
        getYoutubeData();
        getSoundCloudData();
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
        mCollapsingToolbar.setTitle(label);
        mTvRecommendedGraph.setText("Why " + label + " is recommended?");
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
                                isTypeValue = false;
                                mLinearTypeCategory.setVisibility(View.GONE);
                                return;
                            }
                            isTypeValue = true;

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
            tvLabel.setTextColor(Color.BLACK);

            mLinearTypeCategory.addView(tvLabel);

            List<TypeUri> values = mapType.get(key);
            for(int i = 0; i < values.size(); i++){
                final TypeUri value = values.get(i);
                TextView tvValue = new TextView(this);
                tvValue.setText((i + 1) + "." + value.getLabel());
                tvValue.setTextAppearance(getApplicationContext(), TextAppearance_Material_Body1);
                tvValue.setTextColor(Color.BLACK);

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

    private void getYoutubeData(){
        String currentUriLabel = getLabelFromUri(mUri);
        List<String> fromUris = AppController.getInstance().getFromUriDecovery();
        for(String from: fromUris){
            String fromUriLabel = getLabelFromUri(from);
            final String keyword = currentUriLabel + " " + fromUriLabel;
            final String keywordSearch = keyword.replace(" ", "+");
            String url = Utils.createUrlGetVideoYoutube(keywordSearch);
            StringRequest request = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            callBack.requestCompleted();
                            parseDataYoutube(response, keyword);
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

    private void parseDataYoutube(String response, String keyword){
        try {
            JSONObject data = new JSONObject(response);
            JSONArray items = data.getJSONArray("items");
            if(items.length() == 0){
                showLog("Youtube no results");
                return;
            }
            TextView tvLabel = new TextView(this);
            tvLabel.setText("Youtube's videos for " + URLDecoder.decode(keyword, "UTF-8"));
            tvLabel.setTypeface(null, Typeface.BOLD);
            mLinearContentDetails.addView(tvLabel);

            List<YoutubeVideo> youtubeVideos = new ArrayList<>();
            YoutubeAdapter youtubeAdapter =
                    new YoutubeAdapter(this, youtubeVideos, this);

            RecyclerView recyclerView = new RecyclerView(this);
            recyclerView.setHasFixedSize(true);
            RecyclerView.LayoutManager layoutManager =
                    new LinearLayoutManager(getApplicationContext(),
                            LinearLayoutManager.HORIZONTAL, false);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.addItemDecoration(
                    new DividerItemDecoration(this, LinearLayout.HORIZONTAL));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(youtubeAdapter);

            mLinearContentDetails.addView(recyclerView, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            for(int i = 0; i < items.length(); i++){
                JSONObject item = items.getJSONObject(i);
                String videoId = item.getJSONObject("id").getString("videoId");
                JSONObject snippet = item.getJSONObject("snippet");
                String title = snippet.getString("title");
                String channelTitle = snippet.getString("channelTitle");
                String thumbnail = snippet.getJSONObject("thumbnails")
                        .getJSONObject("default").getString("url");
                YoutubeVideo youtubeVideo = new YoutubeVideo(
                        videoId, title, thumbnail, channelTitle);
                youtubeVideos.add(youtubeVideo);
                youtubeAdapter.notifyDataSetChanged();
            }
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void playVideoYoutube(String videoId) {
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.google.android.youtube");
        if(launchIntent != null){
            Intent intent = new Intent(this, YoutubePlayerActivity.class);
            intent.putExtra(YoutubePlayerActivity.VIDEOID, videoId);
            startActivity(intent);
        }else{
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + videoId)));
        }
    }

    private String getLabelFromUri(String uri){
        List<String> splitUri = Arrays.asList(uri.split("/"));
        String label = splitUri.get(splitUri.size() - 1).replace("_", " ");
        return label;
    }

    private void getSoundCloudData(){
        final String keyword = getLabelFromUri(mUri);
        String url = Utils.createUrlGetSoundCloud(keyword);
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        callBack.requestCompleted();
                        parseDataSoundCloud(response, keyword);
                        showLog("soundcloud data: " + response);
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

    private void parseDataSoundCloud(String response, String keyword){
        try {
            JSONArray data = new JSONArray(response);
            if(data.length() == 0){
                showLog("Soundcloud no results");
                return;
            }

            TextView tvLabel = new TextView(this);
            tvLabel.setText("SoundCloud's Tracks For " + URLDecoder.decode(keyword, "UTF-8"));
            tvLabel.setTypeface(null, Typeface.BOLD);
            mLinearContentDetails.addView(tvLabel);

            List<SCMusic> scMusics = new ArrayList<>();
            SoundCloudAdapter SCAdapter = new SoundCloudAdapter(this, scMusics, this);

            RecyclerView recyclerView = new RecyclerView(this);
            recyclerView.setHasFixedSize(true);
            RecyclerView.LayoutManager layoutManager =
                    new LinearLayoutManager(getApplicationContext(),
                            LinearLayoutManager.HORIZONTAL, false);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.addItemDecoration(
                    new DividerItemDecoration(this, LinearLayout.HORIZONTAL));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(SCAdapter);

            mLinearContentDetails.addView(recyclerView, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            for(int i = 0; i < data.length(); i++){
                JSONObject item = data.getJSONObject(i);
                long musicId = item.getLong("id");
                String title = item.getString("title");
                String streamUrl = item.getString("stream_url");
                String artworkUrl = item.getString("artwork_url");
                SCMusic scMusic = new SCMusic(musicId, title, streamUrl, artworkUrl);
                scMusics.add(scMusic);
                SCAdapter.notifyDataSetChanged();
            }

        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void playSoundCloudMusic(String streamUrl, String songName) {
        String musicUrl = streamUrl + "?client_id=" + Config.SOUNDCLOUD_CLIENT_ID;
        Intent intent = new Intent(DetailsActivity.this, PlayMusicActivity.class);
        intent.putExtra(PlayMusicActivity.URL, musicUrl);
        intent.putExtra(PlayMusicActivity.SONG_NAME, songName);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
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
