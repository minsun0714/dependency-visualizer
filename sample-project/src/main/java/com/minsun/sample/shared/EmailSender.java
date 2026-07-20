package com.minsun.sample.shared;

import org.springframework.stereotype.Component;

@Component
public class EmailSender implements Notifier {

    @Override
    public void send(String to, String message) {
        // no-op
    }
}
