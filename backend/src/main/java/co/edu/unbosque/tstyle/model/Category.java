package co.edu.unbosque.tstyle.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 
 * Representa una categoria de productos del catalogo
 * 
 * @author Sebastián
 */
@Entity
@Table(name = "category")
public class Category {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(name = "name", nullable = false, unique = true, length = 100)
	private String name;
	
	protected Category() {
		
	}
	
	/**
	 * 
	 * Crea una categoria
	 * 
	 * @param name nombre unico de la categoria
	 */
	
	public Category(String name) {
		this.name = name;
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
		public void setName(String name) {
			this.name = name;
		}
}
