package com.hucmuaf.locket_mobile.repo;

import com.hucmuaf.locket_mobile.modedb.Image;

import java.util.List;

/**
 * Xử lý bất đồng bộ
 * Callback với UI sau khi load ảnh từ Firebase xong
 * cách xử lý khi thành công và thất bại*/
public interface ImageLoadCallback {
    void onSuccess(List<Image> images);

    void onFailure(Exception e);
}
