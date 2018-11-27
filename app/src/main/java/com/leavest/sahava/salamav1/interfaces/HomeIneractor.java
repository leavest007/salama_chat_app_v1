package com.leavest.sahava.salamav1.interfaces;

import com.leavest.sahava.salamav1.models.Contact;
import com.leavest.sahava.salamav1.models.User;

import java.util.ArrayList;

public interface HomeIneractor {
    User getUserMe();

    ArrayList<Contact> getLocalContacts();
}
