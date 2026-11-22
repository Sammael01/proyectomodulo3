package co.edu.unbosque.tstyle.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Representa un proveedor de origen de los productos del catalogo
 *
 * @author Sebastián
 */

@Entity
@Table(name = "provider")
public class Provider {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "name", nullable = false, unique = true, length = 50)
	private String name;

	@Column(name = "active", nullable = false)
	private boolean active = true;

	protected Provider() {

	}

	/**
	 *
	 * Crea un proveedor activo
	 *
	 */

	public Provider(String name) {
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
