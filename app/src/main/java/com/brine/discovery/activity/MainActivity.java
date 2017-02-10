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
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
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
import com.brine.discovery.util.Utils;
import com.cunoraz.tagview.Tag;

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

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = MainActivity.class.getCanonicalName();
    public static final String DATASEACH = "exsearch";

    private EditText mEdtSearch;
    private ListView mListviewKS;
    private RecyclerView mRecycleRecommed;
    private ImageButton mBtnEXSearch;
    private RelativeLayout mRltSelectedRecommend;

    private KSAdapter mKSAdapter;
    private SelectedResultsAdapter mRecommedAdapter;
    private List<KeywordSearch> mKeywordSearchs;
    private List<String> mInputURIs;
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
        mBtnEXSearch = (ImageButton) findViewById(R.id.btn_EXSearch);
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
        mInputURIs = new ArrayList<>();
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
                List<String> inputUris = new ArrayList<>();
                for(Recommend node : mSelectedRecommends){
                    inputUris.add(node.getUri());
                }
                EXSearch(inputUris);
                break;
        }
    }

    private void EXSearch(final List<String> recommends){
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST,
                "http://api.discoveryhub.co/recommendations", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parserPostDataRecommend(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                for(String uri: recommends){
                    params.put("nodes[]", uri);
                }
                showLog("params: " + params.toString());
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(request, "EXSearch");
    }

    private void parserPostDataRecommend(String response){
        if(response == null) return;
        try {
            JSONObject json = new JSONObject(response);
            String id = json.getString("id");
            List<String> splitId = Arrays.asList(id.split("/"));
            String key = splitId.get(splitId.size() - 1);
            showLogAndToast(key);
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
        intent.putExtra(RecommendActivity.KEY, response);
//        finish();
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
                Recommend recommend = new Recommend(
                        keywordSearch.getLabel(), keywordSearch.getUri(), keywordSearch.getThumb());
                mSelectedRecommends.add(recommend);
                mRecommedAdapter.notifyDataSetChanged();
                showSelectedRecommend();
                clearLookupResult();
                mEdtSearch.setText("");
            }
        });
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

            Element element=doc.getDocumentElement();
            element.normalize();

            NodeList nList = doc.getElementsByTagName("Result");
            for(int i = 0; i < nList.getLength(); i++){
                Node node = nList.item(i);
                if(node.getNodeType() == Node.ELEMENT_NODE){
                    Element e = (Element) node;
                    String label = e.getElementsByTagName("Label").item(0)
                            .getChildNodes().item(0).getNodeValue();
                    String uri = e.getElementsByTagName("URI").item(0)
                            .getChildNodes().item(0).getNodeValue();
                    String description = "";
                    if(e.getElementsByTagName("Description").item(0).getChildNodes().item(0) != null) {
                        description = e.getElementsByTagName("Description").item(0)
                                .getChildNodes().item(0).getNodeValue();
                    }
                    KeywordSearch ks = new KeywordSearch(label, uri, description, null, "Type");
                    mKeywordSearchs.add(ks);
                    mKSAdapter.notifyDataSetChanged();
                }
            }
            searchImageForUri();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
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

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
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
