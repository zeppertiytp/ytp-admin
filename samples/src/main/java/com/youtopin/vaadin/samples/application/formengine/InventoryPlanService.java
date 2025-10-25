package com.youtopin.vaadin.samples.application.formengine;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Service;

import com.youtopin.vaadin.samples.application.formengine.model.InventoryManagementFormData;

/**
 * Application service providing inventory plan data for the form engine samples.
 */
@Service
public class InventoryPlanService {

    private final AtomicReference<InventoryManagementFormData> store = new AtomicReference<>(seed());

    public InventoryManagementFormData load() {
        return InventoryManagementFormData.copyOf(store.get());
    }

    public void save(InventoryManagementFormData formData) {
        store.set(InventoryManagementFormData.copyOf(formData));
    }

    private InventoryManagementFormData seed() {
        InventoryManagementFormData data = new InventoryManagementFormData();
        InventoryManagementFormData.Inventory inventory = data.getInventory();
        inventory.setCode("INV-2040-Q4");
        inventory.setName("دسته‌بندی تجهیزات پاییز");
        inventory.setOwner("Fatemeh Rastgar");
        inventory.setNotes("بازنگری لیست تجهیزات تیم توسعه برای فصل جدید.");

        InventoryManagementFormData.InventoryItem laptopBundle = new InventoryManagementFormData.InventoryItem();
        laptopBundle.setSku("KIT-001");
        laptopBundle.setDescription("کیت لپ‌تاپ توسعه‌دهندگان");
        laptopBundle.setQuantity(15);
        laptopBundle.setLocation("تهران");

        InventoryManagementFormData.InventoryItem monitorBundle = new InventoryManagementFormData.InventoryItem();
        monitorBundle.setSku("MON-27-UHD");
        monitorBundle.setDescription("مانیتور ۲۷ اینچ 4K");
        monitorBundle.setQuantity(10);
        monitorBundle.setLocation("اصفهان");

        inventory.setItems(List.of(laptopBundle, monitorBundle));
        return data;
    }
}
