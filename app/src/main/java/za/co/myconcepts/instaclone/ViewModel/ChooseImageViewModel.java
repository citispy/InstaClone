package za.co.myconcepts.instaclone.ViewModel;


import android.arch.lifecycle.ViewModel;
import android.net.Uri;
import android.support.annotation.Nullable;

public class ChooseImageViewModel extends ViewModel {

    @Nullable
    private Uri selectedImage;

    @Nullable
    public Uri getSelectedImage() {
        return selectedImage;
    }

    public void setSelectedImage(@Nullable Uri selectedImage) {
        this.selectedImage = selectedImage;
    }
}
