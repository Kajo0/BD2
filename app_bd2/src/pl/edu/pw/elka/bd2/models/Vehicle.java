package pl.edu.pw.elka.bd2.models;

import java.util.Date;

public class Vehicle {

	private int clientId;
	private int vehicleId;
	private String vinNumber;
	private Date productionDate;
	private String type;
	private String brand;
	private String registration;

	public Vehicle() {

	}

	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("VehicleId: " + vehicleId);
		str.append("\tClientId: " + clientId);
		str.append("\tVIN: " + vinNumber);
		str.append("\tProduction: " + productionDate);
		str.append("\tType: " + type);
		str.append("\tBrand: " + brand);

		return str.toString();
	}

	public int getClientId() {
		return clientId;
	}

	public void setClientId(int clientId) {
		this.clientId = clientId;
	}

	public int getVehicleId() {
		return vehicleId;
	}

	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}

	public String getVinNumber() {
		return vinNumber;
	}

	public void setVinNumber(String vinNumber) {
		this.vinNumber = vinNumber;
	}

	public Date getProductionDate() {
		return productionDate;
	}

	public void setProductionDate(Date productionDate) {
		this.productionDate = productionDate;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getRegistration() {
		return registration;
	}

	public void setRegistration(String registration) {
		this.registration = registration;
	}

}
