package com.ims.stockmanagement.config;

import com.ims.stockmanagement.models.Category;
import com.ims.stockmanagement.models.Product;
import com.ims.stockmanagement.models.Supplier;
import com.ims.stockmanagement.repositories.CategoryRepository;
import com.ims.stockmanagement.repositories.ProductRepository;
import com.ims.stockmanagement.repositories.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
@RequiredArgsConstructor
public class TestDataInitializer {

    @Bean
    CommandLineRunner initTestData(
            CategoryRepository categoryRepository,
            SupplierRepository supplierRepository,
            ProductRepository productRepository
    ) {
        return args -> {
            // Eğer zaten veri varsa atla
            if (productRepository.count() > 0) {
                System.out.println("Test data already exists, skipping initialization.");
                return;
            }

            System.out.println("Initializing test data...");

            // Create Categories
            Category electronics = new Category();
            electronics.setName("Electronics");
            electronics.setDescription("Electronic devices and accessories");
            categoryRepository.save(electronics);

            Category computers = new Category();
            computers.setName("Computers");
            computers.setDescription("Computer hardware and software");
            categoryRepository.save(computers);

            Category accessories = new Category();
            accessories.setName("Accessories");
            accessories.setDescription("Computer accessories");
            categoryRepository.save(accessories);

            // Create Suppliers
            Supplier dell = new Supplier();
            dell.setName("Dell Inc.");
            dell.setDescription("Contact: John Doe - Dell authorized supplier");
            dell.setEmail("contact@dell.com");
            dell.setPhone("+1-555-1234");
            dell.setAddress("Round Rock, TX, USA");
            supplierRepository.save(dell);

            Supplier hp = new Supplier();
            hp.setName("HP Turkey");
            hp.setDescription("Contact: Jane Smith - HP Turkey distributor");
            hp.setEmail("info@hp.com.tr");
            hp.setPhone("+90-212-555-5678");
            hp.setAddress("Istanbul, Turkey");
            supplierRepository.save(hp);

            Supplier logitech = new Supplier();
            logitech.setName("Logitech");
            logitech.setDescription("Contact: Mike Johnson - Logitech official partner");
            logitech.setEmail("sales@logitech.com");
            logitech.setPhone("+1-555-9999");
            logitech.setAddress("Lausanne, Switzerland");
            supplierRepository.save(logitech);

            Supplier samsung = new Supplier();
            samsung.setName("Samsung Electronics");
            samsung.setDescription("Contact: Kim Lee - Samsung authorized dealer");
            samsung.setEmail("contact@samsung.com");
            samsung.setPhone("+82-2-555-1234");
            samsung.setAddress("Seoul, South Korea");
            supplierRepository.save(samsung);

            // Create Products with DIFFERENT suppliers
            Product laptop1 = new Product();
            laptop1.setName("Dell Latitude 5420");
            laptop1.setSku("DELL-LAP-5420");
            laptop1.setDescription("Business laptop with Intel i5");
            laptop1.setPrice(new BigDecimal("25000.00"));
            laptop1.setStockQuantity(15);
            laptop1.setReorderLevel(5);
            laptop1.setCategory(computers);
            laptop1.setSupplier(dell); // DELL
            productRepository.save(laptop1);

            Product laptop2 = new Product();
            laptop2.setName("HP EliteBook 840");
            laptop2.setSku("HP-ELITE-840");
            laptop2.setDescription("Premium business laptop");
            laptop2.setPrice(new BigDecimal("28000.00"));
            laptop2.setStockQuantity(12);
            laptop2.setReorderLevel(5);
            laptop2.setCategory(computers);
            laptop2.setSupplier(hp); // HP
            productRepository.save(laptop2);

            Product mouse = new Product();
            mouse.setName("Logitech MX Master 3");
            mouse.setSku("LOG-MX-MASTER3");
            mouse.setDescription("Wireless mouse for professionals");
            mouse.setPrice(new BigDecimal("1200.00"));
            mouse.setStockQuantity(8);
            mouse.setReorderLevel(10);
            mouse.setCategory(accessories);
            mouse.setSupplier(logitech); // LOGITECH
            productRepository.save(mouse);

            Product monitor = new Product();
            monitor.setName("Samsung 27\" 4K Monitor");
            monitor.setSku("SAM-MON-27-4K");
            monitor.setDescription("4K UHD display");
            monitor.setPrice(new BigDecimal("8500.00"));
            monitor.setStockQuantity(20);
            monitor.setReorderLevel(5);
            monitor.setCategory(electronics);
            monitor.setSupplier(samsung); // SAMSUNG
            productRepository.save(monitor);

            Product keyboard = new Product();
            keyboard.setName("Logitech K780 Keyboard");
            keyboard.setSku("LOG-K780");
            keyboard.setDescription("Multi-device wireless keyboard");
            keyboard.setPrice(new BigDecimal("850.00"));
            keyboard.setStockQuantity(4); // Low stock
            keyboard.setReorderLevel(10);
            keyboard.setCategory(accessories);
            keyboard.setSupplier(logitech); // LOGITECH
            productRepository.save(keyboard);

            Product dellMonitor = new Product();
            dellMonitor.setName("Dell UltraSharp U2720Q");
            dellMonitor.setSku("DELL-U2720Q");
            dellMonitor.setDescription("27-inch 4K USB-C monitor");
            dellMonitor.setPrice(new BigDecimal("12000.00"));
            dellMonitor.setStockQuantity(7);
            dellMonitor.setReorderLevel(5);
            dellMonitor.setCategory(electronics);
            dellMonitor.setSupplier(dell);
            productRepository.save(dellMonitor);

            System.out.println("Test data initialized successfully!");
            System.out.println("   - 3 Categories created");
            System.out.println("   - 4 Suppliers created");
            System.out.println("   - 6 Products created");
        };
    }
}

