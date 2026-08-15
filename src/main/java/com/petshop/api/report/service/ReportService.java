package com.petshop.api.report.service;

import com.petshop.api.customer.domain.Customer;
import com.petshop.api.customer.repository.CustomerRepository;
import com.petshop.api.report.dto.AbsentCustomer;
import com.petshop.api.report.repository.ReportRepository;
import com.petshop.api.schedule.domain.Scheduling;
import com.petshop.api.schedule.domain.enums.ScheduleStatus;
import com.petshop.api.schedule.repository.SchedulingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.ai.chat.client.ChatClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ChatClient chatClient;
    private final SchedulingRepository schedulingRepository;
    private final CustomerRepository customerRepository;
    private final ReportRepository reportRepository;

    @Cacheable(value = "report")
    public String generateReport() {
        List<Scheduling> schedulings = schedulingRepository.findAll();
        List<Customer> customers = customerRepository.findAll();

        String data = buildDataSummary(schedulings, customers);

        String prompt = """
                Você é um assistente especializado em análise de dados de petshops.
                Analise os dados abaixo e gere um relatório em português com:
                
                1. **Resumo geral** — total de agendamentos, clientes ativos, serviços mais realizados
                2. **Alertas de frequência** — clientes que costumavam vir regularmente e pararam, ou que aumentaram a frequência
                3. **Clientes em destaque** — quem mais frequenta, quem não vem há mais tempo
                4. **Tendências** — dias/períodos com mais agendamentos, serviços mais populares
                5. **Recomendações** — ações que o petshop pode tomar com base nos dados
                
                Seja objetivo e use linguagem simples. Use emojis para destacar alertas importantes.
                
                Dados:
                %s
                """.formatted(data);

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    @CacheEvict(value = "report", allEntries = true)
    public void evictReportCache() {}

    private String buildDataSummary(List<Scheduling> schedulings, List<Customer> customers) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysAgo = now.minusDays(30);
        LocalDateTime ninetyDaysAgo = now.minusDays(90);

        // Agendamentos por cliente
        Map<String, List<Scheduling>> byCustomer = schedulings.stream()
                .collect(Collectors.groupingBy(Scheduling::getCustomerName));

        StringBuilder sb = new StringBuilder();

        sb.append("=== DADOS GERAIS ===\n");
        sb.append("Total de clientes: ").append(customers.size()).append("\n");
        sb.append("Total de agendamentos: ").append(schedulings.size()).append("\n");
        sb.append("Agendamentos realizados: ").append(
                schedulings.stream().filter(s -> ScheduleStatus.HAPPENED.equals(s.getScheduleStatus())).count()
        ).append("\n");
        sb.append("Agendamentos pendentes: ").append(
                schedulings.stream().filter(s -> ScheduleStatus.SCHEDULED.equals(s.getScheduleStatus())).count()
        ).append("\n");
        sb.append("Agendamentos cancelados: ").append(
                schedulings.stream().filter(s -> ScheduleStatus.CANCELED.equals(s.getScheduleStatus())).count()
        ).append("\n\n");

        sb.append("=== HISTÓRICO POR CLIENTE ===\n");
        byCustomer.forEach((customerName, customerSchedulings) -> {
            sb.append("Cliente: ").append(customerName).append("\n");
            sb.append("  Total de agendamentos: ").append(customerSchedulings.size()).append("\n");

            // Último agendamento
            customerSchedulings.stream()
                    .filter(s -> s.getTime() != null)
                    .max((a, b) -> a.getTime().compareTo(b.getTime()))
                    .ifPresent(last -> sb.append("  Último agendamento: ")
                            .append(last.getTime().format(fmt)).append("\n"));

            // Agendamentos últimos 30 dias
            long last30 = customerSchedulings.stream()
                    .filter(s -> s.getTime() != null && s.getTime().isAfter(thirtyDaysAgo))
                    .count();
            sb.append("  Agendamentos últimos 30 dias: ").append(last30).append("\n");

            // Agendamentos entre 30 e 90 dias atrás
            long prev30to90 = customerSchedulings.stream()
                    .filter(s -> s.getTime() != null
                            && s.getTime().isAfter(ninetyDaysAgo)
                            && s.getTime().isBefore(thirtyDaysAgo))
                    .count();
            sb.append("  Agendamentos entre 30-90 dias atrás: ").append(prev30to90).append("\n");

            // Pets atendidos
            String pets = customerSchedulings.stream()
                    .map(Scheduling::getPetName)
                    .distinct()
                    .collect(Collectors.joining(", "));
            sb.append("  Pets: ").append(pets).append("\n\n");
        });

        sb.append("=== SERVIÇOS MAIS REALIZADOS ===\n");
        schedulings.stream()
                .flatMap(s -> s.getProtocols().stream())
                .collect(Collectors.groupingBy(p -> p.getProtocolName(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .forEach(e -> sb.append(e.getKey()).append(": ").append(e.getValue()).append("x\n"));

        return sb.toString();
    }

    public List<AbsentCustomer> generated15Or30DaysOff() {
        return reportRepository.findBy15offDay();
    }
}
