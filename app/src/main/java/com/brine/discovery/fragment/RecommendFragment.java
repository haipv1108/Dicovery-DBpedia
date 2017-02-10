package com.brine.discovery.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.brine.discovery.R;
import com.brine.discovery.activity.MainActivity;
import com.brine.discovery.adapter.GridViewAdapter;
import com.brine.discovery.model.Recommend;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecommendFragment extends Fragment implements GridViewAdapter.GridAdapterCallback{
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
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getContext(), mRecommendDatas.get(i).getUri(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void parserResponseData(){
        try {
            JSONArray jsonArray = new JSONArray(response);
            for(int i = 0; i < jsonArray.length(); i++){
                String label = jsonArray.getJSONObject(i).getString("label");
                String uri = jsonArray.getJSONObject(i).getString("uri");
                String image = jsonArray.getJSONObject(i).getString("image");
                if(label != null){
                    Recommend recommend = new Recommend(label, uri, image);
                    mRecommendDatas.add(recommend);
                    mGridAdapter.notifyDataSetChanged();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showDetails(Recommend recommend) {

    }

    @Override
    public void exSearch(Recommend recommend) {
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.putExtra(MainActivity.DATASEACH, recommend.getUri());
        startActivity(intent);
    }
}
