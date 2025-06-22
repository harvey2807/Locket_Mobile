package com.hucmuaf.locket_mobile.modedb;

import androidx.annotation.NonNull;

import java.util.List;

public class Image {
    private String imageId;
    private String urlImage;
    private String caption;
    private long timestamp;
    private String senderId;
    private List<String> receiverIds;

    public Image() {
    }

    public Image(String imageId, String urlImage, String caption, long timestamp, String senderId, List<String> receiverIds) {
        this.imageId = imageId;
        this.urlImage = urlImage;
        this.caption = caption;
        this.timestamp = timestamp;
        this.senderId = senderId;
        this.receiverIds = receiverIds;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public List<String> getReceiverIds() {
        return receiverIds;
    }

    public void setReceiverIds(List<String> receiverIds) {
        this.receiverIds = receiverIds;
    }

    @Override
    public String toString() {
        return "Image{" +
                "imageId='" + imageId + '\'' +
                ", urlImage='" + urlImage + '\'' +
                ", caption='" + caption + '\'' +
                ", timestamp=" + timestamp +
                ", senderId='" + senderId + '\'' +
                ", receiverIds=" + receiverIds +
                '}';
    }
}