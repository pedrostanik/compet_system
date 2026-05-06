package com.petshop.api.schedule.service;

import com.petshop.api.protocols.domain.Protocol;
import com.petshop.api.protocols.repository.ProtocolRepository;
import com.petshop.api.schedule.domain.Scheduling;
import com.petshop.api.schedule.domain.SchedulingProtocol;
import com.petshop.api.schedule.dto.SchedulingProtocolResponse;
import com.petshop.api.schedule.dto.SchedulingRequest;
import com.petshop.api.schedule.dto.SchedulingResponse;
import com.petshop.api.schedule.repository.SchedulingRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SchedulingService {

    private final SchedulingRepository schedulingRepository;
    private final ProtocolRepository protocolRepository;

    public SchedulingResponse createScheduling(SchedulingRequest schedulingRequest) {
        Scheduling scheduling = new Scheduling();
        scheduling.setCustomerId(schedulingRequest.customerId());
        scheduling.setCustomerName(schedulingRequest.customerName());
        scheduling.setPetId(schedulingRequest.petId());
        scheduling.setPetName(schedulingRequest.petName());
        scheduling.setTime(schedulingRequest.time());
        scheduling.setSchedulingObservations(schedulingRequest.schedulingObservations());
        scheduling.setDuration(schedulingRequest.duration());
        scheduling.setScheduledHappened(false);

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
        s.setTime(schedulingRequest.time());
        s.setDuration(schedulingRequest.duration());
        s.setPetId(schedulingRequest.petId());
        s.setPetName(schedulingRequest.petName());
        s.setCustomerId(schedulingRequest.customerId());
        s.setCustomerName(schedulingRequest.customerName());
        s.setPackage(schedulingRequest.isPackage());

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
                s.getCustomerId(),
                s.getCustomerName(),
                s.getPetId(),
                s.getPetName(),
                s.getSchedulingObservations(),
                s.getTime(),
                s.getScheduledHappened(),
                s.getPackage(),
                s.getDuration(),
                s.getIntercepted(),
                protocols
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
    public SchedulingResponse markAsHappened(Long id) {
        Scheduling s = schedulingRepository.findById(id)
                .orElseThrow(() ->  new EntityNotFoundException("Scheduling not found with id " + id));

        s.setScheduledHappened(true);

        LocalDateTime start = s.getTime();
        LocalDateTime end = start.plusMinutes(s.getDuration());
        updateAffectedSchedules(start, end);
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
        if (Boolean.TRUE.equals(s.getScheduledHappened())) return;

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
}
