package com.twigchat.xiebaiyuan.im;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import java.io.ByteArrayOutputStream;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by xiebaiyuan on 2018/2/24.
 */

public class XmppService {


    final String HOST = "xxx.xxx.xxx.xxx";
    final int PORT = 5222;
    private final String TAG = "xmpptag";
    private final String SERVICE_NAME = "xxx.xxx.xxx.xxx";
    AbstractXMPPConnection mXMPPTCPConnection;
    XmppCallback callback;
    Chat mChat;
    private SSLContext mSSLContext;

    public AbstractXMPPConnection initXMPPTCPConnection() {
        SmackConfiguration.DEBUG = true;
        try {
            mSSLContext = SSLContext.getInstance("TLS");
            mSSLContext.init(null, new TrustManager[]{new MyTrustManager()}, null);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("connecting failed", e);
        }

        XMPPTCPConnectionConfiguration.Builder builder = XMPPTCPConnectionConfiguration.builder();
//        builder.setUsernameAndPassword(USER_NAME, PASSWORD);
        builder.setHost(HOST);
        //此处只能使用域名或PC机器名称
        builder.setServiceName(SERVICE_NAME);
        builder.setPort(PORT);
        //连接套将使用流压缩。
        builder.setCompressionEnabled(false);
//        builder.setConnectTimeout(5000);
//        builder.setDebuggerEnabled(true);
//        //上线通知系统
//        builder.setSendPresence(true);
        //安全模式
        builder.setSecurityMode(ConnectionConfiguration.SecurityMode.required);
        //https不验证证书方式（信任所有证书）
        builder.setCustomSSLContext(mSSLContext);
        builder.setHostnameVerifier(new MyHostnameVerifier());
        SASLAuthentication.unBlacklistSASLMechanism("SCRAM-SHA-1");
        SASLAuthentication.unBlacklistSASLMechanism("PLAIN");
        SASLAuthentication.unBlacklistSASLMechanism("DIGEST-MD5");
//        SASLAuthentication.registerSASLMechanism(new SASLPlainMechanism());
        mXMPPTCPConnection = new XMPPTCPConnection(builder.build());
        return mXMPPTCPConnection;
    }

