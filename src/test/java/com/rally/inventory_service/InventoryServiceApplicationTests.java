package com.rally.inventory_service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Requires active PostgreSQL/Kafka instance - run as integration test only")
@SpringBootTest
class InventoryServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
