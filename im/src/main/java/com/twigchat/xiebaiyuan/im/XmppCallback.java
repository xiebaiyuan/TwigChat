package com.twigchat.xiebaiyuan.im;

/**
 * Created by xiebaiyuan on 2018/2/24.
 */

public interface XmppCallback {
    void connect(String msg);
    void login(String msg);
    void register(String msg);
    void changePassword(String msg);
    void setAvatar(String msg);
}
