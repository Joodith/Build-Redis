package com.connections.handlers.timeoutHandler;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HeapTTL {
    private List<LastActivityTimeMap> array;
    private int size;
    private int FRONT = 1;


    public HeapTTL() {
        array = new ArrayList<>();
        size = 0;
        array.add(new LastActivityTimeMap(null,(long)Integer.MIN_VALUE));

    }

    private int getParentIndex(int pos) {
        return pos / 2;
    }

    private int getLeftIndex(int pos) {
        return 2 * pos;
    }

    private int getRightIndex(int pos) {
        return (2 * pos) + 1;
    }

    private void swap(int pos1, int pos2) {
        LastActivityTimeMap temp = array.get(pos1);
        array.set(pos1, array.get(2));
        array.set(pos2, temp);

    }

    private boolean isLeaf(int pos) {
        return pos > size / 2;
    }

    public void minHeap(int pos) {
        if (!isLeaf(pos)) {
            int minpos = -1;
            int leftInd=getLeftIndex(pos);
            int rightInd=getRightIndex(pos);
            int parentInd=getParentIndex(pos);
            if (array.get(leftInd).getActivityTime() < array.get(rightInd).getActivityTime()) {
                minpos = leftInd;
            } else {
                minpos = rightInd;
            }
            if (array.get(minpos).getActivityTime() < array.get(parentInd).getActivityTime()) {
                swap(minpos, parentInd);
                minHeap(minpos);
            }
        }
    }

    public void insert(LastActivityTimeMap elem) {
        size += 1;
        int currentpos = size;
        array.add(elem);
        while (array.get(currentpos).getActivityTime() < array.get(getParentIndex(currentpos)).getActivityTime()) {
            swap(currentpos, getParentIndex(currentpos));
            currentpos = getParentIndex(currentpos);
        }
    }

    public LastActivityTimeMap popMin() {
        LastActivityTimeMap popped = array.get(FRONT);
        array.set(FRONT, array.get(size));
        array.remove(size);
        size -= 1;
        return popped;


    }

    public LastActivityTimeMap getMin() {
        if (size > 0) {
            return array.get(FRONT);
        }
        return null;
    }


}
