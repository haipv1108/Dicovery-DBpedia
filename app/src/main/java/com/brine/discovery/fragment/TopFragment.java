package com.brine.discovery.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.brine.discovery.R;
import com.brine.discovery.activity.DetailsActivity;
import com.brine.discovery.activity.RecommendActivity;
import com.brine.discovery.adapter.GridViewAdapter;
import com.brine.discovery.model.Recommend;
import com.brine.discovery.util.DbpediaConstant;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

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
public class TopFragment extends Fragment implements GridViewAdapter.GridAdapterCallback{
    private final static String TAG = TopFragment.class.getCanonicalName();
    private final static int MAXRESULT = 20;
    private GridView mGridView;

    private List<Recommend> mRecommendDatas;
    private GridViewAdapter mGridAdapter;
    private String response;

    public TopFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        response = getArguments().getString(RecommendFragment.DATA);
        return inflater.inflate(R.layout.fragment_top, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mGridView = (GridView) view.findViewById(R.id.grid_view);

        mRecommendDatas = new ArrayList<>();
        mGridAdapter = new GridViewAdapter(getContext(), mRecommendDatas, this);
        mGridView.setAdapter(mGridAdapter);

        parserResponseData();
    }

    private void parserResponseData(){
        try {
            JSONArray jsonArray = new JSONArray(response);
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
                    insertRecommend(recommend);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void insertRecommend(Recommend recommend){
        mRecommendDatas.add(recommend);
        Collections.sort(mRecommendDatas, new Comparator<Recommend>() {
            @Override
            public int compare(Recommend recommend, Recommend t1) {
                return recommend.getThreshold() < t1.getThreshold()? 1 : -1;
            }
        });
        if(mRecommendDatas.size() > MAXRESULT){
            mRecommendDatas.remove(mRecommendDatas.size() - 1);
        }
        mGridAdapter.notifyDataSetChanged();
    }

    @Override
    public void showDetails(Recommend recommend) {
        Intent intent = new Intent(getContext(), DetailsActivity.class);
        intent.putExtra(DetailsActivity.DATA, recommend.getUri());
        startActivity(intent);
    }

    @Override
    public void addSearch(Recommend recommend) {

    }

    @Override
    public void exSearch(Recommend recommend) {
        List<String> recommends = new ArrayList<>();
        recommends.add(recommend.getUri());
        ((RecommendActivity)getActivity()).EXSearch(recommends);
    }
}
