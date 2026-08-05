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
import util.AdultContentFilter;

@WebServlet("/api/search")
public class SearchServlet extends HttpServlet {

    // cap on how many exact name matches we'll pull before backfilling with similar games
    private static final int NAME_MATCH_LIMIT = 10;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String q = request.getParameter("q");

        int limit = Integer.parseInt(
                request.getParameter("limit") == null ? "20" : request.getParameter("limit"));
        int offset = Integer.parseInt(
                request.getParameter("offset") == null ? "0" : request.getParameter("offset"));
        boolean showAdult = AdultContentFilter.isShowAdult(request.getParameter("adult"));

        if (q == null || q.isBlank()) {
            response.getWriter().print("[]");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {

            List<String> allTags = loadAllTagNames(conn);

            String[] words = q.trim().split("\\s+");
            boolean[] consumed = new boolean[words.length];
            List<String> matchedTags = new ArrayList<>();

            // greedy: try to match the longest run of words to a tag name first,
            // so "open world" matches the tag before we'd try "open" and "world" alone
            for (int len = words.length; len >= 1; len--) {
                for (int start = 0; start + len <= words.length; start++) {
                    if (anyConsumed(consumed, start, len)) continue;

                    String phrase = String.join(" ", Arrays.copyOfRange(words, start, start + len));
                    String matched = findTag(allTags, phrase);

                    if (matched != null) {
                        matchedTags.add(matched);
                        for (int i = start; i < start + len; i++) consumed[i] = true;
                    }
                }
            }

            StringBuilder nameQueryBuilder = new StringBuilder();
            for (int i = 0; i < words.length; i++) {
                if (!consumed[i]) {
                    if (nameQueryBuilder.length() > 0) nameQueryBuilder.append(" ");
                    nameQueryBuilder.append(words[i]);
                }
            }
            String nameQuery = nameQueryBuilder.toString().trim();

            List<GameRow> results;

            if (!nameQuery.isEmpty()) {
                results = searchByName(conn, nameQuery, matchedTags, showAdult, limit, offset);
            } else if (!matchedTags.isEmpty()) {
                results = searchByTags(conn, matchedTags, showAdult, limit, offset);
            } else {
                // nothing recognized as a tag and nothing left over either (shouldn't
                // really happen), fall back to a loose name search on the raw query
                results = searchByName(conn, q.trim(), Collections.emptyList(), showAdult, limit, offset);
            }

            writeJson(response.getWriter(), results);

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(500);
            response.getWriter().print("{\"error\":\"Database error\"}");
        }
    }

    // ---- search strategies ----

    private List<GameRow> searchByName(Connection conn, String nameQuery, List<String> requiredTags,
            boolean showAdult, int limit, int offset) throws SQLException {

        List<GameRow> nameMatches = findGamesByName(conn, nameQuery, showAdult);

        if (offset < nameMatches.size()) {
            List<GameRow> page = new ArrayList<>(nameMatches.subList(offset, nameMatches.size()));

            int remaining = limit - page.size();
            if (remaining > 0) {
                page.addAll(findSimilarGames(conn, nameMatches, requiredTags, showAdult, remaining, 0));
            }

            return page.size() > limit ? page.subList(0, limit) : page;
        }

        int similarOffset = offset - nameMatches.size();
        return findSimilarGames(conn, nameMatches, requiredTags, showAdult, limit, similarOffset);
    }

