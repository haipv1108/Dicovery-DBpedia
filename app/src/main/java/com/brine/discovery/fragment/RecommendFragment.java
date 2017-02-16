package com.brine.discovery.fragment;

import android.content.Intent;
import android.media.Image;
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
import com.brine.discovery.activity.MainActivity;
import com.brine.discovery.activity.RecommendActivity;
import com.brine.discovery.adapter.GridViewAdapter;
import com.brine.discovery.model.Recommend;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecommendFragment extends Fragment
        implements GridViewAdapter.GridAdapterCallback{
    private final static String TAG = RecommendFragment.class.getCanonicalName();
    public final static String DATA = "data";

    private GridView mGridView;

    private List<Recommend> mRecommendDatas;
    private GridViewAdapter mGridAdapter;
    private String response;

    public RecommendFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        response = getArguments().getString(DATA);
        return inflater.inflate(R.layout.fragment_recommendation, container, false);
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
                String label = jsonArray.getJSONObject(i).getString("label");
                String uri = jsonArray.getJSONObject(i).getString("uri");
                String image = jsonArray.getJSONObject(i).getString("image");
                if(label.equals("null")){
                    continue;
                }
                Recommend recommend = new Recommend(label, uri, image);
                mRecommendDatas.add(recommend);
                mGridAdapter.notifyDataSetChanged();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
