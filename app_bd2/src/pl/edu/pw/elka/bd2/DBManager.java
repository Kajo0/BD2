package pl.edu.pw.elka.bd2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import pl.edu.pw.elka.bd2.models.Brand;
import pl.edu.pw.elka.bd2.models.Client;
import pl.edu.pw.elka.bd2.models.Company;
import pl.edu.pw.elka.bd2.models.Order;
import pl.edu.pw.elka.bd2.models.Person;
import pl.edu.pw.elka.bd2.models.ServiceType;
import pl.edu.pw.elka.bd2.models.Vehicle;

public class DBManager {
	private final static String url = "jdbc:oracle:thin:@//ikar.elka.pw.edu.pl:1521/elka.elka.pw.edu.pl";
	private final static String username = "mmarkiew";
	private final static String password = "mmarkiew";

	static {
		try {
			Class.forName("oracle.jdbc.OracleDriver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Nie udało się podłączyć do bazy. Spróbuj ponownie :)");
			System.exit(1);
		}
	}

	public static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(url, username, password);
	}

	public static <R> R executeTask(Task<R> task, String sql, Connection connection, String[] columns)
			throws SQLException {
		R result = null;
		Connection conn = null;

		try {
			conn = connection;

			PreparedStatement ps = null;
			try {
				ps = conn.prepareStatement(sql, columns);
				result = task.execute(ps);
//				conn.commit();

				return result;
			} catch (Exception ex) {
				System.err.println("Cannot execute a statement : "
						+ ex.getMessage());
//				conn.rollback();

				throw new RuntimeException(ex);
			} finally {
				if (ps != null)
					ps.close();
			}
		} catch (Exception ex) {
			System.err.println("Cannot open a connection : " + ex.getMessage());

			throw new RuntimeException(ex);
		} finally {
			try {
//				if (conn != null)
//					conn.close();
			} catch (Exception ignore) {
			}
		}
	}

	public static <R> R executeTask(Task<R> task, String sql)
			throws SQLException {
		R result = null;
		Connection conn = null;

		try {
			conn = getConnection();

			PreparedStatement ps = null;
			try {
				ps = conn.prepareStatement(sql);
				result = task.execute(ps);
				conn.commit();

				return result;
			} catch (Exception ex) {
				System.err.println("Cannot execute a statement : "
						+ ex.getMessage());
				conn.rollback();

				throw new RuntimeException(ex);
			} finally {
				if (ps != null)
					ps.close();
			}
		} catch (Exception ex) {
			System.err.println("Cannot open a connection : " + ex.getMessage());

			throw new RuntimeException(ex);
		} finally {
			try {
				if (conn != null)
					conn.close();
			} catch (Exception ignore) {
			}
		}
	}

	public static <R, B> List<B> run(Query query, ResultSetToBean<B> converter,
			String sql) {
		Connection conn = null;

		try {
			conn = getConnection();

			List<B> list = new ArrayList<B>();
			PreparedStatement stmt = null;

			try {
				stmt = conn.prepareStatement(sql);
				query.prepareQuery(stmt);
				ResultSet rs = stmt.executeQuery();

				try {
					while (rs.next()) {
						list.add(converter.convert(rs));
					}

					return list;
				} catch (Exception ex) {
					System.err.println("Cannot convert bean : "
							+ ex.getMessage());

					throw new RuntimeException(ex);
				} finally {
					rs.close();
				}
			} catch (Exception ex) {
				System.err.println("Cannot execute a statement : "
						+ ex.getMessage());

				throw new RuntimeException(ex);
			} finally {
				if (stmt != null)
					stmt.close();
			}
		} catch (Exception ex) {
			System.err.println("Cannot open a connection : " + ex.getMessage());

			throw new RuntimeException(ex);
		} finally {
			try {
				if (conn != null)
					conn.close();
			} catch (Exception ignore) {
			}
		}
	}

	/************************
	 * Converters 2 Beans *
	 ************************/

	public final static ResultSetToBean<Brand> brandConverter = new ResultSetToBean<Brand>() {
		public Brand convert(ResultSet rs) throws Exception {
			Brand e = new Brand();
			e.setBrand(rs.getString("brand"));

			return e;
		}
	};

	public final static ResultSetToBean<Order> orderConverter = new ResultSetToBean<Order>() {
		public Order convert(ResultSet rs) throws Exception {
			Order e = new Order();
			e.setClientId(rs.getInt("client_id"));
			e.setVehicleId(rs.getInt("vehicle_id"));
			e.setOrderId(rs.getInt("order_id"));
			e.setServiceId(rs.getInt("service_id"));
			e.setNote(rs.getString("note"));
			e.setOrderDate(rs.getDate("order_date"));
			e.setServiceType(rs.getString("service_type"));
			e.setValue(rs.getFloat("value"));

			return e;
		}
	};

	public final static ResultSetToBean<Vehicle> vehicleConverter = new ResultSetToBean<Vehicle>() {
		public Vehicle convert(ResultSet rs) throws Exception {
			Vehicle e = new Vehicle();
			e.setClientId(rs.getInt("client_id"));
			e.setVehicleId(rs.getInt("vehicle_id"));
			e.setVinNumber(rs.getString("vin_number"));
			e.setType(rs.getString("type"));
			e.setProductionDate(rs.getDate("production_date"));
			e.setBrand(rs.getString("brand"));

			return e;
		}
	};

	public final static ResultSetToBean<ServiceType> serviceTypeConverter = new ResultSetToBean<ServiceType>() {
		public ServiceType convert(ResultSet rs) throws Exception {
			ServiceType e = new ServiceType();
			e.setServiceType(rs.getString("service_type"));
			e.setDefaultCharge(rs.getFloat("default_charge"));

			return e;
		}
	};

	public final static ResultSetToBean<Client> clientConverter = new ResultSetToBean<Client>() {
		public Client convert(ResultSet rs) throws Exception {
			Client e = new Client();
			e.setClientId(rs.getInt("client_id"));
			e.setAdditionalAddress(rs.getString("additional_address"));
			e.setStreet(rs.getString("street"));
			e.setBuildingNumber(rs.getInt("building_number"));
			e.setApartmentNumber(rs.getInt("apartment_number"));
			e.setPostalCode(rs.getString("postal_code"));
			e.setCity(rs.getString("city"));
			e.setPhoneNumber(rs.getString("phone_number"));
			e.setEmail(rs.getString("email"));

			e.setFirstName(rs.getString("first_name"));
			e.setLastName(rs.getString("last_name"));
			e.setPesel(rs.getLong("pesel"));

			e.setName(rs.getString("name"));
			e.setNip(rs.getLong("nip"));

			return e;
		}
	};

	public final static ResultSetToBean<Person> personConverter = new ResultSetToBean<Person>() {
		public Person convert(ResultSet rs) throws Exception {
			Person e = new Person();
			e.setFirstName(rs.getString("first_name"));
			e.setLastName(rs.getString("last_name"));
			e.setPesel(rs.getLong("pesel"));
			e.setClientId(rs.getInt("client_id"));
			e.setAdditionalAddress(rs.getString("additional_address"));
			e.setStreet(rs.getString("street"));
			e.setBuildingNumber(rs.getInt("building_number"));
			e.setApartmentNumber(rs.getInt("apartment_number"));
			e.setPostalCode(rs.getString("postal_code"));
			e.setCity(rs.getString("city"));
			e.setPhoneNumber(rs.getString("phone_number"));
			e.setEmail(rs.getString("email"));

			return e;
		}
	};
	
	public final static ResultSetToBean<Company> companyConverter = new ResultSetToBean<Company>() {
		public Company convert(ResultSet rs) throws Exception {
			Company e = new Company();
			e.setName(rs.getString("name"));
			e.setNip(rs.getLong("nip"));
			e.setClientId(rs.getInt("client_id"));
			e.setAdditionalAddress(rs.getString("additional_address"));
			e.setStreet(rs.getString("street"));
			e.setBuildingNumber(rs.getInt("building_number"));
			e.setApartmentNumber(rs.getInt("apartment_number"));
			e.setPostalCode(rs.getString("postal_code"));
			e.setCity(rs.getString("city"));
			e.setPhoneNumber(rs.getString("phone_number"));
			e.setEmail(rs.getString("email"));
			
			return e;
		}
	};

}
