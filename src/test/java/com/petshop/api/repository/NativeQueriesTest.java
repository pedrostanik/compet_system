package com.petshop.api.repository;

import com.petshop.api.TestcontainersConfiguration;
import com.petshop.api.customer.domain.Customer;
import com.petshop.api.history.repository.HistoryRepository;
import com.petshop.api.report.dto.AbsentCustomer;
import com.petshop.api.report.repository.ReportRepository;
import com.petshop.api.schedule.domain.Scheduling;
import com.petshop.api.schedule.domain.enums.ScheduleStatus;
import com.petshop.api.schedule.repository.SchedulingRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The four native SQL queries, run on real PostgreSQL. H2 could not execute them
 * (::interval, DISTINCT ON, EXTRACT, DATE_TRUNC), so until now they were untested — and they
 * are exactly what must later be made tenant-aware (roadmap §2, Phase 2).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class NativeQueriesTest {

    @Autowired private SchedulingRepository schedulingRepository;
    @Autowired private ReportRepository reportRepository;
    @Autowired private HistoryRepository historyRepository;
    @Autowired private EntityManager em;

    private static final LocalDateTime TEN_AM = LocalDateTime.of(2026, 11, 2, 10, 0);

    // --- SchedulingRepository.existsOverlapping: the double-booking guard ---

    @Test
    void existsOverlapping_detectsOverlapWithActiveBooking() {
        long id = booking(1L, 1L, TEN_AM, 60, ScheduleStatus.SCHEDULED).getId();

        assertThat(schedulingRepository.existsOverlapping(-1L, TEN_AM.plusMinutes(30), TEN_AM.plusMinutes(90))).isTrue();
        assertThat(schedulingRepository.existsOverlapping(-1L, TEN_AM.minusMinutes(30), TEN_AM.plusMinutes(1))).isTrue();
        // Back-to-back slots do not overlap.
        assertThat(schedulingRepository.existsOverlapping(-1L, TEN_AM.plusMinutes(60), TEN_AM.plusMinutes(90))).isFalse();
        assertThat(schedulingRepository.existsOverlapping(-1L, TEN_AM.minusMinutes(60), TEN_AM)).isFalse();
        // A booking never conflicts with itself (editing it).
        assertThat(schedulingRepository.existsOverlapping(id, TEN_AM, TEN_AM.plusMinutes(60))).isFalse();
    }

    @Test
    void existsOverlapping_ignoresCanceledAndHappenedBookings() {
        booking(1L, 1L, TEN_AM, 60, ScheduleStatus.CANCELED);
        booking(1L, 1L, TEN_AM, 60, ScheduleStatus.HAPPENED);

        assertThat(schedulingRepository.existsOverlapping(-1L, TEN_AM, TEN_AM.plusMinutes(60))).isFalse();
    }

    @Test
    void existsOverlapping_countsConfirmedBookings() {
        booking(1L, 1L, TEN_AM, 30, ScheduleStatus.CONFIRMED);

        assertThat(schedulingRepository.existsOverlapping(-1L, TEN_AM.plusMinutes(15), TEN_AM.plusMinutes(45))).isTrue();
    }

    // --- SchedulingRepository.findOverlapping ---

    @Test
    void findOverlapping_returnsBookingsIntersectingTheWindow() {
        Scheduling inside = booking(1L, 1L, TEN_AM, 60, ScheduleStatus.SCHEDULED);
        booking(1L, 2L, TEN_AM.plusHours(3), 60, ScheduleStatus.SCHEDULED);

        List<Scheduling> found = schedulingRepository.findOverlapping(TEN_AM.plusMinutes(45), TEN_AM.plusMinutes(75));

        assertThat(found).extracting(Scheduling::getId).containsExactly(inside.getId());
    }

    // --- ReportRepository.findBy15offDay: customers absent for more than 15 days ---

    @Test
    void findBy15offDay_listsCustomersWhoseLastVisitIsOlderThan15Days() {
        Customer absent = customer("Ana Ausente");
        Customer recent = customer("Bruno Recente");
        Customer neverCame = customer("Carla Agendada");

        LocalDate today = LocalDate.now();
        booking(absent.getId(), 1L, today.minusDays(40).atTime(12, 0), 60, ScheduleStatus.HAPPENED);
        Scheduling lastVisit = booking(absent.getId(), 1L, today.minusDays(20).atTime(12, 0), 60, ScheduleStatus.HAPPENED);
        booking(recent.getId(), 2L, today.minusDays(40).atTime(12, 0), 60, ScheduleStatus.HAPPENED);
        booking(recent.getId(), 2L, today.minusDays(5).atTime(12, 0), 60, ScheduleStatus.HAPPENED);
        booking(neverCame.getId(), 3L, today.minusDays(40).atTime(12, 0), 60, ScheduleStatus.SCHEDULED);

        List<AbsentCustomer> result = reportRepository.findBy15offDay();

        assertThat(result).hasSize(1);
        AbsentCustomer row = result.get(0);
        assertThat(row.customerId()).isEqualTo(absent.getId());
        assertThat(row.customerName()).isEqualTo("Ana Ausente");
        assertThat(row.schedulingId()).isEqualTo(lastVisit.getId());   // the most recent visit, not the oldest
        assertThat(row.days()).isBetween(19, 21);                       // DB clock (UTC) vs JVM local date
    }

    // --- HistoryRepository.getPetFrequency: visits per month over the last 12 months ---

    @Test
    void getPetFrequency_countsHappenedVisitsPerMonth_lastTwelveMonths() {
        LocalDate monthA = LocalDate.now().minusMonths(3).withDayOfMonth(10);
        LocalDate monthB = LocalDate.now().minusMonths(1).withDayOfMonth(10);

        booking(1L, 7L, monthA.atTime(10, 0), 60, ScheduleStatus.HAPPENED);
        booking(1L, 7L, monthB.atTime(10, 0), 60, ScheduleStatus.HAPPENED);
        booking(1L, 7L, monthB.plusDays(5).atTime(10, 0), 60, ScheduleStatus.HAPPENED);
        booking(1L, 7L, monthB.plusDays(6).atTime(10, 0), 60, ScheduleStatus.CANCELED);       // not a visit
        booking(1L, 7L, LocalDate.now().minusMonths(14).atTime(10, 0), 60, ScheduleStatus.HAPPENED); // too old
        booking(1L, 8L, monthB.atTime(10, 0), 60, ScheduleStatus.HAPPENED);                    // other pet

        List<Object[]> rows = historyRepository.getPetFrequency(7L);

        assertThat(rows).hasSize(2);
        assertThat(((Number) rows.get(0)[1]).longValue()).isEqualTo(1);   // month A, oldest first
        assertThat(((Number) rows.get(1)[1]).longValue()).isEqualTo(2);   // month B
    }

    // --- helpers ---

    private Customer customer(String name) {
        Customer c = new Customer();
        c.setName(name);
        c.setPhone("11 99999-9999");
        c.setCpf("529.982.247-25");
        c.setEmail("cliente@email.com");
        em.persist(c);
        return c;
    }

    private Scheduling booking(Long customerId, Long petId, LocalDateTime time, int minutes, ScheduleStatus status) {
        Scheduling s = new Scheduling();
        s.setCustomerId(customerId);
        s.setPetId(petId);
        s.setTime(time);
        s.setDuration(minutes);
        s.setScheduleStatus(status);
        em.persist(s);
        em.flush();
        return s;
    }
}
