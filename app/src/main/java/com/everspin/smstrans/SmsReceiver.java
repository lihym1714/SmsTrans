package com.everspin.smstrans;

import static com.everspin.smstrans.MailSender.sendAsync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("SmsReceiver", "onReceive CALLED");

        if (!Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            return;
        }

        SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);

        for (SmsMessage sms : messages) {
            if (sms == null) continue;

            String sender = sms.getOriginatingAddress();
            String body = sms.getMessageBody();

            Log.d(TAG, "sender=" + sender);
            Log.d(TAG, "body=" + body);

            if (sender == null || body == null) continue;
            if (!isTargetSender(sender,getPrefs(context).getString("Numbers"," "))) continue;

            String code = extractAuthCode(body);
            if (code == null) continue;

            Log.d(TAG, "AUTH CODE = " + code);

            sendAsync(
                    "Auth Code Delivery",
                    "Receive Body:\n"+body+"\n From. "+sender
            );
        }
    }

    private boolean isTargetSender(String sender, String targets) {
        Log.d(TAG, "isTargetSender, sender: "+sender+" targets: "+targets+" targets.contains(sender): "+targets.contains(sender));
        return sender.equals("09012341234") ||  sender.contains("1588") || targets.contains(sender);
    }


    private String extractAuthCode(String message) {
        Pattern pattern = Pattern.compile("\\d{6}");

        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    private SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences("receiveNum", Context.MODE_PRIVATE);
    }

}
