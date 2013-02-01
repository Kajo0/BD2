package pl.edu.pw.elka.bd2.models;

public class VehicleVersion {

	private int vehicleVersionId;
	private String brand;
	private String model;

	public VehicleVersion() {

	}

	public int getVehicleVersionId() {
		return vehicleVersionId;
	}

	public void setVehicleVersionId(int vehicleVersionId) {
		this.vehicleVersionId = vehicleVersionId;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

}
