package com.brine.discovery.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.brine.discovery.R;
import com.brine.discovery.model.KeywordSearch;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by phamhai on 08/02/2017.
 */

public class KSAdapter extends BaseAdapter {
    private static final String TAG = KSAdapter.class.getCanonicalName();

    private List<KeywordSearch> mListData;
    private Context mContext;
    private LayoutInflater mLayoutInflater;

    public KSAdapter(Context context, List<KeywordSearch> listData){
        this.mContext = context;
        this.mListData = listData;
        mLayoutInflater = LayoutInflater.from(mContext);
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
        ViewHolder holder;
        if(view == null){
            view = mLayoutInflater.inflate(
                    R.layout.ks_item, null);
            holder = new ViewHolder();
            holder.tvUri = (TextView) view.findViewById(R.id.tv_uri);
            holder.tvLabel = (TextView) view.findViewById(R.id.tv_label);
            holder.tvDescription = (TextView) view.findViewById(R.id.tv_description);
            holder.tvType = (TextView) view.findViewById(R.id.tv_type);
            holder.imgThumb = (ImageView) view.findViewById(R.id.img_thumb);
            view.setTag(holder);
        }else {
            holder = (ViewHolder) view.getTag();
        }

        KeywordSearch keywordSearch = mListData.get(i);
        holder.tvUri.setText(keywordSearch.getUri());
        holder.tvLabel.setText(keywordSearch.getLabel());
        holder.tvDescription.setText(keywordSearch.getDescription());
        holder.tvType.setText(keywordSearch.getType());
//        if(keywordSearch.getThumb() != null){
//            Picasso.with(mContext).load(keywordSearch.getThumb()).into(holder.imgThumb);
//        }
        return view;
    }

    static class ViewHolder {
        TextView tvUri;
        TextView tvLabel;
        TextView tvDescription;
        TextView tvType;
        ImageView imgThumb;
    }
}
