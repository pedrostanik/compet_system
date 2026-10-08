package com.petshop.api.auth.service;

import com.petshop.api.auth.config.BootstrapProperties;
import com.petshop.api.auth.domain.Role;
import com.petshop.api.auth.domain.User;
import com.petshop.api.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Creates the first OWNER from app.bootstrap.* (APP_OWNER_EMAIL + APP_ADMIN_PASSWORD) when the
 * users table is empty — i.e. on the first start after V3__identity. Does nothing afterwards,
 * so changing those variables later has no effect on existing accounts.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OwnerBootstrap implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BootstrapProperties props;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            return;
        }
        if (!StringUtils.hasText(props.ownerEmail()) || !StringUtils.hasText(props.ownerPassword())) {
            log.warn("No user accounts exist and APP_OWNER_EMAIL / APP_ADMIN_PASSWORD are not set: nobody can log in");
            return;
        }

        User owner = new User();
        owner.setEmail(props.ownerEmail().trim());
        owner.setName(StringUtils.hasText(props.ownerName()) ? props.ownerName().trim() : "Proprietário");
        owner.setRole(Role.OWNER);
        owner.setPasswordHash(passwordEncoder.encode(props.ownerPassword()));
        owner.setMustChangePassword(false);   // the existing admin password, already known to the owner
        userRepository.save(owner);
        log.info("Created the first OWNER account ({}) from APP_OWNER_EMAIL", owner.getPublicId());
    }
}
