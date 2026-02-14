package com.htc.enter.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "ewt_client")
public class Client extends BaseEntity{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long client_id;
	private String name;
	private String email;
	private long phn_no;
	private String address;

	// Explicit getters/setters to ensure tooling recognizes them (some environments can't see Lombok-generated methods)
	public long getClient_id() { return client_id; }
	public void setClient_id(long client_id) { this.client_id = client_id; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }

	public long getPhn_no() { return phn_no; }
	public void setPhn_no(long phn_no) { this.phn_no = phn_no; }

	public String getAddress() { return address; }
	public void setAddress(String address) { this.address = address; }

	// Compatibility getter
	public Long getClientId() { return Long.valueOf(this.client_id); }

}