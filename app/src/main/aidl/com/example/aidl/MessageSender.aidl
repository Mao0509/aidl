package com.example.aidl;

import com.example.aidl.MessageModel;
import com.example.aidl.MessageReceiver;

interface MessageSender {

    void sendMessage(in MessageModel messageModel);

    void registerReceiverListener(in MessageReceiver receiver);

    void unregisterReceiverListener(in MessageReceiver receiver);

}