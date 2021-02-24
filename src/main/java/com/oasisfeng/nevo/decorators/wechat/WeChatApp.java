package com.oasisfeng.nevo.decorators.wechat;

import android.app.Application;
import android.content.Context;

public class WeChatApp extends Application {

    private boolean isReplying = false;

    public boolean getReplying() {
        return isReplying;
    }

    public void setReplying(boolean state) {
        this.isReplying = state;
    }
}
