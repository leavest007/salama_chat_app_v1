package com.leavest.sahava.salamav1.interfaces;

import com.leavest.sahava.salamav1.models.Message;

public interface OnMessageItemClick {
    void OnMessageClick(Message message, int position);

    void OnMessageLongClick(Message message, int position);
}
