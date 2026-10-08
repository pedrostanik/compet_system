package com.petshop.api.auth.domain;

/**
 * What a user may do (enforced in SecurityConfig):
 * OWNER  – everything, including managing other owners;
 * ADMIN  – everything except creating/changing OWNER accounts;
 * STAFF  – day-to-day work (customers, pets, schedule, services, packs, products);
 *          no revenue reports, receipts or user management;
 * VIEWER – read-only, including reports and receipts.
 */
public enum Role {
    OWNER, ADMIN, STAFF, VIEWER;

    public String authority() {
        return "ROLE_" + name();
    }
}
