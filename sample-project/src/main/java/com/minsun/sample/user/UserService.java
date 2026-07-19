package com.minsun.sample.user;

import com.minsun.sample.order.OrderService;
import com.minsun.sample.shared.EmailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final OrderService orderService;   // 👈 cross-domain: user -> order  (SCC A 사이클)
    private final EmailSender emailSender;

    public User findUser(Long id) {
        return userRepository.findById(id);
    }

    public int orderCount(Long userId) {
        return orderService.countByUser(userId);
    }
}
