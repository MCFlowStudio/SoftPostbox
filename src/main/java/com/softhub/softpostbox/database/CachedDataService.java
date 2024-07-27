package com.softhub.softpostbox.database;

import com.softhub.softframework.database.cache.CacheStorage;
import com.softhub.softpostbox.PostboxContainer;
import com.softhub.softpostbox.PostboxData;

import java.util.ArrayList;
import java.util.UUID;

public class CachedDataService {

    public static PostboxData get(UUID userId) {
        PostboxData postboxData = new PostboxData(userId, new PostboxContainer(new ArrayList<>()));
        if (CacheStorage.get("postboxdata", userId.toString()) instanceof PostboxData) {
            postboxData = (PostboxData) CacheStorage.get("postboxdata", userId.toString());
        }
        return postboxData;
    }

    public static void set(UUID userId, PostboxData flyData) {
        CacheStorage.set("postboxdata", userId.toString(), flyData);
    }

    public static void remove(UUID userId) {
        CacheStorage.remove("postboxdata", userId.toString());
    }

}
