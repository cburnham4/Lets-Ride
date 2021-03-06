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
    public double durationInMilli;
    public double distance;
    public long startTime;
    public double maxSpeed;
    public double avgSpeed;
    public double maxElevation;
    public double minElevation;

    public ArrayList<PastLocation> pastLocations  = new ArrayList<>();

    public PastRunItem(int runId, int dayId, String date, double durationInMilli, double distance,
                       long startTime, double maxSpeed, double avgSpeed, double maxElevation, double minElevation) {
        this.runId = runId;
        this.dayId = dayId;
        this.date = date;
        this.durationInMilli = durationInMilli;
        this.distance = distance;
        this.startTime = startTime;
        this.maxSpeed = maxSpeed;
        this.avgSpeed = avgSpeed;
        this.maxElevation = maxElevation;
        this.minElevation = minElevation;
    }

    protected PastRunItem(Parcel in) {
        runId = in.readInt();
        dayId = in.readInt();
        date = in.readString();
        durationInMilli = in.readDouble();
        distance = in.readDouble();
        startTime = in.readLong();
        maxSpeed = in.readDouble();
        avgSpeed = in.readDouble();
        maxElevation = in.readDouble();
        minElevation = in.readDouble();
        pastLocations = in.createTypedArrayList(PastLocation.CREATOR);
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
        parcel.writeDouble(distance);
        parcel.writeLong(startTime);
        parcel.writeDouble(maxSpeed);
        parcel.writeDouble(avgSpeed);
        parcel.writeDouble(maxElevation);
        parcel.writeDouble(minElevation);

        parcel.writeTypedList(pastLocations);


    }


}


