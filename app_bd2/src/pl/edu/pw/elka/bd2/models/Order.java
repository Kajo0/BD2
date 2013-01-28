package pl.edu.pw.elka.bd2.models;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

import pl.edu.pw.elka.bd2.DBManager;
import pl.edu.pw.elka.bd2.Task;

public class Order {

	private int clientId;
	private String serviceType;
	private int vehicleId;
	private int orderId;
	private Date orderDate;
	private float value;
	private String note;

	public Order() {

	}

	public boolean insert() throws SQLException {
		return DBManager
				.executeTask(
						new Task<Boolean>() {
							public Boolean execute(PreparedStatement ps)
									throws Exception {
								ps.setInt(1, clientId);
								ps.setString(2, serviceType);
								ps.setInt(3, vehicleId);
								ps.setInt(4, orderId);
								ps.setDate(5,
										new java.sql.Date(orderDate.getTime()));
								ps.setFloat(6, value);
								ps.setString(7, note == null ? "" : note);
								return ps.executeUpdate() > 0;
							}
						},
						"insert into orders (client_id, service_type, vehicle_id, order_id, order_date, value, note) values (?, ?, ?, ?, ?, ?, ?)");
	}

	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("OrderId: " + orderId);
		str.append("\tClientId: " + clientId);
		str.append("\tVehicleId: " + vehicleId);
		str.append("\tOrderDate: " + orderDate);
		str.append("\tServiceType:" + serviceType);
		str.append("\tValue: " + value);
		str.append("\tNote: " + note);

		return str.toString();
	}

	public int getClientId() {
		return clientId;
	}

	public void setClientId(int clientId) {
		this.clientId = clientId;
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public int getVehicleId() {
		return vehicleId;
	}

	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}

	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public Date getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}

	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		this.value = value;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

}
