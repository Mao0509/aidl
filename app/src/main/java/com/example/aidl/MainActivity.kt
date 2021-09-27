package com.example.aidl

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private var mMessageSender: MessageSender? = null

    /**
     * Binder可能会意外死忙（比如Service Crash），Client监听到Binder死忙后可以进行重连服务等操作
     */
    private val mDeathRecipient = object : IBinder.DeathRecipient {
        override fun binderDied() {
            Log.d(TAG, "binderDied")
            mMessageSender?.asBinder()?.unlinkToDeath(this, 0)
            mMessageSender = null
            setupService()
        }
    }
    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, iBinder: IBinder?) {
            val messageModel = MessageModel(
                "client",
                "service",
                "content"
            )
            val messageSender = MessageSender.Stub.asInterface(iBinder)
            messageSender.asBinder().linkToDeath(mDeathRecipient, 0)
            messageSender.registerReceiverListener(mMessageReceiver)
            messageSender.sendMessage(messageModel)
            mMessageSender = messageSender
        }

        override fun onServiceDisconnected(p0: ComponentName?) {

        }
    }

    private val mMessageReceiver = object : MessageReceiver.Stub() {
        override fun onMessageReceived(messageModel: MessageModel?) {
            Log.d(TAG, "onMessageReceived messageModel: $messageModel")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupService()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mMessageSender?.asBinder()?.isBinderAlive ?: false) {
            mMessageSender?.unregisterReceiverListener(mMessageReceiver)
        }
        unbindService(mServiceConnection)
    }

    /**
     * bindService & startService：
     * 使用bindService方式，多个Client可以同时bind一个Service，但是当所有Client unbind后，Service会退出
     * 通常情况下，如果希望和Service交互，一般使用bindService方法，获取到onServiceConnected中的IBinder对象，和Service进行交互，
     * 不需要和Service交互的情况下，使用startService方法即可，Service主线程执行完成后会自动关闭；
     * unbind后Service仍保持运行，可以同时调用bindService和startService（比如像聊天软件，退出UI进程，Service仍能接收消息）
     */
    private fun setupService() {
        val intent = Intent(this, MessageService::class.java)
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
        startService(intent)
    }

}