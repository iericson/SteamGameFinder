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
import java.util.LinkedHashSet;
import java.util.Set;

import util.DatabaseConnection;

@WebServlet("/api/mylist")
public class MyListServlet extends HttpServlet {

    private static final String SESSION_KEY = "myListIds";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Set<Integer> ids = getSavedIds(request);

        if (ids.isEmpty()) {
            response.getWriter().print("[]");
            return;
        }

        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) placeholders.append(",");
            placeholders.append("?");
        }

        String sql = "SELECT app_id, name, header_image FROM games WHERE app_id IN (" + placeholders + ")";

        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            PrintWriter out = response.getWriter()
        ) {

            int index = 1;
            for (Integer id : ids) {
                ps.setInt(index++, id);
            }

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
            response.getWriter().print("{\"error\":\"Database error\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Integer id = parseId(request);

        if (id == null) {
            response.setStatus(400);
            response.getWriter().print("{\"error\":\"Missing or invalid id\"}");
            return;
        }

        getSavedIds(request).add(id);

        response.getWriter().print("{\"saved\":true,\"id\":" + id + "}");
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Integer id = parseId(request);

        if (id == null) {
            response.setStatus(400);
            response.getWriter().print("{\"error\":\"Missing or invalid id\"}");
            return;
        }

        getSavedIds(request).remove(id);

        response.getWriter().print("{\"saved\":false,\"id\":" + id + "}");
    }

    @SuppressWarnings("unchecked")
    private Set<Integer> getSavedIds(HttpServletRequest request) {
        HttpSession session = request.getSession(true);

        Set<Integer> ids = (Set<Integer>) session.getAttribute(SESSION_KEY);

        if (ids == null) {
            ids = new LinkedHashSet<>();
            session.setAttribute(SESSION_KEY, ids);
        }

        return ids;
    }

    private Integer parseId(HttpServletRequest request) {
        String idParam = request.getParameter("id");

        if (idParam == null || !idParam.matches("\\d+")) {
            return null;
        }

        return Integer.parseInt(idParam);
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
