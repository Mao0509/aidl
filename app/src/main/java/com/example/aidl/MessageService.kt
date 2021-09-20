package com.example.aidl

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Parcel
import android.os.RemoteCallbackList
import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean

class MessageService : Service() {

    companion object {
        private const val TAG = "MessageService"
    }

    /**
     * 经过Binder处理 Service接收的一个新的对象
     * 所以如果使用普通的List在unregisterListener时会无法找到对象
     * 但是底层使用的Binder对象相同
     * RemoteCallbackList可以根据这个特性来识别对象进行unregister
     */
    private val mMessageReceiverListener = RemoteCallbackList<MessageReceiver>()
    private val mServiceRunning = AtomicBoolean(false)

    private inner class FakeMessageTask : Runnable {

        override fun run() {
            while (mServiceRunning.get()) {
                try {
                    Thread.sleep(1000L)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                val messageModel = MessageModel("Service", "Client", System.currentTimeMillis().toString())
                val count = mMessageReceiverListener.beginBroadcast()
                for (i in 0..count) {
                    val receiver = mMessageReceiverListener.getBroadcastItem(i)
                    receiver.onMessageReceived(messageModel)
                }
                mMessageReceiverListener.finishBroadcast()
            }
        }
    }

    private val mBinder = object : MessageSender.Stub() {
        override fun sendMessage(messageModel: MessageModel?) {
            Log.i(TAG, "messageModel:$messageModel")
        }

        override fun registerReceiverListener(receiver: MessageReceiver) {
            mMessageReceiverListener.register(receiver)
        }

        override fun unregisterReceiverListener(receiver: MessageReceiver) {
            mMessageReceiverListener.unregister(receiver)
        }

        override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            return super.onTransact(code, data, reply, flags)
        }
    }

    override fun onBind(intent: Intent): IBinder = mBinder

    override fun onCreate() {
        super.onCreate()
        mServiceRunning.set(true)
        Thread(FakeMessageTask()).start()

    }

    override fun onDestroy() {
        super.onDestroy()
        mServiceRunning.set(false)
    }

}