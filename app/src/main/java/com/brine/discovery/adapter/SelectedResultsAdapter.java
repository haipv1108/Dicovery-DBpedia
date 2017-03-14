package com.brine.discovery.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.brine.discovery.R;
import com.brine.discovery.model.Recommend;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by phamhai on 10/02/2017.
 */

public class SelectedResultsAdapter extends
        RecyclerView.Adapter<SelectedResultsAdapter.ViewHolder> {
    private static final String TAG = SelectedResultsAdapter.class.getCanonicalName();
    private Context mContext;
    private List<Recommend> mListData;
    private SelectedAdapterCallback mCallback;

    public interface SelectedAdapterCallback{
        void onClickItem(Recommend recommend);
    }
    public SelectedResultsAdapter(Context context, List<Recommend> listData,
                                  SelectedAdapterCallback callback){
        this.mContext = context;
        this.mListData = listData;
        this.mCallback = callback;
    }

    @Override
    public SelectedResultsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.selected_recommend_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final SelectedResultsAdapter.ViewHolder holder, int position) {
        Recommend recommend = mListData.get(position);
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
    }

    @Override
    public int getItemCount() {
        return mListData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView tvUri;
        TextView tvLabel;
        ImageView image;
        ProgressBar progressLoading;

        ViewHolder(View view){
            super(view);
            tvUri = (TextView) view.findViewById(R.id.tv_uri);
            tvLabel = (TextView) view.findViewById(R.id.tv_label);
            image = (ImageView) view.findViewById(R.id.img_thumb);
            progressLoading = (ProgressBar) view.findViewById(R.id.progress_loading);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            mCallback.onClickItem(mListData.get(position));
        }
    }
}