    private List<GameRow> searchByTags(Connection conn, List<String> tags, boolean showAdult,
            int limit, int offset) throws SQLException {

        String placeholders = placeholders(tags.size());

        String sql = """
            SELECT g.app_id, g.name, g.header_image
            FROM games g
            JOIN game_tags gt ON g.app_id = gt.app_id
            JOIN tags t ON gt.tag_id = t.tag_id
            WHERE t.name IN (%s)
            %%ADULT_FILTER%%
            GROUP BY g.app_id, g.name, g.header_image
            HAVING COUNT(DISTINCT t.tag_id) = ?
            ORDER BY (g.positive + g.recommendations) DESC
            LIMIT ? OFFSET ?
            """.formatted(placeholders)
               .replace("%ADULT_FILTER%", showAdult ? "" : AdultContentFilter.exclusionClause("g.app_id"));

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            for (String tag : tags) ps.setString(idx++, tag);
            ps.setInt(idx++, tags.size());
            ps.setInt(idx++, limit);
            ps.setInt(idx, offset);

            return readGames(ps);
        }
    }

    // ---- helper queries ----

    private List<GameRow> findGamesByName(Connection conn, String nameQuery, boolean showAdult)
        throws SQLException {

        String sql = """
            SELECT g.app_id, g.name, g.header_image FROM games g
            WHERE g.name LIKE ?
            %ADULT_FILTER%
            ORDER BY (g.positive + g.recommendations) DESC
            LIMIT ?
            """.replace("%ADULT_FILTER%", showAdult ? "" : AdultContentFilter.exclusionClause("g.app_id"));

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + nameQuery + "%");
            ps.setInt(2, NAME_MATCH_LIMIT);
            return readGames(ps);
        }
    }

    private List<GameRow> findSimilarGames(Connection conn, List<GameRow> baseGames, List<String> requiredTags,
            boolean showAdult, int limit, int offset) throws SQLException {

        if (baseGames.isEmpty()) return Collections.emptyList();

        String idPlaceholders = placeholders(baseGames.size());

        StringBuilder requiredClause = new StringBuilder();
        for (int i = 0; i < requiredTags.size(); i++) {
            requiredClause.append("AND EXISTS (\n")
                .append("    SELECT 1 FROM game_tags gt2 JOIN tags t2 ON gt2.tag_id = t2.tag_id\n")
                .append("    WHERE gt2.app_id = g.app_id AND t2.name = ?\n")
                .append(")\n");
        }

        String sql = """
            SELECT g.app_id, g.name, g.header_image, COUNT(DISTINCT gt.tag_id) AS shared_count
            FROM games g
            JOIN game_tags gt ON g.app_id = gt.app_id
            JOIN game_tags base_gt ON base_gt.tag_id = gt.tag_id AND base_gt.app_id IN (%s)
            WHERE g.app_id NOT IN (%s)
            %s
            %%ADULT_FILTER%%
            GROUP BY g.app_id, g.name, g.header_image
            ORDER BY shared_count DESC, (g.positive + g.recommendations) DESC
            LIMIT ? OFFSET ?
            """.formatted(idPlaceholders, idPlaceholders, requiredClause.toString())
               .replace("%ADULT_FILTER%", showAdult ? "" : AdultContentFilter.exclusionClause("g.app_id"));

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            for (GameRow g : baseGames) ps.setInt(idx++, g.id);
            for (GameRow g : baseGames) ps.setInt(idx++, g.id);
            for (String tag : requiredTags) ps.setString(idx++, tag);
            ps.setInt(idx++, limit);
            ps.setInt(idx, offset);

            return readGames(ps);
        }
    }

    private List<String> loadAllTagNames(Connection conn) throws SQLException {
        List<String> tags = new ArrayList<>();
        try (
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT name FROM tags")
        ) {
            while (rs.next()) tags.add(rs.getString("name"));
        }
        return tags;
    }

    // ---- small utilities ----

    private String findTag(List<String> allTags, String phrase) {
        for (String tag : allTags) {
            if (tag.equalsIgnoreCase(phrase)) return tag;
        }
        return null;
    }

    private boolean anyConsumed(boolean[] consumed, int start, int len) {
        for (int i = start; i < start + len; i++) {
            if (consumed[i]) return true;
        }
        return false;
    }

    private String placeholders(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0) sb.append(",");
            sb.append("?");
        }
        return sb.toString();
    }

    private List<GameRow> readGames(PreparedStatement ps) throws SQLException {
        List<GameRow> rows = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                GameRow row = new GameRow();
                row.id = rs.getInt("app_id");
                row.name = rs.getString("name");
                row.headerImage = rs.getString("header_image");
                rows.add(row);
            }
        }
        return rows;
    }

    private void writeJson(PrintWriter out, List<GameRow> rows) {
        StringBuilder json = new StringBuilder("[");
        boolean first = true;

        for (GameRow row : rows) {
            if (!first) json.append(",");
            json.append("{")
                .append("\"id\":").append(row.id).append(",")
                .append("\"name\":\"").append(escape(row.name)).append("\",")
                .append("\"headerImage\":\"").append(escape(row.headerImage)).append("\"")
                .append("}");
            first = false;
        }

        json.append("]");
        out.print(json.toString());
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static class GameRow {
        int id;
        String name;
        String headerImage;
    }
}
