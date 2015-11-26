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
    private String isPublished;
    private int views;

    public PostDataProvider(String message, String createTime, String id, String isPublished, int views) {
        this.message = message;
        this.createTime = createTime;
        this.id = id;
        this.isPublished = isPublished;
        this.views = views;
    }

    public String getMessage() {
        return message;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
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

    public String isPublished() {
        return isPublished;
    }

    public void setIsPublished(String isPublished) {
        this.isPublished = isPublished;
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
        dest.writeString(isPublished);
        dest.writeInt(views);
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
        isPublished = in.readString();
        views = in.readInt();
    }

    public String toString() {
        return "Message: "+this.message + "\nViews: " + this.views + "\nPublished: " + this.isPublished + "\nCreated Time: " + this.createTime;
    }
}
