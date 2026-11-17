package com.cartpilot;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("local-lite")
@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.NONE,
		properties = "spring.datasource.url=jdbc:h2:mem:cartpilot-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
class LocalLiteApplicationTest {

	@Autowired
	private DataSource dataSource;

	@Autowired
	private Environment environment;

	@Test
	void startsWithLocalLiteProfileAndH2() throws Exception {
		assertThat(environment.acceptsProfiles(Profiles.of("local-lite"))).isTrue();

		try (Connection connection = dataSource.getConnection();
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery("select 1")) {
			assertThat(connection.getMetaData().getDatabaseProductName()).isEqualTo("H2");
			assertThat(resultSet.next()).isTrue();
			assertThat(resultSet.getInt(1)).isEqualTo(1);
		}
	}
}

