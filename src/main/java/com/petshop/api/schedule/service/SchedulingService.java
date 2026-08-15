package com.petshop.api.schedule.service;

import com.petshop.api.packages.domain.Pack;
import com.petshop.api.packages.domain.PackProtocol;
import com.petshop.api.packages.repository.PackRepository;
import com.petshop.api.protocols.domain.Protocol;
import com.petshop.api.protocols.repository.ProtocolRepository;
import com.petshop.api.schedule.domain.Scheduling;
import com.petshop.api.schedule.domain.SchedulingProtocol;
import com.petshop.api.schedule.domain.enums.ScheduleStatus;
import com.petshop.api.schedule.dto.SchedulingProtocolResponse;
import com.petshop.api.schedule.dto.SchedulingRequest;
import com.petshop.api.schedule.dto.SchedulingResponse;
import com.petshop.api.schedule.repository.SchedulingRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SchedulingService {

    private final SchedulingRepository schedulingRepository;
    private final ProtocolRepository protocolRepository;
    private final PackRepository packRepository;

    public SchedulingResponse createScheduling(SchedulingRequest schedulingRequest) {
        Scheduling scheduling = new Scheduling();
        if (schedulingRequest.isPackage() ) {
            scheduling.setPackCycle(1);
        }
        scheduling.setPackId(schedulingRequest.packId());
        scheduling.setCustomerId(schedulingRequest.customerId());
        scheduling.setCustomerName(schedulingRequest.customerName());
        scheduling.setPetId(schedulingRequest.petId());
        scheduling.setPetName(schedulingRequest.petName());
        scheduling.setTime(schedulingRequest.time());
        scheduling.setSchedulingObservations(schedulingRequest.schedulingObservations());
        scheduling.setDuration(schedulingRequest.duration());
        scheduling.setScheduleStatus(ScheduleStatus.SCHEDULED);
        scheduling.setPrice(schedulingRequest.price());

        if (schedulingRequest.protocolIds() != null) {
            List<Protocol> protocols = protocolRepository.findAllById(schedulingRequest.protocolIds());
            for (Protocol p : protocols) {
                SchedulingProtocol sp = new SchedulingProtocol();
                sp.setScheduling(scheduling);
                sp.setProtocolId(p.getId());
                sp.setProtocolName(p.getName());
                scheduling.getProtocols().add(sp);
            }
        }

        Scheduling saved = schedulingRepository.save(scheduling);
        LocalDateTime start = saved.getTime();
        LocalDateTime end = start.plusMinutes(saved.getDuration());
        updateAffectedSchedules(start, end);
        return toResponse(saved);

    }

    @Transactional
    public SchedulingResponse updateSchedulingTime(Long id, SchedulingRequest schedulingRequest) {

        Scheduling s = schedulingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Scheduling not found with id " + id));

        // 1. Guardamos o "rastro" antigo antes de mudar
        LocalDateTime oldStart = s.getTime();
        int oldDuration = s.getDuration() != null ? s.getDuration() : 60;
        LocalDateTime oldEnd = oldStart.plusMinutes(oldDuration);

        // 2. Atualizamos para os novos valores
        s.setPackId(schedulingRequest.packId());
        s.setTime(schedulingRequest.time());
        s.setDuration(schedulingRequest.duration());
        s.setPetId(schedulingRequest.petId());
        s.setPetName(schedulingRequest.petName());
        s.setCustomerId(schedulingRequest.customerId());
        s.setCustomerName(schedulingRequest.customerName());
        s.setPackage(schedulingRequest.isPackage());
        s.setSchedulingObservations(schedulingRequest.schedulingObservations());
        s.setPrice(schedulingRequest.price());

        if (schedulingRequest.protocolIds() != null) {
            s.getProtocols().clear();
            List<Protocol> protocols = protocolRepository.findAllById(schedulingRequest.protocolIds());
            for (Protocol p : protocols) {
                SchedulingProtocol sp = new SchedulingProtocol();
                sp.setScheduling(s);
                sp.setProtocolId(p.getId());
                sp.setProtocolName(p.getName());
                s.getProtocols().add(sp);
            }
        }

        // 3. Salvamos primeiro
        Scheduling saved = schedulingRepository.save(s);
        schedulingRepository.flush();

        // 4. Recalculamos o impacto no horário antigo (quem ficou lá agora pode estar livre)
        updateAffectedSchedules(oldStart, oldEnd);

        // 5. Recalculamos o impacto no horário novo (quem já estava lá agora tem conflito)
        LocalDateTime newStart = saved.getTime();
        LocalDateTime newEnd = newStart.plusMinutes(saved.getDuration());
        updateAffectedSchedules(newStart, newEnd);

        // 6. Por fim, garantimos que o status do próprio objeto atualizado esteja correto
        recalculateSingleIntercept(saved);

        return toResponse(saved);

    }

    @Transactional
    public void deleteScheduling(Long id) {
        if (!schedulingRepository.existsById(id)) {
            throw new EntityNotFoundException("Scheduling not found with id " + id);
        }
        Scheduling s = schedulingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Scheduling not found with id " + id));

        LocalDateTime start = s.getTime();
        LocalDateTime end = start.plusMinutes(s.getDuration());

        schedulingRepository.deleteById(id);
        schedulingRepository.flush();
        updateAffectedSchedules(start, end);

    }

    private SchedulingResponse toResponse(Scheduling s) {
        List<SchedulingProtocolResponse> protocols = s.getProtocols().stream()
                .map(p -> new SchedulingProtocolResponse(
                        p.getProtocolId(),
                        p.getProtocolName(),
                        p.getProtocolPrice()
                ))
                .toList();

        return new SchedulingResponse(
                s.getId(),
                s.getPackId(),
                s.getCustomerId(),
                s.getPackCycle(),
                s.getCustomerName(),
                s.getPetId(),
                s.getPetName(),
                s.getSchedulingObservations(),
                s.getTime(),
                s.getScheduleStatus(),
                s.getPackage(),
                s.getDuration(),
                s.getIntercepted(),
                protocols,
                s.getPrice()
        );
    }

    public SchedulingResponse getScheduling(Long id) {
        return schedulingRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Scheduling not found"));
    }

    public List<SchedulingResponse> listScheduling() {
        return schedulingRepository.findAll().stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    public SchedulingResponse changeStatus(Long id, String status) {
        Scheduling s = schedulingRepository.findById(id)
                .orElseThrow(() ->  new EntityNotFoundException("Scheduling not found with id " + id));

//        if (ScheduleStatus.HAPPENED.name().equals(status)) {
//            s.setScheduleStatus(ScheduleStatus.HAPPENED);
//            if (Boolean.TRUE.equals(s.getPackage()) && s.getPackageCycleId() != null) {
//                consumeCycleServices(s);
//            }
//
//        }
        if (ScheduleStatus.HAPPENED.name().equals(status)) s.setScheduleStatus(ScheduleStatus.HAPPENED);
        if (ScheduleStatus.CANCELED.name().equals(status)) s.setScheduleStatus(ScheduleStatus.CANCELED);
        if (ScheduleStatus.CONFIRMED.name().equals(status)) s.setScheduleStatus(ScheduleStatus.CONFIRMED);

        LocalDateTime start = s.getTime();
        LocalDateTime end = start.plusMinutes(s.getDuration());
        //updateAffectedSchedules(start, end);
        return toResponse(schedulingRepository.save(s));
    }

    @Transactional
    public SchedulingResponse markAsHappened(Long id) {
        Scheduling s = schedulingRepository.findById(id)
                .orElseThrow(() ->  new EntityNotFoundException("Scheduling not found with id " + id));

        s.setScheduleStatus(ScheduleStatus.HAPPENED);

        LocalDateTime start = s.getTime();
        LocalDateTime end = start.plusMinutes(s.getDuration());
        updateAffectedSchedules(start, end);
        return toResponse(schedulingRepository.save(s));
    }

    @Transactional
    public SchedulingResponse markAsConfirmed(Long id) {
        Scheduling s = schedulingRepository.findById(id)
                .orElseThrow(() ->  new EntityNotFoundException("Scheduling not found with id " + id));

        s.setScheduleStatus(ScheduleStatus.CONFIRMED);
        return toResponse(schedulingRepository.save(s));
    }

    @Transactional
    public SchedulingResponse markAsCancelled(Long id) {
        Scheduling s = schedulingRepository.findById(id)
                .orElseThrow(() ->  new EntityNotFoundException("Scheduling not found with id " + id));

        LocalDateTime start = s.getTime();
        LocalDateTime end = start.plusMinutes(s.getDuration());
        updateAffectedSchedules(start, end);

        s.setScheduleStatus(ScheduleStatus.CANCELED);
        return toResponse(schedulingRepository.save(s));
    }

    private void updateAffectedSchedules(LocalDateTime start, LocalDateTime end) {
        // Busca quem está no mesmo "quadrante" de tempo
        List<Scheduling> candidates = schedulingRepository.findOverlapping(start, end);

        // Para cada um desses, verificamos se eles ainda batem com alguém
        for (Scheduling candidate : candidates) {
            recalculateSingleIntercept(candidate);
        }
    }

    private void recalculateSingleIntercept(Scheduling s) {
        // 1. Uso de Boolean.TRUE.equals evita NPE se scheduledHappened for null
        if (ScheduleStatus.HAPPENED.equals(s.getScheduleStatus())) return;

        LocalDateTime start = s.getTime();
        int duration = s.getDuration() != null ? s.getDuration() : 60;
        LocalDateTime end = start.plusMinutes(duration);

        boolean intercepted = schedulingRepository.existsOverlapping(s.getId(), start, end);

        // Aqui comparamos se o valor atual (que pode ser null) é diferente do novo cálculo
        if (!Boolean.valueOf(intercepted).equals(s.getIntercepted())) {
            s.setIntercepted(intercepted);
            schedulingRepository.save(s);
        }
    }

            public List<SchedulingResponse> createFutureFromPack(
                    SchedulingRequest schedulingRequest, LocalDateTime time, Long packId) {

                Pack pack = packRepository.findById(packId)
                        .orElseThrow(() -> new EntityNotFoundException("Pack not found with id " + packId));

                List<SchedulingResponse> schedulingResponses = new ArrayList<>();

                int totalAgendamentos;
                int intervaloDias;
                if ("Semanal".equals(pack.getFrequencia())) {
                    totalAgendamentos = 4;
                    intervaloDias = 7;
                } else if ("Quinzenal".equals(pack.getFrequencia())) {
                    totalAgendamentos = 2;
                    intervaloDias = 14;
                } else {
                    throw new IllegalStateException("Frequência de pacote desconhecida: " + pack.getFrequencia());
                }

                Map<Long, Integer> initialQtyPorProtocolo = new HashMap<>();
                Map<Long, Integer> remainingByProtocol = new HashMap<>();
                Map<Long, Protocol> protocolById = new HashMap<>();
                for (PackProtocol packProtocol : pack.getProtocols()) {
                    Protocol protocol = packProtocol.getProtocol();
                    protocolById.put(protocol.getId(), protocol);

                    int qtd = packProtocol.getQuantity();
                    remainingByProtocol.put(protocol.getId(), qtd);
                    initialQtyPorProtocolo.put(protocol.getId(), qtd);
                }

                // === 1) Cria o agendamento MANUAL (ciclo 1, data original) ===
                Scheduling manual = new Scheduling();
                manual.setPackId(pack.getId());
                manual.setPackCycle(1);
                manual.setCustomerId(schedulingRequest.customerId());
                manual.setCustomerName(schedulingRequest.customerName());
                manual.setPetId(schedulingRequest.petId());
                manual.setPetName(schedulingRequest.petName());
                manual.setTime(schedulingRequest.time());
                manual.setSchedulingObservations(schedulingRequest.schedulingObservations());
                manual.setDuration(schedulingRequest.duration());
                manual.setScheduleStatus(ScheduleStatus.SCHEDULED);
                manual.setPrice(schedulingRequest.price());

                List<SchedulingProtocol> manualProtocols = new ArrayList<>();
                if (schedulingRequest.protocolIds() != null) {
                    for (Long protocolId : schedulingRequest.protocolIds()) {
                        Protocol protocol = protocolById.get(protocolId);
                        if (protocol != null) {
                            SchedulingProtocol sp = new SchedulingProtocol();
                            sp.setScheduling(manual);
                            sp.setProtocolId(protocol.getId());
                            sp.setProtocolName(protocol.getName());
                            manualProtocols.add(sp);

                            remainingByProtocol.computeIfPresent(protocolId, (id, qty) -> qty - 1);
                        }
                    }
                }
                manual.setProtocols(manualProtocols);

                Scheduling savedManual = schedulingRepository.save(manual);
                schedulingResponses.add(toResponse(savedManual));

                // === 2) Cria os FUTUROS (ciclos 2, 3, 4...) ===
                int agendamentosFuturos = totalAgendamentos - 1;
                LocalDateTime currSchedule = schedulingRequest.time();

                for (int i = 1; i <= agendamentosFuturos; i++) {

                    Scheduling scheduling = new Scheduling();
                    scheduling.setPackId(pack.getId());
                    scheduling.setPackCycle(i + 1);
                    scheduling.setCustomerId(schedulingRequest.customerId());
                    scheduling.setCustomerName(schedulingRequest.customerName());
                    scheduling.setPetId(schedulingRequest.petId());
                    scheduling.setPetName(schedulingRequest.petName());

                    currSchedule = currSchedule.plusDays(intervaloDias);
                    scheduling.setTime(currSchedule);
                    scheduling.setSchedulingObservations(schedulingRequest.schedulingObservations());
                    scheduling.setDuration(schedulingRequest.duration());
                    scheduling.setScheduleStatus(ScheduleStatus.SCHEDULED);
                    scheduling.setPrice(schedulingRequest.price());

                    List<SchedulingProtocol> schedulingProtocols = new ArrayList<>();

                    for (Map.Entry<Long, Integer> entry : remainingByProtocol.entrySet()) {
                        Long pId = entry.getKey();
                        int remaining = entry.getValue();

                        int slotsRestantes = (agendamentosFuturos - i) + 1;

                        if (remaining > 0 && (remaining * 1.0 / slotsRestantes) >= 0.35) {
                            Protocol protocol = protocolById.get(pId);

                            SchedulingProtocol sp = new SchedulingProtocol();
                            sp.setScheduling(scheduling);
                            sp.setProtocolId(protocol.getId());
                            sp.setProtocolName(protocol.getName());
                            schedulingProtocols.add(sp);

                            remainingByProtocol.put(pId, remaining - 1);
                         }
                    }

                    scheduling.setProtocols(schedulingProtocols);
                    Scheduling saved = schedulingRepository.save(scheduling);
                    schedulingResponses.add(toResponse(saved));
                }

                return schedulingResponses;
        }

    @Transactional
    public List<SchedulingResponse> updateFutureFromPack(Long id, SchedulingRequest schedulingRequest) {

        Scheduling current = schedulingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Scheduling not found with id " + id));

        Long packId = current.getPackId();
        if (packId == null) {
            throw new IllegalStateException("Este agendamento não pertence a um pacote");
        }

        List<SchedulingResponse> responses = new ArrayList<>();

        // 1) Atualiza o próprio agendamento editado
        LocalDateTime oldStart = current.getTime(); // ← usado como referência de posição, ANTES de sobrescrever
        int oldDuration = current.getDuration() != null ? current.getDuration() : 60;
        LocalDateTime oldEnd = oldStart.plusMinutes(oldDuration);

        current.setTime(schedulingRequest.time());
        current.setDuration(schedulingRequest.duration());
        current.setSchedulingObservations(schedulingRequest.schedulingObservations());
        current.setPrice(schedulingRequest.price());
        current.setPetId(schedulingRequest.petId());
        current.setPetName(schedulingRequest.petName());
        current.setCustomerId(schedulingRequest.customerId());
        current.setCustomerName(schedulingRequest.customerName());

        if (schedulingRequest.protocolIds() != null) {
            current.getProtocols().clear();
            List<Protocol> protocols = protocolRepository.findAllById(schedulingRequest.protocolIds());
            for (Protocol p : protocols) {
                SchedulingProtocol sp = new SchedulingProtocol();
                sp.setScheduling(current);
                sp.setProtocolId(p.getId());
                sp.setProtocolName(p.getName());
                current.getProtocols().add(sp);
            }
        }

        Scheduling savedCurrent = schedulingRepository.save(current);
        schedulingRepository.flush();

        updateAffectedSchedules(oldStart, oldEnd);
        LocalDateTime newStart = savedCurrent.getTime();
        LocalDateTime newEnd = newStart.plusMinutes(savedCurrent.getDuration());
        updateAffectedSchedules(newStart, newEnd);
        recalculateSingleIntercept(savedCurrent);

        responses.add(toResponse(savedCurrent));

        // 2) Propaga o novo dia da semana para quem vem depois dele NA SEQUÊNCIA ORIGINAL
        DayOfWeek novoDia = newStart.getDayOfWeek();

        List<Scheduling> futureSiblings = schedulingRepository
                .findByPackIdAndPetIdAndTimeAfterOrderByTimeAsc(packId, savedCurrent.getPetId(), oldStart);

        for (Scheduling sibling : futureSiblings) {
            if (sibling.getId().equals(savedCurrent.getId())) continue; // segurança extra, evita reprocessar o próprio
            if (sibling.getScheduleStatus() == ScheduleStatus.HAPPENED
                    || sibling.getScheduleStatus() == ScheduleStatus.CANCELED) {
                continue;
            }

            LocalDateTime siblingOldStart = sibling.getTime();
            int siblingOldDuration = sibling.getDuration() != null ? sibling.getDuration() : 60;
            LocalDateTime siblingOldEnd = siblingOldStart.plusMinutes(siblingOldDuration);

            LocalDateTime siblingNewStart = siblingOldStart.with(ChronoField.DAY_OF_WEEK, novoDia.getValue());

            sibling.setTime(siblingNewStart);
            sibling.setDuration(schedulingRequest.duration());
            sibling.setSchedulingObservations(schedulingRequest.schedulingObservations());

            Scheduling savedSibling = schedulingRepository.save(sibling);
            LocalDateTime siblingNewEnd = siblingNewStart.plusMinutes(savedSibling.getDuration());
            updateAffectedSchedules(siblingOldStart, siblingOldEnd);
            updateAffectedSchedules(siblingNewStart, siblingNewEnd);
            recalculateSingleIntercept(savedSibling);

            responses.add(toResponse(savedSibling));
        }

        return responses;
    }
}
