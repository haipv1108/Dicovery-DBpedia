package com.brine.discovery.search;

import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.brine.discovery.AppController;
import com.brine.discovery.activity.MainActivity;
import com.brine.discovery.model.KeywordSearch;
import com.brine.discovery.util.Utils;

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
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by phamhai on 29/03/2017.
 */

public class LookupUri {
    private static final String TAG = LookupUri.class.getCanonicalName();

    private MainActivity mActivity;
    private Callback mCallback;

    public interface Callback{
        void showMessageKSNoResult(String message);
        void completedKS();
    }

    public LookupUri(MainActivity activity, String keyword, Callback callback){
        this.mActivity = activity;
        this.mCallback = callback;
        search(keyword);
    }

    public void search(String keyword){
        AppController.getInstance().cancelPendingRequests("lookup");

        String url = Utils.createUrlKeywordSearch(keyword);
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                clearData();
                parse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showLog(error.getMessage());
                mCallback.showMessageKSNoResult("No results");
            }
        });
        AppController.getInstance().addToRequestQueue(stringRequest, "lookup");
    }

    private void clearData(){
        mActivity.mKeywordSearchs.clear();
        mActivity.mKSAdapter.notifyDataSetChanged();
    }

    private void parse(String response){
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
                mCallback.showMessageKSNoResult("No result. Try other keyword!");
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
                    mActivity.mKeywordSearchs.add(ks);
                    mActivity.mKSAdapter.notifyDataSetChanged();
                }
            }
            mCallback.completedKS();
            searchImage();
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

    private void searchImage(){
        if(mActivity.mKeywordSearchs.size() == 0){
            return;
        }
        String query = "SELECT ?uri ?img\n" +
                "WHERE\n" +
                "{\n" +
                "?uri <http://dbpedia.org/ontology/thumbnail> ?img .\n" +
                "FILTER (";
        for(int i = 0; i < mActivity.mKeywordSearchs.size() - 1; i++){
            query += " ?uri = <" + mActivity.mKeywordSearchs.get(i).getUri() + "> || ";
        }
        query += " ?uri = <" + mActivity.mKeywordSearchs.get(mActivity.mKeywordSearchs.size() - 1) + "> ) }";
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
        AppController.getInstance().addToRequestQueue(request, "lookup");
    }

    private void insertImage(String uri, String thumb){
        for(KeywordSearch ks : mActivity.mKeywordSearchs){
            if(ks.getUri().equals(uri)){
                ks.setThumb(thumb);
                mActivity.mKSAdapter.notifyDataSetChanged();
                return;
            }
        }
    }

    private void showLog(String message){
        if(message != null){
            Log.d(TAG, message);
        }
    }
}
