package pl.edu.pw.elka.bd2.models;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import pl.edu.pw.elka.bd2.DBManager;
import pl.edu.pw.elka.bd2.Task;

public class Brand {

	private String brand;

	public Brand() {

	}

	public boolean insert() throws SQLException {
		return DBManager.executeTask(new Task<Boolean>() {
			public Boolean execute(PreparedStatement ps) throws Exception {
				ps.setString(1, brand);
				return ps.executeUpdate() > 0;
			}
		}, "insert into brands (brand) values (?)");
	}

	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("Brand: " + brand);

		return str.toString();
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

}
