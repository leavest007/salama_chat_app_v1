package com.leavest.sahava.salamav1.utils;

import android.os.Parcel;
import android.text.style.URLSpan;
import android.view.View;

public class customTabsURLSpan extends URLSpan {

    public CustomTabsURLSpan(String url) {
        super(url);
    }

    public CustomTabsURLSpan(Parcel src) {
        super(src);
    }

    @Override
    public void onClick(View widget) {
        String url = getURL();
        Helper.loadUrl(widget.getContext(), url);

    }
}
