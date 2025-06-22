package com.hucmuaf.locket_mobile.adapter;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.hucmuaf.locket_mobile.fragment.PageHomeFragment;
import com.hucmuaf.locket_mobile.fragment.PageReactFragment;

public class PagerAdapter extends FragmentStateAdapter {
    private String userId;
    private String initialImageId;
    private String initialFriendId;
    private String initialFriendName;
    private PageHomeFragment pageHomeFragment;
    private PageReactFragment pageReactFragment;

    public PagerAdapter(FragmentActivity fragmentActivity, String userId){
        super(fragmentActivity);
        this.userId = userId;

        Bundle bundle = new Bundle();
        bundle.putString("userId", userId);

        pageHomeFragment = new PageHomeFragment();
        pageHomeFragment.setArguments(bundle);

        pageReactFragment = new PageReactFragment();
//        pageReactFragment.setArguments(bundle);
    }

    public void setInitialImageData(String imageId, String friendId, String friendName) {
        this.initialImageId = imageId;
        this.initialFriendId = friendId;
        this.initialFriendName = friendName;
        Bundle reactBundle = new Bundle();
        reactBundle.putString("userId", userId);
        reactBundle.putString("imageId", imageId);
        reactBundle.putString("friendId", friendId);
        reactBundle.putString("friendName", friendName);
        pageReactFragment.setArguments(reactBundle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0: return pageHomeFragment;
            case 1: return pageReactFragment;
            default: return pageHomeFragment;
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
