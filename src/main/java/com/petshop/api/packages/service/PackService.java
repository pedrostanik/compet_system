package com.petshop.api.packages.service;

import com.petshop.api.packages.domain.Pack;
import com.petshop.api.packages.domain.PackProtocol;
import com.petshop.api.packages.dto.PackProtocolResponse;
import com.petshop.api.packages.dto.PackRequest;
import com.petshop.api.packages.dto.PackResponse;
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

    public PackResponse createPack(PackRequest packRequest) {
        Pack pack = new Pack();
        pack.setCustomerId(packRequest.customerId());
        pack.setCustomerName(packRequest.customerName());
        pack.setPetId(packRequest.petId());
        pack.setPetName(packRequest.petName());

        if (packRequest.protocolIds() != null) {
            List<Protocol> protocols = protocolRepository.findAllById(packRequest.protocolIds());
            for (Protocol p : protocols) {
                PackProtocol pp = new PackProtocol();
                pp.setPack(pack);
                pp.setProtocolId(p.getId());
                pp.setProtocolName(p.getName());
                pp.setProtocolDescription(p.getDescription());
                pack.getProtocols().add(pp);
            }
        }
        return toResponse(packRepository.save(pack));
    }

    public List<PackResponse> findAllPacks() {

        return packRepository.findAll().stream()
                .map(this::toResponse).toList();

    }

    public PackResponse findPack(Long packId){
        Pack pack = packRepository.findById(packId).
                orElseThrow(() -> new EntityNotFoundException("Pack not found with id " + packId));

        return toResponse(pack);
    }

    public PackResponse updatePack(Long packId, PackRequest packRequest) {
        Pack pack = packRepository.findById(packId)
                .orElseThrow(() -> new EntityNotFoundException("Pack not found with id " + packId));

        pack.setPetId(packRequest.petId());
        pack.setPetName(packRequest.petName());
        pack.setCustomerId(packRequest.customerId());
        pack.setCustomerName(packRequest.customerName());

        List<PackProtocol> newProtocols = new ArrayList<>();

        if (packRequest.protocolIds() != null) {
            List<Protocol> protocols = protocolRepository.findAllById(packRequest.protocolIds());

            for (Protocol p : protocols) {
                PackProtocol pp = new PackProtocol();
                pp.setPack(pack);
                pp.setProtocolId(p.getId());
                pp.setProtocolName(p.getName());
                pp.setProtocolDescription(p.getDescription());
                newProtocols.add(pp);
            }
        }
        pack.getProtocols().clear();
        pack.getProtocols().addAll(newProtocols);
        return toResponse(packRepository.save(pack));
    }

    public void deletePack(Long packId) {
        packRepository.deleteById(packId);
    }

    public PackResponse toResponse(Pack p) {
        List<PackProtocolResponse> protocols = p.getProtocols().stream()
                .map(pp -> new PackProtocolResponse(
                        pp.getProtocolId(),
                        pp.getProtocolName(),
                        pp.getProtocolDescription()
                ))
                .toList();
        return new PackResponse(
                p.getId(),
                p.getPetId(),
                p.getPetName(),
                p.getCustomerId(),
                p.getCustomerName(),
                protocols
        );
    }
}
