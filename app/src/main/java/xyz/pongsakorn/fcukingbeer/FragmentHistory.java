/**
 * Created by Pongsakorn on 11/24/2015.
 */
package xyz.pongsakorn.fcukingbeer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FragmentHistory extends Fragment {

    private HistoryAdapter historyAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history, container, false);

        historyAdapter = new HistoryAdapter(getContext(), loadItems());

        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.recycler_view);
        recyclerView.addItemDecoration(new MarginDecoration(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        mSwipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipeRefreshLayout);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new LoadFileList().execute();
            }
        });


        recyclerView.setAdapter(historyAdapter);
        return root;
    }

    private List<String> loadItems() {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/FcukBeer";

        File f = new File(path);
        if (!f.exists())
            f.mkdirs();

        File[] files = f.listFiles();

        List<String> filename = new ArrayList<>();

        for (File inFile : files) {
            if (inFile.isFile()) {
                if (inFile.getName().contains("out"))
                    filename.add(path+"/"+inFile.getName());
            }
        }

        Collections.reverse(filename);
        return filename;
    }

    private class LoadFileList extends AsyncTask<Void, Void, List<String>> {
        protected void onPostExecute(List<String> result) {
            historyAdapter.update(result);
            mSwipeRefreshLayout.setRefreshing(false);
        }

        @Override
        protected List<String> doInBackground(Void... params) {
            return loadItems();
        }
    }

}
