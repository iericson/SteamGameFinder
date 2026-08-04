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

import util.DatabaseConnection;

@WebServlet("/api/categories")
public class CategoriesServlet extends HttpServlet {

    // top tags become the homepage rows. Steam tags are user-submitted and
    // there are hundreds of distinct ones, so this is paged (like the games
    // endpoint) instead of dumping every tag that exists at once -- the
    // caller loads more rows in batches as the page is scrolled.
    private static final int DEFAULT_LIMIT = 20;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        int limit = Integer.parseInt(
                request.getParameter("limit") == null
                ? String.valueOf(DEFAULT_LIMIT)
                : request.getParameter("limit")
        );

        int offset = Integer.parseInt(
                request.getParameter("offset") == null
                ? "0"
                : request.getParameter("offset")
        );

        String sql = """
            SELECT t.name AS category,
                   COUNT(gt.app_id) AS game_count
            FROM tags t
            JOIN game_tags gt
                ON t.tag_id = gt.tag_id
            GROUP BY t.tag_id, t.name
            ORDER BY game_count DESC
            LIMIT ? OFFSET ?
            """;

        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {

            ps.setInt(1, limit);
            ps.setInt(2, offset);

            try (
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
                    out.print(escape(rs.getString("category")));
                    out.print("\"");

                    first = false;
                }

                out.print("]");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(500);
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
