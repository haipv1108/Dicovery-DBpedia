package com.brine.discovery.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.brine.discovery.R;
import com.brine.discovery.model.SCMusic;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;


/**
 * Created by phamhai on 13/02/2017.
 */

public class SoundCloudAdapter extends RecyclerView.Adapter<SoundCloudAdapter.ViewHolder> {

    private Context mContext;
    private List<SCMusic> mListData;
    private SCAdapterCallback mCallback;

    public interface SCAdapterCallback {
        void playSoundCloudMusic(String streamUrl, String songName);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.soundcloud_music_item, parent, false);
        return new SoundCloudAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        SCMusic scMusic = mListData.get(position);
        holder.tvMusicId.setText(String.valueOf(scMusic.getMusicId()));
        holder.tvTitle.setText(scMusic.getTitle());
        holder.tvStreamUrl.setText(scMusic.getStreamUrl());
        holder.progressLoading.setVisibility(View.VISIBLE);
        Picasso.with(mContext)
                .load(scMusic.getArtworkUrl())
                .error(R.drawable.no_image_loading)
                .into(holder.imgThumbnail, new Callback() {
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

    public SoundCloudAdapter(Context context, List<SCMusic> listData, SCAdapterCallback callback){
        this.mContext = context;
        this.mListData = listData;
        this.mCallback = callback;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView tvMusicId;
        TextView tvTitle;
        TextView tvStreamUrl;
        ImageView imgThumbnail;
        ProgressBar progressLoading;

        ViewHolder(View view){
            super(view);
            tvMusicId = (TextView) view.findViewById(R.id.tv_music_id);
            tvTitle = (TextView) view.findViewById(R.id.tv_title);
            tvStreamUrl = (TextView) view.findViewById(R.id.tv_stream_url);
            imgThumbnail = (ImageView) view.findViewById(R.id.img_thumbnail);
            progressLoading = (ProgressBar) view.findViewById(R.id.progress_loading);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            mCallback.playSoundCloudMusic(mListData.get(position).getStreamUrl(), mListData.get(position).getTitle());
        }
    }
}
