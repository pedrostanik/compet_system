package com.petshop.api.packages.controller;

import com.petshop.api.packages.dto.PackRequest;
import com.petshop.api.packages.dto.PackResponse;
import com.petshop.api.packages.service.PackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pack")
@RequiredArgsConstructor
public class PackController {

    final PackService packService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PackResponse createPack(@RequestBody PackRequest packRequest) {
       return packService.createPack(packRequest);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<PackResponse> createPack() {
        return packService.findAllPacks();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PackResponse createPack(@PathVariable Long id) {
        return packService.findPack(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PackResponse updatePack(@PathVariable Long id, @RequestBody PackRequest packRequest) {
        return packService.updatePack(id, packRequest);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePack(@PathVariable Long id) {
        packService.deletePack(id);
    }
}
