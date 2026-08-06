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
import util.AdultContentFilter;

@WebServlet("/api/games")
public class GamesServlet extends HttpServlet {

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
        %ADULT_FILTER%
        ORDER BY (positive + recommendations) DESC
        LIMIT ? OFFSET ?
        """;

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
        %ADULT_FILTER%
        ORDER BY (positive + recommendations) DESC
        LIMIT ? OFFSET ?
        """;

    private static final String ALL_SQL = """
        SELECT app_id, name, header_image FROM (
            SELECT app_id, name, header_image,
                   positive, recommendations,
                   ROW_NUMBER() OVER (
                       PARTITION BY name
                       ORDER BY (positive + recommendations) DESC
                   ) AS rn
            FROM games
        ) ranked
        WHERE rn = 1
        %ADULT_FILTER%
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
        boolean noCategory = category == null || category.isBlank() || "Popular".equals(category);
        boolean showAdult = AdultContentFilter.isShowAdult(request.getParameter("adult"));

        String template = noCategory ? ALL_SQL : (primaryOnly ? PRIMARY_SQL : FULL_SQL);
        String sql = template.replace(
            "%ADULT_FILTER%",
            showAdult ? "" : AdultContentFilter.exclusionClause("ranked.app_id")
        );

        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            PrintWriter out = response.getWriter()
        ) {

            int paramIndex = 1;
            if (!noCategory) {
                ps.setString(paramIndex++, category);
            }
            ps.setInt(paramIndex++, limit);
            ps.setInt(paramIndex, offset);

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