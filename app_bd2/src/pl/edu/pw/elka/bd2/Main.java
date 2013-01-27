package pl.edu.pw.elka.bd2;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import pl.edu.pw.elka.bd2.models.Brand;
import pl.edu.pw.elka.bd2.models.Client;
import pl.edu.pw.elka.bd2.models.Company;
import pl.edu.pw.elka.bd2.models.Order;
import pl.edu.pw.elka.bd2.models.Person;
import pl.edu.pw.elka.bd2.models.ServiceType;
import pl.edu.pw.elka.bd2.views.AppWindow;

public class Main {

	public static void main(String[] args) {
		System.out.println("Hello BD2!");

		new AppWindow();
		// List<Brand> brands = DBManager.run(new Query() {
		// public void prepareQuery(PreparedStatement ps) throws Exception {
		// }
		// }, DBManager.brandConverter, "select * from brands");
		//
		// for (Brand e : brands) {
		// System.out.println(e);
		// }

		// List<Company> serviceTypes = DBManager.run(new Query() {
		// public void prepareQuery(PreparedStatement ps) throws Exception {
		// }
		// }, DBManager.companyConverter, "select * from clients");
		//
		// for (Company e : serviceTypes) {
		// System.out.println(e);
		// }

		System.out.println("Bye BD2!");
	}

}
