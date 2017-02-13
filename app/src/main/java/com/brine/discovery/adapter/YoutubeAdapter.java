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
import com.brine.discovery.model.YoutubeVideo;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by phamhai on 13/02/2017.
 */

public class YoutubeAdapter extends RecyclerView.Adapter<YoutubeAdapter.ViewHolder>{

    private Context mContext;
    private List<YoutubeVideo> mListData;
    private YoutubeAdapterCallBack mCallBack;

    public interface YoutubeAdapterCallBack{
        void playVideoYoutube(String videoId);
    }

    public YoutubeAdapter(Context context, List<YoutubeVideo> listData,
                          YoutubeAdapterCallBack callBack){
        this.mContext = context;
        this.mListData = listData;
        this.mCallBack = callBack;
    }

    @Override
    public YoutubeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.youtube_video_item, parent, false);
        return new YoutubeAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final YoutubeAdapter.ViewHolder holder, int position) {
        YoutubeVideo youtubeVideo = mListData.get(position);
        holder.tvVideoId.setText(youtubeVideo.getVideoId());
        holder.tvTitle.setText(youtubeVideo.getTitle());
        holder.progressLoading.setVisibility(View.VISIBLE);
        Picasso.with(mContext)
                .load(youtubeVideo.getThumbnail())
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

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView tvVideoId;
        TextView tvTitle;
        ImageView imgThumbnail;
        ProgressBar progressLoading;

        ViewHolder(View view){
            super(view);
            tvVideoId = (TextView) view.findViewById(R.id.tv_video_id);
            tvTitle = (TextView) view.findViewById(R.id.tv_title);
            imgThumbnail = (ImageView) view.findViewById(R.id.img_thumbnail);
            progressLoading = (ProgressBar) view.findViewById(R.id.progress_loading);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                YoutubeVideo youtubeVideo = mListData.get(position);
                mCallBack.playVideoYoutube(youtubeVideo.getVideoId());
            }
        }
    }
}
