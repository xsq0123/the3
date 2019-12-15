package com.a51work6.myaudioplayer.service;

import android.os.Parcel;
import android.os.Parcelable;

public class Song implements Parcelable {

    String title;
    String artist;
    String album;
    int duration;
    int currentPosition;
    int state;

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    public static Creator<Song> getCreator() {
        return CREATOR;
    }

    public Song() {
    }

    private Song(Parcel in) {
        title = in.readString();
        artist = in.readString();
        album = in.readString();
        duration = in.readInt();
        currentPosition = in.readInt();
        state = in.readInt();
    }

    @Override
    public int describeContents() {
        return CONTENTS_FILE_DESCRIPTOR;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(album);
        dest.writeInt(duration);
        dest.writeInt(currentPosition);
        dest.writeInt(state);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

}

