package com.ims.stockmanagement.services;

import com.ims.stockmanagement.dtos.Response;
import com.ims.stockmanagement.dtos.SupplierDTO;
import com.ims.stockmanagement.exceptions.AlreadyExistsException;
import com.ims.stockmanagement.exceptions.NotFoundException;
import com.ims.stockmanagement.models.Supplier;
import com.ims.stockmanagement.repositories.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final ModelMapper modelMapper;

    /**
     * Tüm tedarikçileri listele (READ - CRUD)
     */
    public Response getAllSuppliers() {
        List<Supplier> suppliers = supplierRepository.findAll();
        List<SupplierDTO> supplierDTOs = suppliers.stream()
                .map(supplier -> modelMapper.map(supplier, SupplierDTO.class))
                .collect(Collectors.toList());

        return Response.builder()
                .statusCode(200)
                .message("Suppliers retrieved successfully")
                .supplierList(supplierDTOs)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * ID'ye göre tedarikçi getir (READ - CRUD)
     */
    public Response getSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Supplier not found with id: " + id));

        SupplierDTO supplierDTO = modelMapper.map(supplier, SupplierDTO.class);

        return Response.builder()
                .statusCode(200)
                .message("Supplier retrieved successfully")
                .supplier(supplierDTO)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Yeni tedarikçi oluştur (CREATE - CRUD)
     * Security: Controlled at controller level (ADMIN only)
     */
    @Transactional
    public Response createSupplier(SupplierDTO supplierDTO) {
        // Email kontrolü
        if (supplierDTO.getEmail() != null && supplierRepository.existsByEmail(supplierDTO.getEmail())) {
            throw new AlreadyExistsException("Supplier already exists with email: " + supplierDTO.getEmail());
        }

        Supplier supplier = modelMapper.map(supplierDTO, Supplier.class);
        Supplier savedSupplier = supplierRepository.save(supplier);
        SupplierDTO savedSupplierDTO = modelMapper.map(savedSupplier, SupplierDTO.class);

        return Response.builder()
                .statusCode(201)
                .message("Supplier created successfully")
                .supplier(savedSupplierDTO)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Tedarikçi güncelle (UPDATE - CRUD)
     * Security: Controlled at controller level (ADMIN only)
     */
    @Transactional
    public Response updateSupplier(Long id, SupplierDTO supplierDTO) {
        Supplier existingSupplier = supplierRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Supplier not found with id: " + id));

        // Email değiştiyse ve başka biri kullanıyorsa hata ver (null-safe check)
        String existingEmail = existingSupplier.getEmail();
        String newEmail = supplierDTO.getEmail();
        boolean emailChanged = (newEmail != null && !newEmail.equals(existingEmail)) ||
                               (newEmail == null && existingEmail != null);

        if (newEmail != null && emailChanged && supplierRepository.existsByEmail(newEmail)) {
            throw new AlreadyExistsException("Supplier already exists with email: " + newEmail);
        }

        existingSupplier.setName(supplierDTO.getName());
        existingSupplier.setEmail(supplierDTO.getEmail());
        existingSupplier.setPhone(supplierDTO.getPhone());
        existingSupplier.setAddress(supplierDTO.getAddress());
        existingSupplier.setDescription(supplierDTO.getDescription());

        Supplier updatedSupplier = supplierRepository.save(existingSupplier);
        SupplierDTO updatedSupplierDTO = modelMapper.map(updatedSupplier, SupplierDTO.class);

        return Response.builder()
                .statusCode(200)
                .message("Supplier updated successfully")
                .supplier(updatedSupplierDTO)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Tedarikçi sil (DELETE - CRUD)
     * Security: Controlled at controller level (ADMIN only)
     * This prevents accidental deletion of suppliers that have products
     */
    @Transactional
    public Response deleteSupplier(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Supplier not found with id: " + id));

        // Check if supplier has products
        if (!supplier.getProducts().isEmpty()) {
            throw new IllegalStateException("Cannot delete supplier with existing products. " +
                    "Supplier has " + supplier.getProducts().size() + " product(s).");
        }

        supplierRepository.delete(supplier);

        return Response.builder()
                .statusCode(200)
                .message("Supplier deleted successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * İsme göre tedarikçi ara
     */
    public Response searchSuppliersByName(String name) {
        List<Supplier> suppliers = supplierRepository.findByNameContainingIgnoreCase(name);
        List<SupplierDTO> supplierDTOs = suppliers.stream()
                .map(supplier -> modelMapper.map(supplier, SupplierDTO.class))
                .collect(Collectors.toList());

        return Response.builder()
                .statusCode(200)
                .message("Suppliers found: " + supplierDTOs.size())
                .supplierList(supplierDTOs)
                .timestamp(LocalDateTime.now())
                .build();
    }
}

