package com.petshop.api.report.repository;

import com.petshop.api.customer.domain.Customer;
import com.petshop.api.report.dto.AbsentCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Customer, Long> {

    @Query(value = """
    SELECT EXTRACT(DAY FROM (CURRENT_DATE - last_sched.time))::int as days,
           last_sched.customer_id as customerId,
           c.name as customerName,
           last_sched.id as schedulingId
    FROM (
        SELECT DISTINCT ON (customer_id) id, customer_id, time
        FROM schedulings
        WHERE schedule_status = 'HAPPENED'
        ORDER BY customer_id, time DESC
    ) last_sched
    INNER JOIN customer c ON c.id = last_sched.customer_id
    WHERE last_sched.time < CURRENT_DATE - INTERVAL '15 days'
    ORDER BY days DESC
    """, nativeQuery = true)
    List<AbsentCustomer> findBy15offDay();

}
