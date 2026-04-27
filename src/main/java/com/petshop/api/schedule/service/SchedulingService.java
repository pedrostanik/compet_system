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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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


        return toResponse(schedulingRepository.save(scheduling));

    }

    public SchedulingResponse updateSchedulingTime(Long id, SchedulingRequest schedulingRequest) {
        Scheduling scheduling = schedulingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Scheduling not found with id " + id));
        scheduling.setTime(schedulingRequest.time());

        return toResponse(schedulingRepository.save(scheduling));

    }

    public void deleteScheduling(Long id) {
        if (!schedulingRepository.existsById(id)) {
            throw new EntityNotFoundException("Scheduling not found with id " + id);
        }
        schedulingRepository.deleteById(id);
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

    public SchedulingResponse markAsHappened(Long id) {
        Scheduling scheduling = schedulingRepository.findById(id)
                .orElseThrow(() ->  new EntityNotFoundException("Scheduling not found with id " + id));

        scheduling.setScheduledHappened(true);
        return toResponse(schedulingRepository.save(scheduling));
    }
}
