
package co.edu.unbosque.tstyle.config;

import java.sql.Connection;
import java.sql.DriverManager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseConnectionTest {

	@Test
	void shouldConnectToDatabase() throws Exception {

		String url = System.getenv("DB_URL");
		String username = System.getenv("DB_USERNAME");
		String password = System.getenv("DB_PASSWORD");

		assertNotNull(url, "Falta configurar DB_URL");
		assertNotNull(username, "Falta configurar DB_USERNAME");
		assertNotNull(password, "Falta configurar DB_PASSWORD");

		try (Connection connection = DriverManager.getConnection(url, username, password)) {

			assertNotNull(connection);
			assertTrue(connection.isValid(5));

			System.out.println("Conexion a MySQL exitosa");

		}
	}

	@Test
	void shouldListDatabaseTables() throws Exception {

		String url = System.getenv("DB_URL");
		String username = System.getenv("DB_USERNAME");
		String password = System.getenv("DB_PASSWORD");

		try (Connection connection = DriverManager.getConnection(url, username, password)) {

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

		String url = System.getenv("DB_URL");
		String username = System.getenv("DB_USERNAME");
		String password = System.getenv("DB_PASSWORD");

		try (Connection connection = DriverManager.getConnection(url, username, password)) {

			String[] tableNames = { "exnovia", "pareja", "relacion", "useraccount" };

			var metadata = connection.getMetaData();

			for (String tableName : tableNames) {

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

		String url = System.getenv("DB_URL");
		String username = System.getenv("DB_USERNAME");
		String password = System.getenv("DB_PASSWORD");

		try (Connection connection = DriverManager.getConnection(url, username, password)) {

			String[] tableNames = { "exnovia", "pareja", "relacion", "useraccount" };

			var metadata = connection.getMetaData();

			for (String tableName : tableNames) {

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
	void shouldInspectUserAccountStructure() throws Exception {

		String url = System.getenv("DB_URL");
		String username = System.getenv("DB_USERNAME");
		String password = System.getenv("DB_PASSWORD");

		try (Connection connection = DriverManager.getConnection(url, username, password)) {

			var metadata = connection.getMetaData();

			System.out.println("\nINDICES DE USERACCOUNT");

			try (var indexes = metadata.getIndexInfo(connection.getCatalog(), null, "useraccount", false, false)) {

				while (indexes.next()) {
					System.out.println("Indice: " + indexes.getString("INDEX_NAME") + " | Columna: "
							+ indexes.getString("COLUMN_NAME") + " | Unico: " + !indexes.getBoolean("NON_UNIQUE"));
				}
			}

			System.out.println("\nCLAVES FORANEAS DE USERACCOUNT");

			try (var foreignKeys = metadata.getImportedKeys(connection.getCatalog(), null, "useraccount")) {

				while (foreignKeys.next()) {
					System.out.println("Columna: " + foreignKeys.getString("FKCOLUMN_NAME") + " | Tabla relacionada: "
							+ foreignKeys.getString("PKTABLE_NAME") + " | Columna relacionada: "
							+ foreignKeys.getString("PKCOLUMN_NAME"));
				}
			}
		}
	}

	@Test
	void shouldInspectUserAccountData() throws Exception {

		String url = System.getenv("DB_URL");
		String username = System.getenv("DB_USERNAME");
		String password = System.getenv("DB_PASSWORD");

		try (Connection connection = DriverManager.getConnection(url, username, password)) {

			System.out.println("\nINFORMACION DE USERACCOUNT");

			String sqlRole = """
					SELECT COLUMN_TYPE
					FROM INFORMATION_SCHEMA.COLUMNS
					WHERE TABLE_SCHEMA = DATABASE()
					AND TABLE_NAME = 'useraccount'
					AND COLUMN_NAME = 'role'
					""";

			try (var statement = connection.prepareStatement(sqlRole); var result = statement.executeQuery()) {

				if (result.next()) {
					System.out.println("Valores permitidos para role: " + result.getString("COLUMN_TYPE"));
				}
			}

			String sqlCount = "SELECT COUNT(*) FROM useraccount";

			try (var statement = connection.prepareStatement(sqlCount); var result = statement.executeQuery()) {

				if (result.next()) {
					System.out.println("Cantidad de registros: " + result.getInt(1));
				}
			}
		}
	}

	@Test
	void shouldInspectDatabasePermissions() throws Exception {

		String url = System.getenv("DB_URL");
		String username = System.getenv("DB_USERNAME");
		String password = System.getenv("DB_PASSWORD");

		try (Connection connection = DriverManager.getConnection(url, username, password)) {

			System.out.println("\nINFORMACION DE LA BASE DE DATOS");

			try (var statement = connection.createStatement();
					var result = statement.executeQuery("SELECT DATABASE(), CURRENT_USER(), USER()")) {

				if (result.next()) {
					System.out.println("Base de datos: " + result.getString(1));
					System.out.println("Usuario MySQL: " + result.getString(2));
					System.out.println("Conexion desde: " + result.getString(3));
				}
			}

			System.out.println("\nPERMISOS DISPONIBLES");

			try (var statement = connection.createStatement(); var result = statement.executeQuery("SHOW GRANTS")) {

				while (result.next()) {
					System.out.println(result.getString(1));
				}
			}
		}
	}
}
