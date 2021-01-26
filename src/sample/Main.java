package sample;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class Main extends Application {

    static String SERVER = "jdbc:mysql://localhost:3306/database";
    static String USER = "admin";
    static String PASSWORD = "admin";

    Connection connection;

    @Override
    public void start(Stage primaryStage) throws Exception{
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                    SERVER, USER, PASSWORD);
        } catch(Exception e){
            System.out.println("Connection failed.");
            System.out.println(e);
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Controller controller = new Controller(connection);
        fxmlLoader.setController(controller);
        Parent root = (Parent) fxmlLoader.load();
        primaryStage.setOnHiding( event -> {
            try {
                if (!controller.connection.isClosed())
                    controller.connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        primaryStage.setTitle("Gallery Interface");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        controller.findIsUnused.setSelected(true);
        controller.findIsSecondHand.setSelected(true);
        controller.findEmployeeOld.setSelected(false);
        controller.buildCars();
        controller.buildEmployees();
        controller.buildEmployeesHistory();
        controller.buildSalesHistory();
        controller.buildInvoices();
        controller.buildCustomers();

        controller.carsTable.setRowFactory( tv -> {
            TableRow row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    System.out.println();
                    try {
                        String data = ((ObservableList<String>) controller.carsTable.getItems().get(row.getIndex())).get(0);
                        int index = Integer.parseInt(data);
                        controller.selectedCarId = index;
                        controller.openRepairHistory(index);
                    } catch (IOException | SQLException e) {
                        e.printStackTrace();
                    }
                }
            });
            return row ;
        });
    }

    @Override
    public void stop() throws SQLException {
        if (connection != null) {
            connection.close();
            System.out.println("connnection closed");
        }
        System.out.println("Stage is closing");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
