package com.leavest.sahava.salamav1.interfaces;

import android.view.View;

import com.leavest.sahava.salamav1.models.Group;
import com.leavest.sahava.salamav1.models.User;

public interface OnUserGroupItemClick {
    void OnUserClick(User user, int position, View userImage);
    void OnGroupClick(Group group, int position, View userImage);
}
