package com.petshop.api.protocols.service;

import com.petshop.api.protocols.domain.Protocol;
import com.petshop.api.protocols.dto.ProtocolRequest;
import com.petshop.api.protocols.dto.ProtocolResponse;
import com.petshop.api.protocols.repository.ProtocolRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProtocolService {

    private static final String NOT_FOUND_MESSAGE = "Protocol not found with id ";

    private final ProtocolRepository protocolRepository;

    public ProtocolResponse createProtocol(ProtocolRequest request) {
        Protocol protocol = new Protocol();
        applyRequest(protocol, request);
        return toResponse(protocolRepository.save(protocol));
    }

    @Transactional(readOnly = true)
    public List<ProtocolResponse> listProtocols() {
        return protocolRepository.findAll().stream()
                .map(ProtocolService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProtocolResponse getProtocol(Long id) {
        return toResponse(findByIdOrThrow(id));
    }

    public ProtocolResponse updateProtocol(Long id, ProtocolRequest request) {
        Protocol protocol = findByIdOrThrow(id);
        applyRequest(protocol, request);
        return toResponse(protocolRepository.save(protocol));
    }

    public void deleteProtocol(Long id) {
        if (!protocolRepository.existsById(id)) {
            throw new EntityNotFoundException(NOT_FOUND_MESSAGE + id);
        }
        protocolRepository.deleteById(id);
    }

    private Protocol findByIdOrThrow(Long id) {
        return protocolRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE + id));
    }

    private void applyRequest(Protocol protocol, ProtocolRequest request) {
        protocol.setName(request.name());
        protocol.setDescription(request.description());
    }

    private static ProtocolResponse toResponse(Protocol p) {
        return new ProtocolResponse(
                p.getId(),
                p.getName(),
                p.getDescription()
        );
    }
}