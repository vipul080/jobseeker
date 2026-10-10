package com.jobseeker;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:context", "jobs.remotive.enabled=false"})
class JobseekerApplicationTests {

	@Test
	void contextLoads() {
	}

}
