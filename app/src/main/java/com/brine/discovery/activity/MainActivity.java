package com.brine.discovery.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.brine.discovery.AppController;
import com.brine.discovery.R;
import com.brine.discovery.adapter.KSAdapter;
import com.brine.discovery.adapter.SelectedResultsAdapter;
import com.brine.discovery.model.KeywordSearch;
import com.brine.discovery.model.Recommend;
import com.brine.discovery.util.Config;
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
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = MainActivity.class.getCanonicalName();
    public static final String DATASEACH = "exsearch";
    private static final int MAXITEM = 4;

    private EditText mEdtSearch;
    private ListView mListviewKS;
    private RecyclerView mRecycleRecommed;
    private RelativeLayout mRltSelectedRecommend;

    private KSAdapter mKSAdapter;
    private SelectedResultsAdapter mRecommedAdapter;
    private List<KeywordSearch> mKeywordSearchs;
    private List<Recommend> mSelectedRecommends;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
        init();
        listeningEdtSearch();
    }

    private void initUI(){
        mEdtSearch = (EditText) findViewById(R.id.edt_search);
        mListviewKS = (ListView) findViewById(R.id.lv_keyword_search);
        mRecycleRecommed = (RecyclerView) findViewById(R.id.recycle_selected_uri);
        ImageButton mBtnEXSearch = (ImageButton) findViewById(R.id.btn_EXSearch);
        mBtnEXSearch.setOnClickListener(this);
        mRltSelectedRecommend = (RelativeLayout) findViewById(R.id.rlt_seledted_recommend);
    }

    private void hideSelectedRecommend(){
        mListviewKS.setVisibility(View.VISIBLE);
        mRltSelectedRecommend.setVisibility(View.GONE);
    }

    private void showSelectedRecommend(){
        mListviewKS.setVisibility(View.GONE);
        mRltSelectedRecommend.setVisibility(View.VISIBLE);
    }

    private void init(){
        mKeywordSearchs = new ArrayList<>();
        mSelectedRecommends = new ArrayList<>();

        mKSAdapter = new KSAdapter(this, mKeywordSearchs);
        mListviewKS.setAdapter(mKSAdapter);

        mRecommedAdapter = new SelectedResultsAdapter(this, mSelectedRecommends);
        mRecycleRecommed.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManagerRecommend =
                new LinearLayoutManager(getApplicationContext(),
                        LinearLayoutManager.HORIZONTAL, false);
        mRecycleRecommed.setLayoutManager(layoutManagerRecommend);
        mRecycleRecommed.addItemDecoration(
                new DividerItemDecoration(this, LinearLayout.HORIZONTAL));
        mRecycleRecommed.setItemAnimator(new DefaultItemAnimator());
        mRecycleRecommed.setAdapter(mRecommedAdapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_EXSearch:
                if(mSelectedRecommends.size() == 0){
                    showLogAndToast("Please select uri!");
                    return;
                }
                final List<String> inputUris = convertToListString(mSelectedRecommends);
                EXSearch(inputUris);
                break;
        }
    }

    private List<String> convertToListString(List<Recommend> recommends){
        List<String> inputUris = new ArrayList<>();
        for(Recommend node : recommends){
            inputUris.add(node.getUri());
        }
        return inputUris;
    }

    private void EXSearch(final List<String> recommends){
        AppController.getInstance().setUriDecovery(recommends);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.show();

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        for(String param : recommends){
            params.add("nodes[]", param);
        }
        showLog("Params: " + params.toString());
        client.post(Config.DISCOVERYHUB_RECOMMEND_API, params, new AsyncHttpResponseHandler() {
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
        if(key == null) return;
        String url = "http://api.discoveryhub.co/recommendations/" + key;
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                mProgressDialog.dismiss();
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    if(jsonArray.length() == 1 &&
                            jsonArray.getJSONObject(0).getJSONArray("results").length() == 1){
                        showLogAndToast("No results. Try again!");
                    }else{
                        showResultRecommendation(response);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mProgressDialog.dismiss();
            }
        });
        AppController.getInstance().addToRequestQueue(request, "recommendation");
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
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String word = String.valueOf(editable.toString().trim());
                        if(word.length() >= 3){
                            clearLookupResult();
                            hideSelectedRecommend();
                            lookupUri(word);
                        }else{
                            clearLookupResult();
                            if(mSelectedRecommends.size() > 0){
                                showSelectedRecommend();
                            }
                        }
                    }
                }, 1000);
            }
        });

        mListviewKS.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                KeywordSearch keywordSearch = mKeywordSearchs.get(i);
                if(!isSelectedItem(keywordSearch.getUri())){
                    Recommend recommend = new Recommend(
                            keywordSearch.getLabel(), keywordSearch.getUri(), keywordSearch.getThumb());

                    if(mSelectedRecommends.size() == MAXITEM){
                        mSelectedRecommends.remove(mSelectedRecommends.size() - 1);
                        mRecommedAdapter.notifyDataSetChanged();
                        showLogAndToast("Max item is 4!");
                    }
                    mSelectedRecommends.add(recommend);
                    mRecommedAdapter.notifyDataSetChanged();
                    showSelectedRecommend();
                    clearLookupResult();
                    mEdtSearch.setText("");
                }else{
                    showLogAndToast("Item added. Please select other item!");
                }
            }
        });
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
            if(nodeList.getLength() == 0){
                showLogAndToast("No results");
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
            searchImageForUri();
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

    private void searchImageForUri(){
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
        AppController.getInstance().addToRequestQueue(request, "image");
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
