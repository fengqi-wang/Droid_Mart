package com.fengqi.motions;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {
    private RecyclerView mRecyclerView;
    private ProgressDialog mProgressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.motions);

        mRecyclerView = (RecyclerView) findViewById(R.id.thumbs);
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,
                StaggeredGridLayoutManager.VERTICAL));

        mProgressDialog = ProgressDialog.show(MainActivity.this, "Please wait ...", "Scanning Giffies ...", true);
        new LoadTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class LoadTask extends AsyncTask<Void, Void, Void> {
        private List<Motion> motions =  new ArrayList<>();

        @Override
        protected Void doInBackground(Void... v) {
            File storage = Environment.getExternalStorageDirectory();
            walkThrough(storage);
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            MotionsAdapter rcAdapter = new MotionsAdapter(MainActivity.this, motions);
            mRecyclerView.setAdapter(rcAdapter);
            mProgressDialog.dismiss();
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}

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
}
