package com.oasisfeng.nevo.decorators.wechat;

import android.app.Application;
import android.content.SharedPreferences;

public class WeChatApp extends Application {

    private boolean isReplying = false;
    private boolean lastIsRecalled = false;
    private boolean isRecasted = false;
    private SharedPreferences preferences;

    public boolean getReplying() {
        return isReplying;
    }
    public boolean getLastIsRecalled() { return lastIsRecalled; }
    public boolean getIsRecasted() { return isRecasted; }
    public SharedPreferences getSharedPreferences() { return preferences; }

    public void setReplying(boolean state) {
        this.isReplying = state;
    }
    public void setLastIsRecalled(boolean state) { this.lastIsRecalled = state; }
    public void setSharedPreferences(SharedPreferences pref) { this.preferences = pref; }
    public void setIsRecasted(boolean state) { this.isRecasted = state; }
}
