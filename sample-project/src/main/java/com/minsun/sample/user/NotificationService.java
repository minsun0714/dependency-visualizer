package com.minsun.sample.user;

import com.minsun.sample.shared.Notifier;
import org.springframework.stereotype.Service;

/**
 * 명시적 생성자 주입(Lombok 미사용) 예시.
 * Notifier(shared 인터페이스)와 UserRepository(같은 패키지)를 생성자로 주입받는다.
 * 👉 필드 주입이 아니라 생성자 파라미터로만 의존을 표현하므로,
 *    생성자 파라미터를 훑지 않으면 이 간선을 놓친다.
 */
@Service
public class NotificationService {

    private final Notifier notifier;
    private final UserRepository userRepository;

    public NotificationService(Notifier notifier, UserRepository userRepository) {
        this.notifier = notifier;
        this.userRepository = userRepository;
    }

    public void notifyUser(Long userId) {
        userRepository.findById(userId);
        notifier.send("user:" + userId, "hello");
    }
}
