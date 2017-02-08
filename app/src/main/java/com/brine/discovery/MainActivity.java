package com.brine.discovery;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.brine.discovery.adapter.KSAdapter;
import com.brine.discovery.model.KeywordSearch;
import com.brine.discovery.util.Utils;
import com.cunoraz.tagview.Tag;
import com.cunoraz.tagview.TagView;

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

    private EditText mEdtSearch;
    private ListView mKSListview;
    private TagView mTagGroup;
    private Button mBtnEXSearch;

    private KSAdapter mKSAdapter;
    private List<KeywordSearch> mKeywordSearchs;
    private List<String> mInputURIs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
        init();
        listeningEdtSearch();
    }

    private void initUI(){
        getSupportActionBar().hide();
        mEdtSearch = (EditText) findViewById(R.id.edt_search);
        mKSListview = (ListView) findViewById(R.id.lv_keyword_search);
        mTagGroup = (TagView) findViewById(R.id.tag_group_uri);
        mBtnEXSearch = (Button) findViewById(R.id.btn_EXSearch);
        mBtnEXSearch.setOnClickListener(this);
    }

    private void init(){
        mKeywordSearchs = new ArrayList<>();
        mInputURIs = new ArrayList<>();
        mKSAdapter = new KSAdapter(this, mKeywordSearchs);
        mKSListview.setAdapter(mKSAdapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_EXSearch:
                showLog("INPUT URI: " + mInputURIs.toString());
                if(mInputURIs.size() == 0){
                    showLogAndToast("Please select uri!");
                    return;
                }
                StringRequest request = new StringRequest(Request.Method.POST,
                        "http://api.discoveryhub.co/recommendations", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        showLogAndToast(response);
                        parseJsonRecommendation(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }){
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        for(String node : mInputURIs){
                            params.put("nodes[]", node);
                        }
                        return params;
                    }
                };
                AppController.getInstance().addToRequestQueue(request, "EXSearch");
                break;
        }
    }

    private void parseJsonRecommendation(String response){
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
                showLogAndToast(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //TODO:
            }
        });
        AppController.getInstance().addToRequestQueue(request, "recommendation");
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
            public void afterTextChanged(Editable editable) {
                String word = String.valueOf(editable.toString().trim());
                if(word.length() >= 3){
                    clearLookupResult();
                    lookupUri(word);
                }else{
                    clearLookupResult();
                }
            }
        });

        mKSListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                addTagGroup(mKeywordSearchs.get(i).getLabel(), mKeywordSearchs.get(i).getUri());
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
                }
            }
            searchImageForUri();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    private void searchImageForUri(){
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
                                    insertThumb(uri, thumb);
                                }
                                mKSAdapter.notifyDataSetChanged();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showLog("Error: " + error.getMessage());
            }
        });
        AppController.getInstance().addToRequestQueue(request, "image");
    }

    private void insertThumb(String uri, String thumb){
        showLog("Thumb: " + thumb);
        for(KeywordSearch ks : mKeywordSearchs){
            if(ks.getUri().equals(uri)){
                ks.setThumb(thumb);
            }
        }
    }

    private void addTagGroup(String label, String uri){
        if(!mInputURIs.contains(uri)){
            Tag tag = new Tag(label);
            tag.isDeletable = true;
            mTagGroup.addTag(tag);
            mInputURIs.add(uri);
        }else{
            showLogAndToast(label + " added!");
        }
    }

    private void showLog(String message){
        Log.d(TAG, message);
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
