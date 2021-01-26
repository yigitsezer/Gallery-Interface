package sample;

import com.sun.xml.internal.ws.api.message.ExceptionHasMessage;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import java.sql.Connection;
import java.sql.ResultSet;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.io.IOException;
import java.sql.*;
import javafx.stage.Modality;

public class Controller {
    Connection connection;

    public TextField findCarId;
    public TextField findCarFuelType;
    public TextField findCarBrand;
    public TextField findCarName;
    public TextField findCarColor;
    public CheckBox findIsSecondHand;
    public CheckBox findIsUnused;
    public TextField findCarMinPrice;
    public TextField findCarMaxPrice;
    public CheckBox includeSold;

    public TextField findEmployeeSsn;
    public TextField findEmployeeName;
    public TextField findEmployeeTitle;
    public TextField findEmployeeMinSalary;
    public TextField findEmployeeMaxSalary;
    public CheckBox findEmployeeOld;

    public TextField findHistEmployeeId;
    public TextField findHistEmployeeSsn;
    public TextField findHistEmployeeDesc;
    public CheckBox findHistEmployeeOld;

    public TextField salesId;
    public TextField salesDate;
    public TextField salesCustomerSsn;
    public TextField salesCarId;
    public TextField salesInvoiceId;
    public TextField salesEmployeeSsn;
    public CheckBox salesSell;
    public CheckBox salesBuy;

    public TextField invoiceId;
    public TextField invoiceBank;
    public TextField invoicePaymentTpye;

    public TextField customersSsn;
    public TextField customersName;

    public TabPane root;
    public TableView carsTable;
    public TableView employeesTable;
    public TableView employeesHistoryTable;
    public TableView salesTable;
    public TableView invoicesTable;
    public TableView customersTable;

    public TextField sellCarId;
    public TextField sellCustomerName;
    public TextField sellCustomerSsn;
    public TextField sellCarPrice;
    public TextField sellCarEmpSsn;
    public TextField sellCarBank;
    public TextField sellCarPaymentType;

    public TextField buyCarModel;
    public TextField buyCarBrand;
    public TextField buyCarColor;
    public CheckBox buyCarUsed;
    public TextField buyCarFuelType;
    public TextField buyCarKm;
    public TextField buyPrice;
    public TextField buyEmpSsn;
    public TextField buyCustomerSsn;
    public TextField buyCustomerName;

    int selectedCarId = 0;

    public Controller(Connection connection) throws SQLException {
        this.connection = connection;
        connection.setAutoCommit(false);
        System.out.println("Controller is initialized");
    }

    @FXML
    private void buyCar(ActionEvent event) throws SQLException {
        if (connection.isClosed())
            this.reconnect();

        connection.rollback();
        String SQL = "SELECT count(*) FROM employees WHERE employees.ssn = ? AND employees.salary IS NOT NULL";
        PreparedStatement ps = connection.prepareStatement(SQL);
        ps.setInt(1, Integer.parseInt(buyEmpSsn.getText()));
        ResultSet rs = ps.executeQuery();

        rs.next();
        int count = rs.getInt(1);
        if (count == 0) {
            System.out.println("Invalid inputs");
        } else {
            SQL = "INSERT IGNORE INTO customers VALUES(?, ?)";
            ps = connection.prepareStatement(SQL);
            ps.setInt(1, Integer.parseInt(buyCustomerSsn.getText()));
            ps.setString(2, buyCustomerName.getText());
            ps.executeUpdate();

            //Doesn't require validation
            SQL = "INSERT INTO invoices (bank, payment_type) VALUES('Gallery', 'cash')";
            connection.createStatement().executeUpdate(SQL);

            SQL = "INSERT INTO cars (fuel_type, price, brand, model, color, kilometers, second_hand) VALUES (?, ?, ?, ?, ?, ?, ?)";
            ps = connection.prepareStatement(SQL);
            ps.setString(1, buyCarFuelType.getText());
            ps.setInt(2, Integer.parseInt(buyPrice.getText()));
            ps.setString(3, buyCarBrand.getText());
            ps.setString(4, buyCarModel.getText());
            ps.setString(5, buyCarColor.getText());
            ps.setInt(6, (buyCarUsed.isSelected() ? Integer.parseInt(buyCarKm.getText()) : 0));
            ps.setString(7, (buyCarUsed.isSelected() ? "second hand" : "first hand"));
            ps.executeUpdate();

            SQL = "INSERT INTO sales (customer_ssn, car_id, invoice_id, employee_ssn, customer_transaction) VALUES (?, (SELECT cars.id FROM cars ORDER BY cars.id DESC LIMIT 1), (SELECT invoices.id FROM invoices ORDER BY invoices.id DESC LIMIT 1), ?, 'sell')";
            ps = connection.prepareStatement(SQL);
            ps.setInt(1, Integer.parseInt(buyCustomerSsn.getText()));
            ps.setInt(2, Integer.parseInt(buyEmpSsn.getText()));
            ps.executeUpdate();

            connection.commit();
        }
        event.consume();
    }

