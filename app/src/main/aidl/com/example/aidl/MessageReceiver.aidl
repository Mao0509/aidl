package com.example.aidl;

import com.example.aidl.MessageModel;

interface MessageReceiver {

   void onMessageReceived(in MessageModel messageModel);

}