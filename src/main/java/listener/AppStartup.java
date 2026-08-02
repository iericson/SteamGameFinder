/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package listener;

/**
 *
 * @author Isaac Ericson
 */

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import util.DatabaseInitializer;
import util.CSVImporter;
import util.DatabaseConnection;

@WebListener
public class AppStartup implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        try {
//            DatabaseInitializer.resetDatabase(); // FOR TESTING
            DatabaseInitializer.initialize();

            try (Connection conn = DatabaseConnection.getConnection()) {

                if (isDatabaseEmpty(conn)) {
                    System.out.println("Database empty. Importing games...");
                    CSVImporter.importGames(conn);
                    CSVImporter.importTags(conn);
                } else {
                    System.out.println("Games already imported. Skipping import.");
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


    private boolean isDatabaseEmpty(Connection conn) throws SQLException {

        String sql = "SELECT COUNT(*) FROM games";

        try (
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)
        ) {

            rs.next();
            return rs.getInt(1) == 0;
        }
    }
}