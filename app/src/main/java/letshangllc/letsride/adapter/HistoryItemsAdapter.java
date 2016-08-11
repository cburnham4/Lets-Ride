package letshangllc.letsride.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Locale;

import letshangllc.letsride.R;
import letshangllc.letsride.data_objects.PastRunItem;
import letshangllc.letsride.objects.StopWatch;

/**
 * Created by cvburnha on 11/3/2015.
 */
public class HistoryItemsAdapter extends RecyclerView.Adapter<HistoryItemsAdapter.ViewHolder> {
    public ArrayList<PastRunItem> items;

    private Context context;

    /* Listen for item clicks */
    private static RecyclerViewClickListener mListener;


    // Provide a suitable constructor (depends on the kind of dataset)
    public HistoryItemsAdapter(ArrayList<PastRunItem> items, Context context) {
        this.items = items;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public HistoryItemsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
            // create a new view
            View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_run_history, null);

            // create ViewHolder

            ViewHolder viewHolder = new ViewHolder(itemLayoutView);
            return viewHolder;
            }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // - get data from your itemsData at this position
        // - replace the contents of the view with that itemsData
        final PastRunItem item = items.get(position);

        int[] times = StopWatch.milliToHourMinSecs(item.timeInMilli);

        viewHolder.tvMainText.setText(String.format(Locale.getDefault(), "%.2f", item.getDistance()*1000));
        viewHolder.tvSecondaryText.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", times[0],
                times[1],times[2]));
        viewHolder.tvDate.setText(item.date);
        viewHolder.tvAvgSpeed.setText(item.maxSpeed+"");

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return  items.size();
    }

    // inner class to hold a reference to each item of RecyclerView
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView tvMainText;
        public TextView tvSecondaryText;
        public TextView tvDate;
        public TextView tvAvgSpeed;

        public ViewHolder(View view) {
            super(view);
            tvMainText = (TextView) view.findViewById(R.id.tvMainText);
            tvSecondaryText = (TextView) view.findViewById(R.id.tvSecondaryText);
            tvDate = (TextView) view.findViewById(R.id.tvRunDate);
            tvAvgSpeed = (TextView) view.findViewById(R.id.tvAvgSpeed);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mListener.recyclerViewListClicked(view, getLayoutPosition());
        }
    }

}
