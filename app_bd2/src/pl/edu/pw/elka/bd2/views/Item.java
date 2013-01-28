package pl.edu.pw.elka.bd2.views;

public class Item {

	private int id;
	private String description;
	private float floatThing = 0f;

	public Item(int id, String description) {
		this.id = id;
		this.description = description;
	}

	public Item(int id, String description, float floatThing) {
		this.id = id;
		this.description = description;
		this.floatThing = floatThing;
	}

	public int getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public Float getFloat() {
		return floatThing;
	}

	public String toString() {
		return description;
	}

}