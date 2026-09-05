package com.rally.inventory_service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class InventoryServiceApplicationTests {

	@Test
	@Disabled("Requires active PostgreSQL/Kafka instance - run as integration test only")
	void contextLoads() {
	}

}
