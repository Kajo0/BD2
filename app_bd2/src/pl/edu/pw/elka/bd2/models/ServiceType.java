package pl.edu.pw.elka.bd2.models;

public class ServiceType {

	private String serviceType;
	private float defaultCharge;

	public ServiceType() {

	}

	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("ServiceType: " + serviceType);
		str.append("\tDefaultCharge: " + defaultCharge);

		return str.toString();
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public float getDefaultCharge() {
		return defaultCharge;
	}

	public void setDefaultCharge(float defaultCharge) {
		this.defaultCharge = defaultCharge;
	}

}
