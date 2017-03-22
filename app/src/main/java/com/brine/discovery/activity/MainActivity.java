package com.brine.discovery.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
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
import com.brine.discovery.model.SLDResult;
import com.brine.discovery.util.Config;
import com.brine.discovery.util.DbpediaConstant;
import com.brine.discovery.util.Utils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, SelectedResultsAdapter.SelectedAdapterCallback,
        FSAdapter.FSAdapterCallback{
    private static final String TAG = MainActivity.class.getCanonicalName();
    private static final int MAXITEM = 4;
    private static final int DEFAULT_TIMEOUT = 20 * 1000;

    private static final int LOOKUP_URI = 1;
    private static final int FACTED_SEARCH = 2;
    private static final int SLIDING_WINDOW = 3;
    private static final int FACTED_SEARCH_ADVANCED = 4;

    public static final String TYPE_OPTION = "type";
    public static final String ATTRIBUTE_OPTION = "attribute";
    public static final String VALUE_OPTION = "value";
    public static final String DISTINCT_OPTION = "distinct";

    private EditText mEdtSearch;
    private ListView mListviewKS;
    private RecyclerView mRecycleRecommed;
    private RelativeLayout mRltSelectedRecommend;

    private LinearLayout mLinearFSFilter;
    private LinearLayout mFSFilterType, mFSFilterAttribute, mFSFilterValue, mFSFilterDistinct;

    private KSAdapter mKSAdapter;
    private SelectedResultsAdapter mRecommedAdapter;
    private List<KeywordSearch> mKeywordSearchs;
    private List<Recommend> mSelectedRecommends;
    private ProgressDialog mProgressDialog;

    private FSAdapter mFSAdapter;
    private List<FSResult> mFSResults;
    private RecyclerView mRecyclerFS;
    private ImageView mImgSearchOption;
    private ImageView mImgSearch;

    private String keywordSearch = "";
    private Map<String, String> mapSearchOptions;

    private int typeSearch;

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
        if(typeSearch == LOOKUP_URI){
            listeningEdtSearch();
        }
    }

    private TypeSearchCallBack callBack = new TypeSearchCallBack() {
        @Override
        public void changeTypeSearch() {
            resetSearch();
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
                case SLIDING_WINDOW:
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

    private void resetSearch(){
        mEdtSearch.setText("");
    }

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
                EXSearch(mSelectedRecommends);
                break;
            case R.id.img_search_option:
                showPopupSearchOption();
                break;
            case R.id.img_search:
                String keyword = getKeywordInput();
                String keywordRemovedStopWord = removeStopWord(keyword);
                if(keyword != null){
                    keywordSearch = keywordRemovedStopWord;
                    if(typeSearch == FACTED_SEARCH){
                        showLogAndToast("Search " + keywordSearch);
                        facetedSearch(keywordSearch, "");
                    }if(typeSearch == SLIDING_WINDOW) {
                        slidingWindow(keywordSearch);
                    }
                    if(typeSearch == FACTED_SEARCH_ADVANCED){
                        facetedSearchAdvanced(keywordSearch);
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
                        facetedSearch(keywordSearch, option);
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
                        facetedSearch(keywordSearch, optionSearch);
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
                        facetedSearch(keywordSearch, optionSearch);
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
                        facetedSearch(keywordSearch, option);
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
                    fsResult.getLabel(), fsResult.getUri(), fsResult.getThumbnail());
            addSelectedRecommend(recommend);
        }else{
            showLogAndToast("Item added. Please choice other item!");
        }
    }

    private void showPopupSearchOption(){
        PopupMenu popupMenu = new PopupMenu(this, mImgSearchOption);
        popupMenu.getMenuInflater().inflate(R.menu.menu_search_option, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_lookup_uri:
                        if(typeSearch != LOOKUP_URI){
                            typeSearch = LOOKUP_URI;
                            callBack.changeTypeSearch();
                        }
                        return true;
                    case R.id.menu_faceted_search:
                        if(typeSearch != FACTED_SEARCH){
                            typeSearch = FACTED_SEARCH;
                            callBack.changeTypeSearch();
                        }
                        return true;
                    case R.id.menu_sliding_window:
                        if(typeSearch != SLIDING_WINDOW){
                            typeSearch = SLIDING_WINDOW;
                            callBack.changeTypeSearch();
                        }
                        return true;
                    case R.id.menu_faceted_search_advanced:
                        if(typeSearch != FACTED_SEARCH_ADVANCED){
                            typeSearch = FACTED_SEARCH_ADVANCED;
                            callBack.changeTypeSearch();
                        }
                        return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private String getKeywordInput(){
        String keywords = mEdtSearch.getText().toString().trim();
        if(keywords.length() == 0){
            mEdtSearch.setError("Keyword cannot empty!");
            return null;
        }
        return keywords;
    }

    private void facetedSearch(String keywords, String optionSearch){
        mFSResults.clear();
        mFSAdapter.notifyDataSetChanged();
        hideFSFilter();

        String url = Utils.createUrlFacetedSearch(keywords, optionSearch);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
                parseFSResult(response);
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

    private void parseFSResult(String response){
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
                    searchImageFSSearch();
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
            showFSFilter();
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

    private void searchImageFSSearch(){
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
                                    insertFSThumbnail(uri, thumb);
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
        AppController.getInstance().addToRequestQueue(request, "lookup");
    }

    private void insertFSThumbnail(String uri, String thumb){
        for(FSResult fs : mFSResults){
            if(fs.getUri().equals(uri)){
                fs.setThumbnail(thumb);
                mKSAdapter.notifyDataSetChanged();
                return;
            }
        }
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++
    private void slidingWindow(String keywords){
        List<String> phrases = splitKeywordToPhrase(keywords);
        new SLDWindowTop(this).execute(phrases);
        new SLDWindowRecommend(this).execute(phrases);
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

    private class SLDWindowTop extends AsyncTask<List<String>, Void, List<String>>{
        ProgressDialog progressDialog;
        Context context;

        public SLDWindowTop(Context _context){
            this.context = _context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Loading...");
            progressDialog.show();
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

        }
    }

    private boolean isStopWord(String word) {
        List<String> listStopWord = Arrays.asList(Config.STOP_WORD);
        return listStopWord.contains(word.toLowerCase());
    }

    private class SLDWindowRecommend extends AsyncTask<List<String>, Void, List<SLDResult>>{
        private ProgressDialog progressDialog;
        private Context context;

        public SLDWindowRecommend(Context _context){
            this.context = _context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Loading");
            progressDialog.show();
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
        }
    }

    //+++++++++++++++++++++++++++++++++++++++++++++++++++++
    private void facetedSearchAdvanced(final String keyword){
        String url = Utils.createUrlGetTypes(keyword);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
                Map<String, Integer> entitiesMap = new HashMap<>();
                List<String> splitKeyword = Arrays.asList(keyword.split(" "));
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
                        facetedSearch(keyword, "");
                    }else if(entitiesMap.size() == 1){
                        String uri = (String) entitiesMap.keySet().toArray()[0];
                        String option = " ?s1 a " + "<" + uri + "> .\n" ;
                        showLog("1. OPTION SEARCH: " + option);
                        facetedSearch(keyword, option);
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
                        facetedSearch(keyword, option);
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

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++
    private void EXSearch(final List<Recommend> recommends){
        AppController.getInstance().setUriDecovery(recommends);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.show();

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        for(Recommend param : recommends){
            params.add("nodes[]", param.getUri());
        }
        showLog("Params: " + params.toString());
        client.post(Config.DISCOVERYHUB_RECOMMEND_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                showLog(response);
                parserPostDataRecommend(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                showLogAndToast("Error request to server. Try again!");
                mProgressDialog.dismiss();
            }
        });
    }

    private void parserPostDataRecommend(String response){
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
                        showResultRecommendation(response);
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

    private void showResultRecommendation(String response){
        Intent intent = new Intent(MainActivity.this, RecommendActivity.class);
        intent.putExtra(RecommendActivity.DATA, response);
        startActivity(intent);
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
                if(typeSearch == LOOKUP_URI){
                    String word = String.valueOf(editable.toString().trim());
                    if(word.length() >= 3){
                        hideSelectedRecommend();
                        lookupUri(word);
                    }else{
                        clearLookupResult();
                        if(mSelectedRecommends.size() > 0){
                            showSelectedRecommend();
                        }
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
                            keywordSearch.getLabel(), keywordSearch.getUri(), keywordSearch.getThumb());

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

    private void lookupUri(String word){
        AppController.getInstance().cancelPendingRequests("lookup");
        String url = Utils.createUrlKeywordSearch(word);
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                clearLookupResult();
                parseXmlLookupResult(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showLog(error.getMessage());
                showLogAndToast("No results");
            }
        });
        AppController.getInstance().addToRequestQueue(stringRequest, "lookup");
    }

    private void parseXmlLookupResult(String response){
        if(response == null) return;
        InputStream inputStream = new ByteArrayInputStream(
                response.getBytes(StandardCharsets.UTF_8));

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputStream);

            Element root = doc.getDocumentElement();

            NodeList nodeList = root.getChildNodes();
            if(nodeList.getLength() == 1){
                showLogAndToast("No result. Try other keyword!");
                return;
            }
            for(int i = 0; i < nodeList.getLength(); i++){
                Node node = nodeList.item(i);
                if(node instanceof Element){
                    Element result = (Element) node;
                    String label = result.getElementsByTagName("Label").item(0).getTextContent();
                    String uri = result.getElementsByTagName("URI").item(0).getTextContent();
                    String description = result.getElementsByTagName("Description")
                            .item(0).getTextContent();

                    if(uri.isEmpty() || description.isEmpty()) continue;
                    NodeList childNodeList = result.getElementsByTagName("Class");
                    String type = "";
                    if(childNodeList.getLength() == 0){
                        type = "Other";
                    }else{
                        String typeValue = ((Element)childNodeList.item(0)).getElementsByTagName("Label").item(0).getTextContent();
                        type = firstUpperString(typeValue);
                    }

                    KeywordSearch ks = new KeywordSearch(label, uri, description, null, type);
                    mKeywordSearchs.add(ks);
                    mKSAdapter.notifyDataSetChanged();
                }
            }
            searchImageForUriKSSearch();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
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

    private void searchImageForUriKSSearch(){
        if(mKeywordSearchs.size() == 0){
            return;
        }
        String query = "SELECT ?uri ?img\n" +
                "WHERE\n" +
                "{\n" +
                "?uri <http://dbpedia.org/ontology/thumbnail> ?img .\n" +
                "FILTER (";
        for(int i = 0; i < mKeywordSearchs.size() - 1; i++){
            query += " ?uri = <" + mKeywordSearchs.get(i).getUri() + "> || ";
        }
        query += " ?uri = <" + mKeywordSearchs.get(mKeywordSearchs.size() - 1) + "> ) }";
        showLog("Query: " + query);
        String url = Utils.createUrlDbpedia(query);
        showLog("URL: " + url);

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
                                    insertKSThumbnail(uri, thumb);
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
        AppController.getInstance().addToRequestQueue(request, "lookup");
    }

    private void insertKSThumbnail(String uri, String thumb){
        for(KeywordSearch ks : mKeywordSearchs){
            if(ks.getUri().equals(uri)){
                ks.setThumb(thumb);
                mKSAdapter.notifyDataSetChanged();
                return;
            }
        }
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