    @FXML
    private void sellCar(ActionEvent event) throws SQLException {
        if (connection.isClosed())
            this.reconnect();

        connection.rollback();
        String SQL = "SELECT count(*) FROM customers, cars, employees WHERE" +
                " employees.ssn = ?" +
                " AND cars.id = ?" +
                " AND employees.salary IS NOT NULL " +
                " AND cars.availability = 'available'";
        PreparedStatement ps = connection.prepareStatement(SQL);
        ps.setInt(1, Integer.parseInt(sellCarEmpSsn.getText()));
        ps.setInt(2, Integer.parseInt(sellCarId.getText()));
        ResultSet rs = ps.executeQuery();

        rs.next();
        int count = rs.getInt(1);
        if (count == 0) {
            System.out.println("Invalid inputs");
        } else {
            SQL = "INSERT IGNORE INTO customers VALUES(?, ?)";
            ps = connection.prepareStatement(SQL);
            ps.setInt(1, Integer.parseInt(sellCustomerSsn.getText()));
            ps.setString(2, sellCustomerName.getText());
            ps.executeUpdate();

            SQL = "INSERT INTO invoices (bank, payment_type) VALUES(?, ?)";
            ps = connection.prepareStatement(SQL);
            ps.setString(1, sellCarBank.getText());
            ps.setString(2, sellCarPaymentType.getText());
            ps.executeUpdate();

            SQL = "INSERT INTO sales (customer_ssn, car_id, invoice_id, employee_ssn, customer_transaction) VALUES (?, ?, (SELECT invoices.id FROM invoices ORDER BY invoices.id DESC LIMIT 1), ?, 'buy')";
            ps = connection.prepareStatement(SQL);
            ps.setInt(1, Integer.parseInt(sellCustomerSsn.getText()));
            ps.setInt(2, Integer.parseInt(sellCarId.getText()));
            ps.setInt(3, Integer.parseInt(sellCarEmpSsn.getText()));
            ps.executeUpdate();

            SQL = "UPDATE cars SET availability = 'sold' WHERE cars.id = ?";
            ps = connection.prepareStatement(SQL);
            ps.setInt(1, Integer.parseInt(sellCarId.getText()));
            ps.executeUpdate();

            connection.commit();
        }
        event.consume();
    }

    @FXML
    private void rebuildCustomers(ActionEvent event) throws SQLException {
        buildCustomers();
        event.consume();
    }

    public void buildCustomers() throws SQLException {
        if (connection.isClosed())
            this.reconnect();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("customers").append(" WHERE ");
        if (!customersSsn.getText().isEmpty())
            stringBuilder.append("customers.ssn = ").append(customersSsn.getText()).append(" AND ");
        if (!customersName.getText().isEmpty())
            stringBuilder.append("customers.name_surname LIKE ").append("'%").append(customersName.getText()).append("%'").append(" AND ");

        stringBuilder.append("true");
        System.out.println(stringBuilder.toString());

        buildTable(customersTable, stringBuilder.toString());
    }

    @FXML
    private void rebuildInvoices(ActionEvent event) throws SQLException {
        buildInvoices();
        event.consume();
    }

    public void buildInvoices() throws SQLException {
        if (connection.isClosed())
            this.reconnect();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("invoices").append(" WHERE ");
        if (!invoiceId.getText().isEmpty())
            stringBuilder.append("invoices.id = ").append(invoiceId.getText()).append(" AND ");
        if (!invoiceBank.getText().isEmpty())
            stringBuilder.append("invoices.bank LIKE ").append("'%").append(invoiceBank.getText()).append("%'").append(" AND ");
        if (!invoicePaymentTpye.getText().isEmpty())
            stringBuilder.append("invoices.payment_type = ").append(invoicePaymentTpye.getText()).append(" AND ");

        stringBuilder.append("true");
        System.out.println(stringBuilder.toString());

        buildTable(invoicesTable, stringBuilder.toString());
    }

    @FXML
    private void rebuildSalesHistory(ActionEvent event) throws SQLException {
        buildSalesHistory();
        event.consume();
    }

