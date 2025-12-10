package com.ims.stockmanagement.controllers;

import com.ims.stockmanagement.dtos.Response;
import com.ims.stockmanagement.dtos.SupplierDTO;
import com.ims.stockmanagement.services.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> createSupplier(@Valid @RequestBody SupplierDTO supplierDTO) {
        Response response = supplierService.createSupplier(supplierDTO);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Response> getAllSuppliers() {
        Response response = supplierService.getAllSuppliers();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Response> getSupplierById(@PathVariable Long id) {
        Response response = supplierService.getSupplierById(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> updateSupplier(@PathVariable Long id, @Valid @RequestBody SupplierDTO supplierDTO) {
        Response response = supplierService.updateSupplier(id, supplierDTO);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> deleteSupplier(@PathVariable Long id) {
        Response response = supplierService.deleteSupplier(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
