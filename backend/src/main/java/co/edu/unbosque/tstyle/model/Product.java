package co.edu.unbosque.tstyle.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * 
 * Representa un producto del catalogo: es unico por proveedor
 * e identificador externo, nunca por titulo
 * 
 * @author Sebastián
 */
@Entity
@Table(name = "product", uniqueConstraints = @UniqueConstraint(
		name = "uk_product_provider_external",
		columnNames = { "provider_id", "external_id" }))
public class Product {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "provider_id", nullable = false)
	private Provider provider;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;
	
	@Column(name = "external_id", nullable = false, length = 100)
	private String externalId;
	
	@Column(name = "name", nullable = false, length = 200)
	private String name;
	
	@Column(name = "description", length = 2000)
	private String description;
	
	@Column(name = "image_url", nullable = false, length = 500)
	private String imageUrl;
	
	@Column(name = "price", nullable = false, precision = 12, scale = 2)
	private BigDecimal price;
	
	@Column(name = "currency", nullable = false, length = 3)
	private String currency;
	
	@Column(name = "external_url", nullable = false, length = 500)
	private String externalUrl;
	
	/** Es null cuando el proveedor no informa disponibilidad **/
	@Column(name = "provider_avalaible")
	private Boolean providerAvalaible;
	
	@Column(name = "last_synced_at", nullable = false)
	private LocalDateTime lastSyncedAt;
	
	protected Product() {
		
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Provider getProvider() {
		return provider;
	}

	public void setProvider(Provider provider) {
		this.provider = provider;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getExternalUrl() {
		return externalUrl;
	}

	public void setExternalUrl(String externalUrl) {
		this.externalUrl = externalUrl;
	}

	public Boolean getProviderAvalaible() {
		return providerAvalaible;
	}

	public void setProviderAvalaible(Boolean providerAvalaible) {
		this.providerAvalaible = providerAvalaible;
	}

	public LocalDateTime getLastSyncedAt() {
		return lastSyncedAt;
	}

	public void setLastSyncedAt(LocalDateTime lastSyncedAt) {
		this.lastSyncedAt = lastSyncedAt;
	}
	
	
	
}
