package com.orygenapps.sema.data;

import io.realm.RealmObject;
import io.realm.annotations.Required;

/**
 * Created by ashemah on 15/02/2015.
 */
public class User extends RealmObject {

    @Required
    private String username;

    @Required
    private String authToken;
    private long lastSyncTimestamp;

    public String getPushToken() {
        return pushToken;
    }

    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }

    @Required
    private String pushToken;
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public long getLastSyncTimestamp() {
        return lastSyncTimestamp;
    }

    public void setLastSyncTimestamp(long lastSyncTimestamp) {
        this.lastSyncTimestamp = lastSyncTimestamp;
    }
}
