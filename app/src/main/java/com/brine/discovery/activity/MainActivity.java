package com.brine.discovery.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.brine.discovery.AppController;
import com.brine.discovery.R;
import com.brine.discovery.adapter.FSAdapter;
import com.brine.discovery.adapter.KSAdapter;
import com.brine.discovery.adapter.SelectedResultsAdapter;
import com.brine.discovery.model.FSResult;
import com.brine.discovery.model.KeywordSearch;
import com.brine.discovery.model.Recommend;
import com.brine.discovery.search.EXSearch;
import com.brine.discovery.search.FacetedSearch;
import com.brine.discovery.search.FacetedSearchAdvanced;
import com.brine.discovery.search.LookupUri;
import com.brine.discovery.search.SlidingWindow;
import com.brine.discovery.util.Config;
import com.brine.discovery.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, SelectedResultsAdapter.SelectedAdapterCallback,
        FSAdapter.FSAdapterCallback, FacetedSearch.CallBack, FacetedSearchAdvanced.Callback, EXSearch.Callback{
    private static final String TAG = MainActivity.class.getCanonicalName();
    private static final int MAXITEM = 4;

    private static final int LOOKUP_URI = 1;
    private static final int FACTED_SEARCH = 2;
    private static final int FACTED_SEARCH_ADVANCED = 4;

    public EditText mEdtSearch;
    public ListView mListviewKS;
    public RecyclerView mRecycleRecommed;
    public RelativeLayout mRltSelectedRecommend;

    public LinearLayout mLinearFSFilter;
    public LinearLayout mFSFilterType, mFSFilterAttribute, mFSFilterValue, mFSFilterDistinct;

    public KSAdapter mKSAdapter;
    public SelectedResultsAdapter mRecommedAdapter;
    public List<KeywordSearch> mKeywordSearchs;
    public List<Recommend> mSelectedRecommends;

    public FSAdapter mFSAdapter;
    public List<FSResult> mFSResults;
    public RecyclerView mRecyclerFS;
    public ImageView mImgSearchOption;
    public ImageView mImgSearch;

    public String keywordSearch = "";
    public Map<String, String> mapSearchOptions;

    public int typeSearch = LOOKUP_URI;

    long startTime = 0;
    long elapsedTime = 0;

    private interface TypeSearchCallBack {
        void changeTypeSearch();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
        init();
        setDefaultTypeSearch();
    }

    private void initUI(){
        mEdtSearch = (EditText) findViewById(R.id.edt_search);
        mListviewKS = (ListView) findViewById(R.id.lv_keyword_search);
        mRecycleRecommed = (RecyclerView) findViewById(R.id.recycle_selected_uri);
        ImageButton mBtnEXSearch = (ImageButton) findViewById(R.id.btn_EXSearch);
        mBtnEXSearch.setOnClickListener(this);
        mRltSelectedRecommend = (RelativeLayout) findViewById(R.id.rlt_seledted_recommend);
        mLinearFSFilter = (LinearLayout) findViewById(R.id.fs_filter);
        mFSFilterType = (LinearLayout) findViewById(R.id.fs_filter_type);
        mFSFilterAttribute = (LinearLayout) findViewById(R.id.fs_filter_attribute);
        mFSFilterValue = (LinearLayout) findViewById(R.id.fs_filter_value);
        mFSFilterDistinct = (LinearLayout) findViewById(R.id.fs_filter_distinct);

        mRecyclerFS = (RecyclerView)findViewById(R.id.recycle_fsresult);
        mImgSearchOption = (ImageView) findViewById(R.id.img_search_option);
        mImgSearch = (ImageView) findViewById(R.id.img_search);
        mImgSearchOption.setOnClickListener(this);
        mImgSearch.setOnClickListener(this);
        mFSFilterType.setOnClickListener(this);
        mFSFilterAttribute.setOnClickListener(this);
        mFSFilterValue.setOnClickListener(this);
        mFSFilterDistinct.setOnClickListener(this);
    }

    private void setDefaultTypeSearch(){
        typeSearch = LOOKUP_URI;
        callBack.changeTypeSearch();
        listeningEdtSearch();
    }

    private TypeSearchCallBack callBack = new TypeSearchCallBack() {
        @Override
        public void changeTypeSearch() {
            switch (typeSearch){
                case LOOKUP_URI:
                    mKeywordSearchs.clear();
                    mKSAdapter.notifyDataSetChanged();
                    mListviewKS.setVisibility(View.VISIBLE);
                    mRecyclerFS.setVisibility(View.GONE);
                    hideFSFilter();
                    listeningEdtSearch();
                    break;
                case FACTED_SEARCH:
                    mFSResults.clear();
                    mFSAdapter.notifyDataSetChanged();
                    mListviewKS.setVisibility(View.GONE);
                    mRecyclerFS.setVisibility(View.VISIBLE);
                    hideFSFilter();
                    break;
                case FACTED_SEARCH_ADVANCED:
                    mFSResults.clear();
                    mFSAdapter.notifyDataSetChanged();
                    mListviewKS.setVisibility(View.GONE);
                    mRecyclerFS.setVisibility(View.VISIBLE);
                    hideFSFilter();
                    break;
                default:
                    break;
            }
        }
    };

    private void hideSelectedRecommend(){
        mListviewKS.setVisibility(View.VISIBLE);
        mRltSelectedRecommend.setVisibility(View.GONE);
    }

    private void showSelectedRecommend(){
        mListviewKS.setVisibility(View.GONE);
        mRltSelectedRecommend.setVisibility(View.VISIBLE);
    }

    private void hideFSFilter(){
        mLinearFSFilter.setVisibility(View.GONE);
    }

    private void showFSFilter(){
        mLinearFSFilter.setVisibility(View.VISIBLE);
    }

    private void init(){
        mKeywordSearchs = new ArrayList<>();
        mSelectedRecommends = new ArrayList<>();
        mapSearchOptions = new HashMap<>();

        mKSAdapter = new KSAdapter(this, mKeywordSearchs);
        mListviewKS.setAdapter(mKSAdapter);

        mRecommedAdapter = new SelectedResultsAdapter(this, mSelectedRecommends, this);
        mRecycleRecommed.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManagerRecommend =
                new LinearLayoutManager(getApplicationContext(),
                        LinearLayoutManager.HORIZONTAL, false);
        mRecycleRecommed.setLayoutManager(layoutManagerRecommend);
        mRecycleRecommed.addItemDecoration(
                new DividerItemDecoration(this, LinearLayout.HORIZONTAL));
        mRecycleRecommed.setItemAnimator(new DefaultItemAnimator());
        mRecycleRecommed.setAdapter(mRecommedAdapter);

        mFSResults = new ArrayList<>();
        mFSAdapter = new FSAdapter(this, mFSResults, this);
        mRecyclerFS.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManagerFS =
                new LinearLayoutManager(getApplicationContext(),
                        LinearLayoutManager.VERTICAL, false);
        mRecyclerFS.setLayoutManager(layoutManagerFS);
        mRecyclerFS.addItemDecoration(
                new DividerItemDecoration(getApplicationContext(), LinearLayout.VERTICAL));
        mRecyclerFS.setItemAnimator(new DefaultItemAnimator());
        mRecyclerFS.setAdapter(mFSAdapter);

    }

    @Override
    public void onClickItem(final Recommend recommend) {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to delete " + recommend.getLabel())
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mSelectedRecommends.remove(recommend);
                        mRecommedAdapter.notifyDataSetChanged();
                        if(mSelectedRecommends.isEmpty()){
                            hideSelectedRecommend();
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_EXSearch:
                if(mSelectedRecommends.size() == 0){
                    showLogAndToast("Please select uri!");
                    return;
                }
                startTime = System.currentTimeMillis();
                new EXSearch(MainActivity.this, mSelectedRecommends, MainActivity.this);
                break;
//            case R.id.img_search_option:
//                showPopupSearchOption();
//                break;
            case R.id.img_search:
                String keyword = getKeywordInput();
                String keywordRemovedStopWord = removeStopWord(keyword);
                if(keyword != null){
                    keywordSearch = keywordRemovedStopWord;
                    switch (typeSearch){
                        case FACTED_SEARCH:
                            showLogAndToast("Search " + keywordSearch);
                            new FacetedSearch(MainActivity.this, keywordSearch, "", mFSResults, mFSAdapter, MainActivity.this);
                            break;
                        case FACTED_SEARCH_ADVANCED:
                            new FacetedSearchAdvanced(MainActivity.this, keywordSearch, MainActivity.this);
                            break;
                    }
                }
                break;
            case R.id.fs_filter_type:
                getTypesFilter();
                break;
            case R.id.fs_filter_attribute:
                getAttributesFilter();
                break;
            case R.id.fs_filter_value:
                getValuesFilter();
                break;
            case R.id.fs_filter_distinct:
                getDistinctFilter();
                break;
        }
    }

    @Override
    public void showResultRecommend(String response) {
        elapsedTime = System.currentTimeMillis() - startTime;
        showLogAndToast("Time request: " + elapsedTime/1000 + "s");
        Intent intent = new Intent(MainActivity.this, RecommendActivity.class);
        intent.putExtra(RecommendActivity.DATA, response);
        startActivity(intent);
    }

    @Override
    public void runFacetedSearch(String keywordSearch, String optionSearch) {
        new FacetedSearch(MainActivity.this, keywordSearch, optionSearch, mFSResults, mFSAdapter, MainActivity.this);
    }

    @Override
    public void FSCompleted() {
        showLogAndToast("Done!");
        if(mKeywordSearchs.size() == 0){
            activeFSAdvanced();
        }
    }

    private void activeFSAdvanced(){
        mFSResults.clear();
        mFSAdapter.notifyDataSetChanged();
        mListviewKS.setVisibility(View.GONE);
        mRecyclerFS.setVisibility(View.VISIBLE);
        hideFSFilter();
        new FacetedSearchAdvanced(MainActivity.this, keywordSearch, MainActivity.this);
    }

    //+++++++++++++++++++++++++++++++++++++++++++

    private void getTypesFilter(){
        String url = Utils.createUrlGetTypes(keywordSearch);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
                parseTypesFilterResult(response);
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

    private void parseTypesFilterResult(String response){
        Map<String, String> entitiesMap = new HashMap<>();
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray data = jsonObject.getJSONObject("results").getJSONArray("bindings");
            for(int i = 0; i < data.length(); i++){
                JSONObject element = data.getJSONObject(i);
                String uri = element.getJSONObject("c1").getString("value");
                List<String> splitUri = Arrays.asList(uri.split("/"));
                String label = splitUri.get(splitUri.size()-1);
                label = label.replace("_", " ");
                entitiesMap.put(uri, label);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        showDialogTypesEntity(entitiesMap);
    }

    private void showDialogTypesEntity(final Map<String, String> entitiesMap){
        final String[] strarray = new String[entitiesMap.size()];

        List<String> list = new ArrayList<String>(entitiesMap.values());
        list.toArray(strarray);

        new AlertDialog.Builder(this)
                .setTitle("Select type entity")
                .setSingleChoiceItems(strarray, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String uri = (String) entitiesMap.keySet().toArray()[i];
                        String option = " ?s1 a " + "<" + uri + "> .\n" ;
                        new FacetedSearch(MainActivity.this, keywordSearch, option, mFSResults, mFSAdapter, MainActivity.this);
                        dialogInterface.dismiss();
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    //+++++++++++++++++++++++++++++++++++++++++++

    private void getAttributesFilter(){
        String url = Utils.createUrlGetAttributes(keywordSearch);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
                parseAttributesFilterResult(response);
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

    private void parseAttributesFilterResult(String response){
        Map<String, String> entitiesMap = new HashMap<>();
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray data = jsonObject.getJSONObject("results").getJSONArray("bindings");
            for(int i = 0; i < data.length(); i++){
                JSONObject element = data.getJSONObject(i);
                String uri = element.getJSONObject("c1").getString("value");
                List<String> splitUri = Arrays.asList(uri.split("/"));
                String label = splitUri.get(splitUri.size()-1);
                label = label.replace("_", " ");
                entitiesMap.put(uri, label);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        showDialogAttributesEntity(entitiesMap);
    }

    private void showDialogAttributesEntity(final Map<String, String> entitiesMap){
        final String[] strarray = new String[entitiesMap.size()];

        List<String> list = new ArrayList<String>(entitiesMap.values());
        list.toArray(strarray);

        new AlertDialog.Builder(this)
                .setTitle("Select type entity")
                .setSingleChoiceItems(strarray, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String uri = (String) entitiesMap.keySet().toArray()[i];
                        String option = " ?s1 " + "<" + uri + "> ?s2 .\n" ;
                        dialogInterface.dismiss();
                        getAttributesValue(option);
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void getAttributesValue(final String option){
        String url = Utils.createUrlGetAttributesValue(keywordSearch, option);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
                parseAttributesValue(response, option);
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

    private void parseAttributesValue(String response, String option){
        Map<String, String> entitiesMap = new HashMap<>();
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray data = jsonObject.getJSONObject("results").getJSONArray("bindings");
            for(int i = 0; i < data.length(); i++){
                JSONObject element = data.getJSONObject(i);
                String uri = element.getJSONObject("c1").getString("value");
                List<String> splitUri = Arrays.asList(uri.split("/"));
                String label = splitUri.get(splitUri.size()-1);
                label = label.replace("_", " ");
                entitiesMap.put(uri, label);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        showDialogAttributesValue(entitiesMap, option);
    }

    private void showDialogAttributesValue(final Map<String, String> entitiesMap, final String option){
        final String[] strarray = new String[entitiesMap.size()];

        List<String> list = new ArrayList<String>(entitiesMap.values());
        list.toArray(strarray);

        new AlertDialog.Builder(this)
                .setTitle("Select value")
                .setSingleChoiceItems(strarray, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String uri = (String) entitiesMap.keySet().toArray()[i];
                        String optionSearch = option + "\n" +
                                "    filter (?s2 = <" + uri + ">) .";
                        dialogInterface.dismiss();
                        new FacetedSearch(MainActivity.this, keywordSearch, optionSearch, mFSResults, mFSAdapter, MainActivity.this);
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    //+++++++++++++++++++++++++++++++++++++++++++

    private void getValuesFilter(){
        String url = Utils.createUrlGetValues(keywordSearch);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
                parseValuesFilterResult(response);
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

    private void parseValuesFilterResult(String response){
        Map<String, String> entitiesMap = new HashMap<>();
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray data = jsonObject.getJSONObject("results").getJSONArray("bindings");
            for(int i = 0; i < data.length(); i++){
                JSONObject element = data.getJSONObject(i);
                String uri = element.getJSONObject("c1").getString("value");
                List<String> splitUri = Arrays.asList(uri.split("/"));
                String label = splitUri.get(splitUri.size()-1);
                label = label.replace("_", " ");
                entitiesMap.put(uri, label);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        showDialogValuesEntity(entitiesMap);
    }

    private void showDialogValuesEntity(final Map<String, String> entitiesMap){
        final String[] strarray = new String[entitiesMap.size()];

        List<String> list = new ArrayList<String>(entitiesMap.values());
        list.toArray(strarray);

        new AlertDialog.Builder(this)
                .setTitle("Select type entity")
                .setSingleChoiceItems(strarray, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String uri = (String) entitiesMap.keySet().toArray()[i];
                        String option = "?s2 <" + uri + "> ?s1 .";
                        dialogInterface.dismiss();
                        getValuesOfValue(option);
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void getValuesOfValue(final String option){
        String url = Utils.createUrlGetValuesOfValue(keywordSearch, option);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
                parseValuesOfValue(response, option);
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

    private void parseValuesOfValue(String response, String option){
        Map<String, String> entitiesMap = new HashMap<>();
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray data = jsonObject.getJSONObject("results").getJSONArray("bindings");
            for(int i = 0; i < data.length(); i++){
                JSONObject element = data.getJSONObject(i);
                String uri = element.getJSONObject("c1").getString("value");
                List<String> splitUri = Arrays.asList(uri.split("/"));
                String label = splitUri.get(splitUri.size()-1);
                label = label.replace("_", " ");
                entitiesMap.put(uri, label);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        showDialogValuesOfValue(entitiesMap, option);
    }

    private void showDialogValuesOfValue(final Map<String, String> entitiesMap, final String option){
        final String[] strarray = new String[entitiesMap.size()];

        List<String> list = new ArrayList<String>(entitiesMap.values());
        list.toArray(strarray);

        new AlertDialog.Builder(this)
                .setTitle("Select value")
                .setSingleChoiceItems(strarray, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String uri = (String) entitiesMap.keySet().toArray()[i];
                        String optionSearch = option + "\n" +
                                "    filter (?s2 = <" + uri + ">) .";
                        dialogInterface.dismiss();
                        new FacetedSearch(MainActivity.this, keywordSearch, optionSearch, mFSResults, mFSAdapter, MainActivity.this);
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    //+++++++++++++++++++++++++++++++++++++++++++

    private void getDistinctFilter(){
        String url = Utils.createUrlGetDistincts(keywordSearch);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
                parseDistinctFilterResult(response);
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

    private void parseDistinctFilterResult(String response){
        Map<String, String> entitiesMap = new HashMap<>();
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray data = jsonObject.getJSONObject("results").getJSONArray("bindings");
            for(int i = 0; i < data.length(); i++){
                JSONObject element = data.getJSONObject(i);
                String uri = element.getJSONObject("c1").getString("value");
                List<String> splitUri = Arrays.asList(uri.split("/"));
                String label = splitUri.get(splitUri.size()-1);
                label = label.replace("_", " ");
                entitiesMap.put(uri, label);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        showDialogDistinctsEntity(entitiesMap);
    }

    private void showDialogDistinctsEntity(final Map<String, String> entitiesMap){
        final String[] strarray = new String[entitiesMap.size()];

        List<String> list = new ArrayList<String>(entitiesMap.values());
        list.toArray(strarray);

        new AlertDialog.Builder(this)
                .setTitle("Select entity")
                .setSingleChoiceItems(strarray, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String uri = (String) entitiesMap.keySet().toArray()[i];
                        String option = "filter (?s1 = <" + uri + ">) .";
                        dialogInterface.dismiss();
                        new FacetedSearch(MainActivity.this, keywordSearch, option, mFSResults, mFSAdapter, MainActivity.this);
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    //+++++++++++++++++++++++++++++++++++++++++++
    @Override
    public void onClickFS(FSResult fsResult) {
        if(!isSelectedItem(fsResult.getUri())){
            Recommend recommend = new Recommend(
                    fsResult.getLabel(), fsResult.getDescription(), fsResult.getUri(), fsResult.getThumbnail());
            addSelectedRecommend(recommend);
        }else{
            showLogAndToast("Item added. Please choice other item!");
        }
    }

//    private void showPopupSearchOption(){
//        PopupMenu popupMenu = new PopupMenu(this, mImgSearchOption);
//        popupMenu.getMenuInflater().inflate(R.menu.menu_search_option, popupMenu.getMenu());
//        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                switch (item.getItemId()){
//                    case R.id.menu_lookup_uri:
//                        if(typeSearch != LOOKUP_URI){
//                            typeSearch = LOOKUP_URI;
//                            callBack.changeTypeSearch();
//                        }
//                        return true;
//                    case R.id.menu_faceted_search:
//                        if(typeSearch != FACTED_SEARCH){
//                            typeSearch = FACTED_SEARCH;
//                            callBack.changeTypeSearch();
//                        }
//                        return true;
//                    case R.id.menu_faceted_search_advanced:
//                        if(typeSearch != FACTED_SEARCH_ADVANCED){
//                            typeSearch = FACTED_SEARCH_ADVANCED;
//                            callBack.changeTypeSearch();
//                        }
//                        return true;
//                }
//                return false;
//            }
//        });
//        popupMenu.show();
//    }

    private String getKeywordInput(){
        String keywords = mEdtSearch.getText().toString().trim();
        if(keywords.length() == 0){
            mEdtSearch.setError("Keyword cannot empty!");
            return null;
        }
        return keywords;
    }

    @Override
    public void hideFSFilterCallback() {
        hideFSFilter();
    }

    @Override
    public void showFSFilterCallback() {
        showFSFilter();
    }


    private static String removeStopWord(String keyword){
        if(keyword == null || keyword.isEmpty())
            return "";
        String result = "";
        List<String> splitWord = Arrays.asList(keyword.split(" "));
        List<String> stopWord = Arrays.asList(Config.STOP_WORD);
        for(String word : splitWord){
            if(!stopWord.contains(word)){
                result += " " + word;
            }
        }
        return result.trim();
    }

    private void listeningEdtSearch(){
        mEdtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(final Editable editable) {
                String keyword = String.valueOf(editable.toString().trim());
                if(keyword.split(" ").length <= 2){
                    if(typeSearch != LOOKUP_URI){
                        typeSearch = LOOKUP_URI;
                        callBack.changeTypeSearch();
                    }
                    if(keyword.length() >= 3){
                        hideSelectedRecommend();
                        new LookupUri(MainActivity.this, keyword, new LookupUri.Callback() {
                            @Override
                            public void showMessageKSNoResult(String message) {
                                typeSearch = FACTED_SEARCH_ADVANCED;
                                callBack.changeTypeSearch();
                                new FacetedSearchAdvanced(MainActivity.this, keywordSearch, MainActivity.this);
                            }

                            @Override
                            public void completedKS() {

                            }
                        });
                    }else{
                        clearLookupResult();
                        if(mSelectedRecommends.size() > 0){
                            showSelectedRecommend();
                        }
                    }
                }else{
                    if(typeSearch != FACTED_SEARCH_ADVANCED){
                        typeSearch = FACTED_SEARCH_ADVANCED;
                        callBack.changeTypeSearch();
                    }
                }
            }
        });

        mListviewKS.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                KeywordSearch keywordSearch = mKeywordSearchs.get(i);
                if(!isSelectedItem(keywordSearch.getUri())){
                    Recommend recommend = new Recommend(
                            keywordSearch.getLabel(), keywordSearch.getDescription(),keywordSearch.getUri(), keywordSearch.getThumb());

                    addSelectedRecommend(recommend);
                    clearLookupResult();
                    mEdtSearch.setText("");
                }else{
                    showLogAndToast("Item added. Please select other item!");
                }
            }
        });
    }

    private void addSelectedRecommend(Recommend recommend){
        if(mSelectedRecommends.size() == MAXITEM){
            mSelectedRecommends.remove(mSelectedRecommends.size() - 1);
            mRecommedAdapter.notifyDataSetChanged();
            showLogAndToast("Max item is 4!");
        }
        mSelectedRecommends.add(recommend);
        mRecommedAdapter.notifyDataSetChanged();
        showSelectedRecommend();
    }

    private boolean isSelectedItem(String uri){
        if(mSelectedRecommends.size() == 0) return false;
        for(Recommend recommend : mSelectedRecommends){
            if(recommend.getUri().equals(uri)){
                return true;
            }
        }
        return false;
    }

    private void clearLookupResult(){
        mKeywordSearchs.clear();
        mKSAdapter.notifyDataSetChanged();
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
