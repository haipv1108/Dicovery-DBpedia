package com.brine.discovery.activity;

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

import com.brine.discovery.R;
import com.brine.discovery.fragment.RecommendFragment;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class RecommendActivity extends AppCompatActivity {
    private final static String TAG = RecommendActivity.class.getCanonicalName();
    public final static String KEY = "response";

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

        String response = getIntent().getStringExtra(KEY);
        try {
            JSONArray jsonArray = new JSONArray(response);
            for(int i = 0; i < jsonArray.length(); i++){
                JSONArray results = jsonArray.getJSONObject(i).getJSONArray("results");
                String label = jsonArray.getJSONObject(i).getString("label");
                showLog("Label: " + label);
                if(label.length() == 0){
                    label = jsonArray.getJSONObject(i).getString("uri");
                }
                adapter.addFrag(new RecommendFragment(), label, results.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
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

        public void addFrag(Fragment fragment, String title, String data) {
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
