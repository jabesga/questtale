package com.besga.jonander.questtale;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.Serializable;

/**
 * Created by CAMP on 19/05/2016.
 */
public class MapEntity implements Parcelable {

    int entity_id;
    String entity_name;
    String conversation;
    String close_answer;
    MarkerOptions markerOption;


    public static final Parcelable.Creator<MapEntity> CREATOR
            = new Parcelable.Creator<MapEntity>() {
        public MapEntity createFromParcel(Parcel in) {
            return new MapEntity(in);
        }

        public MapEntity[] newArray(int size) {
            return new MapEntity[size];
        }
    };

    public MapEntity(int entity_id, String entity_name, String conversation, String close_answer, MarkerOptions markerOption){
        this.entity_id = entity_id;
        this.entity_name = entity_name;
        this.conversation = conversation;
        this.close_answer = close_answer;

        this.markerOption = markerOption;
    }

    public MapEntity(Parcel in){
        readFromParcel(in);
    }

    public MarkerOptions getMarkerOptions() {
        return markerOption;
    }

    @Override
    public String toString() {
        return markerOption.getTitle();
    }

    public String getEntityName() {
        return entity_name;
    }

    public String getEntityDescription() {
        return conversation;
    }

    public String getEntityCloseAnswer() {
        return close_answer;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(markerOption, flags);
        dest.writeInt(entity_id);
        dest.writeString(entity_name);
        dest.writeString(conversation);
        dest.writeString(close_answer);
    }

    private void readFromParcel(Parcel in) {
        this.markerOption = in.readParcelable(MarkerOptions.class.getClassLoader());
        this.entity_id = in.readInt();
        this.entity_name = in.readString();
        this.conversation = in.readString();
        this.close_answer = in.readString();
    }


}
