/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package api;

/**
 *
 * @author Isaac Ericson
 */
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import util.CSVImporter;
import util.DatabaseConnection;
import util.DatabaseInitializer;

@WebServlet("/api/reimport")
public class ReimportServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            DatabaseInitializer.resetDatabase();
            DatabaseInitializer.initialize();

            Connection conn = DatabaseConnection.getConnection();
            CSVImporter.importGames(conn);
            CSVImporter.importTags(conn);

            try (PrintWriter out = response.getWriter()) {
                out.print("{\"status\":\"ok\"}");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(500);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"status\":\"error\"}");
            }
        }
    }
}