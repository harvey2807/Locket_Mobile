package com.hucmuaf.locket_mobile.service;

import com.hucmuaf.locket_mobile.ModelDB.Image;

import java.util.List;

public interface OnImagesLoadedListener {
    void onSuccess(List<Image> images);
    void onFailure(String error);
}
