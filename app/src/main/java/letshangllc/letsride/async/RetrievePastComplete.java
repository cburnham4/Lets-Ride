package letshangllc.letsride.async;

import java.util.ArrayList;

import letshangllc.letsride.data_objects.PastRunItem;

/**
 * Created by Carl on 8/18/2016.
 */
public interface RetrievePastComplete {
    void onRetrievalComplete(ArrayList<PastRunItem> pastRunItems);
}
