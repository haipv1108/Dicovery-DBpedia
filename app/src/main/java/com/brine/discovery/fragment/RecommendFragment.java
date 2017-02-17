package com.brine.discovery.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.brine.discovery.R;
import com.brine.discovery.activity.DetailsActivity;
import com.brine.discovery.activity.RecommendActivity;
import com.brine.discovery.adapter.GridViewAdapter;
import com.brine.discovery.model.Recommend;
import com.brine.discovery.util.DbpediaConstant;

import org.json.JSONArray;
import org.json.JSONException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecommendFragment extends Fragment
        implements GridViewAdapter.GridAdapterCallback{
    private final static String TAG = RecommendFragment.class.getCanonicalName();
    public final static String DATA = "data";
    public final static String TOP_TYPE = "top";
    private final static int MAXTOPRESULT = 20;

    private GridView mGridView;

    private List<Recommend> mRecommendDatas;
    private GridViewAdapter mGridAdapter;
    private String mResponse;
    private boolean mTopType;

    public RecommendFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mResponse = getArguments().getString(DATA);
        mTopType = getArguments().getBoolean(TOP_TYPE);
        return inflater.inflate(R.layout.fragment_recommendation, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mGridView = (GridView) view.findViewById(R.id.grid_view);

        mRecommendDatas = new ArrayList<>();
        mGridAdapter = new GridViewAdapter(getContext(), mRecommendDatas, this);
        mGridView.setAdapter(mGridAdapter);

        if(mTopType){
            parserTopResponseData();
        }else{
            parserNormalResponseData();
        }
    }
    //======================TOP recommend=========================
    private void parserTopResponseData(){
        try {
            JSONArray jsonArray = new JSONArray(mResponse);
            for(int i = 0; i < jsonArray.length(); i++){
                JSONArray results = jsonArray.getJSONObject(i).getJSONArray("results");
                String uriType = results.getJSONObject(i).getString("uri");
                if(DbpediaConstant.isContext(uriType)) continue;
                for(int j = 0; j < results.length(); j++){
                    final float threshold = BigDecimal.valueOf(results.getJSONObject(i)
                            .getDouble("value")).floatValue();
                    final String label = results.getJSONObject(j).getString("label");
                    String abtract = results.getJSONObject(j).getString("abstract");
                    final String uri = results.getJSONObject(j).getString("uri");
                    final String image = results.getJSONObject(j).getString("image");
                    if(label.equals("null") || abtract.equals("null"))
                        continue;
                    Recommend recommend = new Recommend(label, uri, image, threshold);
                    insertTopRecommend(recommend);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void insertTopRecommend(Recommend recommend){
        mRecommendDatas.add(recommend);
        Collections.sort(mRecommendDatas, new Comparator<Recommend>() {
            @Override
            public int compare(Recommend recommend, Recommend t1) {
                return recommend.getThreshold() < t1.getThreshold()? 1 : -1;
            }
        });
        if(mRecommendDatas.size() > MAXTOPRESULT){
            mRecommendDatas.remove(mRecommendDatas.size() - 1);
        }
        mGridAdapter.notifyDataSetChanged();
    }

    //========================Normal recommend================

    private void parserNormalResponseData(){
        try {
            JSONArray jsonArray = new JSONArray(mResponse);
            for(int i = 0; i < jsonArray.length(); i++){
                String label = jsonArray.getJSONObject(i).getString("label");
                String uri = jsonArray.getJSONObject(i).getString("uri");
                String image = jsonArray.getJSONObject(i).getString("image");
                if(label.equals("null")){
                    continue;
                }
                Recommend recommend = new Recommend(label, uri, image);
                insertNormalRecommend(recommend);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void insertNormalRecommend(Recommend recommend){
        mRecommendDatas.add(recommend);
        mGridAdapter.notifyDataSetChanged();
    }

    @Override
    public void showDetails(Recommend recommend) {
        Intent intent = new Intent(getContext(), DetailsActivity.class);
        intent.putExtra(DetailsActivity.DATA, recommend.getUri());
        startActivity(intent);
    }

    @Override
    public void exSearch(Recommend recommend) {
        List<String> recommends = new ArrayList<>();
        recommends.add(recommend.getUri());
        ((RecommendActivity)getActivity()).EXSearch(recommends);
    }
}
