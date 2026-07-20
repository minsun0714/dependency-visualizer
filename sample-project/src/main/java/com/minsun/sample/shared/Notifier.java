package com.minsun.sample.shared;

/**
 * 알림 발송 추상화 — 구현(implements) 간선 예시.
 * {@link EmailSender} 가 이 인터페이스를 구현한다.
 */
public interface Notifier {

    void send(String to, String message);
}
