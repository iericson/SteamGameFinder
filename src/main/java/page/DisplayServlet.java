/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package page;

/**
 *
 * @author Isaac Ericson
 */

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import model.Game;
import util.DatabaseConnection;

@WebServlet("/display")
public class DisplayServlet extends HttpServlet {

    private static final String DETAILS_JSP = "/WEB-INF/details.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idParam = request.getParameter("id");

        if (idParam == null || !idParam.matches("\\d+")) {
            request.setAttribute("error", "Missing or invalid game id.");
            request.getRequestDispatcher(DETAILS_JSP).forward(request, response);
            return;
        }

        int id = Integer.parseInt(idParam);

        String sql = """
            SELECT g.app_id, g.name, g.header_image, g.release_date,
                   g.developers, g.publishers,
                   d.about_the_game, d.tags, d.screenshots
            FROM games g
            JOIN game_details d ON g.app_id = d.app_id
            WHERE g.app_id = ?
            """;

        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    request.setAttribute("error", "Game not found.");
                    request.getRequestDispatcher(DETAILS_JSP).forward(request, response);
                    return;
                }

                Game game = new Game();
                game.setId(rs.getInt("app_id"));
                game.setName(rs.getString("name"));
                game.setHeaderImage(rs.getString("header_image"));
                game.setAboutTheGame(rs.getString("about_the_game"));
                game.setReleaseDate(rs.getString("release_date"));
                game.setDevelopers(rs.getString("developers"));
                game.setPublishers(rs.getString("publishers"));
                game.setTags(splitToList(rs.getString("tags")));
                game.setScreenshots(splitToList(rs.getString("screenshots")));

                request.setAttribute("game", game);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "Database error.");
        }

        request.getRequestDispatcher(DETAILS_JSP).forward(request, response);
    }

    private List<String> splitToList(String csv) {
        List<String> list = new ArrayList<>();
        if (csv == null || csv.isBlank()) {
            return list;
        }
        for (String piece : csv.split(",")) {
            String trimmed = piece.trim();
            if (!trimmed.isEmpty()) {
                list.add(trimmed);
            }
        }
        return list;
    }
}