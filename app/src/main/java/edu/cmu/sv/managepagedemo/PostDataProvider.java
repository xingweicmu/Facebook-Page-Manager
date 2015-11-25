package edu.cmu.sv.managepagedemo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by xingwei on 11/25/15.
 */
public class PostDataProvider implements Parcelable {
    private String message;
    private String createTime;
    private String id;

    public PostDataProvider(String message, String createTime, String id) {
        this.message = message;
        this.createTime = createTime;
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(message);
        dest.writeString(createTime);
        dest.writeString(id);
    }

    public static Creator<PostDataProvider> CREATOR = new Creator<PostDataProvider>() {
        public PostDataProvider createFromParcel(Parcel parcel) {
            return new PostDataProvider(parcel);
        }

        public PostDataProvider[] newArray(int size) {
            return new PostDataProvider[size];
        }
    };

    private PostDataProvider(Parcel in) {
        message = in.readString();
        createTime = in.readString();
        id = in.readString();
    }
}
