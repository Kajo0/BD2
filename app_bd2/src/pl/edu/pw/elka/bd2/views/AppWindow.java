package pl.edu.pw.elka.bd2.views;

import java.awt.BorderLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import pl.edu.pw.elka.bd2.DBManager;
import pl.edu.pw.elka.bd2.Query;
import pl.edu.pw.elka.bd2.Task;
import pl.edu.pw.elka.bd2.models.Brand;
import pl.edu.pw.elka.bd2.models.Client;
import pl.edu.pw.elka.bd2.models.Company;
import pl.edu.pw.elka.bd2.models.Order;
import pl.edu.pw.elka.bd2.models.Person;
import pl.edu.pw.elka.bd2.models.ServiceType;
import pl.edu.pw.elka.bd2.models.Vehicle;
import pl.edu.pw.elka.bd2.models.VehicleVersion;

@SuppressWarnings({ "serial", "rawtypes" })
public class AppWindow extends JFrame {

	public static Object tmp = null;
	private Client clientTmp = null;
	private Vehicle vehicleTmp = null;
	private Order order = null;

	private Connection connection = null;

	private static int STATE = 10;

	public static final int JUST_ADD_CLIENT = 0;
	public static final int JUST_SHOW_CLIENTS = 1;
	public static final int JUST_ADD_VEHICLE = 2;
	public static final int JUST_SHOW_VEHICLES = 3;

	public static final int ADD_ORDER_CHOOSE_CLIENT = 4;
	public static final int ADD_ORDER_ADD_CLIENT = 5;
	public static final int ADD_ORDER_CHOOSE_VEHICLE = 6;
	public static final int ADD_ORDER_ADD_VEHICLE = 7;
	public static final int ADD_ORDER_COST_THINGS = 8;
	public static final int ADD_ORDER_FINALIZATION = 9;

	public static final int SHOW_ORDERS = 10;

	/**
	 * Creates new form MainFrame
	 */
	@SuppressWarnings("unchecked")
	public AppWindow() {
		initComponents();

		setVisible(true);
		setResizable(false);
		setLocation(450, 150);

		this.topPanel.setLayout(new BorderLayout());
		this.leftPanel.setLayout(new BorderLayout());

		changeTopPanel(this.emptyTop);
		changeLeftPanel(this.orderTablePanel);

		this.companyPanel.setVisible(false);

		List<Brand> brands = DBManager.run(new Query() {
			public void prepareQuery(PreparedStatement ps) throws Exception {
			}
		}, DBManager.brandConverter, "select * from brand");

		LinkedList<String> brnds = new LinkedList<>();
		brnds.add("Dowolna");
		for (Brand e : brands)
			brnds.add(e.getBrand());

		findByBrandCombo.setModel(new javax.swing.DefaultComboBoxModel(brnds
				.toArray()));

		brnds.pollFirst();
		brand.setModel(new javax.swing.DefaultComboBoxModel(brnds.toArray()));

		changeState(STATE);
	}

	/************************
	 * DOWN TABLE FILLERS *
	 ************************/

	public void getOrdersToTable() {
		List<Order> orders = DBManager.run(new Query() {
			public void prepareQuery(PreparedStatement ps) throws Exception {
			}
		}, DBManager.orderConverter,
				"select * from order_t order by order_date desc");

		Object[][] data = new Object[orders.size()][orderTable.getModel()
				.getColumnCount()];

		int i = 0;
		for (Order o : orders) {
			data[i][0] = o.getOrderId();
			data[i][1] = o.getClientId();
			data[i][2] = o.getVehicleId();
			data[i][3] = o.getOrderDate();
			data[i][4] = o.getValue();
			data[i][5] = o.getNote();

			++i;
		}

		fillTable(orderTable, data);
	}

	public void getVehiclesToTable() {
		String sql = "select * from vehicle";

		if (STATE == ADD_ORDER_CHOOSE_VEHICLE || STATE == ADD_ORDER_ADD_VEHICLE
				|| STATE == ADD_ORDER_CHOOSE_CLIENT || STATE == ADD_ORDER_ADD_CLIENT) // due to invoke this before state changed
			sql += " where client_id = " + this.order.getClientId();

		List<Vehicle> vehicles = DBManager.run(new Query() {
			public void prepareQuery(PreparedStatement ps) throws Exception {
			}
		}, DBManager.vehicleConverter, sql);

		Object[][] data = new Object[vehicles.size()][vehicleTable.getModel()
				.getColumnCount()];

		int i = 0;
		for (Vehicle v : vehicles) {
			data[i][0] = v.getVehicleId();
			data[i][1] = v.getVinNumber();
			data[i][2] = v.getProductionDate();
			data[i][3] = v.getRegistration();

			final int vversion_id = v.getVversion_id();
			List<VehicleVersion> vversion = DBManager.run(new Query() {
				public void prepareQuery(PreparedStatement ps) throws Exception {
					ps.setInt(1, vversion_id);
				}
			}, DBManager.vehicleVersionConverter, "select * from vehicle_version where vversion_id = ?");

			data[i][4] = vversion.get(0).getBrand();

			++i;
		}

		fillTable(vehicleTable, data);
	}

	public void getClientsToTable() {
		List<Person> persons = DBManager
				.run(new Query() {
					public void prepareQuery(PreparedStatement ps)
							throws Exception {
					}
				}, DBManager.personConverter,
						"select * from client where pesel is not null order by client_id asc");

		List<Company> companies = DBManager
				.run(new Query() {
					public void prepareQuery(PreparedStatement ps)
							throws Exception {
					}
				}, DBManager.companyConverter,
						"select * from client where pesel is null order by client_id asc");

		Object[][] data = new Object[persons.size() + companies.size()][clientTable
				.getModel().getColumnCount()];

		List<Client> clients = new ArrayList<>();
		clients.addAll(persons);
		clients.addAll(companies);

		int i = 0;
		for (Client c : clients) {
			data[i][0] = c.getClientId();
			data[i][1] = "ul. " + c.getStreet() + " " + c.getBuildingNumber()
					+ "/" + c.getApartmentNumber() + ", "
					+ c.getPostalCode().substring(0, 2) + "-"
					+ c.getPostalCode().substring(2, 5) + " " + c.getCity();
			data[i][2] = c.getPhoneNumber();
			data[i][3] = c.getEmail();
			if (c instanceof Person) {
				data[i][4] = ((Person) c).getFirstName() + " "
						+ ((Person) c).getLastName();
				data[i][5] = ((Person) c).getPesel();
			} else {
				data[i][4] = ((Company) c).getName();
				data[i][5] = ((Company) c).getNip();
			}

			++i;
		}

		fillTable(clientTable, data);
	}

	public void fillTable(JTable table, Object[][] data) {
		DefaultTableModel dm = (DefaultTableModel) table.getModel();

		String[] columns = new String[dm.getColumnCount()];
		for (int i = 0; i < dm.getColumnCount(); ++i)
			columns[i] = dm.getColumnName(i);

		table.setModel(new DefaultTableModel(data, columns));
	}

	/************************
	 * UP TABLE FILLERS *
	 ************************/

	/************************************************
	 * DOWN WINDOWS & PANELS APP STATES & SWITCHES *
	 ************************************************/

