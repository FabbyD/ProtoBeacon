//package net.lg2gent.protobeacon
//
//import android.app.IntentService
//import android.content.Intent
//import android.util.Log
//import com.google.android.gms.nearby.Nearby
//import com.google.android.gms.nearby.messages.Message
//import com.google.android.gms.nearby.messages.MessageListener
//
//
//class BackgroundSubscribeIntentService: IntentService("BackgroundSubscribeIntentService") {
//
//    private val TAG = BackgroundSubscribeIntentService::class.java.getSimpleName()
//
//    override fun onHandleIntent(intent: Intent?) {
//        if (intent != null) {
//            Nearby.Messages.handleIntent(intent, object: MessageListener() {
//                override fun onFound(message: Message) {
//                    Log.i(TAG, "found message = " + message)
//                }
//
//                override fun onLost(message: Message) {
//                    Log.i(TAG, "lost message = " + message)
//                }
//            })
//        }
//    }
//}