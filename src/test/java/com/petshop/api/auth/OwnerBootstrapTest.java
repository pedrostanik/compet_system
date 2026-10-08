package com.petshop.api.auth;

import com.petshop.api.auth.config.BootstrapProperties;
import com.petshop.api.auth.domain.Role;
import com.petshop.api.auth.domain.User;
import com.petshop.api.auth.repository.UserRepository;
import com.petshop.api.auth.service.OwnerBootstrap;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OwnerBootstrapTest {

    private final UserRepository users = mock(UserRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);

    @Test
    void createsTheFirstOwner_whenThereAreNoUsers() {
        when(users.count()).thenReturn(0L);
        when(encoder.encode("old-admin-pass")).thenReturn("hashed");

        new OwnerBootstrap(users, encoder, new BootstrapProperties(" dona@pet.com ", "Dona Compet", "old-admin-pass"))
                .run(null);

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(users).save(saved.capture());
        assertThat(saved.getValue().getEmail()).isEqualTo("dona@pet.com");
        assertThat(saved.getValue().getName()).isEqualTo("Dona Compet");
        assertThat(saved.getValue().getRole()).isEqualTo(Role.OWNER);
        assertThat(saved.getValue().getPasswordHash()).isEqualTo("hashed");
        assertThat(saved.getValue().isMustChangePassword()).isFalse();
    }

    @Test
    void doesNothing_onceUsersExist() {
        when(users.count()).thenReturn(3L);

        new OwnerBootstrap(users, encoder, new BootstrapProperties("dona@pet.com", null, "x")).run(null);

        verify(users, never()).save(any());
    }

    @Test
    void doesNothing_withoutEmailOrPassword() {
        when(users.count()).thenReturn(0L);

        new OwnerBootstrap(users, encoder, new BootstrapProperties("", null, "x")).run(null);
        new OwnerBootstrap(users, encoder, new BootstrapProperties("dona@pet.com", null, null)).run(null);

        verify(users, never()).save(any());
    }
}
