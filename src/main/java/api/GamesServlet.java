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

@WebServlet("/api/games")
public class GamesServlet extends HttpServlet {

    // Homepage rows: only games where this tag is their primary_tag.
    // The dedup by name has to happen globally (across ALL rows, not just
    // this tag's matches) or the same name can independently win the
    // "best row" for multiple different categories, since different
    // app_id entries sharing a display name can each have a different
    // primary_tag (soundtracks, dedicated servers, event editions, etc).
    private static final String PRIMARY_SQL = """
        SELECT app_id, name, header_image FROM (
            SELECT app_id, name, header_image, primary_tag,
                   positive, recommendations,
                   ROW_NUMBER() OVER (
                       PARTITION BY name
                       ORDER BY (positive + recommendations) DESC
                   ) AS rn
            FROM games
        ) ranked
        WHERE rn = 1 AND primary_tag = ?
        ORDER BY (positive + recommendations) DESC
        LIMIT ? OFFSET ?
        """;

    // Category page: every game tagged with this, not just primary,
    // deduped by name -> keeps the lowest app_id per name (the original
    // listing rather than re-releases/bundles/editions sharing a name).
    private static final String FULL_SQL = """
        SELECT app_id, name, header_image FROM (
            SELECT g.app_id, g.name, g.header_image,
                   g.positive, g.recommendations,
                   ROW_NUMBER() OVER (
                       PARTITION BY g.name
                       ORDER BY g.app_id ASC
                   ) AS rn
            FROM games g
            JOIN game_tags gt ON g.app_id = gt.app_id
            JOIN tags t ON gt.tag_id = t.tag_id
            WHERE t.name = ?
        ) ranked
        WHERE rn = 1
        ORDER BY (positive + recommendations) DESC
        LIMIT ? OFFSET ?
        """;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String category = request.getParameter("category");

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

        boolean primaryOnly = "true".equalsIgnoreCase(request.getParameter("primaryOnly"));

        String sql = primaryOnly ? PRIMARY_SQL : FULL_SQL;

        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            PrintWriter out = response.getWriter()
        ) {

            ps.setString(1, category);
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