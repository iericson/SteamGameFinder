/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package api;

/**
 *
 * @author namu
 */

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

import util.DatabaseConnection;

@WebServlet("/api/games")
public class GamesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String genre = request.getParameter("genre");

        int limit = Integer.parseInt(
                request.getParameter("limit") == null
                ? "20"
                : request.getParameter("limit")
        );

        int offset = Integer.parseInt(
                request.getParameter("offset") == null
                ? "0"
                : request.getParameter("offset")
        );


        String sql = """
            SELECT app_id, name, header_image
            FROM games
            WHERE genres LIKE ?
            ORDER BY (positive + recommendations) DESC
            LIMIT ? OFFSET ?
            """;


        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            PrintWriter out = response.getWriter()
        ) {

            ps.setString(1, "%" + genre + "%");
            ps.setInt(2, limit);
            ps.setInt(3, offset);


            ResultSet rs = ps.executeQuery();


            StringBuilder json = new StringBuilder();
            json.append("[");


            boolean first = true;

            while (rs.next()) {

                if (!first) {
                    json.append(",");
                }

                json.append("{")
                    .append("\"id\":")
                    .append(rs.getInt("app_id"))
                    .append(",")
                    .append("\"name\":\"")
                    .append(escape(rs.getString("name")))
                    .append("\",")
                    .append("\"headerImage\":\"")
                    .append(escape(rs.getString("header_image")))
                    .append("\"")
                    .append("}");

                first = false;
            }

            json.append("]");

            out.print(json.toString());


        } catch (SQLException e) {

            e.printStackTrace();

            response.setStatus(500);
            response.getWriter().print(
                "{\"error\":\"Database error\"}"
            );
        }
    }


    private String escape(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