	/**
	 * What is visible
	 * 
	 * @param nextState
	 */
	@SuppressWarnings("unchecked")
	public void changeState(int nextState) {
		switch (nextState) {
		case JUST_ADD_CLIENT:
			try {
				if (this.connection != null)
					this.connection.rollback();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			changeTopPanel(this.emptyTop);
			changeLeftPanel(this.addClientPanel);

			this.addClientButton.setVisible(false);
			this.addVehicleButton.setVisible(false);
			this.saveButton.setVisible(true);
			this.nextButton.setVisible(false);
			this.cancelButton.setVisible(false);

			break;
		case JUST_SHOW_CLIENTS:
			try {
				if (this.connection != null)
					this.connection.rollback();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			changeTopPanel(this.findClient);
			changeLeftPanel(this.clientTablePanel);

			this.addClientButton.setVisible(false);
			this.addVehicleButton.setVisible(false);
			this.saveButton.setVisible(false);
			this.nextButton.setVisible(false);
			this.cancelButton.setVisible(false);

			getClientsToTable();

			break;
		case JUST_ADD_VEHICLE:
			try {
				if (this.connection != null)
					this.connection.rollback();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			changeTopPanel(this.findClient);
			changeLeftPanel(this.addVehicle);

			this.addClientButton.setVisible(false);
			this.addVehicleButton.setVisible(false);
			this.saveButton.setVisible(true);
			this.nextButton.setVisible(false);
			this.cancelButton.setVisible(false);

			List<Client> clients = DBManager.run(new Query() {
				public void prepareQuery(PreparedStatement ps) throws Exception {
				}
			}, DBManager.clientConverter, "select * from client");

			this.client.removeAllItems();
			for (Client c : clients)
				this.client
						.addItem(new Item(c.getClientId(),
								(c.getFirstName() != null ? c.getFirstName()
										+ " " : "")
										+ (c.getLastName() != null ? c
												.getLastName() + " " : "")
										+ (c.getName() != null ? c.getName()
												: "")));

			break;
		case JUST_SHOW_VEHICLES:
			try {
				if (this.connection != null)
					this.connection.rollback();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			changeTopPanel(this.findVehicle);
			changeLeftPanel(this.vehicleTablePanel);

			this.addClientButton.setVisible(false);
			this.addVehicleButton.setVisible(false);
			this.saveButton.setVisible(false);
			this.nextButton.setVisible(false);
			this.cancelButton.setVisible(false);

			getVehiclesToTable();

			break;
		case SHOW_ORDERS:
			try {
				if (this.connection != null)
					this.connection.rollback();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			changeTopPanel(this.emptyTop);
			changeLeftPanel(this.orderTablePanel);

			this.addClientButton.setVisible(false);
			this.addVehicleButton.setVisible(false);
			this.saveButton.setVisible(false);
			this.nextButton.setVisible(false);
			this.cancelButton.setVisible(false);

			getOrdersToTable();

			break;
		case ADD_ORDER_CHOOSE_CLIENT:
			try {
				if (this.connection != null)
					this.connection.rollback();

				// Start transaction
				this.connection = DBManager.getConnection();
				this.connection.setAutoCommit(false);

				this.order = new Order();
				this.clientTmp = new Client();
				this.vehicleTmp = new Vehicle();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			changeTopPanel(this.findClient);
			changeLeftPanel(this.clientTablePanel);

			this.addClientButton.setVisible(true);
			this.addVehicleButton.setVisible(false);
			this.saveButton.setVisible(false);
			this.nextButton.setVisible(true);
			this.cancelButton.setVisible(false);

			getClientsToTable();

			break;
		case ADD_ORDER_ADD_CLIENT:
			changeTopPanel(this.emptyTop);
			changeLeftPanel(this.addClientPanel);

			this.addClientButton.setVisible(false);
			this.addVehicleButton.setVisible(false);
			this.saveButton.setVisible(true);
			this.nextButton.setVisible(false);
			this.cancelButton.setVisible(true);

			break;
		case ADD_ORDER_CHOOSE_VEHICLE:
			changeTopPanel(this.findVehicle);
			changeLeftPanel(this.vehicleTablePanel);

			this.addClientButton.setVisible(false);
			this.addVehicleButton.setVisible(true);
			this.saveButton.setVisible(false);
			this.nextButton.setVisible(true);
			this.cancelButton.setVisible(true);

			getVehiclesToTable();

			break;
		case ADD_ORDER_ADD_VEHICLE:
			changeTopPanel(this.emptyTop);
			changeLeftPanel(this.addVehicle);

			this.addClientButton.setVisible(false);
			this.addVehicleButton.setVisible(false);
			this.saveButton.setVisible(true);
			this.nextButton.setVisible(false);
			this.cancelButton.setVisible(true);

			clients = DBManager.run(
					new Query() {
						public void prepareQuery(PreparedStatement ps)
								throws Exception {
						}
					},
					DBManager.clientConverter,
					"select * from client where client_id = "
							+ this.order.getClientId());

			if (clients.size() == 0)
				clients.add(this.clientTmp);

			this.client.removeAllItems();
			for (Client c : clients)
				this.client
						.addItem(new Item(c.getClientId(),
								(c.getFirstName() != null ? c.getFirstName()
										+ " " : "")
										+ (c.getLastName() != null ? c
												.getLastName() + " " : "")
										+ (c.getName() != null ? c.getName()
												: "")));

			break;
		case ADD_ORDER_COST_THINGS:
			changeTopPanel(this.emptyTop);
			changeLeftPanel(this.moneyPanel);

			this.addClientButton.setVisible(false);
			this.addVehicleButton.setVisible(false);
			this.saveButton.setVisible(false);
			this.nextButton.setVisible(true);
			this.cancelButton.setVisible(true);

			List<ServiceType> services = DBManager.run(new Query() {
				public void prepareQuery(PreparedStatement ps) throws Exception {
				}
			}, DBManager.serviceTypeConverter, "select * from service_type");

			this.serviceTypeComboBox.removeAllItems();
			for (ServiceType s : services)
				this.serviceTypeComboBox.addItem(new Item(0,
						s.getServiceType(), s.getDefaultCharge()));

			break;
		case ADD_ORDER_FINALIZATION:
			changeTopPanel(this.emptyTop);
			changeLeftPanel(this.finalizationPanel);

			this.addClientButton.setVisible(false);
			this.addVehicleButton.setVisible(false);
			this.saveButton.setVisible(true);
			this.nextButton.setVisible(false);
			this.cancelButton.setVisible(true);

			break;
		}

		STATE = nextState;
	}

	public void changeLeftPanel(JPanel another) {
		this.leftPanel.removeAll();
		this.leftPanel.add(another);

		revalidate();
	}

	public void changeTopPanel(JPanel another) {
		this.topPanel.removeAll();
		this.topPanel.add(another);

		revalidate();
	}

	/************************************************
	 * UP WINDOWS & PANELS APP STATES & SWITCHES *
	 ************************************************/

	/**
	 * zmiana combo boxa typu uslugi w wyborze kosztow zamowienia
	 * 
	 * @param evt
	 */
	private void serviceTypeComboBoxItemStateChanged(
			java.awt.event.ItemEvent evt) {
		if (this.serviceTypeComboBox.getSelectedIndex() != -1)
			this.moneyCostTextField.setText(""
					+ ((Item) this.serviceTypeComboBox.getSelectedItem())
							.getFloat());
	}

	/****************
	 * DOWN GUZIKI *
	 ****************/

	/**
	 * dodaj klienta guzik w zamowieniu
	 * 
	 * @param evt
	 */
	private void addClientButtonActionPerformed(java.awt.event.ActionEvent evt) {
		changeState(ADD_ORDER_ADD_CLIENT);
	}

	/**
	 * dodaj pojazd guzik w zamowieniu
	 * 
	 * @param evt
	 */
	private void addVehicleButtonActionPerformed(java.awt.event.ActionEvent evt) {
		changeState(ADD_ORDER_ADD_VEHICLE);
	}

	/**
	 * anuluj guzik: - dodaj klienta w zamowieniu - dodaj pojazd w zamowieniu -
	 * cale skladanie zamowienia
	 * 
	 * @param evt
	 */
	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
		if (STATE == ADD_ORDER_ADD_CLIENT) {
			changeState(ADD_ORDER_CHOOSE_CLIENT);
		} else if (STATE == ADD_ORDER_ADD_VEHICLE) {
			changeState(ADD_ORDER_CHOOSE_VEHICLE);
		} else {
			changeState(SHOW_ORDERS);
		}
	}

	/**
	 * dalej guzik: - zamowienie, przy wyborze klienta - zamowienie, przy
	 * wyborze pojazdu - zamowienie, przy wyborze uslugi/kosztow
	 * 
	 * @param evt
	 */
	private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {
		if (STATE == ADD_ORDER_CHOOSE_CLIENT) {
			if (this.clientTable.getSelectedRow() == -1)
				JOptionPane.showMessageDialog(this, "Wybierz klienta!");
			else {
				int row = this.clientTable.getSelectedRow();
				this.order.setClientId((int) this.clientTable
						.getValueAt(row, 0));

				this.clientTmp.setAdditionalAddress((String) this.clientTable
						.getValueAt(row, 1));
				this.clientTmp.setName((String) this.clientTable.getValueAt(
						row, 4));

				changeState(ADD_ORDER_CHOOSE_VEHICLE);
			}
		} else if (STATE == ADD_ORDER_CHOOSE_VEHICLE) {
			if (this.vehicleTable.getSelectedRow() == -1)
				JOptionPane.showMessageDialog(this, "Wybierz pojazd!");
			else {
				int row = this.vehicleTable.getSelectedRow();
				this.order.setVehicleId((int) this.vehicleTable.getValueAt(row,
						0));

				this.vehicleTmp.setVinNumber((String) this.vehicleTable
						.getValueAt(row, 1));
				this.vehicleTmp.setType((String) this.vehicleTable.getValueAt(
						row, 3));
				this.vehicleTmp.setRegistration(this.vehicleTmp.getType());
				this.vehicleTmp.setBrand((String) this.vehicleTable.getValueAt(
						row, 4));

				changeState(ADD_ORDER_COST_THINGS);
			}
		} else if (STATE == ADD_ORDER_COST_THINGS) {
			try {
				this.order.setServiceType(this.serviceTypeComboBox
						.getSelectedItem().toString());
				this.order.setValue(Float.parseFloat(this.moneyCostTextField
						.getText()));
				this.order.setNote(this.moneyNoteArea.getText());

				Client c = this.clientTmp;
				Vehicle v = this.vehicleTmp;

				this.finalClientLabel.setText((c.getFirstName() != null
						&& c.getFirstName().length() != 0 ? c.getFirstName()
						+ " " : "")
						+ (c.getLastName() != null
								&& c.getLastName().length() != 0 ? c
								.getLastName() + " " : "")
						+ (c.getName() != null && c.getName().length() != 0 ? c
								.getName() : ""));

				this.finalCostLabel.setText("" + this.order.getValue());
				this.finalNoteLabel.setText(this.order.getNote());
				this.finalServiceLabel.setText(this.order.getServiceType());
				this.finalVehicleLabel.setText(v.getBrand() + " " + v.getRegistration() + " v/n:" + v.getVinNumber());

				changeState(ADD_ORDER_FINALIZATION);
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this, "Podaj poprawną wartość!");
			}
		}
	}

	/**
	 * szukaj w panelu znajdz klienta, uzywane w: - wyswietl klientow - wybierz
	 * klienta do zamowienia - wybierz klienta do dodawania pojazdu nie podczas
	 * zamowienia
	 * 
	 * @param evt
	 */
	@SuppressWarnings("unchecked")
	private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
		boolean company = this.findClientCompanyRadio.isSelected();
		List<Client> clients = new LinkedList<>();
		List<Company> companies;
		List<Person> persons;

		final String search = this.findClientTextField.getText();

		if (!company) {
			persons = DBManager.run(
					new Query() {
						public void prepareQuery(PreparedStatement ps)
								throws Exception {
							ps.setString(1, "%" + search + "%");
							ps.setString(2, "%" + search + "%");
						}
					},
					DBManager.personConverter,
					"select * from client where pesel is not null and (first_name like ? or last_name like ?) order by client_id asc");

			clients.addAll(persons);
		} else {
			companies = DBManager.run(new Query() {
				public void prepareQuery(PreparedStatement ps) throws Exception {
					ps.setString(1, "%" + search + "%");
				}
			}, DBManager.companyConverter,
					"select * from client where pesel is null and name like ?");

			clients.addAll(companies);
		}

		if (STATE == JUST_SHOW_CLIENTS || STATE == ADD_ORDER_CHOOSE_CLIENT) {
			Object[][] data = new Object[clients.size()][clientTable.getModel()
					.getColumnCount()];

			int i = 0;
			for (Client c : clients) {
				data[i][0] = c.getClientId();
				data[i][1] = "ul. " + c.getStreet() + " "
						+ c.getBuildingNumber() + "/" + c.getApartmentNumber()
						+ ", " + c.getPostalCode().substring(0, 2) + "-"
						+ c.getPostalCode().substring(2, 5) + " " + c.getCity();
				data[i][2] = c.getPhoneNumber();
				data[i][3] = c.getEmail();
				if (!company) {
					data[i][4] = ((Person) c).getFirstName() + " "
							+ ((Person) c).getLastName();
					data[i][5] = ((Person) c).getPesel();
				} else {
					data[i][4] = ((Company) c).getName();
					data[i][5] = ((Company) c).getNip();
				}

				++i;
			}

			fillTable(clientTable, data);
		} else if (STATE == JUST_ADD_VEHICLE) {
			this.client.removeAllItems();
			for (Client c : clients)
				this.client
						.addItem(new Item(c.getClientId(),
								(c.getFirstName() != null ? c.getFirstName()
										+ " " : "")
										+ (c.getLastName() != null ? c
												.getLastName() + " " : "")
										+ (c.getName() != null ? c.getName()
												: "")));
		}
	}

	/**
	 * szukaj w panelu pojazdu, uzywane w: - wyswietl pojazdy - wybierz pojazd
	 * do zamowienia
	 * 
	 * @param evt
	 */
	private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
		final String brand = this.findByBrandCombo.getSelectedItem().toString();
		final int allBrand = this.findByBrandCombo.getSelectedIndex();
		final String search = this.findByBrandTextField.getText();

		String sql = "select * from vehicle where ";

		if (allBrand != 0)
			sql += "brand = ? and ";

		if (STATE == ADD_ORDER_CHOOSE_VEHICLE)
			sql += "client_id = " + this.order.getClientId() + " and ";

		sql += "(vin_number like ? or registration like ?)";

		List<Vehicle> vehicles = DBManager.run(new Query() {
			public void prepareQuery(PreparedStatement ps) throws Exception {
				int i = 1;
				if (allBrand != 0)
					ps.setString(i++, brand);
				ps.setString(i++, "%" + search + "%");
				ps.setString(i++, "%" + search + "%");
			}
		}, DBManager.vehicleConverter, sql);

		Object[][] data = new Object[vehicles.size()][vehicleTable.getModel()
				.getColumnCount()];

		int i = 0;
		for (Vehicle v : vehicles) {
			data[i][0] = v.getVehicleId();
			data[i][1] = v.getVinNumber();
			data[i][2] = v.getProductionDate();
			data[i][3] = v.getRegistration();
			data[i][4] = "TODO";//v.getBrand();

			++i;
		}

		fillTable(vehicleTable, data);
	}

	/**
	 * zapisz guzik: - dodawanie nowego klienta - dodawanie nowego pojazdu -
	 * zapisanie zamowienia na koncu transakcji
	 * 
	 * @param evt
	 */
	private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {
		if (STATE == JUST_ADD_VEHICLE || STATE == ADD_ORDER_ADD_VEHICLE) {

			if (this.client.getSelectedIndex() < 0
					|| this.brand.getSelectedIndex() < 0
					|| this.vin.getText().length() != 17
					|| this.vehicleType.getText().length() <= 0)
				JOptionPane.showMessageDialog(this, "Źle wypełniłeś dane! vin("
						+ this.vin.getText().length() + "/17)");
			else {
				try {
					final Date date = new SimpleDateFormat("yyyy-MM-dd")
							.parse(this.productionDate.getText());
					final int clientId = ((Item) this.client.getSelectedItem())
							.getId();
					final String vin = this.vin.getText();
					final String type = this.vehicleType.getText();
					final String brand = this.brand.getSelectedItem()
							.toString();

					boolean result = false;

					if (STATE == JUST_ADD_VEHICLE) {

						// Model inserter
						result = DBManager.executeTask(
								new Task<Boolean>() {
									public Boolean execute(PreparedStatement ps)
											throws Exception {
										ps.setString(1, "x5"); // due to simplifier
										ps.setString(2, brand);

										boolean r = ps.executeUpdate() > 0;

										ResultSet rs = ps.getGeneratedKeys();
										if (rs.next()) {
											int key = rs.getInt(1);
											AppWindow.tmp = key;
										}
										return r;
									}
								},
								"insert into vehicle_version (model, brand) values (?, ?)", new String[] { "vversion_id" });
						
						final int vversion_id = (int) AppWindow.tmp;
						
						// Proper vehicle inserter
						result = DBManager.executeTask(
								new Task<Boolean>() {
									public Boolean execute(PreparedStatement ps)
											throws Exception {
										ps.setInt(1, clientId);
										ps.setString(2, vin);
										ps.setDate(
												3,
												new java.sql.Date(date
														.getTime()));
										ps.setString(4, type);
										ps.setInt(5, vversion_id);

										return ps.executeUpdate() > 0;
									}
								},
								"insert into vehicle (client_id, vin_number, production_date, registration, vversion_id) values (?, ?, ? ,?, ?)");

					} else if (STATE == ADD_ORDER_ADD_VEHICLE) {
						// Model inserter
						result = DBManager.executeTask(
								new Task<Boolean>() {
									public Boolean execute(PreparedStatement ps)
											throws Exception {
										ps.setString(1, "x5"); // due to simplifier
										ps.setString(2, brand);

										boolean r = ps.executeUpdate() > 0;

										ResultSet rs = ps.getGeneratedKeys();
										if (rs.next()) {
											int key = rs.getInt(1);
											AppWindow.tmp = key;
										}
										return r;
									}
								},
								"insert into vehicle_version (model, brand) values (?, ?)",
								this.connection, new String[] { "vversion_id" });
						
						final int vversion_id = (int) AppWindow.tmp;
						
						// Proper vehicle inserter
						result = DBManager.executeTask(
								new Task<Boolean>() {
									public Boolean execute(PreparedStatement ps)
											throws Exception {
										ps.setInt(1, clientId);
										ps.setString(2, vin);
										ps.setDate(
												3,
												new java.sql.Date(date
														.getTime()));
										ps.setString(4, type);
										ps.setInt(5, vversion_id);

										boolean r = ps.executeUpdate() > 0;

										ResultSet rs = ps.getGeneratedKeys();
										if (rs.next()) {
											int key = rs.getInt(1);
											AppWindow.tmp = key;
										}
										return r;
									}
								},
								"insert into vehicle (client_id, vin_number, production_date, registration, vversion_id) values (?, ?, ? ,?, ?)",
								this.connection, new String[] { "vehicle_id" });

					}

					if (result) {
						JOptionPane.showMessageDialog(this, "Dodano pojazd!");
						this.vin.setText("");
						this.productionDate.setText("YYYY-MM-DD");
						this.vehicleType.setText("");

						if (STATE == ADD_ORDER_ADD_VEHICLE) {
							this.vehicleTmp.setClientId(clientId);
							this.vehicleTmp.setVehicleId((int) AppWindow.tmp);
							this.vehicleTmp.setBrand(brand);
							this.vehicleTmp.setProductionDate(date);
							this.vehicleTmp.setType(type);
							this.vehicleTmp.setRegistration(type);
							this.vehicleTmp.setVinNumber(vin);
						}

						if (STATE == JUST_ADD_VEHICLE)
							changeState(JUST_SHOW_VEHICLES);
						else if (STATE == ADD_ORDER_ADD_VEHICLE) {
							this.order.setVehicleId((int) AppWindow.tmp);
							changeState(ADD_ORDER_COST_THINGS);
						}
					} else {
						JOptionPane
								.showMessageDialog(this, "Coś nie poszło :/");
					}
				} catch (ParseException e) {
					JOptionPane.showMessageDialog(this, "Niepoprawna data!");
				} catch (SQLException e) {
					e.printStackTrace();
				} catch (RuntimeException e) {
					if (e.getMessage().indexOf("KEY_2F") != -1)
						JOptionPane.showMessageDialog(this,
								"Numer VIN nie jest unikalny!");
				}
			}
		} else if (STATE == JUST_ADD_CLIENT || STATE == ADD_ORDER_ADD_CLIENT) {
			if (this.street.getText().length() == 0
					|| this.city.getText().length() == 0
					|| this.phoneNumber.getText().length() == 0
					|| this.email.getText().length() == 0)
				JOptionPane.showMessageDialog(this, "Źle wypełniłeś dane!");
			else {
				final boolean company = this.companyRadio.isSelected();

				try {
					final String street = this.street.getText();
					final int buildingNumber = (int) this.buildingNumber
							.getValue();
					if (buildingNumber <= 0)
						throw new IllegalArgumentException("Nr budynku <= 0 ?!");
					final int apartmentNumber = (int) this.apartmentNumber
							.getValue();
					if (apartmentNumber < 0)
						throw new IllegalArgumentException(
								"Nr mieszkania < 0 ?!");
					final String postalCode = this.postalCode.getText()
							.replaceAll("[A-z\\-]*", "");
					final String city = this.city.getText();
					final String additionalAddress = this.additionalAddress
							.getText();
					final String phone = this.phoneNumber.getText().replaceAll(
							"[^\\d\\+\\(\\)]*", "");
					if (phone.length() < 7)
						throw new IllegalArgumentException(
								"Numer telefonu coś nie taki! (min 7 liczb: "
										+ phone.length() + "/7)");
					final String email = this.email.getText();
					final String firstName = this.firstName.getText();
					final String lastName = this.lastName.getText();
					final String peselString = this.pesel.getText();
					final String name = this.name.getText();
					final String nipString = this.nip.getText();

					if (postalCode.length() != 5)
						throw new IllegalArgumentException(
								"Kod pocztowy nie taki!");

					Pattern pattern = Pattern
							.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
					Matcher matcher = pattern.matcher(email);
					if (!matcher.matches())
						throw new IllegalArgumentException(
								"Podaj poprawny email!");

					String sql = "insert into client (street, building_number, apartment_number, postal_code, city, additional_address, phone_number, email";

					long psl = 0;
					long np = 0;
					if (company) {
						if (name.length() == 0 || nipString.length() != 10)
							throw new IllegalArgumentException(
									"Nie wypełniłeś dobrze danych! nip("
											+ nipString.length() + "/10)");

						sql += ", name, nip) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

						np = Long.parseLong(nipString);
					} else {
						if (firstName.length() == 0 || lastName.length() == 0
								|| peselString.length() != 11)
							throw new IllegalArgumentException(
									"Nie wypełniłeś dobrze danych! pesel("
											+ peselString.length() + "/11)");

						sql += ", first_name, last_name, pesel) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

						psl = Long.parseLong(peselString);
					}

					final long pesel = psl;
					final long nip = np;

					boolean result = false;

					if (STATE == JUST_ADD_CLIENT) {
						result = DBManager.executeTask(new Task<Boolean>() {
							public Boolean execute(PreparedStatement ps)
									throws Exception {
								ps.setString(1, street);
								ps.setInt(2, buildingNumber);
								ps.setInt(3, apartmentNumber);
								ps.setString(4, postalCode);
								ps.setString(5, city);
								ps.setString(6, additionalAddress);
								ps.setString(7, phone);
								ps.setString(8, email);

								if (!company) {
									ps.setString(9, firstName);
									ps.setString(10, lastName);
									ps.setLong(11, pesel);
								} else {
									ps.setString(9, name);
									ps.setLong(10, nip);
								}

								return ps.executeUpdate() > 0;
							}
						}, sql);

					} else if (STATE == ADD_ORDER_ADD_CLIENT) {
						result = DBManager.executeTask(new Task<Boolean>() {
							public Boolean execute(PreparedStatement ps)
									throws Exception {
								ps.setString(1, street);
								ps.setInt(2, buildingNumber);
								ps.setInt(3, apartmentNumber);
								ps.setString(4, postalCode);
								ps.setString(5, city);
								ps.setString(6, additionalAddress);
								ps.setString(7, phone);
								ps.setString(8, email);

								if (!company) {
									ps.setString(9, firstName);
									ps.setString(10, lastName);
									ps.setLong(11, pesel);
								} else {
									ps.setString(9, name);
									ps.setLong(10, nip);
								}

								boolean r = ps.executeUpdate() > 0;

								ResultSet rs = ps.getGeneratedKeys();
								if (rs.next()) {
									int key = rs.getInt(1);
									AppWindow.tmp = key;
								}
								return r;
							}
						}, sql, this.connection, new String[] { "client_id" });
					}

					if (result) {
						JOptionPane.showMessageDialog(this, "Dodano klienta!");

						this.additionalAddress.setText("");
						this.street.setText("Ulica");
						this.buildingNumber.setValue(0);
						this.apartmentNumber.setValue(0);
						this.postalCode.setText("00-000");
						this.city.setText("Miasto");
						this.phoneNumber.setText("Telefon");
						this.email.setText("Email");
						this.firstName.setText("Imię");
						this.lastName.setText("Nazwisko");
						this.pesel.setText("pesel");
						this.name.setText("Nazwa");
						this.nip.setText("NIP");

						if (STATE == ADD_ORDER_ADD_CLIENT) {
							this.clientTmp.setClientId((int) AppWindow.tmp);
							this.clientTmp
									.setAdditionalAddress(additionalAddress);
							this.clientTmp.setApartmentNumber(apartmentNumber);
							this.clientTmp.setBuildingNumber(buildingNumber);
							this.clientTmp.setCity(city);
							this.clientTmp.setEmail(email);
							this.clientTmp.setFirstName(firstName);
							this.clientTmp.setLastName(lastName);
							this.clientTmp.setName(name);
							this.clientTmp.setNip(nip);
							this.clientTmp.setPesel(pesel);
							this.clientTmp.setPhoneNumber(phone);
							this.clientTmp.setPostalCode(postalCode);
							this.clientTmp.setStreet(street);
						}

						if (STATE == JUST_ADD_CLIENT)
							changeState(JUST_SHOW_CLIENTS);
						else if (STATE == ADD_ORDER_ADD_CLIENT) {
							this.order.setClientId((int) AppWindow.tmp);
							changeState(ADD_ORDER_CHOOSE_VEHICLE);
						}
					} else {
						JOptionPane
								.showMessageDialog(this, "Coś nie poszło :/");
					}

				} catch (SQLException e) {
					e.printStackTrace();
				} catch (NumberFormatException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(this, "Coś źle "
							+ (company ? "nip" : "pesel") + " piszesz!");
				} catch (IllegalArgumentException e) {
					JOptionPane.showMessageDialog(this, e.getMessage());
				} catch (RuntimeException e) {
					if (e.getMessage().indexOf("KEY_2A") != -1)
						JOptionPane.showMessageDialog(this,
								"Numer telefonu nie jest unikalny!");
					else if (e.getMessage().indexOf("KEY_3") != -1)
						JOptionPane.showMessageDialog(this,
								"Adres email nie jest unikalny!");
					else if (e.getMessage().indexOf("KEY_1") != -1)
						JOptionPane.showMessageDialog(this,
								"PESEL nie jest unikalny!");
					else if (e.getMessage().indexOf("KEY_1V1") != -1)
						JOptionPane.showMessageDialog(this,
								"NIP nie jest unikalny!");
				}
			}
		} else if (STATE == ADD_ORDER_FINALIZATION) {
			try {

				final int clientId = this.order.getClientId();
				final int vehicleId = this.order.getVehicleId();
				final String note = this.order.getNote();
				final float value = this.order.getValue();
				final String serviceType = this.order.getServiceType();

				boolean result = DBManager
						.executeTask(
								new Task<Boolean>() {
									public Boolean execute(PreparedStatement ps)
											throws Exception {
										ps.setInt(1, clientId);
										ps.setInt(2, vehicleId);
										ps.setString(3, serviceType);
										ps.setFloat(4, value);
										ps.setString(5, note);

										return ps.executeUpdate() > 0;
									}
								},
								"insert into order_t (client_id, vehicle_id, service_type, value, note) values (?, ?, ?, ?, ?)",
								this.connection, new String[] { "order_id" });

				if (result) {
					connection.commit();

					JOptionPane.showMessageDialog(this, "Zamówienie dodane!");
				} else {

					JOptionPane
							.showMessageDialog(this,
									"Coś poszło nie tak :/ pewnie padło połączenie z bazą.");
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}

			changeState(SHOW_ORDERS);
		}
	}

	/****************
	 * UP GUZIKI *
	 ****************/

	/********************************
	 * DOWN MENUBAR ITEMS LISTENERS *
	 ********************************/

	/**
	 * wyswietl klientow menubar
	 * 
	 * @param evt
	 */
	private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {
		changeState(JUST_SHOW_CLIENTS);
	}

	/**
	 * dodaj klienta menubar
	 * 
	 * @param evt
	 */
	private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {
		changeState(JUST_ADD_CLIENT);
	}

	/**
	 * wyswietl pojazdy menubar
	 * 
	 * @param evt
	 */
	private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {
		changeState(JUST_SHOW_VEHICLES);
	}

	/**
	 * dodaj pojazd menubar
	 * 
	 * @param evt
	 */
	private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {
		changeState(JUST_ADD_VEHICLE);
	}

	/**
	 * wyswietl zamowienia menubar
	 * 
	 * @param evt
	 */
	private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {
		changeState(SHOW_ORDERS);
	}

	/**
	 * dodaj nowe zamowienie menubar
	 * 
	 * @param evt
	 */
	private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {
		changeState(ADD_ORDER_CHOOSE_CLIENT);
	}

	/********************************
	 * UP MENUBAR ITEMS LISTENERS *
	 ********************************/

	/****************************************
	 * DOWN ADD CLIENT FORM RADIO SHOW/HIDE *
	 ****************************************/

	/**
	 * radio button firma w dodawaniu klienta
	 * 
	 * @param evt
	 */
	private void companyRadioStateChanged(javax.swing.event.ChangeEvent evt) {
		if (companyRadio.isSelected()) {
			this.companyPanel.setVisible(true);
			this.personPanel.setVisible(false);
		}
	}

	/**
	 * radio button osoba fizyczna w dodawaniu klienta
	 * 
	 * @param evt
	 */
	private void personRadioStateChanged(javax.swing.event.ChangeEvent evt) {
		if (personRadio.isSelected()) {
			this.companyPanel.setVisible(false);
			this.personPanel.setVisible(true);
		}
	}

	/****************************************
	 * UP ADD CLIENT FORM RADIO SHOW/HIDE *
	 ****************************************/

	/********************************
	 * DOWN NETBEANS GENERATED GUI *
	 ********************************/

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {

		findClient = new javax.swing.JPanel();
		findClientTextField = new javax.swing.JTextField();
		jButton1 = new javax.swing.JButton();
		findClientPersonRadio = new javax.swing.JRadioButton();
		findClientCompanyRadio = new javax.swing.JRadioButton();
		buttonGroup1 = new javax.swing.ButtonGroup();
		findVehicle = new javax.swing.JPanel();
		findByBrandTextField = new javax.swing.JTextField();
		jButton2 = new javax.swing.JButton();
		findByBrandCombo = new javax.swing.JComboBox();
		jLabel1 = new javax.swing.JLabel();
		vehicleTablePanel = new javax.swing.JPanel();
		jScrollPane1 = new javax.swing.JScrollPane();
		vehicleTable = new javax.swing.JTable();
		clientTablePanel = new javax.swing.JPanel();
		jScrollPane2 = new javax.swing.JScrollPane();
		clientTable = new javax.swing.JTable();
		addClientPanel = new javax.swing.JPanel();
		personRadio = new javax.swing.JRadioButton();
		companyRadio = new javax.swing.JRadioButton();
		jScrollPane3 = new javax.swing.JScrollPane();
		additionalAddress = new javax.swing.JTextArea();
		street = new javax.swing.JTextField();
		postalCode = new javax.swing.JTextField();
		buildingNumber = new javax.swing.JSpinner();
		city = new javax.swing.JTextField();
		apartmentNumber = new javax.swing.JSpinner();
		phoneNumber = new javax.swing.JTextField();
		email = new javax.swing.JTextField();
		jLabel2 = new javax.swing.JLabel();
		jLabel3 = new javax.swing.JLabel();
		jLabel4 = new javax.swing.JLabel();
		jLabel5 = new javax.swing.JLabel();
		jLabel6 = new javax.swing.JLabel();
		jLabel7 = new javax.swing.JLabel();
		jLabel8 = new javax.swing.JLabel();
		jLabel9 = new javax.swing.JLabel();
		companyPanel = new javax.swing.JPanel();
		jLabel13 = new javax.swing.JLabel();
		name = new javax.swing.JTextField();
		nip = new javax.swing.JTextField();
		jLabel14 = new javax.swing.JLabel();
		personPanel = new javax.swing.JPanel();
		firstName = new javax.swing.JTextField();
		jLabel10 = new javax.swing.JLabel();
		jLabel11 = new javax.swing.JLabel();
		lastName = new javax.swing.JTextField();
		pesel = new javax.swing.JTextField();
		jLabel12 = new javax.swing.JLabel();
		buttonGroup2 = new javax.swing.ButtonGroup();
		moneyPanel = new javax.swing.JPanel();
		serviceTypeComboBox = new javax.swing.JComboBox();
		jLabel15 = new javax.swing.JLabel();
		jScrollPane4 = new javax.swing.JScrollPane();
		moneyNoteArea = new javax.swing.JTextArea();
		moneyCostTextField = new javax.swing.JTextField();
		jLabel16 = new javax.swing.JLabel();
		jLabel17 = new javax.swing.JLabel();
		addVehicle = new javax.swing.JPanel();
		client = new javax.swing.JComboBox();
		vin = new javax.swing.JTextField();
		productionDate = new javax.swing.JTextField();
		brand = new javax.swing.JComboBox();
		jLabel18 = new javax.swing.JLabel();
		jLabel19 = new javax.swing.JLabel();
		jLabel20 = new javax.swing.JLabel();
		jLabel21 = new javax.swing.JLabel();
		vehicleType = new javax.swing.JTextField();
		jLabel27 = new javax.swing.JLabel();
		emptyTop = new javax.swing.JPanel();
		finalizationPanel = new javax.swing.JPanel();
		finalClientLabel = new javax.swing.JLabel();
		finalVehicleLabel = new javax.swing.JLabel();
		finalServiceLabel = new javax.swing.JLabel();
		finalCostLabel = new javax.swing.JLabel();
		finalNoteLabel = new javax.swing.JLabel();
		jLabel22 = new javax.swing.JLabel();
		jLabel23 = new javax.swing.JLabel();
		jLabel24 = new javax.swing.JLabel();
		jLabel25 = new javax.swing.JLabel();
		jLabel26 = new javax.swing.JLabel();
		orderTablePanel = new javax.swing.JPanel();
		jScrollPane5 = new javax.swing.JScrollPane();
		orderTable = new javax.swing.JTable();
		topPanel = new javax.swing.JPanel();
		leftPanel = new javax.swing.JPanel();
		buttons = new javax.swing.JPanel();
		addClientButton = new javax.swing.JButton();
		addVehicleButton = new javax.swing.JButton();
		saveButton = new javax.swing.JButton();
		nextButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();
		jMenuBar1 = new javax.swing.JMenuBar();
		jMenu1 = new javax.swing.JMenu();
		jMenuItem1 = new javax.swing.JMenuItem();
		jMenuItem2 = new javax.swing.JMenuItem();
		jMenu2 = new javax.swing.JMenu();
		jMenuItem3 = new javax.swing.JMenuItem();
		jMenuItem4 = new javax.swing.JMenuItem();
		jMenu3 = new javax.swing.JMenu();
		jMenuItem5 = new javax.swing.JMenuItem();
		jMenuItem6 = new javax.swing.JMenuItem();

		findClient.setMaximumSize(new java.awt.Dimension(663, 100));
		findClient.setMinimumSize(new java.awt.Dimension(663, 100));

		jButton1.setText("Szukaj");
		jButton1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton1ActionPerformed(evt);
			}
		});

		buttonGroup1.add(findClientPersonRadio);
		findClientPersonRadio.setSelected(true);
		findClientPersonRadio.setText("Osoby fizyczne");

		buttonGroup1.add(findClientCompanyRadio);
		findClientCompanyRadio.setText("Firmy");

		javax.swing.GroupLayout findClientLayout = new javax.swing.GroupLayout(
				findClient);
		findClient.setLayout(findClientLayout);
		findClientLayout
				.setHorizontalGroup(findClientLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								findClientLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												findClientLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																findClientLayout
																		.createSequentialGroup()
																		.addComponent(
																				findClientPersonRadio)
																		.addGap(18,
																				18,
																				18)
																		.addComponent(
																				findClientCompanyRadio))
														.addGroup(
																findClientLayout
																		.createSequentialGroup()
																		.addComponent(
																				findClientTextField,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				200,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addGap(18,
																				18,
																				18)
																		.addComponent(
																				jButton1)))
										.addContainerGap(372, Short.MAX_VALUE)));
		findClientLayout
				.setVerticalGroup(findClientLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								findClientLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												findClientLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																findClientTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(jButton1))
										.addGap(18, 18, 18)
										.addGroup(
												findClientLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																findClientPersonRadio)
														.addComponent(
																findClientCompanyRadio))
										.addContainerGap(25, Short.MAX_VALUE)));

		findVehicle.setMaximumSize(new java.awt.Dimension(663, 100));
		findVehicle.setMinimumSize(new java.awt.Dimension(663, 100));

		jButton2.setText("Szukaj");
		jButton2.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton2ActionPerformed(evt);
			}
		});

		findByBrandCombo.setModel(new javax.swing.DefaultComboBoxModel(
				new String[] { "Dowolna", "Item 2", "Item 3", "Item 4" }));

		jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel1.setText("Marka");

		javax.swing.GroupLayout findVehicleLayout = new javax.swing.GroupLayout(
				findVehicle);
		findVehicle.setLayout(findVehicleLayout);
		findVehicleLayout
				.setHorizontalGroup(findVehicleLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								findVehicleLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												findVehicleLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING,
																false)
														.addGroup(
																findVehicleLayout
																		.createSequentialGroup()
																		.addComponent(
																				jLabel1,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				56,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addGap(18,
																				18,
																				18)
																		.addComponent(
																				findByBrandCombo,
																				0,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				Short.MAX_VALUE))
														.addComponent(
																findByBrandTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																200,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGap(18, 18, 18)
										.addComponent(jButton2)
										.addContainerGap(372, Short.MAX_VALUE)));
		findVehicleLayout
				.setVerticalGroup(findVehicleLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								findVehicleLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												findVehicleLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																findByBrandTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(jButton2))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addGroup(
												findVehicleLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																findByBrandCombo,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(jLabel1))
										.addContainerGap(35, Short.MAX_VALUE)));

		vehicleTablePanel.setMaximumSize(new java.awt.Dimension(495, 295));
		vehicleTablePanel.setMinimumSize(new java.awt.Dimension(495, 295));

		vehicleTable
				.setModel(new javax.swing.table.DefaultTableModel(
						new Object[][] {

						}, new String[] { "ID", "VIN", "Data produkcji", "Numer rejestracyjny",
								"Marka" }) {
					Class[] types = new Class[] { java.lang.Integer.class,
							java.lang.String.class, java.lang.Object.class,
							java.lang.String.class, java.lang.String.class };
					boolean[] canEdit = new boolean[] { false, false, false,
							false, false };

					public Class getColumnClass(int columnIndex) {
						return types[columnIndex];
					}

					public boolean isCellEditable(int rowIndex, int columnIndex) {
						return canEdit[columnIndex];
					}
				});
		vehicleTable
				.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		jScrollPane1.setViewportView(vehicleTable);

		javax.swing.GroupLayout vehicleTablePanelLayout = new javax.swing.GroupLayout(
				vehicleTablePanel);
		vehicleTablePanel.setLayout(vehicleTablePanelLayout);
		vehicleTablePanelLayout.setHorizontalGroup(vehicleTablePanelLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(jScrollPane1,
						javax.swing.GroupLayout.DEFAULT_SIZE, 495,
						Short.MAX_VALUE));
		vehicleTablePanelLayout.setVerticalGroup(vehicleTablePanelLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(jScrollPane1,
						javax.swing.GroupLayout.DEFAULT_SIZE, 295,
						Short.MAX_VALUE));

		clientTablePanel.setMaximumSize(new java.awt.Dimension(495, 295));
		clientTablePanel.setMinimumSize(new java.awt.Dimension(495, 295));

		clientTable.setModel(new javax.swing.table.DefaultTableModel(
				new Object[][] {

				}, new String[] { "ID", "Adres", "Telefon", "E-mail", "Dane",
						"PESEL/NIP" }) {
			Class[] types = new Class[] { java.lang.Integer.class,
					java.lang.String.class, java.lang.String.class,
					java.lang.String.class, java.lang.String.class,
					java.lang.String.class };
			boolean[] canEdit = new boolean[] { false, false, false, false,
					false, false };

			public Class getColumnClass(int columnIndex) {
				return types[columnIndex];
			}

			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return canEdit[columnIndex];
			}
		});
		clientTable
				.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		jScrollPane2.setViewportView(clientTable);

		javax.swing.GroupLayout clientTablePanelLayout = new javax.swing.GroupLayout(
				clientTablePanel);
		clientTablePanel.setLayout(clientTablePanelLayout);
		clientTablePanelLayout.setHorizontalGroup(clientTablePanelLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(jScrollPane2,
						javax.swing.GroupLayout.DEFAULT_SIZE, 495,
						Short.MAX_VALUE));
		clientTablePanelLayout.setVerticalGroup(clientTablePanelLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(jScrollPane2,
						javax.swing.GroupLayout.DEFAULT_SIZE, 295,
						Short.MAX_VALUE));

		addClientPanel.setMaximumSize(new java.awt.Dimension(495, 302));
		addClientPanel.setMinimumSize(new java.awt.Dimension(495, 302));

		buttonGroup2.add(personRadio);
		personRadio.setSelected(true);
		personRadio.setText("Osoba fizyczna");
		personRadio.addChangeListener(new javax.swing.event.ChangeListener() {
			public void stateChanged(javax.swing.event.ChangeEvent evt) {
				personRadioStateChanged(evt);
			}
		});

		buttonGroup2.add(companyRadio);
		companyRadio.setText("Firma");
		companyRadio.addChangeListener(new javax.swing.event.ChangeListener() {
			public void stateChanged(javax.swing.event.ChangeEvent evt) {
				companyRadioStateChanged(evt);
			}
		});

		additionalAddress.setColumns(5);
		additionalAddress.setFont(new java.awt.Font("Arial", 0, 10)); // NOI18N
		additionalAddress.setRows(2);
		additionalAddress.setMinimumSize(new java.awt.Dimension(4, 25));
		jScrollPane3.setViewportView(additionalAddress);

		street.setText("Ulica");

		postalCode.setText("00-000");

		city.setText("Miasto");

		phoneNumber.setText("Telefon");

		email.setText("E-mail");

		jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel2.setText("Ulica");

		jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel3.setText("Nr. budynku");

		jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel4.setText("Nr. mieszkania");

		jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel5.setText("Inny adres");

		jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel6.setText("Miasto");

		jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel7.setText("Kod pocztowy");

		jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel8.setText("Email");

		jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel9.setText("Telefon");

		companyPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		companyPanel.setMaximumSize(new java.awt.Dimension(152, 72));
		companyPanel.setMinimumSize(new java.awt.Dimension(152, 72));

		jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel13.setText("Nazwa firmy");

		name.setText("Nazwa");
		name.setMinimumSize(new java.awt.Dimension(38, 20));

		nip.setText("NIP");
		nip.setMinimumSize(new java.awt.Dimension(38, 20));
		nip.setPreferredSize(new java.awt.Dimension(38, 20));

		jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel14.setText("NIP");

		javax.swing.GroupLayout companyPanelLayout = new javax.swing.GroupLayout(
				companyPanel);
		companyPanel.setLayout(companyPanelLayout);
		companyPanelLayout
				.setHorizontalGroup(companyPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								javax.swing.GroupLayout.Alignment.TRAILING,
								companyPanelLayout
										.createSequentialGroup()
										.addGroup(
												companyPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																companyPanelLayout
																		.createSequentialGroup()
																		.addGap(18,
																				18,
																				18)
																		.addComponent(
																				jLabel13,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				72,
																				javax.swing.GroupLayout.PREFERRED_SIZE))
														.addGroup(
																javax.swing.GroupLayout.Alignment.TRAILING,
																companyPanelLayout
																		.createSequentialGroup()
																		.addContainerGap()
																		.addComponent(
																				jLabel14,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				72,
																				javax.swing.GroupLayout.PREFERRED_SIZE)))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addGroup(
												companyPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																name,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addComponent(
																nip,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE))
										.addContainerGap()));
		companyPanelLayout
				.setVerticalGroup(companyPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								companyPanelLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												companyPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																name,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(jLabel13))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												companyPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																nip,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(jLabel14))
										.addContainerGap(
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)));

		personPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		personPanel.setMaximumSize(new java.awt.Dimension(226, 108));
		personPanel.setMinimumSize(new java.awt.Dimension(226, 108));

		firstName.setText("Imię");

		jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel10.setText("Imię");

		jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel11.setText("Nazwisko");

		lastName.setText("Nazwisko");

		pesel.setText("PESEL");

		jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel12.setText("PESEL");

		javax.swing.GroupLayout personPanelLayout = new javax.swing.GroupLayout(
				personPanel);
		personPanel.setLayout(personPanelLayout);
		personPanelLayout
				.setHorizontalGroup(personPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								javax.swing.GroupLayout.Alignment.TRAILING,
								personPanelLayout
										.createSequentialGroup()
										.addGap(17, 17, 17)
										.addGroup(
												personPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.TRAILING)
														.addGroup(
																personPanelLayout
																		.createParallelGroup(
																				javax.swing.GroupLayout.Alignment.LEADING)
																		.addComponent(
																				jLabel11,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				72,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addComponent(
																				jLabel12,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				72,
																				javax.swing.GroupLayout.PREFERRED_SIZE))
														.addComponent(
																jLabel10,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																72,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addGroup(
												personPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																firstName,
																javax.swing.GroupLayout.Alignment.TRAILING)
														.addComponent(
																lastName,
																javax.swing.GroupLayout.Alignment.TRAILING,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																113,
																Short.MAX_VALUE)
														.addComponent(
																pesel,
																javax.swing.GroupLayout.Alignment.TRAILING))
										.addContainerGap()));
		personPanelLayout
				.setVerticalGroup(personPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								personPanelLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												personPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																firstName,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(jLabel10))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addGroup(
												personPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																lastName,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(jLabel11))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addGroup(
												personPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																pesel,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(jLabel12))
										.addContainerGap(
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)));

		javax.swing.GroupLayout addClientPanelLayout = new javax.swing.GroupLayout(
				addClientPanel);
		addClientPanel.setLayout(addClientPanelLayout);
		addClientPanelLayout
				.setHorizontalGroup(addClientPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								addClientPanelLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												addClientPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																addClientPanelLayout
																		.createParallelGroup(
																				javax.swing.GroupLayout.Alignment.TRAILING)
																		.addComponent(
																				jLabel2,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				72,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addComponent(
																				jLabel3,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				72,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addComponent(
																				jLabel4,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				72,
																				javax.swing.GroupLayout.PREFERRED_SIZE))
														.addComponent(
																jLabel5,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																72,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jLabel7,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																72,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jLabel6,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																72,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addGroup(
												addClientPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.TRAILING)
														.addGroup(
																addClientPanelLayout
																		.createSequentialGroup()
																		.addGap(44,
																				44,
																				44)
																		.addComponent(
																				personRadio)
																		.addGap(38,
																				38,
																				38)
																		.addComponent(
																				companyRadio)
																		.addGap(0,
																				0,
																				Short.MAX_VALUE))
														.addGroup(
																addClientPanelLayout
																		.createSequentialGroup()
																		.addGroup(
																				addClientPanelLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING,
																								false)
																						.addComponent(
																								street)
																						.addComponent(
																								city)
																						.addComponent(
																								jScrollPane3,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								98,
																								javax.swing.GroupLayout.PREFERRED_SIZE)
																						.addComponent(
																								postalCode,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								100,
																								Short.MAX_VALUE)
																						.addComponent(
																								apartmentNumber)
																						.addComponent(
																								buildingNumber))
																		.addGroup(
																				addClientPanelLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addGroup(
																								addClientPanelLayout
																										.createSequentialGroup()
																										.addGap(50,
																												50,
																												50)
																										.addGroup(
																												addClientPanelLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.TRAILING)
																														.addComponent(
																																jLabel9,
																																javax.swing.GroupLayout.PREFERRED_SIZE,
																																72,
																																javax.swing.GroupLayout.PREFERRED_SIZE)
																														.addComponent(
																																jLabel8,
																																javax.swing.GroupLayout.PREFERRED_SIZE,
																																72,
																																javax.swing.GroupLayout.PREFERRED_SIZE))
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																										.addGroup(
																												addClientPanelLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.LEADING)
																														.addComponent(
																																phoneNumber,
																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																110,
																																Short.MAX_VALUE)
																														.addComponent(
																																email,
																																javax.swing.GroupLayout.Alignment.TRAILING))
																										.addGap(61,
																												61,
																												61))
																						.addGroup(
																								addClientPanelLayout
																										.createSequentialGroup()
																										.addGap(29,
																												29,
																												29)
																										.addGroup(
																												addClientPanelLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.LEADING,
																																false)
																														.addComponent(
																																personPanel,
																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																Short.MAX_VALUE)
																														.addComponent(
																																companyPanel,
																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																Short.MAX_VALUE))
																										.addContainerGap(
																												javax.swing.GroupLayout.DEFAULT_SIZE,
																												Short.MAX_VALUE)))))));
		addClientPanelLayout
				.setVerticalGroup(addClientPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								addClientPanelLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												addClientPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																companyRadio)
														.addComponent(
																personRadio))
										.addGap(13, 13, 13)
										.addGroup(
												addClientPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																street,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																phoneNumber,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(jLabel2)
														.addComponent(jLabel9))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addGroup(
												addClientPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																buildingNumber,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																email,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(jLabel3)
														.addComponent(jLabel8))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addGroup(
												addClientPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING,
																false)
														.addGroup(
																addClientPanelLayout
																		.createSequentialGroup()
																		.addGroup(
																				addClientPanelLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.BASELINE)
																						.addComponent(
																								apartmentNumber,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.PREFERRED_SIZE)
																						.addComponent(
																								jLabel4))
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																		.addGroup(
																				addClientPanelLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.BASELINE)
																						.addComponent(
																								postalCode,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.PREFERRED_SIZE)
																						.addComponent(
																								jLabel7))
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																		.addGroup(
																				addClientPanelLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.BASELINE)
																						.addComponent(
																								city,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.PREFERRED_SIZE)
																						.addComponent(
																								jLabel6))
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																		.addGroup(
																				addClientPanelLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addComponent(
																								jLabel5)
																						.addComponent(
																								jScrollPane3)))
														.addGroup(
																addClientPanelLayout
																		.createSequentialGroup()
																		.addComponent(
																				personPanel,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(
																				companyPanel,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				javax.swing.GroupLayout.PREFERRED_SIZE)))
										.addContainerGap(
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)));

		moneyPanel
				.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
		moneyPanel.setMaximumSize(new java.awt.Dimension(495, 301));
		moneyPanel.setMinimumSize(new java.awt.Dimension(495, 301));

		serviceTypeComboBox.addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(java.awt.event.ItemEvent evt) {
				serviceTypeComboBoxItemStateChanged(evt);
			}
		});

		jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel15.setText("Typ usługi");

		moneyNoteArea.setColumns(20);
		moneyNoteArea.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
		moneyNoteArea.setRows(5);
		jScrollPane4.setViewportView(moneyNoteArea);

		moneyCostTextField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
		moneyCostTextField.setText("0.0");

		jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel16.setText("Wstępny koszt");

		jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel17.setText("Uwagi");

		javax.swing.GroupLayout moneyPanelLayout = new javax.swing.GroupLayout(
				moneyPanel);
		moneyPanel.setLayout(moneyPanelLayout);
		moneyPanelLayout
				.setHorizontalGroup(moneyPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								moneyPanelLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												moneyPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.TRAILING,
																false)
														.addComponent(
																jLabel15,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addComponent(
																jLabel16,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addComponent(
																jLabel17,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																83,
																Short.MAX_VALUE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addGroup(
												moneyPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																moneyPanelLayout
																		.createParallelGroup(
																				javax.swing.GroupLayout.Alignment.LEADING,
																				false)
																		.addComponent(
																				moneyCostTextField,
																				javax.swing.GroupLayout.Alignment.TRAILING)
																		.addComponent(
																				serviceTypeComboBox,
																				javax.swing.GroupLayout.Alignment.TRAILING,
																				0,
																				150,
																				Short.MAX_VALUE))
														.addComponent(
																jScrollPane4,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																373,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addContainerGap(19, Short.MAX_VALUE)));
		moneyPanelLayout
				.setVerticalGroup(moneyPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								moneyPanelLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												moneyPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																serviceTypeComboBox,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(jLabel15))
										.addGap(18, 18, 18)
										.addGroup(
												moneyPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																moneyCostTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(jLabel16))
										.addGap(18, 18, 18)
										.addGroup(
												moneyPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																jScrollPane4,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																191,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(jLabel17))
										.addContainerGap(23, Short.MAX_VALUE)));

		addVehicle
				.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
		addVehicle.setMaximumSize(new java.awt.Dimension(495, 301));
		addVehicle.setMinimumSize(new java.awt.Dimension(495, 301));

		client.setModel(new javax.swing.DefaultComboBoxModel(new String[] {
				"Item 1", "Item 2", "Item 3", "Item 4" }));

		productionDate.setText("YYYY-MM-DD");

		brand.setModel(new javax.swing.DefaultComboBoxModel(new String[] {
				"Item 1", "Item 2", "Item 3", "Item 4" }));

		jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel18.setText("Klient");

		jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel19.setText("VIN");

		jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel20.setText("Data produkcji");

		jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel21.setText("Marka");

		jLabel27.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel27.setText("Numer rejestracyjny");

		javax.swing.GroupLayout addVehicleLayout = new javax.swing.GroupLayout(
				addVehicle);
		addVehicle.setLayout(addVehicleLayout);
		addVehicleLayout
				.setHorizontalGroup(addVehicleLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								addVehicleLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												addVehicleLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																addVehicleLayout
																		.createParallelGroup(
																				javax.swing.GroupLayout.Alignment.TRAILING)
																		.addGroup(
																				addVehicleLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addComponent(
																								jLabel18,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								85,
																								javax.swing.GroupLayout.PREFERRED_SIZE)
																						.addComponent(
																								jLabel19,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								85,
																								javax.swing.GroupLayout.PREFERRED_SIZE))
																		.addComponent(
																				jLabel20,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				85,
																				javax.swing.GroupLayout.PREFERRED_SIZE))
														.addComponent(
																jLabel21,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																85,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jLabel27,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																85,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												addVehicleLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING,
																false)
														.addComponent(
																vehicleType)
														.addComponent(
																client,
																0,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addComponent(vin)
														.addComponent(
																productionDate)
														.addComponent(brand, 0,
																200,
																Short.MAX_VALUE))
										.addContainerGap(196, Short.MAX_VALUE)));
		addVehicleLayout
				.setVerticalGroup(addVehicleLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								addVehicleLayout
										.createSequentialGroup()
										.addGap(29, 29, 29)
										.addGroup(
												addVehicleLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																client,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(jLabel18))
										.addGap(18, 18, 18)
										.addGroup(
												addVehicleLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																vin,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(jLabel19))
										.addGap(18, 18, 18)
										.addGroup(
												addVehicleLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																productionDate,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(jLabel20))
										.addGap(18, 18, 18)
										.addGroup(
												addVehicleLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																brand,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(jLabel21))
										.addGap(18, 18, 18)
										.addGroup(
												addVehicleLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																vehicleType,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(jLabel27))
										.addContainerGap(100, Short.MAX_VALUE)));

		emptyTop.setMaximumSize(new java.awt.Dimension(663, 100));
		emptyTop.setMinimumSize(new java.awt.Dimension(663, 100));
		emptyTop.setPreferredSize(new java.awt.Dimension(663, 100));

		javax.swing.GroupLayout emptyTopLayout = new javax.swing.GroupLayout(
				emptyTop);
		emptyTop.setLayout(emptyTopLayout);
		emptyTopLayout.setHorizontalGroup(emptyTopLayout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 663,
				Short.MAX_VALUE));
		emptyTopLayout.setVerticalGroup(emptyTopLayout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 100,
				Short.MAX_VALUE));

		finalizationPanel.setCursor(new java.awt.Cursor(
				java.awt.Cursor.DEFAULT_CURSOR));
		finalizationPanel.setMaximumSize(new java.awt.Dimension(495, 301));
		finalizationPanel.setMinimumSize(new java.awt.Dimension(495, 301));

		finalClientLabel.setFocusable(false);

		finalVehicleLabel.setFocusable(false);

		finalServiceLabel.setFocusable(false);

		finalCostLabel.setFocusable(false);

		finalNoteLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
		finalNoteLabel.setFocusable(false);
		finalNoteLabel
				.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
		finalNoteLabel.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

		jLabel22.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel22.setText("Uwagi:");
		jLabel22.setVerticalAlignment(javax.swing.SwingConstants.TOP);
		jLabel22.setFocusable(false);
		jLabel22.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

		jLabel23.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel23.setText("Wstępny koszt:");
		jLabel23.setVerticalAlignment(javax.swing.SwingConstants.TOP);
		jLabel23.setFocusable(false);
		jLabel23.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

		jLabel24.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel24.setText("Usługa:");
		jLabel24.setVerticalAlignment(javax.swing.SwingConstants.TOP);
		jLabel24.setFocusable(false);
		jLabel24.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

		jLabel25.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel25.setText("Pojazd:");
		jLabel25.setVerticalAlignment(javax.swing.SwingConstants.TOP);
		jLabel25.setFocusable(false);
		jLabel25.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

		jLabel26.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		jLabel26.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel26.setText("Klient:");
		jLabel26.setVerticalAlignment(javax.swing.SwingConstants.TOP);
		jLabel26.setFocusable(false);
		jLabel26.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

		javax.swing.GroupLayout finalizationPanelLayout = new javax.swing.GroupLayout(
				finalizationPanel);
		finalizationPanel.setLayout(finalizationPanelLayout);
		finalizationPanelLayout
				.setHorizontalGroup(finalizationPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								finalizationPanelLayout
										.createSequentialGroup()
										.addGap(21, 21, 21)
										.addGroup(
												finalizationPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.TRAILING)
														.addComponent(
																jLabel22,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																104,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jLabel23,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																104,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jLabel24,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																104,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jLabel25,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																104,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jLabel26,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																104,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addGroup(
												finalizationPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING,
																false)
														.addComponent(
																finalClientLabel,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addComponent(
																finalVehicleLabel,
																javax.swing.GroupLayout.Alignment.TRAILING,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addComponent(
																finalServiceLabel,
																javax.swing.GroupLayout.Alignment.TRAILING,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addComponent(
																finalCostLabel,
																javax.swing.GroupLayout.Alignment.TRAILING,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addComponent(
																finalNoteLabel,
																javax.swing.GroupLayout.Alignment.TRAILING,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																335,
																Short.MAX_VALUE))
										.addContainerGap(25, Short.MAX_VALUE)));
		finalizationPanelLayout
				.setVerticalGroup(finalizationPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								finalizationPanelLayout
										.createSequentialGroup()
										.addGap(24, 24, 24)
										.addGroup(
												finalizationPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																finalClientLabel)
														.addComponent(jLabel26))
										.addGap(18, 18, 18)
										.addGroup(
												finalizationPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																finalVehicleLabel)
														.addComponent(jLabel25))
										.addGap(18, 18, 18)
										.addGroup(
												finalizationPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																finalServiceLabel)
														.addComponent(jLabel24))
										.addGap(18, 18, 18)
										.addGroup(
												finalizationPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																finalCostLabel)
														.addComponent(jLabel23))
										.addGap(18, 18, 18)
										.addGroup(
												finalizationPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																finalNoteLabel,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																105,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(jLabel22))
										.addContainerGap(44, Short.MAX_VALUE)));

		orderTablePanel.setMaximumSize(new java.awt.Dimension(495, 295));
		orderTablePanel.setMinimumSize(new java.awt.Dimension(495, 295));

		orderTable.setModel(new javax.swing.table.DefaultTableModel(
				new Object[][] {

				}, new String[] { "ID", "ID Klienta", "ID Pojazdu",
						"Data złożenia", "Koszt", "Uwagi" }) {
			Class[] types = new Class[] { java.lang.Integer.class,
					java.lang.Integer.class, java.lang.Integer.class,
					java.lang.Object.class, java.lang.Float.class,
					java.lang.String.class };
			boolean[] canEdit = new boolean[] { false, false, false, false,
					false, false };

			public Class getColumnClass(int columnIndex) {
				return types[columnIndex];
			}

			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return canEdit[columnIndex];
			}
		});
		orderTable
				.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		jScrollPane5.setViewportView(orderTable);

		javax.swing.GroupLayout orderTablePanelLayout = new javax.swing.GroupLayout(
				orderTablePanel);
		orderTablePanel.setLayout(orderTablePanelLayout);
		orderTablePanelLayout.setHorizontalGroup(orderTablePanelLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(jScrollPane5,
						javax.swing.GroupLayout.DEFAULT_SIZE, 495,
						Short.MAX_VALUE));
		orderTablePanelLayout.setVerticalGroup(orderTablePanelLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(jScrollPane5,
						javax.swing.GroupLayout.DEFAULT_SIZE, 295,
						Short.MAX_VALUE));

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setTitle("Warsztat samochodowy");

		topPanel.setMaximumSize(new java.awt.Dimension(663, 100));
		topPanel.setMinimumSize(new java.awt.Dimension(663, 100));

		javax.swing.GroupLayout topPanelLayout = new javax.swing.GroupLayout(
				topPanel);
		topPanel.setLayout(topPanelLayout);
		topPanelLayout.setHorizontalGroup(topPanelLayout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 663,
				Short.MAX_VALUE));
		topPanelLayout.setVerticalGroup(topPanelLayout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 100,
				Short.MAX_VALUE));

		leftPanel
				.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
		leftPanel.setMaximumSize(new java.awt.Dimension(495, 301));
		leftPanel.setMinimumSize(new java.awt.Dimension(495, 301));

		javax.swing.GroupLayout leftPanelLayout = new javax.swing.GroupLayout(
				leftPanel);
		leftPanel.setLayout(leftPanelLayout);
		leftPanelLayout.setHorizontalGroup(leftPanelLayout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 495,
				Short.MAX_VALUE));
		leftPanelLayout.setVerticalGroup(leftPanelLayout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 301,
				Short.MAX_VALUE));

		buttons.setMaximumSize(new java.awt.Dimension(158, 290));
		buttons.setMinimumSize(new java.awt.Dimension(158, 290));

		addClientButton.setText("Dodaj klienta");
		addClientButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				addClientButtonActionPerformed(evt);
			}
		});

		addVehicleButton.setText("Dodaj pojazd");
		addVehicleButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				addVehicleButtonActionPerformed(evt);
			}
		});

		saveButton.setText("Zapisz");
		saveButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				saveButtonActionPerformed(evt);
			}
		});

		nextButton.setText("Dalej");
		nextButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				nextButtonActionPerformed(evt);
			}
		});

		cancelButton.setText("Anuluj");
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cancelButtonActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout buttonsLayout = new javax.swing.GroupLayout(
				buttons);
		buttons.setLayout(buttonsLayout);
		buttonsLayout
				.setHorizontalGroup(buttonsLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								buttonsLayout
										.createSequentialGroup()
										.addGap(22, 22, 22)
										.addGroup(
												buttonsLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.TRAILING)
														.addComponent(
																cancelButton,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																95,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																nextButton,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																95,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																saveButton,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																95,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																addVehicleButton)
														.addComponent(
																addClientButton))
										.addContainerGap(41, Short.MAX_VALUE)));
		buttonsLayout
				.setVerticalGroup(buttonsLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								buttonsLayout
										.createSequentialGroup()
										.addContainerGap()
										.addComponent(addClientButton)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(addVehicleButton)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(saveButton)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(nextButton)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(cancelButton)
										.addContainerGap(120, Short.MAX_VALUE)));

		jMenu1.setText("Klienci");

		jMenuItem1.setText("Wyświetl klientów");
		jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItem1ActionPerformed(evt);
			}
		});
		jMenu1.add(jMenuItem1);

		jMenuItem2.setText("Dodaj klienta");
		jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItem2ActionPerformed(evt);
			}
		});
		jMenu1.add(jMenuItem2);

		jMenuBar1.add(jMenu1);

		jMenu2.setText("Pojazdy");

		jMenuItem3.setText("Wyświetl pojazdy");
		jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItem3ActionPerformed(evt);
			}
		});
		jMenu2.add(jMenuItem3);

		jMenuItem4.setText("Dodaj pojazd");
		jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItem4ActionPerformed(evt);
			}
		});
		jMenu2.add(jMenuItem4);

		jMenuBar1.add(jMenu2);

		jMenu3.setText("Zamówienia");

		jMenuItem5.setText("Wyświetl zamówienia");
		jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItem5ActionPerformed(evt);
			}
		});
		jMenu3.add(jMenuItem5);

		jMenuItem6.setText("Dodaj nowe zamówienie");
		jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItem6ActionPerformed(evt);
			}
		});
		jMenu3.add(jMenuItem6);

		jMenuBar1.add(jMenu3);

		setJMenuBar(jMenuBar1);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
				getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(topPanel, javax.swing.GroupLayout.DEFAULT_SIZE,
						javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(
						layout.createSequentialGroup()
								.addComponent(leftPanel,
										javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(buttons,
										javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE).addGap(4, 4, 4)));
		layout.setVerticalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addComponent(topPanel,
										javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.LEADING)
												.addComponent(
														leftPanel,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														Short.MAX_VALUE)
												.addGroup(
														layout.createSequentialGroup()
																.addGap(0,
																		0,
																		Short.MAX_VALUE)
																.addComponent(
																		buttons,
																		javax.swing.GroupLayout.PREFERRED_SIZE,
																		javax.swing.GroupLayout.DEFAULT_SIZE,
																		javax.swing.GroupLayout.PREFERRED_SIZE)))));

		pack();
	}// </editor-fold>

	// Variables declaration - do not modify
	private javax.swing.JButton addClientButton;
	private javax.swing.JPanel addClientPanel;
	private javax.swing.JPanel addVehicle;
	private javax.swing.JButton addVehicleButton;
	private javax.swing.JTextArea additionalAddress;
	private javax.swing.JSpinner apartmentNumber;
	private javax.swing.JComboBox brand;
	private javax.swing.JSpinner buildingNumber;
	private javax.swing.ButtonGroup buttonGroup1;
	private javax.swing.ButtonGroup buttonGroup2;
	private javax.swing.JPanel buttons;
	private javax.swing.JButton cancelButton;
	private javax.swing.JTextField city;
	private javax.swing.JComboBox client;
	private javax.swing.JTable clientTable;
	private javax.swing.JPanel clientTablePanel;
	private javax.swing.JPanel companyPanel;
	private javax.swing.JRadioButton companyRadio;
	private javax.swing.JTextField email;
	private javax.swing.JPanel emptyTop;
	private javax.swing.JLabel finalClientLabel;
	private javax.swing.JLabel finalCostLabel;
	private javax.swing.JLabel finalNoteLabel;
	private javax.swing.JLabel finalServiceLabel;
	private javax.swing.JLabel finalVehicleLabel;
	private javax.swing.JPanel finalizationPanel;
	private javax.swing.JComboBox findByBrandCombo;
	private javax.swing.JTextField findByBrandTextField;
	private javax.swing.JPanel findClient;
	private javax.swing.JRadioButton findClientCompanyRadio;
	private javax.swing.JRadioButton findClientPersonRadio;
	private javax.swing.JTextField findClientTextField;
	private javax.swing.JPanel findVehicle;
	private javax.swing.JTextField firstName;
	private javax.swing.JButton jButton1;
	private javax.swing.JButton jButton2;
	private javax.swing.JComboBox serviceTypeComboBox;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel10;
	private javax.swing.JLabel jLabel11;
	private javax.swing.JLabel jLabel12;
	private javax.swing.JLabel jLabel13;
	private javax.swing.JLabel jLabel14;
	private javax.swing.JLabel jLabel15;
	private javax.swing.JLabel jLabel16;
	private javax.swing.JLabel jLabel17;
	private javax.swing.JLabel jLabel18;
	private javax.swing.JLabel jLabel19;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel20;
	private javax.swing.JLabel jLabel21;
	private javax.swing.JLabel jLabel22;
	private javax.swing.JLabel jLabel23;
	private javax.swing.JLabel jLabel24;
	private javax.swing.JLabel jLabel25;
	private javax.swing.JLabel jLabel26;
	private javax.swing.JLabel jLabel27;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JLabel jLabel5;
	private javax.swing.JLabel jLabel6;
	private javax.swing.JLabel jLabel7;
	private javax.swing.JLabel jLabel8;
	private javax.swing.JLabel jLabel9;
	private javax.swing.JMenu jMenu1;
	private javax.swing.JMenu jMenu2;
	private javax.swing.JMenu jMenu3;
	private javax.swing.JMenuBar jMenuBar1;
	private javax.swing.JMenuItem jMenuItem1;
	private javax.swing.JMenuItem jMenuItem2;
	private javax.swing.JMenuItem jMenuItem3;
	private javax.swing.JMenuItem jMenuItem4;
	private javax.swing.JMenuItem jMenuItem5;
	private javax.swing.JMenuItem jMenuItem6;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JScrollPane jScrollPane2;
	private javax.swing.JScrollPane jScrollPane3;
	private javax.swing.JScrollPane jScrollPane4;
	private javax.swing.JScrollPane jScrollPane5;
	private javax.swing.JTextArea moneyNoteArea;
	private javax.swing.JTextField moneyCostTextField;
	private javax.swing.JTextField lastName;
	private javax.swing.JPanel leftPanel;
	private javax.swing.JPanel moneyPanel;
	private javax.swing.JTextField name;
	private javax.swing.JButton nextButton;
	private javax.swing.JTextField nip;
	private javax.swing.JTable orderTable;
	private javax.swing.JPanel orderTablePanel;
	private javax.swing.JPanel personPanel;
	private javax.swing.JRadioButton personRadio;
	private javax.swing.JTextField pesel;
	private javax.swing.JTextField phoneNumber;
	private javax.swing.JTextField postalCode;
	private javax.swing.JTextField productionDate;
	private javax.swing.JButton saveButton;
	private javax.swing.JTextField street;
	private javax.swing.JPanel topPanel;
	private javax.swing.JTable vehicleTable;
	private javax.swing.JPanel vehicleTablePanel;
	private javax.swing.JTextField vehicleType;
	private javax.swing.JTextField vin;
	// End of variables declaration
}
