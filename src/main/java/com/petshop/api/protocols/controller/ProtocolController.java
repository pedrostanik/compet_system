package com.petshop.api.protocols.controller;

import com.petshop.api.protocols.dto.ProtocolRequest;
import com.petshop.api.protocols.dto.ProtocolResponse;
import com.petshop.api.protocols.service.ProtocolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/protocol")
@RequiredArgsConstructor
public class ProtocolController {

    final ProtocolService protocolService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProtocolResponse create(@RequestBody ProtocolRequest request) {
        return protocolService.createProtocol(request);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ProtocolResponse> listAll() {
        return protocolService.listProtocols();
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProtocolResponse findById(@PathVariable Long id, @RequestBody ProtocolRequest request) {
        return protocolService.updateProtocol(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        protocolService.deleteProtocol(id);
    }

}
