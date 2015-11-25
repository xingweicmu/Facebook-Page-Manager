package edu.cmu.sv.managepagedemo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by xingwei on 11/25/15.
 */
public class PostDataProvider implements Parcelable {
    private String content;
    private int views;

    public PostDataProvider(String content, int views) {
        this.content = content;
        this.views = views;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(content);
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
        content = in.readString();
        views = in.readInt();
    }
}
