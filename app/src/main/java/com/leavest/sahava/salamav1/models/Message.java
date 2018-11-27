package com.leavest.sahava.salamav1.models;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmModel;
import io.realm.annotations.Ignore;

public class Message implements Parcelable, RealmModel {

    private String body, senderName, senderId, recipientId, id;
    private long date;
    private boolean delivered = false, sent = false;
    private
    @AttachmentTypes.AttachmentType
    int attachmentType;
    private Attachment attachment;

    @Ignore
    private boolean selected;

    public Message() {

    }

    public Message(int attachmentType) {
        this.attachmentType = attachmentType;
        this.senderId = "";
    }

    protected Message(Parcel in) {
        body = in.readString();
        senderName = in.readString();
        senderId = in.readString();
        recipientId = in.readString();
        id = in.readString();
        date = in.readLong();
        delivered = in.readByte() != 0;
        sent = in.readByte() != 0;
        attachmentType = in.readInt();
        attachment = in.readParcelable(Attachment.class.getClassLoader());
        selected = in.readByte() != 0;
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;

        Message message = (Message) o;
        return getId() != null ? getId().equals(message.getId()) : message.getId() == null;

    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public void setAttachmentType(int attachmentType) {
        this.attachmentType = attachmentType;
    }

    @AttachmentTypes.AttachmentType
    public int getAttachmentType() {
        return attachmentType;
    }

    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public void setBody(String body) {
        this.body= body;
    }

    public String getBody() {
        return body;
    }


    public void setDate(long date) {
        this.date = date;
    }

    public long getDate() {
        return date;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public boolean isSent() {
        return sent;
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }

    public boolean isDelivered() {
        return  delivered;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(body);
        parcel.writeString(senderName);
        parcel.writeString(senderId);
        parcel.writeString(recipientId);
        parcel.writeString(id);
        parcel.writeLong(date);
        parcel.writeByte((byte) (delivered ? 1 : 0));
        parcel.writeByte((byte) (sent ? 1 : 0));
        parcel.writeInt(attachmentType);
        parcel.writeParcelable(attachment, i);
        parcel.writeByte((byte) (selected ? 1 : 0));
    }

}
