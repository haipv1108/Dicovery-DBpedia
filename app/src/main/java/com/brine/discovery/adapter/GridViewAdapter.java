package com.brine.discovery.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.brine.discovery.R;
import com.brine.discovery.model.Recommend;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by phamhai on 09/02/2017.
 */

public class GridViewAdapter extends BaseAdapter {

    private Context mContext;
    private List<Recommend> mListData;
    private LayoutInflater mLayoutInflater;
    private GridAdapterCallback mCallback;

    public GridViewAdapter(Context mContext, List<Recommend> mListData, GridAdapterCallback mCallback){
        this.mContext = mContext;
        this.mListData = mListData;
        this.mCallback = mCallback;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    public interface GridAdapterCallback{
        void showDetails(Recommend recommend);
        void exSearch(Recommend recommend);
    }

    @Override
    public int getCount() {
        return mListData.size();
    }

    @Override
    public Object getItem(int i) {
        return mListData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final GridViewAdapter.ViewHolder holder;
        if(view == null){
            view = mLayoutInflater.inflate(
                    R.layout.recommend_item, null);
            holder = new GridViewAdapter.ViewHolder();
            holder.tvUri = (TextView) view.findViewById(R.id.tv_uri);
            holder.tvLabel = (TextView) view.findViewById(R.id.tv_label);
            holder.image = (ImageView) view.findViewById(R.id.img_thumb);
            holder.tvOption = (TextView) view.findViewById(R.id.tv_option);
            holder.progressLoading = (ProgressBar) view.findViewById(R.id.progress_loading);
            view.setTag(holder);
        }else {
            holder = (GridViewAdapter.ViewHolder) view.getTag();
        }

        final Recommend recommend = mListData.get(i);
        holder.tvUri.setText(recommend.getUri());
        holder.tvLabel.setText(recommend.getLabel());
        holder.progressLoading.setVisibility(View.VISIBLE);
        Picasso.with(mContext)
                .load(recommend.getImage())
                .error(R.drawable.no_image_loading)
                .into(holder.image, new Callback() {
                    @Override
                    public void onSuccess() {
                        holder.progressLoading.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        holder.progressLoading.setVisibility(View.GONE);
                    }
                });
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOptionMenu(holder.tvOption, recommend);
            }
        });
        return view;
    }

    private void showOptionMenu(View view, final Recommend recommend){
        PopupMenu popupMenu = new PopupMenu(mContext, view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_recommend_item, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.menu_details:
                        mCallback.showDetails(recommend);
                        return true;
                    case R.id.menu_ex_search:
                        mCallback.exSearch(recommend);
                        return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    static class ViewHolder{
        TextView tvUri;
        TextView tvLabel;
        ImageView image;
        ProgressBar progressLoading;
        TextView tvOption;
    }
}
