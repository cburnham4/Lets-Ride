package letshangllc.letsride.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

import letshangllc.letsride.R;
import letshangllc.letsride.adapter.HistoryItemsAdapter;
import letshangllc.letsride.data_objects.PastRunItem;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getSimpleName();

    /* Recycleview items */
    private ArrayList<PastRunItem> pastRunItems;
    private HistoryItemsAdapter historyItemsAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.setupRecycleView();
    }

    private void setupRecycleView() {
        pastRunItems = new ArrayList<>();
        pastRunItems.add(new PastRunItem(0,0, "Mon, 05/08/16", 4.59, 110.9, 100000) );
        pastRunItems.add(new PastRunItem(0,0, "Mon, 05/08/16", 4.59, 110.9, 100000) );
        pastRunItems.add(new PastRunItem(0,0, "Mon, 05/08/16", 4.59, 110.9, 100000) );
        pastRunItems.add(new PastRunItem(0,0, "Mon, 05/08/16", 4.59, 110.9, 100000) );
        historyItemsAdapter = new HistoryItemsAdapter(pastRunItems, this);

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rvHistoryOfRuns);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);


        recyclerView.setAdapter(historyItemsAdapter);
    }
}
