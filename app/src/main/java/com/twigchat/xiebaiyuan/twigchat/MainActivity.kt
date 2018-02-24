package com.twigchat.xiebaiyuan.twigchat

import android.app.ProgressDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.twigchat.xiebaiyuan.im.XmppCallback
import com.twigchat.xiebaiyuan.im.XmppService
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main.*
import android.widget.Toast
import com.twigchat.xiebaiyuan.twigchat.R.mipmap.ic_launcher


class MainActivity : AppCompatActivity(), XmppCallback, View.OnClickListener {
    override fun onClick(v: View?) {
        when (v) {
            connect -> mXMPPService.connect()
            register -> mXMPPService.register("test001", "123456")
            login -> mXMPPService.login("test001", "123456")
            changePassword -> mXMPPService.changePassword("123456")
            avatar -> {
                val bitmap = BitmapFactory.decodeResource(resources, ic_launcher)
                mXMPPService.setAvatar(bitmap)
            }
            sendMessage -> mXMPPService.sendMessage("我爱ax")
        }
    }

    private fun toastShow(text: String?) {
        this.runOnUiThread {
            progressDialogDismiss()
            Toast.makeText(this@MainActivity, text, Toast.LENGTH_LONG).show()
        }


    }

    private fun progressDialogShow() {
        mProgressDialog.show()

    }

    private fun progressDialogDismiss() {
        if (mProgressDialog.isShowing) mProgressDialog.dismiss()

    }
    override fun setAvatar(msg: String?) {
        toastShow(msg)
    }

    override fun changePassword(msg: String?) {
        toastShow(msg)
    }

    override fun connect(msg: String?) {
        toastShow(msg)
    }

    override fun login(msg: String?) {
        toastShow(msg)
    }

    override fun register(msg: String?) {
        toastShow(msg)
    }

    private lateinit var mXMPPService: XmppService


    private lateinit var mProgressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, stringFromJNI(), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        initIm()
        initView()

    }

    private fun initView() {
        mProgressDialog = ProgressDialog(this@MainActivity)
        mProgressDialog.setMessage("加载中")
    }

    private fun initIm() {
        mXMPPService = XmppService()
        mXMPPService.initXMPPTCPConnection()
        mXMPPService.setXMPPClickListener(this)

        connect.setOnClickListener(this)
        register.setOnClickListener(this)
        login.setOnClickListener(this)
        changePassword.setOnClickListener(this)
        avatar.setOnClickListener(this)
        sendMessage.setOnClickListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}
