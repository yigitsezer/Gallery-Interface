## As of 26.01.21

I have removed server credentials and replaced with localhost placeholders, edit them in the Main.java
```
    static String SERVER = "jdbc:mysql://localhost:3306/database";
    static String USER = "admin";
    static String PASSWORD = "admin";
```

# Description

This is the "Car Gallery Interface" we have created for our Database Management Systems Course that uses JavaFX for UI elements.

The UI is used to insert and view data from the gallery database such as creating a car sale, adding employees, getting all the cars from the database fitting specific descriptions (using filters).

This interface is specifically created for the MySQL Database we have created and it's CREATE statements are included in the written report. For now it can be used by the login credentals I have left in the code but I will surely remove them, check Extras for more information about that.

## Refresh tables

Tables do not refresh themselves with the exception of repair history table, click "Apply Filters" in your tab to refresh the table by sending another query.

## Executable

.jar executables are present in the report for their respective Operating Systems (Windows and OSX), but you might not have a problem running either executables in either OS, maybe even Linux. If your system has JDK 8 or above installed, the jar files will not work by themselves so you will have to download JavaFX libraries together with the executable and run this command when .jar and (JavaFX)/lib files are in the same directory

```
java --module-path ./javafx-sdk-11.0.2/lib --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.web -jar GalleryInterface.jar
```

## Written Report
[Google Docs](https://docs.google.com/document/d/1t1mxrufCp3eZs2qJamh0VA10-WlxcatRHlnnf_2BCAM/edit#)

## SQL Injection and present credentals

By no means this code is 100% SQL Injection protected even though I used PreparedStatement s wherever I could.
