package com.minsun.sample.user;

import com.minsun.sample.shared.BaseController;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController extends BaseController {

    private final UserService userService;

    @GetMapping("/users")
    public User getUser() {
        return userService.findUser(1L);
    }
}
