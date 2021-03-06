package com.brine.discovery.message;

import android.util.Log;

import com.brine.discovery.model.Recommend;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by phamhai on 17/02/2017.
 */

public class MessageObserverManager {
    private static MessageObserverManager instance = null;
    public static final int ADD_ITEM = 1;
    public static final int DELETE_ITEM = 2;

    private ArrayList<MessageObserver> observers;
    private List<Recommend> mSelectedRecommend = new ArrayList<>();

    MessageObserverManager() {
        observers = new ArrayList<MessageObserver>();
    }

    public static MessageObserverManager getInstance() {
        if (instance == null){
            instance = new MessageObserverManager();
        }
        return instance;
    }

    public void addItem(MessageObserver observer) {
        Log.d("MessageObserver", "update +1 = " + observers.size() + " --size = " + mSelectedRecommend.size());
        observers.add(observer);
    }

    public void removeItem(MessageObserver observer) {
        Log.d("MessageObserver", "update -1 = " + observers.size() + " --size = " + mSelectedRecommend.size());
        observers.remove(observer);
    }

    public void removeAllData() {
        Log.d("MessageObserver", "update -all = " + observers.size() + " --size = " + mSelectedRecommend.size());
        observers.clear();
        mSelectedRecommend.clear();
    }

    public List<Recommend> getSelectedRecommendData(){
        return mSelectedRecommend;
    }

    public void notifyAllObserver(Recommend recommend, int type) {
        if(type == ADD_ITEM){
            mSelectedRecommend.add(recommend);
        }
        if(type == DELETE_ITEM){
            mSelectedRecommend.remove(recommend);
        }
        for (MessageObserver observer : observers) {
            observer.updateSelectedItem(recommend, type);
        }
    }
}
