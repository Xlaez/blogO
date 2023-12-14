package com.dolph.blog.interfaces;

public interface UserProjection {
    String getCreatedAt();
    String getFullname();
    String getEmail();
    String getBio();
    String getTwitter();
    String getPics();

    boolean isEmailVerified();
}