    public void buildSalesHistory() throws SQLException {
        if (connection.isClosed())
            this.reconnect();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("sales").append(" WHERE ");
        if (!salesId.getText().isEmpty())
            stringBuilder.append("sales.id = ").append(salesId.getText()).append(" AND ");
        if (!salesDate.getText().isEmpty())
            stringBuilder.append("sales.date LIKE ").append("'%").append(salesDate.getText()).append("%'").append(" AND ");
        if (!salesCustomerSsn.getText().isEmpty())
            stringBuilder.append("sales.customer_ssn = ").append(salesCustomerSsn.getText()).append(" AND ");
        if (!salesCarId.getText().isEmpty())
            stringBuilder.append("sales.car_id = ").append(salesCarId.getText()).append(" AND ");
        if (!salesInvoiceId.getText().isEmpty())
            stringBuilder.append("sales.invoice_id = ").append(salesInvoiceId.getText()).append(" AND ");
        if (!salesEmployeeSsn.getText().isEmpty())
            stringBuilder.append("sales.employee_ssn = ").append(salesEmployeeSsn.getText()).append(" AND ");
        if (!salesBuy.isSelected() && salesSell.isSelected())
            stringBuilder.append("sales.customer_transaction = ").append("'sell'").append(" AND ");
        else if (salesBuy.isSelected() && !salesSell.isSelected())
            stringBuilder.append("sales.customer_transaction = ").append("'buy'").append(" AND ");

        stringBuilder.append("true");
        System.out.println(stringBuilder.toString());

        buildTable(salesTable, stringBuilder.toString());
    }

    @FXML
    private void rebuildEmployeesHistory(ActionEvent event) throws SQLException {
        buildEmployeesHistory();
        event.consume();
    }

    public void buildEmployeesHistory() throws SQLException {
        if (connection.isClosed())
            this.reconnect();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("employee_file").append(" WHERE ");
        if (!findHistEmployeeId.getText().isEmpty())
            stringBuilder.append("employee_file.id = ").append(findHistEmployeeId.getText()).append(" AND ");
        if (!findHistEmployeeSsn.getText().isEmpty())
            stringBuilder.append("employee_file.ssn = ").append(findHistEmployeeSsn.getText()).append(" AND ");
        if (!findHistEmployeeDesc.getText().isEmpty())
            stringBuilder.append("employee_file.description LIKE ").append("'%").append(findHistEmployeeDesc.getText()).append("%'").append(" AND ");
        if (!findHistEmployeeOld.isSelected())
            stringBuilder.append("employee_file.fire_date IS NULL AND ");

        stringBuilder.append("true");
        System.out.println(stringBuilder.toString());

        buildTable(employeesHistoryTable, stringBuilder.toString());
    }

    @FXML
    private void rebuildEmployees(ActionEvent event) throws SQLException {
        buildEmployees();
        event.consume();
    }

    public void buildEmployees() throws SQLException {
        if (connection.isClosed())
            this.reconnect();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("employees").append(" WHERE ");
        if (!findEmployeeSsn.getText().isEmpty())
            stringBuilder.append("employees.ssn = ").append(findEmployeeSsn.getText()).append(" AND ");
        if (!findEmployeeName.getText().isEmpty())
            stringBuilder.append("employees.name_surname LIKE ").append("'%").append(findEmployeeName.getText()).append("%'").append(" AND ");
        if (!findEmployeeTitle.getText().isEmpty())
            stringBuilder.append("employees.title LIKE ").append("'%").append(findEmployeeTitle.getText()).append("%'").append(" AND ");
        if (!findEmployeeMinSalary.getText().isEmpty() && !findEmployeeMaxSalary.getText().isEmpty())
            stringBuilder.append("employees.salary BETWEEN ").append(findEmployeeMinSalary.getText()).append(" AND ").append(findEmployeeMaxSalary.getText()).append(" AND ");
        else if (!findEmployeeMinSalary.getText().isEmpty())
            stringBuilder.append("employees.salary >= ").append(findEmployeeMinSalary.getText()).append(" AND ");
        else if (!findEmployeeMaxSalary.getText().isEmpty())
            stringBuilder.append("employees.salary <= ").append(findEmployeeMaxSalary.getText()).append(" AND ");
        if (!findEmployeeOld.isSelected())
            stringBuilder.append("employees.salary IS NOT NULL AND ");
        stringBuilder.append("true");
        System.out.println(stringBuilder.toString());

        buildTable(employeesTable, stringBuilder.toString());
    }

    @FXML
    private void rebuildCars(ActionEvent event) throws SQLException {
        buildCars();
        event.consume();
    }

