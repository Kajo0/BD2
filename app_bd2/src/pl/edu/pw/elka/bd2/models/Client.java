package pl.edu.pw.elka.bd2.models;

public class Client {

	protected int clientId = -1;
	protected String additionalAddress;
	protected String street;
	protected int buildingNumber;
	protected int apartmentNumber;
	protected String postalCode;
	protected String city;
	protected String phoneNumber;
	protected String email;

	private String firstName;
	private String lastName;
	private long pesel;

	private String name;
	private long nip;

	public Client() {

	}

	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("ClientId: " + clientId);
		str.append("\tAdditionalAddress: " + additionalAddress);
		str.append("\tStreet: " + street);
		str.append("\tBuildingNumber: " + buildingNumber);
		str.append("\tApartmentNumber: " + apartmentNumber);
		str.append("\tPostalCode: " + postalCode);
		str.append("\tCity: " + city);
		str.append("\tPhoneNumber: " + phoneNumber);
		str.append("\tEmail: " + email);

		str.append("\tFirstName: " + firstName);
		str.append("\tLastName: " + lastName);
		str.append("\tPESEL: " + pesel);

		str.append("\tName: " + name);
		str.append("\tNIP: " + nip);

		return str.toString();
	}

	public int getClientId() {
		return clientId;
	}

	public void setClientId(int clientId) {
		this.clientId = clientId;
	}

	public String getAdditionalAddress() {
		return additionalAddress;
	}

	public void setAdditionalAddress(String additionalAddress) {
		this.additionalAddress = additionalAddress;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public int getBuildingNumber() {
		return buildingNumber;
	}

	public void setBuildingNumber(int buildingNumber) {
		this.buildingNumber = buildingNumber;
	}

	public int getApartmentNumber() {
		return apartmentNumber;
	}

	public void setApartmentNumber(int apartmentNumber) {
		this.apartmentNumber = apartmentNumber;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public long getPesel() {
		return pesel;
	}

	public void setPesel(long pesel) {
		this.pesel = pesel;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getNip() {
		return nip;
	}

	public void setNip(long nip) {
		this.nip = nip;
	}

}
