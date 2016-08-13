package letshangllc.letsride.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import letshangllc.letsride.R;
import letshangllc.letsride.data_objects.PastRunItem;
import letshangllc.letsride.enums.LengthUnits;
import letshangllc.letsride.enums.SpeedUnits;
import letshangllc.letsride.objects.RunStatItem;
import letshangllc.letsride.objects.StopWatch;

/**
 * Created by Carl on 8/13/2016.
 */
public class RunStatsAdapter extends RecyclerView.Adapter<RunStatsAdapter.ViewHolder> {
    public ArrayList<RunStatItem> items;

    private Context context;



    /* Multiplier */
    private LengthUnits lengthUnits;
    private SpeedUnits speedUnits;


    // Provide a suitable constructor (depends on the kind of dataset)
    public RunStatsAdapter(ArrayList<RunStatItem> items, Context context) {
        this.items = items;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RunStatsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_run_stat, null);

        // create ViewHolder

        ViewHolder viewHolder = new ViewHolder(itemLayoutView);
        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // - get data from your itemsData at this position
        // - replace the contents of the view with that itemsData
        final RunStatItem item = items.get(position);

        viewHolder.imgStatIcon.setImageResource(item.iconIndex);
        viewHolder.tvStatLabel.setText(item.label);
        viewHolder.tvStatValue.setText(item.value);
        //viewHolder.tvAvgSpeed.setText(item.maxSpeed+"");

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return  items.size();
    }

    // inner class to hold a reference to each item of RecyclerView
    public static class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView imgStatIcon;
        public TextView tvStatLabel;
        public TextView tvStatValue;

        public ViewHolder(View view) {
            super(view);
            imgStatIcon = (ImageView) view.findViewById(R.id.imgStatIcon);
            tvStatLabel = (TextView) view.findViewById(R.id.tvStatLabel);
            tvStatValue = (TextView) view.findViewById(R.id.tvStatValue);
        }

    }

}