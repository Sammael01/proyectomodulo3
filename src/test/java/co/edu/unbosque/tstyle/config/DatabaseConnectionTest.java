package co.edu.unbosque.tstyle.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * Prueba de integración contra la base de datos MySQL real.
 *
 * Solo se ejecuta cuando existen las variables de entorno DB_URL,
 * DB_USERNAME y DB_PASSWORD. Sin ellas, JUnit la reporta como omitida
 * (skipped) y no rompe la ejecución de las pruebas unitarias.
 *
 * @author Equipo T Style
 */
@Tag("integration")
@EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
class DatabaseConnectionTest {

	/** Tablas que genera JPA a partir de las entidades del proyecto. */
	private static final String[] TABLE_NAMES = { "tstyle_users", "provider", "category", "product" };

	private Connection openConnection() throws SQLException {
		String url = System.getenv("DB_URL");
		String username = System.getenv("DB_USERNAME");
		String password = System.getenv("DB_PASSWORD");

		assertNotNull(username, "Falta configurar DB_USERNAME");
		assertNotNull(password, "Falta configurar DB_PASSWORD");

		return DriverManager.getConnection(url, username, password);
	}

	@Test
	void shouldConnectToDatabase() throws Exception {
		try (Connection connection = openConnection()) {
			assertNotNull(connection);
			assertTrue(connection.isValid(5));
			System.out.println("Conexion a MySQL exitosa");
		}
	}

	@Test
	void shouldListDatabaseTables() throws Exception {
		try (Connection connection = openConnection()) {
			var metadata = connection.getMetaData();
			try (var tables = metadata.getTables(connection.getCatalog(), null, "%", new String[] { "TABLE" })) {
				while (tables.next()) {
					System.out.println("Tabla encontrada: " + tables.getString("TABLE_NAME"));
				}
			}
		}
	}

	@Test
	void shouldDescribeDatabaseTables() throws Exception {
		try (Connection connection = openConnection()) {
			var metadata = connection.getMetaData();
			for (String tableName : TABLE_NAMES) {
				System.out.println("\nTabla: " + tableName);
				try (var columns = metadata.getColumns(connection.getCatalog(), null, tableName, "%")) {
					while (columns.next()) {
						System.out.println("Columna: " + columns.getString("COLUMN_NAME") + " | Tipo: "
								+ columns.getString("TYPE_NAME") + " | Tamaño: " + columns.getInt("COLUMN_SIZE")
								+ " | Nula: " + columns.getString("IS_NULLABLE"));
					}
				}
			}
		}
	}

	@Test
	void shouldListPrimaryKeys() throws Exception {
		try (Connection connection = openConnection()) {
			var metadata = connection.getMetaData();
			for (String tableName : TABLE_NAMES) {
				System.out.println("\nClaves primarias de: " + tableName);
				try (var keys = metadata.getPrimaryKeys(connection.getCatalog(), null, tableName)) {
					while (keys.next()) {
						System.out.println("Columna PK: " + keys.getString("COLUMN_NAME"));
					}
				}
			}
		}
	}

	@Test
	void shouldInspectUserTableStructure() throws Exception {
		try (Connection connection = openConnection()) {
			var metadata = connection.getMetaData();

			System.out.println("\nINDICES DE TSTYLE_USERS");
			try (var indexes = metadata.getIndexInfo(connection.getCatalog(), null, "tstyle_users", false, false)) {
				while (indexes.next()) {
					System.out.println("Indice: " + indexes.getString("INDEX_NAME") + " | Columna: "
							+ indexes.getString("COLUMN_NAME") + " | Unico: " + !indexes.getBoolean("NON_UNIQUE"));
				}
			}

			System.out.println("\nCLAVES FORANEAS DE PRODUCT");
			try (var foreignKeys = metadata.getImportedKeys(connection.getCatalog(), null, "product")) {
				while (foreignKeys.next()) {
					System.out.println("Columna: " + foreignKeys.getString("FKCOLUMN_NAME") + " | Tabla relacionada: "
							+ foreignKeys.getString("PKTABLE_NAME") + " | Columna relacionada: "
							+ foreignKeys.getString("PKCOLUMN_NAME"));
				}
			}
		}
	}

	@Test
	void shouldInspectDatabaseInformation() throws Exception {
		try (Connection connection = openConnection();
				var statement = connection.createStatement();
				var result = statement.executeQuery("SELECT DATABASE(), CURRENT_USER(), USER()")) {

			if (result.next()) {
				System.out.println("Base de datos: " + result.getString(1));
				System.out.println("Usuario MySQL: " + result.getString(2));
				System.out.println("Conexion desde: " + result.getString(3));
			}
		}
	}
}
