package com.petshop.api.packages.service;

import com.petshop.api.packages.domain.Pack;
import com.petshop.api.packages.domain.PackProtocol;
import com.petshop.api.packages.dto.*;
import com.petshop.api.packages.repository.PackRepository;
import com.petshop.api.protocols.domain.Protocol;
import com.petshop.api.protocols.repository.ProtocolRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PackService {

    final PackRepository packRepository;
    final ProtocolRepository protocolRepository;

    public PackResponse createPack(PackRequest request) {
        Pack pack = new Pack();
        pack.setName(request.name());
        pack.setFrequencia(request.frequencia());
        pack.getProtocols().addAll(buildProtocols(pack, request.protocols()));

        return toResponse(packRepository.save(pack));
    }

    public List<PackResponse> findAllPacks() {
        return packRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public PackResponse findPack(Long packId) {
        Pack pack = packRepository.findById(packId)
                .orElseThrow(() -> new EntityNotFoundException("Pack not found with id " + packId));

        return toResponse(pack);
    }

    public PackResponse updatePack(Long packId, PackRequest request) {
        Pack pack = packRepository.findById(packId)
                .orElseThrow(() -> new EntityNotFoundException("Pack not found with id " + packId));

        pack.setName(request.name());
        pack.setFrequencia(request.frequencia());

        // orphanRemoval=true no domain garante que os antigos serão deletados
        pack.getProtocols().clear();
        pack.getProtocols().addAll(buildProtocols(pack, request.protocols()));

        return toResponse(packRepository.save(pack));
    }

    public void deletePack(Long packId) {
        packRepository.deleteById(packId);
    }

    // --- helpers ---

    private List<PackProtocol> buildProtocols(Pack pack, List<PackProtocolRequest> items) {
        if (items == null) return new ArrayList<>();

        return items.stream().map(item -> {
            Protocol protocol = protocolRepository.findById(item.protocolId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Protocol not found with id " + item.protocolId()));

            PackProtocol pp = new PackProtocol();
            pp.setPack(pack);
            pp.setProtocol(protocol);
            pp.setQuantity(item.quantity());
            return pp;
        }).toList();
    }

    private PackResponse toResponse(Pack pack) {
        List<PackProtocolResponse> protocols = pack.getProtocols().stream()
                .map(pp -> new PackProtocolResponse(
                        pp.getId(),
                        pp.getProtocol().getId(),
                        pp.getProtocol().getName(),
                        pp.getProtocol().getDescription(),
                        pp.getQuantity()
                ))
                .toList();

        return new PackResponse(
                pack.getId(),
                pack.getName(),
                pack.getFrequencia(),
                protocols
        );
    }

    // No PackageCycleService


}