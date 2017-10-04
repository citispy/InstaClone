package za.co.myconcepts.instaclone.ViewModel;

import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;

import java.util.List;

import za.co.myconcepts.instaclone.model.User;

public class BrowseUsersViewModel extends ViewModel{

    @Nullable
    private List<User> userlist;

    @Nullable
    public List<User> getUserlist() {
        return userlist;
    }

    public void setUserlist(@Nullable List<User> userlist) {
        this.userlist = userlist;
    }
}
