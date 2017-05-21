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
import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by phamhai on 09/02/2017.
 */

public class RecommendAdapter extends BaseAdapter {

    private Context mContext;
    private List<Recommend> mListData;
    private LayoutInflater mLayoutInflater;

    private RecommendAdapterCallback mCallback;

    public RecommendAdapter(Context mContext, List<Recommend> mListData, RecommendAdapterCallback mCallback){
        this.mContext = mContext;
        this.mListData = mListData;
        mLayoutInflater = LayoutInflater.from(mContext);
        this.mCallback = mCallback;
    }

    public interface RecommendAdapterCallback {
        void showDetails(Recommend recommend);
        void addSearch(Recommend recommend);
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
    public View getView(final int i, View view, ViewGroup viewGroup) {
        final RecommendAdapter.ViewHolder holder;
        if(view == null){
            view = mLayoutInflater.inflate(
                    R.layout.recommend_item_row, null);
            holder = new RecommendAdapter.ViewHolder();
            holder.tvUri = (TextView) view.findViewById(R.id.tv_uri);
            holder.tvLabel = (TextView) view.findViewById(R.id.tv_label);
            holder.tvDescription = (ExpandableTextView) view.findViewById(R.id.tv_description);
            holder.image = (ImageView) view.findViewById(R.id.img_thumb);
            holder.tvOption = (TextView) view.findViewById(R.id.tv_option);
            holder.progressLoading = (ProgressBar) view.findViewById(R.id.progress_loading);
            view.setTag(holder);
        }else {
            holder = (RecommendAdapter.ViewHolder) view.getTag();
        }

        Recommend recommend = mListData.get(i);
        holder.tvUri.setText(recommend.getUri());
        holder.tvLabel.setText(recommend.getLabel());
        holder.tvDescription.setText(recommend.getDescription());
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
                showOptionMenu(view, mListData.get(i));
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
                    case R.id.menu_add_search:
                        mCallback.addSearch(recommend);
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

    class ViewHolder{
        TextView tvUri;
        TextView tvLabel;
        ExpandableTextView tvDescription;
        ImageView image;
        ProgressBar progressLoading;
        TextView tvOption;
    }
}
