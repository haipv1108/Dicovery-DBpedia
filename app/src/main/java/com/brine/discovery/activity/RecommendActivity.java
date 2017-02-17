package com.brine.discovery.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.brine.discovery.AppController;
import com.brine.discovery.R;
import com.brine.discovery.fragment.RecommendFragment;
import com.brine.discovery.fragment.TopFragment;
import com.brine.discovery.util.Config;
import com.brine.discovery.util.DbpediaConstant;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class RecommendActivity extends AppCompatActivity {
    private final static String TAG = RecommendActivity.class.getCanonicalName();
    public final static String DATA = "response";
    public final static float THRESHOLD = 0.0f;
    final int DEFAULT_TIMEOUT = 20 * 1000;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend);

        ViewPager mViewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(mViewPager);

        TabLayout mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        String response = getIntent().getStringExtra(DATA);
        adapter.addFrag(new TopFragment(), "TOP", response);
        try {
            JSONArray jsonArray = new JSONArray(response);
            for(int i = jsonArray.length() - 1; i >=0; i--){
                JSONArray results = jsonArray.getJSONObject(i).getJSONArray("results");
                if(!checkMeasure(results)) continue;
                String label = jsonArray.getJSONObject(i).getString("label");
                String uri = jsonArray.getJSONObject(i).getString("uri");
                if(label.equals("null")){
                    label = uri;
                }
                if(DbpediaConstant.isContext(uri)){
                    adapter.addFrag(new RecommendFragment(), label, results.toString());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        viewPager.setAdapter(adapter);
    }

    private boolean checkMeasure(JSONArray results){
        try {
            for(int i = 0; i < results.length(); i++){
                float threshold = BigDecimal.valueOf(results.getJSONObject(i)
                        .getDouble("value")).floatValue();
                String abtract = results.getJSONObject(i).getString("abstract");
                String label = results.getJSONObject(i).getString("label");
                if(threshold > THRESHOLD && !label.equals("null") && !abtract.equals("null"))
                    return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFrag(Fragment fragment, String title, String data) {
            Bundle bundle = new Bundle();
            bundle.putString(RecommendFragment.DATA, data);
            fragment.setArguments(bundle);
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }


    public void EXSearch(final List<String> recommends){
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
        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                mProgressDialog.dismiss();
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    showLogAndToast(response);
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
        Intent intent = getIntent();
        intent.putExtra(DATA, response);
        startActivity(intent);
    }

    private void showLog(String message){
        if(message != null){
            Log.d(TAG, message);
        }
    }

    public void showLogAndToast(final String message){
        showLog(message);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
