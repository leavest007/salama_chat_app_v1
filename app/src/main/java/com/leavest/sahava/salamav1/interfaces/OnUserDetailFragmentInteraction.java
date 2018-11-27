package com.leavest.sahava.salamav1.interfaces;

import com.leavest.sahava.salamav1.models.Message;

import java.lang.reflect.Array;
import java.util.ArrayList;

public interface OnUserDetailFragmentInteraction {
    void getAttachments();

    ArrayList<Message> getAttachments(int tabPos);

    void switchToMediaFragment();
}