    public void connect() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String connectMsg;
                try {
                    if (mXMPPTCPConnection.isConnected()) {
                        Log.i(TAG, "connect successfull");
                        connectMsg = "connect successfull";
                    } else {
                        mXMPPTCPConnection.connect();
                        if (mXMPPTCPConnection.isConnected()) {
                            connectMsg = "connect successfull";
                        } else {
                            connectMsg = "connect failed";
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "connect e=" + e.getMessage());
                    connectMsg = "connect e=" + e.getMessage();
                }
                callback.connect(connectMsg);
            }
        }

        );
        thread.start();
    }

    public void login(final String userName, final String password) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String loginMsg;
                try {
                    if (!mXMPPTCPConnection.isConnected())
                        mXMPPTCPConnection.connect();
                } catch (Exception e) {
                    e.printStackTrace();
                    mXMPPTCPConnection.disconnect();
                }
                Log.i("xmppTag", "XMPPService login connected=" + mXMPPTCPConnection.isConnected());
                if (mXMPPTCPConnection.isConnected()) {
                    try {
                        mXMPPTCPConnection.login(userName, password);

                        if (mXMPPTCPConnection.isAuthenticated()) {
                            Log.i(TAG, "登录成功~");
                            loginMsg = "登录成功~";
                        } else {
                            Log.i(TAG, "登录失败~");
                            loginMsg = "登录失败~";
                        }

                    } catch (Exception e) {
                        e.printStackTrace();//
                        Log.i(TAG, "登录异常~=" + e.getMessage());
                        loginMsg = "登录异常~=" + e.getMessage();
                    }
                } else {
                    Log.i(TAG, "connect failed");
                    loginMsg = "connect failed";
                }

                callback.login(loginMsg);
            }
        }).start();
    }

    public void register(final String userName, final String password) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                String registerMsg;
                try {
                    if (!mXMPPTCPConnection.isConnected())
                        mXMPPTCPConnection.connect();
                } catch (Exception e) {
                    e.printStackTrace();
                    mXMPPTCPConnection.disconnect();
                }
                Log.i("xmppTag", "XMPPService connected=" + mXMPPTCPConnection.isConnected());
                if (mXMPPTCPConnection.isConnected()) {
                    String serviceName = mXMPPTCPConnection.getServiceName();
                    try {
                        AccountManager accountManager = AccountManager.getInstance(mXMPPTCPConnection);
                        Log.i("xmppTag", "Register supportsAccountCreation=" + accountManager.supportsAccountCreation());
                        if (accountManager.supportsAccountCreation()) {
                            accountManager.createAccount(userName + "@" + serviceName, password);
                            Log.i(TAG, "注册成功~");
                            registerMsg = "注册成功~";
                        } else {
                            Log.i(TAG, "服务端不能注册~");
                            registerMsg = "服务端不能注册~";
                        }

                    } catch (Exception e) {
                        e.printStackTrace();//
                        Log.i(TAG, "注册异常~=" + e.getMessage());
                        registerMsg = "注册异常~=" + e.getMessage();
                    }
                } else {
                    Log.i(TAG, "connect failed");
                    registerMsg = "connect failed";
                }
                callback.register(registerMsg);
            }
        }).start();
    }

    public void changePassword(final String newPassword) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String changePasswordMsg;

                try {
                    if (!mXMPPTCPConnection.isConnected())
                        mXMPPTCPConnection.connect();
                } catch (Exception e) {
                    e.printStackTrace();
                    mXMPPTCPConnection.disconnect();
                }
                Log.i("xmppTag", "XMPPService connected=" + mXMPPTCPConnection.isConnected());
                if (mXMPPTCPConnection.isConnected()) {
                    try {

                        if (mXMPPTCPConnection.isAuthenticated()) {
                            AccountManager accountManager = AccountManager.getInstance(mXMPPTCPConnection);
                            accountManager.changePassword(newPassword);
                            Log.i(TAG, "修改密码成功~");
                            changePasswordMsg = "修改密码成功~";
                        } else {
                            Log.i(TAG, "请先登录~");
                            changePasswordMsg = "请先登录~";
                        }

                    } catch (Exception e) {
                        e.printStackTrace();//
                        Log.i(TAG, "修改密码异常~=" + e.getMessage());
                        changePasswordMsg = "修改密码异常~=" + e.getMessage();
                    }
                } else {

                    changePasswordMsg = "connect failed";
                }
                callback.register(changePasswordMsg);
            }
        }).start();
    }

    public void setAvatar(final Bitmap bitmap) {
        new Thread(new Runnable() {
            @Override
            public void run() {


                try {
                    if (!mXMPPTCPConnection.isConnected())
                        mXMPPTCPConnection.connect();
                } catch (Exception e) {
                    e.printStackTrace();
                    mXMPPTCPConnection.disconnect();
                }
                Log.i("xmppTag", "XMPPService connected=" + mXMPPTCPConnection.isConnected());
                if (mXMPPTCPConnection.isConnected()) {
                    try {
                        if (mXMPPTCPConnection.isAuthenticated()) {
                            VCardManager vCardManager = VCardManager.getInstanceFor(mXMPPTCPConnection);
                            VCard vCard = vCardManager.loadVCard();
                            byte[] bytes = bitmapToByte(bitmap);
                            String encodedImage = Base64.encodeToString(bytes, Base64.DEFAULT);
                            vCard.setAvatar(bytes);
                            vCardManager.saveVCard(vCard);
                            callback.setAvatar("修改头像成功~");
                        } else {
                            callback.setAvatar("请先登录~");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();//
                        callback.setAvatar("修改头像异常~=" + e.getMessage());
                    }
                } else {
                    callback.setAvatar("connect failed");
                }
            }
        }).start();



    }

    private byte[] bitmapToByte(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public void sendMessage(String body) {
        if (mChat == null) {
            ChatManager chatManager = ChatManager.getInstanceFor(mXMPPTCPConnection);
            String servicename = mXMPPTCPConnection.getServiceName();
            mChat = chatManager.createChat("ddctrl-2@" + servicename);

            mChat.addMessageListener(new ChatMessageListener() {
                @Override
                public void processMessage(Chat chat, org.jivesoftware.smack.packet.Message message) {
                    Log.d(TAG, "message result=" + message.getBody());
                }
            });
        }
        try {
            mChat.sendMessage(body);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    public void setXMPPClickListener(XmppCallback callback) {
        this.callback = callback;
    }

    private class MyHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            // TODO Auto-generated method stub
            return true;
        }

    }

    private class MyTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s)
                throws CertificateException {
            Log.i(TAG, "checkClientTrusted:" + s);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s)
                throws CertificateException {
            Log.i(TAG, "checkServerTrusted:" + s);
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            Log.i(TAG, "getAcceptedIssuers");
            return new X509Certificate[0];
        }



    }

}