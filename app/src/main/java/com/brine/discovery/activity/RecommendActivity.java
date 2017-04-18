package com.brine.discovery.activity;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.brine.discovery.R;
import com.brine.discovery.fragment.RecommendFragment;
import com.brine.discovery.message.MessageObserverManager;
import com.brine.discovery.model.Recommend;
import com.brine.discovery.search.EXSearch;
import com.brine.discovery.util.DbpediaConstant;

import org.json.JSONArray;
import org.json.JSONException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class RecommendActivity extends AppCompatActivity implements EXSearch.Callback{
    private final static String TAG = RecommendActivity.class.getCanonicalName();
    public final static String DATA = "response";
    public final static float THRESHOLD = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend);

        ViewPager mViewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(mViewPager);

        TabLayout mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    protected void onStop() {
        super.onStop();
        MessageObserverManager.getInstance().removeAllData();
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        String response = getIntent().getStringExtra(DATA);
//        adapter.addFrag(new RecommendFragment(), "TOP", response, false);
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
                    adapter.addFrag(new RecommendFragment(), label, results.toString(), false);
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

        void addFrag(Fragment fragment, String title, String data, boolean type) {
            Bundle bundle = new Bundle();
            bundle.putString(RecommendFragment.DATA, data);
//            bundle.putBoolean(RecommendFragment.TOP_TYPE, type);
            fragment.setArguments(bundle);
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public void showResultRecommend(String response) {
        MessageObserverManager.getInstance().removeAllData();
        Intent intent = getIntent();
        intent.putExtra(DATA, response);
        startActivity(intent);
    }

    public void exSearch(final List<Recommend> recommends) {
        new EXSearch(RecommendActivity.this, recommends, RecommendActivity.this);
    }

    private void showLog(String message){
        if(message != null){
            Log.d(TAG, message);
        }
    }
}
