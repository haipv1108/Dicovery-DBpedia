package com.brine.discovery.search;

import com.brine.discovery.activity.MainActivity;
import com.brine.discovery.adapter.FSAdapter;

/**
 * Created by phamhai on 29/03/2017.
 */

public class AutoChangeSearch {
    private static final String TAG = AutoChangeSearch.class.getCanonicalName();

    private MainActivity mActivity;
    private String mKeyword;

    public AutoChangeSearch(MainActivity activity, String keyword){
        this.mActivity = activity;
        this.mKeyword = keyword;

        search();
    }

    private void search(){
        if(mKeyword.split(" ").length <= 3){
            lookupUri();
        }else{
            FSAdvanced();
        }
    }

    private void lookupUri(){
        new LookupUri(mActivity, mKeyword, lookupCallback);
    }

    LookupUri.Callback lookupCallback = new LookupUri.Callback() {
        @Override
        public void showMessageKSNoResult(String message) {

        }

        @Override
        public void completedKS() {
            if(mActivity.mKeywordSearchs.size() == 0){
                FSAdvanced();
            }
        }
    };

    private void FSAdvanced(){

    }

}
