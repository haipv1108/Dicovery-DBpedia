package com.brine.discovery.message;

import com.brine.discovery.model.Recommend;

/**
 * Created by phamhai on 17/02/2017.
 */

public interface MessageObserver {
    void updateSelectedItem(Recommend recommend);
}
