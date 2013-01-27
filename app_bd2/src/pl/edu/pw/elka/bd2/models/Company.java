package pl.edu.pw.elka.bd2.models;

public class Company extends Client {

	private String name;
	private long nip;

	public Company() {

	}

	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("Name: " + name);
		str.append("\tNIP: " + nip);
		str.append("\t" + super.toString());

		return str.toString();
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
