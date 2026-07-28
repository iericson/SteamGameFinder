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
import java.sql.*;
import java.util.*;

import util.DatabaseConnection;

@WebServlet("/api/genres")
public class GenresServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");


        String sql = """
            SELECT genre, COUNT(*) AS game_count
            FROM (
                SELECT TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(genres, ',', numbers.n), ',', -1)) AS genre
                FROM games
                JOIN (
                    SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
                    UNION SELECT 6 UNION SELECT 7 UNION SELECT 8
                ) numbers
                ON CHAR_LENGTH(genres)
                    - CHAR_LENGTH(REPLACE(genres, ',', '')) >= numbers.n - 1
            ) AS split_genres
            GROUP BY genre
            ORDER BY game_count DESC
            """;


        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            PrintWriter out = response.getWriter()
        ) {


            out.print("[");

            boolean first = true;


            while (rs.next()) {

                if (!first) {
                    out.print(",");
                }

                out.print("\"");
                out.print(rs.getString("genre"));
                out.print("\"");

                first = false;
            }


            out.print("]");


        } catch(SQLException e) {

            e.printStackTrace();
            response.setStatus(500);
        }
    }
}
