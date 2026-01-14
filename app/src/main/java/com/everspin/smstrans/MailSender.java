package com.everspin.smstrans;

import static com.everspin.smstrans.Environment.*;

import android.util.Log;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailSender {
    private static final String TAG = "MailSender";

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";

    public static void sendAsync(String subject, String body) {
        new Thread(() -> send(subject, body)).start();
    }

    public static void send(String subject, String body) {

        new Thread(() -> {
            try {
                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", SMTP_HOST);
                props.put("mail.smtp.port", SMTP_PORT);

                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                            FROM_EMAIL,
                            FROM_PASSWORD
                        );
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(FROM_EMAIL));
                message.setRecipients(
                        Message.RecipientType.TO,
                        InternetAddress.parse(TO_EMAIL)
                );
                message.setSubject(subject);
                message.setText(body);

                Transport.send(message);

                Log.d(TAG, "Mail sent successfully");


            } catch (Exception e) {
                Log.e(TAG, "Mail send failed", e);
                e.printStackTrace();
            }
        }).start();
    }
}
