package com.brine.discovery.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.brine.discovery.R;
import com.brine.discovery.activity.DetailsActivity;
import com.brine.discovery.activity.RecommendActivity;
import com.brine.discovery.adapter.GridViewAdapter;
import com.brine.discovery.adapter.SelectedResultsAdapter;
import com.brine.discovery.message.MessageObserver;
import com.brine.discovery.message.MessageObserverManager;
import com.brine.discovery.model.Recommend;
import com.brine.discovery.util.DbpediaConstant;

import org.json.JSONArray;
import org.json.JSONException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.brine.discovery.message.MessageObserverManager.ADD_ITEM;
import static com.brine.discovery.message.MessageObserverManager.DELETE_ITEM;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecommendFragment extends Fragment
        implements GridViewAdapter.GridAdapterCallback, View.OnClickListener,
        SelectedResultsAdapter.SelectedAdapterCallback, MessageObserver{
    private final static String TAG = RecommendFragment.class.getCanonicalName();
    public final static String DATA = "data";
    public final static String TOP_TYPE = "top";
    private final static int MAXTOPRESULT = 20;
    private static final int MAXITEM = 4;

    private RelativeLayout mRltSelectedRecommend;
    private RecyclerView mRecycleRecommed;
    private ImageButton mBtnEXSearch;
    private GridView mGridView;

    private List<Recommend> mRecommendDatas;
    private List<Recommend> mSelectedRecommends;
    private GridViewAdapter mGridAdapter;
    private SelectedResultsAdapter mRecommedAdapter;
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

        initUI(view);
        init();
        MessageObserverManager.getInstance().addItem(this);
        getSelectedData();
        if(mTopType){
            parserTopResponseData();
        }else{
            parserNormalResponseData();
        }
    }

    @Override
    public void updateSelectedItem(Recommend recommend, int type) {
        if(type == ADD_ITEM){
            mSelectedRecommends.add(recommend);
            showSelectedRecommend();
        }
        if(type == DELETE_ITEM){
            mSelectedRecommends.remove(recommend);
            if(mSelectedRecommends.size() == 0){
                hideSelectedRecommend();
            }
        }
        mRecommedAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MessageObserverManager.getInstance().removeItem(this);
    }

    private void initUI(View view){
        mRltSelectedRecommend = (RelativeLayout) view.findViewById(R.id.rlt_seledted_recommend);
        mRecycleRecommed = (RecyclerView) view.findViewById(R.id.recycle_selected_uri);
        mGridView = (GridView) view.findViewById(R.id.grid_view);
        mBtnEXSearch = (ImageButton) view.findViewById(R.id.btn_EXSearch);
        mBtnEXSearch.setOnClickListener(this);
    }

    private void init(){
        mRecommendDatas = new ArrayList<>();
        mGridAdapter = new GridViewAdapter(getContext(), mRecommendDatas, this);
        mGridView.setAdapter(mGridAdapter);

        mSelectedRecommends = new ArrayList<>();
        mRecommedAdapter = new SelectedResultsAdapter(getContext(), mSelectedRecommends, this);
        mRecycleRecommed.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManagerRecommend =
                new LinearLayoutManager(getContext(),
                        LinearLayoutManager.HORIZONTAL, false);
        mRecycleRecommed.setLayoutManager(layoutManagerRecommend);
        mRecycleRecommed.addItemDecoration(
                new DividerItemDecoration(getContext(), LinearLayout.HORIZONTAL));
        mRecycleRecommed.setItemAnimator(new DefaultItemAnimator());
        mRecycleRecommed.setAdapter(mRecommedAdapter);
    }

    private void getSelectedData(){
        mSelectedRecommends.addAll(MessageObserverManager.getInstance().getSelectedRecommendData());
        mRecommedAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClickItem(final Recommend recommend) {
        new AlertDialog.Builder(getContext())
                .setMessage("Are you sure you want to delete " + recommend.getLabel())
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        MessageObserverManager.getInstance().notifyAllObserver(recommend, DELETE_ITEM);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    private void hideSelectedRecommend(){
        mRltSelectedRecommend.setVisibility(View.GONE);
    }

    private void showSelectedRecommend(){
        mRltSelectedRecommend.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_EXSearch:
                if(mSelectedRecommends.size() == 0){
                    showLogAndToast("Please select uri!");
                    return;
                }
                ((RecommendActivity)getActivity()).EXSearch(mSelectedRecommends);
                break;
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
    public void addSearch(Recommend recommend) {
        if(!isContain(recommend)){
            if(mSelectedRecommends.size() < MAXITEM){
                MessageObserverManager.getInstance().notifyAllObserver(recommend, ADD_ITEM);
            }else{
                showLogAndToast("Max item. Can't choice!");
            }
        }else {
            showLogAndToast("Item added. Please choice other item!");
        }
    }

    private boolean isContain(Recommend recommend){
        if(mSelectedRecommends.size() == 0) return false;
        for(Recommend rm : mSelectedRecommends){
            if(rm.getUri().equals(recommend.getUri())){
                return true;
            }
        }
        return false;
    }

    @Override
    public void exSearch(Recommend recommend) {
        List<Recommend> recommends = new ArrayList<>();
        //recommends.addAll(AppController.getInstance().getCurrentUriDecovery());
        recommends.add(recommend);
        ((RecommendActivity)getActivity()).EXSearch(recommends);
    }

    private void showLog(String message){
        if(message != null){
            Log.d(TAG, message);
        }
    }

    private void showLogAndToast(final String message){
        showLog(message);
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}
