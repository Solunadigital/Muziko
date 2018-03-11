package com.muziko.models;

/**
 * Created by Bradley on 9/03/2017.
 */

public class MainMenuModel {

    public int title;
    public int image;

    public MainMenuModel(int title, int image) {
        this.title = title;
        this.image = image;
    }

    public int getTitle() {
        return title;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }
}
