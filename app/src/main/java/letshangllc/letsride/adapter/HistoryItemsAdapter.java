package letshangllc.letsride.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

import letshangllc.letsride.R;

/**
 * Created by cvburnha on 11/3/2015.
 */
public class HistoryItemsAdapter extends RecyclerView.Adapter<HistoryItemsAdapter.ViewHolder> {
    public ArrayList<PastRunItem> items;
    private ArrayList<PastRunItem> completedPastRunItems;
    private Context context;



    // Provide a suitable constructor (depends on the kind of dataset)
    public HistoryItemsAdapter(ArrayList<PastRunItem> items, Context context, ArrayList<PastRunItem> completedPastRunItems) {
        this.items = items;
        this.context = context;
        this.completedPastRunItems = completedPastRunItems;
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



        viewHolder.bx_complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;

                PastRunItem item = (PastRunItem) cb.getTag(R.string.TAG_ITEM);
                TextView tv = (TextView) cb.getTag(R.string.TAG_TEXTVIEW);
                item.setIsSelected(cb.isChecked());
                if (item.isSelected()) {
                    completedPastRunItems.add(item);
                    tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    completedPastRunItems.remove(item);
                    tv.setPaintFlags(0);
                }
            }
        });

        viewHolder.tv_item.setText(item.getPastRunItem());
        if(item.isSelected()){
            viewHolder.tv_item.setPaintFlags(viewHolder.tv_item.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }else{
            viewHolder.tv_item.setPaintFlags(0);
        }

        viewHolder.bx_complete.setChecked(item.isSelected());
        viewHolder.bx_complete.setTag(R.string.TAG_ITEM, item);
        viewHolder.bx_complete.setTag(R.string.TAG_TEXTVIEW, viewHolder.tv_item);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getPastRunItemCount() {
        return  items.size();
    }

    // inner class to hold a reference to each item of RecyclerView
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_item;
        public CheckBox bx_complete;

        public ViewHolder(View view) {
            super(view);
            tv_item = (TextView) view.findViewById(R.id.tv_task);
            bx_complete = (CheckBox) view.findViewById(R.id.bx_complete_task);
        }
    }

}
