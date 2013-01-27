package pl.edu.pw.elka.bd2.models;

public class Person extends Client {

	private String firstName;
	private String lastName;
	private String pesel;

	public Person() {

	}

	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("FirstName: " + firstName);
		str.append("\tLastName: " + lastName);
		str.append("\tPESEL: " + pesel);
		str.append("\t" + super.toString());

		return str.toString();
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

	public String getPesel() {
		return pesel;
	}

	public void setPesel(String pesel) {
		this.pesel = pesel;
	}

}