    public void buildCars() throws SQLException {
        if (connection.isClosed())
            this.reconnect();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("cars").append(" WHERE ");
        if (!findCarId.getText().isEmpty())
            stringBuilder.append("cars.id = ").append(findCarId.getText()).append(" AND ");
        if (!findCarFuelType.getText().isEmpty())
            stringBuilder.append("cars.fuel_type LIKE ").append("'%").append(findCarFuelType.getText()).append("%'").append(" AND ");
        if (!findCarBrand.getText().isEmpty())
            stringBuilder.append("cars.brand LIKE ").append("'%").append(findCarBrand.getText()).append("%'").append(" AND ");
        if (!findCarName.getText().isEmpty())
            stringBuilder.append("cars.model LIKE ").append("'%").append(findCarName.getText()).append("%'").append(" AND ");
        if (!findCarColor.getText().isEmpty())
            stringBuilder.append("cars.color LIKE ").append("'%").append(findCarColor.getText()).append("%'").append(" AND ");
        if (!findCarMinPrice.getText().isEmpty() && !findCarMaxPrice.getText().isEmpty())
            stringBuilder.append("cars.price BETWEEN ").append(findCarMinPrice.getText()).append(" AND ").append(findCarMaxPrice.getText()).append(" AND ");
        else if (!findCarMinPrice.getText().isEmpty())
            stringBuilder.append("cars.price >= ").append(findCarMinPrice.getText()).append(" AND ");
        else if (!findCarMaxPrice.getText().isEmpty())
            stringBuilder.append("cars.price <= ").append(findCarMaxPrice.getText()).append(" AND ");
        if (!findIsSecondHand.isSelected() && findIsUnused.isSelected())
            stringBuilder.append("cars.second_hand = ").append("'first hand'").append(" AND ");
        else if (findIsSecondHand.isSelected() && !findIsUnused.isSelected())
            stringBuilder.append("cars.second_hand = ").append("'second hand'").append(" AND ");
        if (!includeSold.isSelected())
            stringBuilder.append("cars.availability = 'available' AND ");
        stringBuilder.append("true");
        System.out.println(stringBuilder.toString());

        buildTable(carsTable, stringBuilder.toString());
    }

    public void buildTable(TableView table, String sqlText) throws SQLException {
        ObservableList<ObservableList> data = FXCollections.observableArrayList();

        table.getItems().clear();
        table.getColumns().clear();

        String SQL = "SELECT * FROM " + sqlText;
        //ResultSet
        ResultSet rs = connection.createStatement().executeQuery(SQL);

        for(int i=0 ; i<rs.getMetaData().getColumnCount(); i++){
            //We are using non property style for making dynamic table
            final int j = i;
            TableColumn col = new TableColumn(rs.getMetaData().getColumnName(i+1));
            col.setCellValueFactory(new Callback<CellDataFeatures<ObservableList,String>,ObservableValue<String>>(){
                public ObservableValue<String> call(CellDataFeatures<ObservableList, String> param) {
                    if (param.getValue().get(j) == null)
                        return new SimpleStringProperty("<null>");
                    else return new SimpleStringProperty(param.getValue().get(j).toString());

                }
            });
            table.getColumns().addAll(col);
        }
        while(rs.next()){
            ObservableList<String> row = FXCollections.observableArrayList();
            for(int i=1 ; i<=rs.getMetaData().getColumnCount(); i++){
                row.add(rs.getString(i));
            }
            System.out.println(row);
            data.add(row);

        }
        table.setItems(data);
    }

