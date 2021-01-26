package sample;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.Callback;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;



public class RepairHistoryController {
    public TableView repairHistoryTable;
    public TextField repairPart;
    public TextField repairCost;
    public Button addRepairButton;

    public RepairHistoryController () { }

    public void buildTable(int id, Connection connection) throws SQLException {
        ObservableList<ObservableList> data = FXCollections.observableArrayList();
        TableView table = repairHistoryTable;
        table.getItems().clear();
        table.getColumns().clear();

        String SQL = "SELECT * FROM repair_history WHERE car_id = " + id;
        //ResultSet
        ResultSet rs = connection.createStatement().executeQuery(SQL);

        for(int i=0 ; i<rs.getMetaData().getColumnCount(); i++){
            //We are using non property style for making dynamic table
            final int j = i;
            TableColumn col = new TableColumn(rs.getMetaData().getColumnName(i+1));
            col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList,String>,ObservableValue<String>>(){
                public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
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
            System.out.println("Row [1] added "+row );
            data.add(row);

        }
        table.setItems(data);
    }
}
