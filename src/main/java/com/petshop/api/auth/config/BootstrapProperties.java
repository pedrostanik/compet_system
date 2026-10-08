package com.petshop.api.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * app.bootstrap.* — the first OWNER account, created only while the users table is empty.
 * The password is the old single-admin password (APP_ADMIN_PASSWORD), so the owner keeps
 * logging in with it — now with their e-mail instead of "admin".
 */
@ConfigurationProperties("app.bootstrap")
public record BootstrapProperties(String ownerEmail, String ownerName, String ownerPassword) {
}