    public void openRepairHistory(int id) throws IOException, SQLException {
        if (connection.isClosed())
            this.reconnect();

        connection.rollback();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("repair_history.fxml"));
        RepairHistoryController repairHistoryController = new RepairHistoryController();
        fxmlLoader.setController(repairHistoryController);
        Parent root1 = (Parent) fxmlLoader.load();
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(root.getScene().getWindow());
        stage.setTitle("Repair History");
        stage.setScene(new Scene(root1));
        stage.show();
        repairHistoryController.buildTable(id, connection);
        repairHistoryController.addRepairButton.setOnAction(e -> {
            try {
                if (connection.isClosed())
                    this.reconnect();

                String SQL = "INSERT INTO repair_history (car_id, part_repaired, repair_cost, repair_date) VALUES(?, ?, ?, current_date)";
                PreparedStatement ps = connection.prepareStatement(SQL);
                ps.setInt(1, selectedCarId);
                ps.setString(2, repairHistoryController.repairPart.getText());
                ps.setInt(3, Integer.parseInt(repairHistoryController.repairCost.getText()));
                ps.executeUpdate();
                connection.commit();

                stage.close();
                openRepairHistory(selectedCarId);
            } catch (SQLException | IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    @FXML
    private void openNewEmployeeWindow(ActionEvent event) throws IOException, SQLException {
        openNewEmployeeWindow();
        event.consume();
    }

    public void openNewEmployeeWindow() throws IOException, SQLException {
        if (connection.isClosed())
            this.reconnect();

        connection.rollback();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("add_employee.fxml"));
        NewEmployeeController newEmployeeController = new NewEmployeeController();
        fxmlLoader.setController(newEmployeeController);
        Parent root1 = (Parent) fxmlLoader.load();
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(root.getScene().getWindow());
        stage.setTitle("New employee");
        stage.setScene(new Scene(root1));
        stage.show();
        newEmployeeController.addNewEmployeeFinalButton.setOnAction(e -> {
            try {
                if (connection.isClosed())
                    this.reconnect();

                connection.rollback();
                String SQL = "SELECT count(*) FROM employee_file WHERE employee_ssn = ? AND fire_date IS NULL ORDER BY employee_file.id LIMIT 1";
                PreparedStatement ps = connection.prepareStatement(SQL);
                ps.setInt(1, Integer.parseInt(newEmployeeController.newEmpSsn.getText()));
                ResultSet rs = ps.executeQuery();

                rs.next();
                int count = rs.getInt(1);
                if (count == 1) {
                    System.out.println("Employee exists and is still working");
                } else {
                    SQL = "INSERT IGNORE INTO employees (ssn, name_surname, title, salary) VALUES (?, ?, ?, ?)";
                    ps = connection.prepareStatement(SQL);
                    ps.setInt(1, Integer.parseInt(newEmployeeController.newEmpSsn.getText()));
                    ps.setString(2, newEmployeeController.newEmpName.getText());
                    ps.setString(3, newEmployeeController.newEmpTitle.getText());
                    ps.setInt(4, Integer.parseInt(newEmployeeController.newEmpSalary.getText()));
                    ps.executeUpdate();

                    SQL = "UPDATE employees SET salary = ? WHERE ssn = ?";
                    ps = connection.prepareStatement(SQL);
                    ps.setInt(1, Integer.parseInt(newEmployeeController.newEmpSalary.getText()));
                    ps.setInt(2, Integer.parseInt(newEmployeeController.newEmpSsn.getText()));
                    ps.executeUpdate();

                    SQL = "INSERT INTO employee_file(employee_ssn, description) VALUES(?, ?)";
                    ps = connection.prepareStatement(SQL);
                    ps.setInt(1, Integer.parseInt(newEmployeeController.newEmpSsn.getText()));
                    ps.setString(2, newEmployeeController.newEmpDescription.getText());
                    ps.executeUpdate();

                    connection.commit();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

    @FXML
    public void removeEmployee(ActionEvent event) throws SQLException {
        if (connection.isClosed())
            this.reconnect();

        connection.rollback();
        String selectedSsn = ((ObservableList<String>) employeesTable.getItems().get(employeesTable.getSelectionModel().getFocusedIndex())).get(0);
        String SQL = "SELECT count(*) FROM employee_file WHERE employee_ssn = ? AND fire_date IS NULL ORDER BY employee_file.id LIMIT 1";
        PreparedStatement ps = connection.prepareStatement(SQL);
        ps.setInt(1, Integer.parseInt(selectedSsn));

        ResultSet rs = ps.executeQuery();
        rs.next();
        int count = rs.getInt(1);
        if (count == 1) {
            SQL = "UPDATE employees SET employees.salary = (?) WHERE employees.ssn = ?";
            ps = connection.prepareStatement(SQL);
            ps.setNull(1, Types.INTEGER);
            ps.setInt(2, Integer.parseInt(selectedSsn));
            ps.executeUpdate();

            SQL = "UPDATE employee_file SET employee_file.fire_date = current_date WHERE employee_file.id = ((SELECT employee_file.id FROM employee_file WHERE employee_ssn = ? AND fire_date IS NULL ORDER BY employee_file.id DESC LIMIT 1))";
            ps = connection.prepareStatement(SQL);
            ps.setInt(1, Integer.parseInt(selectedSsn));
            ps.executeUpdate();

            connection.commit();
        } else {
            System.out.println("ERR");
        }
        event.consume();
    }


    private void reconnect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(Main.SERVER, Main.USER, Main.PASSWORD);
        } catch(Exception e){
            System.out.println("Connection failed.");
            System.out.println(e);
        }
    }
}
