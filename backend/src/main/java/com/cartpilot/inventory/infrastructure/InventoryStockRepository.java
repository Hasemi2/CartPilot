package com.cartpilot.inventory.infrastructure;

import java.util.UUID;

import com.cartpilot.inventory.domain.InventoryStock;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryStockRepository extends JpaRepository<InventoryStock, UUID> {
}
