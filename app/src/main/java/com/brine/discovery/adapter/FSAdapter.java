package com.brine.discovery.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.brine.discovery.R;
import com.brine.discovery.model.FSResult;

import java.util.List;

/**
 * Created by phamhai on 13/02/2017.
 */

public class FSAdapter extends RecyclerView.Adapter<FSAdapter.ViewHolder> {

    private static final String TAG = FSAdapter.class.getCanonicalName();
    private Context mContext;
    private List<FSResult> searchResults;

    public FSAdapter(Context context, List<FSResult> searchResults){
        this.mContext = context;
        this.searchResults = searchResults;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fs_item_row, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FSResult result = searchResults.get(position);
        holder.tvUri.setText(result.getUri());
        holder.tvLabel.setText(result.getLabel());
        holder.tvDescription.setText(Html.fromHtml(result.getDescription()));
        holder.tvScore.setText(String.valueOf(result.getScore()));
        holder.tvRank.setText(String.valueOf(result.getRank()));
        holder.tvOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO:
            }
        });
    }

    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvUri;
        TextView tvLabel;
        TextView tvDescription;
        TextView tvScore;
        TextView tvRank;
        TextView tvOptions;

         ViewHolder(View view){
            super(view);
            tvUri = (TextView)view.findViewById(R.id.tv_uri);
            tvLabel = (TextView)view.findViewById(R.id.tv_label);
            tvDescription = (TextView)view.findViewById(R.id.tv_description);
            tvScore = (TextView)view.findViewById(R.id.tv_score);
            tvRank = (TextView)view.findViewById(R.id.tv_rank);
            tvOptions = (TextView)view.findViewById(R.id.tv_options);
        }
    }
}