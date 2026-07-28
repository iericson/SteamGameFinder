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
    public void contextInitialized(
            ServletContextEvent sce) {


        try {
            //DatabaseInitializer.resetDatabase(); //FOR TESTING
            DatabaseInitializer.initialize();
            
            Connection conn = DatabaseConnection.getConnection();
            CSVImporter.importGames(conn);
        } catch (SQLException ex) {
            System.getLogger(AppStartup.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }

    }
    
    private boolean isDatabaseEmpty(Connection conn) throws SQLException {

        try (
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM games")
        ) {

            rs.next();

            return rs.getInt(1) == 0;
        }
    }
}
