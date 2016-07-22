package com.jme3.anim.blending;

import java.util.*;

/**
 * Created by Nehon on 22/07/2016.
 */
public class BlendingDataPool  {
    private List<BlendingData> list = new ArrayList<>(); 
    private int internalIndex = 0;

    public BlendingData getNext() {
        BlendingData item;
        if (list.size() == internalIndex) {
            item = new BlendingData();
            list.add(item);
        } else {
            item = list.get(internalIndex);
        }
        internalIndex++;
        return item;
    }

    public BlendingData get(int index){
        return list.get(index);
    }

    public void clear(){
        internalIndex = 0;
    }

    public int size(){
        return internalIndex;
    }

}
