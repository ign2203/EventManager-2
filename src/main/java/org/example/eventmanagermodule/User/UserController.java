package org.example.eventmanagermodule.User;

import jakarta.validation.Valid;
import org.example.eventmanagermodule.security.jwt.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final AuthenticationService authenticationService;

    public UserController(UserService userService, AuthenticationService authenticationService) {
        this.userService = userService;
        this.authenticationService = authenticationService;
    }

    @PostMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<UserResponseDto> registerUser(
            @RequestBody @Valid SignUpRequest user) {
        log.info("Get request for sign-up: login ={}", user.login());
        var createdUser = userService.registerUser(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new UserResponseDto(
                        createdUser.id(),
                        createdUser.login(),
                        createdUser.age(),
                        UserRole.USER
                ));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDto> findUserById(@PathVariable Long userId) {
        log.info("Get a user search request: id ={}", userId);
        var findUser = userService.findUserById(userId);
        return
                ResponseEntity
                        .status(HttpStatus.OK)
                        .body(new UserResponseDto(
                                findUser.id(),
                                findUser.login(),
                                findUser.age(),
                                findUser.role()
                        ));
    }

    @PostMapping("/auth")
    @PreAuthorize("permitAll()")
    ResponseEntity<JwtTokenResponse> authenticate(
            @RequestBody @Valid UserLoginRequest userLoginRequest
    ) {
        log.info("Get request for sign-in: login ={}", userLoginRequest.login());

        var token = authenticationService.authenticateUser(userLoginRequest);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new JwtTokenResponse(token));
    }
}
