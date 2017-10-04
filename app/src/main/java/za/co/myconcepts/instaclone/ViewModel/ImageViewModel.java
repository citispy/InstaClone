package za.co.myconcepts.instaclone.ViewModel;

import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;

import java.util.List;

import za.co.myconcepts.instaclone.model.Image;

public class ImageViewModel extends ViewModel {

    @Nullable
private List<Image> imageList;

    @Nullable
    public List<Image> getImageList() {
        return imageList;
    }

    public void setImageList(List<Image> imageList) {
        this.imageList = imageList;
    }
}
