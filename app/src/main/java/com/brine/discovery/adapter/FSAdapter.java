package com.brine.discovery.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.brine.discovery.R;
import com.brine.discovery.model.FSResult;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by phamhai on 13/02/2017.
 */

public class FSAdapter extends RecyclerView.Adapter<FSAdapter.ViewHolder> {

    private static final String TAG = FSAdapter.class.getCanonicalName();
    private Context mContext;
    private List<FSResult> searchResults;
    private FSAdapterCallback mCallback;

    public interface FSAdapterCallback{
        void onClickFS(FSResult fsResult);
    }

    public FSAdapter(Context context, List<FSResult> searchResults, FSAdapterCallback callback){
        this.mContext = context;
        this.searchResults = searchResults;
        this.mCallback = callback;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fs_item_row, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        FSResult result = searchResults.get(position);
        holder.tvUri.setText(result.getUri());
        holder.tvLabel.setText(result.getLabel());
        holder.tvDescription.setText(Html.fromHtml(result.getDescription()));
        holder.progressLoading.setVisibility(View.VISIBLE);
        Picasso.with(mContext)
                .load(result.getThumbnail())
                .error(R.drawable.no_image_loading)
                .into(holder.imgThumb, new Callback() {
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
        return searchResults.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView tvUri;
        TextView tvLabel;
        TextView tvDescription;
        ImageView imgThumb;
        ProgressBar progressLoading;

         ViewHolder(View view){
             super(view);
             tvUri = (TextView)view.findViewById(R.id.tv_uri);
             tvLabel = (TextView)view.findViewById(R.id.tv_label);
             tvDescription = (TextView)view.findViewById(R.id.tv_description);
             imgThumb = (ImageView) view.findViewById(R.id.img_thumb);
             progressLoading = (ProgressBar) view.findViewById(R.id.progress_loading);
             view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            mCallback.onClickFS(searchResults.get(position));
        }
    }
}