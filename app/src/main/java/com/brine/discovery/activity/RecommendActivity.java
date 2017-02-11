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
import android.view.KeyEvent;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecommendActivity extends AppCompatActivity {
    private final static String TAG = RecommendActivity.class.getCanonicalName();
    public final static String DATA = "response";
    public final static float THRESHOLD = 0.00069f;

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
            for(int i = 0; i < jsonArray.length(); i++){
                JSONArray results = jsonArray.getJSONObject(i).getJSONArray("results");
                if(!checkMeasure(results)) continue;
                String label = jsonArray.getJSONObject(i).getString("label");
                if(label.equals("null")){
                    label = jsonArray.getJSONObject(i).getString("uri");
                }
//                adapter.addFrag(new RecommendFragment(), label, results.toString());
                if(label.equals("Mixed")){
                    adapter.addFragTop(new RecommendFragment(), "TOP", results.toString());
                }else{
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

        void addFragTop(Fragment fragment, String title, String data){
            Bundle bundle = new Bundle();
            bundle.putString(RecommendFragment.DATA, data);
            fragment.setArguments(bundle);
            mFragmentList.remove(0);
            mFragmentTitleList.remove(0);

            mFragmentList.add(0, fragment);
            mFragmentTitleList.add(0, title);
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

        StringRequest request = new StringRequest(Request.Method.POST,
                "http://api.discoveryhub.co/recommendations", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parserPostDataRecommend(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showLogAndToast("No results! Try again");
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                for(String param : recommends){
                    params.put("nodes[]", param);
                }
                params.put("accessToken", Config.ACCESS_TOKEN_DISCOVEHUB);
                showLog("Params: " + params.toString());
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        AppController.getInstance().addToRequestQueue(request, "EXSearch");
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
        Intent intent = getIntent();
        intent.putExtra(DATA, response);
        startActivity(intent);
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
