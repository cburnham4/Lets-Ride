package letshangllc.letsride.data_objects;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Carl on 8/9/2016.
 */
public class PastRunItem implements Parcelable{
    public int runId;
    public int dayId;
    public String date;
    public double maxSpeed = 123.123;
    public double durationInMilli;
    public ArrayList<PastLocation> pastLocations  = new ArrayList<>();

    public PastRunItem(int runId, int dayId, String date, double durationInMilli) {
        this.runId = runId;
        this.dayId = dayId;
        this.date = date;
        this.durationInMilli = durationInMilli;
    }

    protected PastRunItem(Parcel in) {
        runId = in.readInt();
        dayId = in.readInt();
        date = in.readString();
        maxSpeed = in.readDouble();
        durationInMilli = in.readDouble();
        //pastLocations = in.createTypedArrayList(PastLocation.CREATOR);
        in.readTypedList(pastLocations, PastLocation.CREATOR);
        //PastLocation[] pastLocations1 = (PastLocation[]) in.readParcelableArray(PastLocation.class.getClassLoader());
        //pastLocations = new ArrayList<PastLocation>((ArrayList<PastLocation>) Arrays.asList(pastLocations1));
    }

    public static final Creator<PastRunItem> CREATOR = new Creator<PastRunItem>() {
        @Override
        public PastRunItem createFromParcel(Parcel in) {
            return new PastRunItem(in);
        }

        @Override
        public PastRunItem[] newArray(int size) {
            return new PastRunItem[size];
        }
    };

    public double getDistance(){
        double km = 0;
        PastLocation prevLocation;
        PastLocation pastLocation;
        for(int i  =1; i <pastLocations.size(); i++){
            prevLocation = pastLocations.get(i-1);
            pastLocation = pastLocations.get(i);
            km+= PastLocation.distance(prevLocation.lat, prevLocation.lon, pastLocation.lat, pastLocation.lon);
        }
        return km * 1000;
    }

    public String getDate(){
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        try{
            Date date= df.parse(this.date);
            DateFormat df2 = new SimpleDateFormat("EEE, MMM dd, yyyy");
            return df2.format(date);
        }catch (ParseException e){
            e.printStackTrace();
        }
        return date;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(runId);
        parcel.writeInt(dayId);
        parcel.writeString(date);
        parcel.writeDouble(durationInMilli);
        parcel.writeDouble(maxSpeed);
        //PastLocation[] pastLocationsArr = pastLocations.toArray(new PastLocation[pastLocations.size()]);
        //parcel.writeArray(pastLocationsArr);
        parcel.writeTypedList(pastLocations);
       // parcel.writeParcelableArray(pastLocationsArr, 0);

    }
}


