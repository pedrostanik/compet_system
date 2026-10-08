package com.petshop.api.repository;

import com.petshop.api.TestcontainersConfiguration;
import com.petshop.api.customer.domain.Customer;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/** Schema rules introduced by migrations after the V1 baseline, checked on real PostgreSQL. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class MigrationsTest {

    @Autowired private EntityManager em;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void allMigrationsApplied() {
        Integer pending = jdbc.queryForObject(
                "SELECT count(*) FROM flyway_schema_history WHERE NOT success", Integer.class);
        assertThat(pending).isZero();
    }

    @Test
    void v2_customerCanBeSavedWithoutEmail() {
        Customer c = new Customer();
        c.setName("Sem E-mail");
        c.setPhone("11 97777-6666");
        c.setCpf("529.982.247-25");
        c.setEmail(null);

        em.persist(c);
        em.flush();

        assertThat(c.getId()).isPositive();
    }
}
