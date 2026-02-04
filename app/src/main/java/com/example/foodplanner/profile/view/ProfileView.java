package com.example.testfoodplanner.profile.view;

import com.example.testfoodplanner.base.BaseView;

public interface ProfileView extends BaseView {
    void onBackupSuccess();

    void onRestoreSuccess();

    void onSyncSuccess();

    void onLogoutSuccess();
}
