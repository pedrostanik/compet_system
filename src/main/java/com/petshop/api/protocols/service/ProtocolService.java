package com.petshop.api.protocols.service;

import com.petshop.api.protocols.domain.Protocol;
import com.petshop.api.protocols.dto.ProtocolRequest;
import com.petshop.api.protocols.dto.ProtocolResponse;
import com.petshop.api.protocols.repository.ProtocolRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProtocolService {

    final ProtocolRepository protocolRepository;

    public ProtocolResponse createProtocol(ProtocolRequest request) {
        Protocol protocol = new Protocol();
        protocol.setName(request.name());
        protocol.setDescription(request.description());
        return toResponse(protocolRepository.save(protocol));
    }

    public List<ProtocolResponse> listProtocols() {
        return protocolRepository.findAll().stream()
                .map(this::toResponse).toList();
    }

    public ProtocolResponse getProtocol(Long id) {
        Protocol protocol = protocolRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Scheduling not found with id " + id));
        return toResponse(protocol);
    }

    public ProtocolResponse updateProtocol(Long id, ProtocolRequest protocolRequest) {

        Protocol protocol = protocolRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Scheduling not found with id " + id));

        protocol.setName(protocolRequest.name());
        protocol.setDescription(protocolRequest.description());

        return toResponse(protocolRepository.save(protocol));

    }

    public void deleteProtocol(Long id) {
        protocolRepository.deleteById(id);
    }

    public ProtocolResponse toResponse(Protocol p) {
        return new ProtocolResponse(
                p.getId(),
                p.getName(),
                p.getDescription()
        );
    }
}
