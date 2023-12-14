package com.dolph.blog.interfaces;

import org.springframework.data.annotation.Id;

public interface UserProjection {
    @Id
    String id();
    String getCreatedAt();
    String getFullname();
    String getEmail();
    String getBio();
    String getTwitter();
    String getPics();

    boolean isEmailVerified();
}
