package za.co.myconcepts.instaclone.ViewModel;

import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;

import java.util.List;

import za.co.myconcepts.instaclone.model.NotificationModel;

/**
 * Created by Bakr on 2017-09-10.
 */

public class NotificationsViewModel extends ViewModel {
    @Nullable
    private List<NotificationModel> notificationModelList;

    @Nullable
    public List<NotificationModel> getNotificationModelList() {
        return notificationModelList;
    }

    public void setNotificationModelList(@Nullable List<NotificationModel> notificationModelList) {
        this.notificationModelList = notificationModelList;
    }
}
