package com.fengqi.motions;

import android.os.Environment;

import java.io.File;
import java.util.List;

/**
 * Created by fengqi on 15-07-14.
 */
public class MotionStore {

    public interface MotionStoreObserver {
        public void updated();
    }

    private List<Motion> motions;
    private int mIndex = 0;

    public Motion next() {
        mIndex++;
        if (mIndex > motions.size()-1)
            mIndex = 0;
        return motions.get(mIndex);
    }

    public Motion prev() {
        mIndex--;
        if (mIndex < 0)
            mIndex = motions.size()-1;
        return motions.get(mIndex);
    }

    public Motion get(int index) {
        Motion m = (index<0 || index>motions.size()-1)? null: motions.get(index);
        return m;
    }

    public void update(final MotionStoreObserver observer) {
        motions = new ArrrayList<Motion>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                File storage = Environment.getExternalStorageDirectory();
                walkThrough(storage);
                observer.updated();
            }
        }).start();
    }

    private void walkThrough(File root) {
        File files[] = root.listFiles();
        if (files != null)
            for (File f : files) {
                if (f.isDirectory()) {
                    walkThrough(f);
                } else if(f.getName().endsWith(".gif")){
                    motions.add(Motion.from(f.getAbsolutePath()));
                }
            }
    }
}
