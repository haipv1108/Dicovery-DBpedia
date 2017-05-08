package com.brine.discovery.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

public class RecommendAdapter extends RecyclerView.Adapter<RecommendAdapter.ViewHolder> {

    private Context mContext;
    private List<Recommend> mListData;
    private GridAdapterCallback mCallback;

    public RecommendAdapter(Context mContext, List<Recommend> mListData, GridAdapterCallback mCallback){
        this.mContext = mContext;
        this.mListData = mListData;
        this.mCallback = mCallback;
    }

    public interface GridAdapterCallback{
        void showDetails(Recommend recommend);
        void addSearch(Recommend recommend);
        void exSearch(Recommend recommend);
    }

    @Override
    public RecommendAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recommend_item_row, parent, false);
        return new RecommendAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final RecommendAdapter.ViewHolder holder, int position) {
        final Recommend recommend = mListData.get(position);
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
    }

    @Override
    public int getItemCount() {
        return mListData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView tvUri;
        TextView tvLabel;
        ExpandableTextView tvDescription;
        ImageView image;
        ProgressBar progressLoading;
        TextView tvOption;

        ViewHolder(View view){
            super(view);
            tvUri = (TextView) view.findViewById(R.id.tv_uri);
            tvLabel = (TextView) view.findViewById(R.id.tv_label);
            tvDescription = (ExpandableTextView) view.findViewById(R.id.tv_description);
            image = (ImageView) view.findViewById(R.id.img_thumb);
            tvOption = (TextView) view.findViewById(R.id.tv_option);
            progressLoading = (ProgressBar) view.findViewById(R.id.progress_loading);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            showOptionMenu(view, mListData.get(position));
        }
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
}
