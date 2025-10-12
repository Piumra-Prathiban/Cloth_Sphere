package com.clothsphere.service.IM;

import com.clothsphere.model.SystemUser;
import com.clothsphere.model.SystemUserId;

public interface ProfileIMService {
    SystemUser getUserProfile(String userName, String role);
    SystemUser updateProfile(SystemUserId id, String email, String phoneNumber);
    void updatePassword(SystemUserId id, String newPassword);
}