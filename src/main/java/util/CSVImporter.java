/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

/**
 *
 * @author Isaac Ericson
 */

import java.io.*;
import java.sql.*;
import org.apache.commons.csv.*;

public class CSVImporter {

    // For console output
    private static final int TOTAL_RECORDS = 125_856;
    private static final int PROGRESS_BAR_WIDTH = 40;
    private static final int BATCH_SIZE = 10000;
    private static final int EXPECTED_COLUMNS = 40;

    // Boolean columns
    private static final int WINDOWS = 17;
    private static final int MAC = 18;
    private static final int LINUX = 19;

    // Integer columns
    private static final int APP_ID = 0;
    private static final int PEAK_CCU = 4;
    private static final int REQUIRED_AGE = 5;
    private static final int DLC_COUNT = 8;
    private static final int METACRITIC_SCORE = 20;
    private static final int USER_SCORE = 22;
    private static final int POSITIVE = 23;
    private static final int NEGATIVE = 24;
    private static final int ACHIEVEMENTS = 26;
    private static final int RECOMMENDATIONS = 27;
    private static final int AVG_PLAYTIME_FOREVER = 29;
    private static final int AVG_PLAYTIME_2_WEEKS = 30;
    private static final int MEDIAN_PLAYTIME_FOREVER = 31;
    private static final int MEDIAN_PLAYTIME_2_WEEKS = 32;

    // Double columns
    private static final int PRICE = 6;
    private static final int DISCOUNT = 7;

    // Text columns pulled directly by index in setGamesValues/setDetailsValues
    private static final int NAME = 1;
    private static final int RELEASE_DATE = 2;
    private static final int ESTIMATED_OWNERS = 3;
    private static final int ABOUT_THE_GAME = 9;
    private static final int SUPPORTED_LANGUAGES = 10;
    private static final int FULL_AUDIO_LANGUAGES = 11;
    private static final int HEADER_IMAGE = 13;
    private static final int WEBSITE = 14;
    private static final int SUPPORT_URL = 15;
    private static final int SUPPORT_EMAIL = 16;
    private static final int METACRITIC_URL = 21;
    private static final int SCORE_RANK = 25;
    private static final int NOTES = 28;
    private static final int DEVELOPERS = 33;
    private static final int PUBLISHERS = 34;
    private static final int TAGS = 37;
    private static final int SCREENSHOTS = 38;
    private static final int MOVIES = 39;

    // Expected CSV header, in order. Checked at import time so a reordered
    // or edited CSV fails loudly instead of silently writing values into
    // the wrong columns.
    private static final String[] EXPECTED_HEADERS = {
        "AppID", "Name", "Release date", "Estimated owners", "Peak CCU",
        "Required age", "Price", "Discount", "DLC count", "About the game",
        "Supported languages", "Full audio languages", "Reviews", "Header image",
        "Website", "Support url", "Support email", "Windows", "Mac", "Linux",
        "Metacritic score", "Metacritic url", "User score", "Positive", "Negative",
        "Score rank", "Achievements", "Recommendations", "Notes",
        "Average playtime forever", "Average playtime two weeks",
        "Median playtime forever", "Median playtime two weeks",
        "Developers", "Publishers", "Categories", "Genres", "Tags",
        "Screenshots", "Movies"
    };

    private static final String INSERT_GAMES_SQL = """
        INSERT IGNORE INTO games (
            app_id, name, release_date, header_image,
            positive, negative, recommendations,
            developers, publishers, primary_tag
        )
        VALUES (?,?,?,?,?,?,?,?,?,?)
        """;

    private static final String INSERT_DETAILS_SQL = """
        INSERT IGNORE INTO game_details (
            app_id, estimated_owners, peak_ccu, required_age,
            price, discount, dlc_count, about_the_game,
            supported_languages, full_audio_languages,
            website, support_url, support_email,
            windows, mac, linux,
            metacritic_score, metacritic_url, user_score,
            score_rank, achievements, notes,
            average_playtime_forever, average_playtime_two_weeks,
            median_playtime_forever, median_playtime_two_weeks,
            tags, screenshots, movies
        )
        VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        """;

    public static void importGames(Connection conn) {
        boolean oldAutoCommit = true;
        int imported = 0;
        int processed = 0;
        int skippedMalformed = 0;
        int skippedBadData = 0;

        try (InputStream input = CSVImporter.class.getResourceAsStream("/games.csv")) {
            if (input == null) {
                throw new FileNotFoundException("games.csv not found");
            }

            try (
                Reader reader = new InputStreamReader(input);
                CSVParser csv = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setQuote('"')
                    .setIgnoreEmptyLines(true)
                    .setAllowMissingColumnNames(true)
                    .build()
                    .parse(reader);
                PreparedStatement gamesPs = conn.prepareStatement(INSERT_GAMES_SQL);
                PreparedStatement detailsPs = conn.prepareStatement(INSERT_DETAILS_SQL)
            ) {
                validateHeaders(csv.getHeaderNames());

                oldAutoCommit = conn.getAutoCommit();
                conn.setAutoCommit(false);

                int batched = 0;
                int line = 1;

                for (CSVRecord row : csv) {
                    line++;

                    if (row.size() != EXPECTED_COLUMNS) {
                        System.out.println("Skipping malformed row " + line + " (columns=" + row.size() + ")");
                        skippedMalformed++;
                        continue;
                    }

                    try {
                        setGamesValues(gamesPs, row);
                        setDetailsValues(detailsPs, row);
                        gamesPs.addBatch();
                        detailsPs.addBatch();
                        batched++;
                        processed++;
                    } catch (RuntimeException e) {
                        System.out.println("Skipping bad data at CSV line " + line + ": " + e.getMessage());
                        skippedBadData++;
                        continue;
                    }

                    if (batched % BATCH_SIZE == 0) {
                        imported += runBatch(conn, gamesPs, detailsPs);
                        printProgress(processed);
                        batched = 0;
                    }
                }

                if (batched > 0) {
                    imported += runBatch(conn, gamesPs, detailsPs);
                    printProgress(processed);
                }

                System.out.println();
                System.out.println(
                    "Finished importing " + imported + " games. "
                    + "Skipped " + skippedMalformed + " malformed rows, "
                    + skippedBadData + " rows with bad data."
                );
            } finally {
                conn.setAutoCommit(oldAutoCommit);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void importTags(Connection conn) {

        String insertTagSQL = """
            INSERT IGNORE INTO tags(name)
            VALUES(?)
            """;

        String insertGameTagSQL = """
            INSERT IGNORE INTO game_tags(app_id, tag_id)
            SELECT ?, tag_id
            FROM tags
            WHERE name = ?
            """;


        try (
            InputStream input = CSVImporter.class.getResourceAsStream("/games.csv");
            Reader reader = new InputStreamReader(input);

            CSVParser csv = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setQuote('"')
                .setIgnoreEmptyLines(true)
                .build()
                .parse(reader);

            PreparedStatement tagPS = conn.prepareStatement(insertTagSQL);
            PreparedStatement gameTagPS = conn.prepareStatement(insertGameTagSQL);

        ) {

            conn.setAutoCommit(false);

            int processed = 0;
            int batched = 0;


            for (CSVRecord row : csv) {

                int appId = getInt(row.get("AppID"));
                String tags = row.get("Tags");

                if (tags == null || tags.isBlank()) {
                    continue;
                }
                String[] tagList = tags.split(",");

                for (String tag : tagList) {
                    tag = tag.trim();
                    if (tag.isEmpty()) {
                        continue;
                    }
                    // Add tag
                    tagPS.setString(1, tag);
                    tagPS.addBatch();

                    // Link game -> tag
                    gameTagPS.setInt(1, appId);
                    gameTagPS.setString(2, tag);
                    gameTagPS.addBatch();
                    batched++;

                    if (batched >= BATCH_SIZE) {
                        tagPS.executeBatch();
                        gameTagPS.executeBatch();
                        conn.commit();
                        tagPS.clearBatch();
                        gameTagPS.clearBatch();
                        batched = 0;
                    }
                }
                processed++;

                if (processed % 1000 == 0) {
                    System.out.println(
                        "Processed tags for " + processed + " games"
                    );
                }
            }

            if (batched > 0) {
                tagPS.executeBatch();
                gameTagPS.executeBatch();
                conn.commit();
            }
            System.out.println("Finished importing tags.");
        } catch (Exception e) {
            e.printStackTrace();
            try {
                conn.rollback();
            } catch (SQLException ignored) {}
        }
    }

    private static void setGamesValues(PreparedStatement ps, CSVRecord row) throws SQLException {
        ps.setInt(1, getInt(row.get(APP_ID)));
        ps.setString(2, row.get(NAME));
        ps.setString(3, row.get(RELEASE_DATE));
        ps.setString(4, row.get(HEADER_IMAGE));
        ps.setInt(5, getInt(row.get(POSITIVE)));
        ps.setInt(6, getInt(row.get(NEGATIVE)));
        ps.setInt(7, getInt(row.get(RECOMMENDATIONS)));
        ps.setString(8, row.get(DEVELOPERS));
        ps.setString(9, row.get(PUBLISHERS));
        ps.setString(10, firstTag(row.get(TAGS)));
    }

    private static void setDetailsValues(PreparedStatement ps, CSVRecord row) throws SQLException {
        ps.setInt(1, getInt(row.get(APP_ID)));
        ps.setString(2, row.get(ESTIMATED_OWNERS));
        ps.setInt(3, getInt(row.get(PEAK_CCU)));
        ps.setInt(4, getInt(row.get(REQUIRED_AGE)));
        ps.setDouble(5, getDouble(row.get(PRICE)));
        ps.setDouble(6, getDouble(row.get(DISCOUNT)));
        ps.setInt(7, getInt(row.get(DLC_COUNT)));
        ps.setString(8, row.get(ABOUT_THE_GAME));
        ps.setString(9, row.get(SUPPORTED_LANGUAGES));
        ps.setString(10, row.get(FULL_AUDIO_LANGUAGES));
        ps.setString(11, row.get(WEBSITE));
        ps.setString(12, row.get(SUPPORT_URL));
        ps.setString(13, row.get(SUPPORT_EMAIL));
        ps.setBoolean(14, getBoolean(row.get(WINDOWS)));
        ps.setBoolean(15, getBoolean(row.get(MAC)));
        ps.setBoolean(16, getBoolean(row.get(LINUX)));
        ps.setInt(17, getInt(row.get(METACRITIC_SCORE)));
        ps.setString(18, row.get(METACRITIC_URL));
        ps.setInt(19, getInt(row.get(USER_SCORE)));
        ps.setString(20, row.get(SCORE_RANK));
        ps.setInt(21, getInt(row.get(ACHIEVEMENTS)));
        ps.setString(22, row.get(NOTES));
        ps.setInt(23, getInt(row.get(AVG_PLAYTIME_FOREVER)));
        ps.setInt(24, getInt(row.get(AVG_PLAYTIME_2_WEEKS)));
        ps.setInt(25, getInt(row.get(MEDIAN_PLAYTIME_FOREVER)));
        ps.setInt(26, getInt(row.get(MEDIAN_PLAYTIME_2_WEEKS)));
        ps.setString(27, row.get(TAGS));
        ps.setString(28, row.get(SCREENSHOTS));
        ps.setString(29, row.get(MOVIES));
    }

    private static String firstTag(String tags) {
        if (tags == null || tags.isBlank()) {
            return null;
        }
        return tags.split(",")[0].trim();
    }

    /**
     * Executes and commits the current batch on both the games and
     * game_details statements together, so a game and its details row
     * always land in the same commit. If the batch fails, it's rolled
     * back and skipped so one bad batch doesn't take down the whole
     * import. Returns the number of rows successfully imported in this
     * batch.
     */
    private static int runBatch(Connection conn, PreparedStatement gamesPs, PreparedStatement detailsPs) {
        try {
            int[] results = gamesPs.executeBatch();
            detailsPs.executeBatch();
            conn.commit();
            return results.length;
        } catch (SQLException e) {
            System.out.println("Batch failed: " + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            return 0;
        } finally {
            try {
                gamesPs.clearBatch();
                detailsPs.clearBatch();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Confirms the CSV's header row matches EXPECTED_HEADERS in order.
     * The column type dispatch in setGamesValues/setDetailsValues relies
     * on fixed positions, so if the CSV's columns are ever reordered this
     * catches it up front instead of silently writing values into the
     * wrong columns.
     */
    private static void validateHeaders(java.util.List<String> actualHeaders) {
        if (actualHeaders.size() != EXPECTED_HEADERS.length) {
            throw new IllegalStateException(
                "games.csv header has " + actualHeaders.size() + " columns, expected " + EXPECTED_HEADERS.length
            );
        }

        for (int i = 0; i < EXPECTED_HEADERS.length; i++) {
            if (!EXPECTED_HEADERS[i].equalsIgnoreCase(actualHeaders.get(i))) {
                throw new IllegalStateException(
                    "games.csv column " + i + " is '" + actualHeaders.get(i)
                    + "', expected '" + EXPECTED_HEADERS[i] + "'"
                );
            }
        }
    }

    private static void printProgress(int processed) {
        int percent = processed * 100 / TOTAL_RECORDS;
        int filled = percent * PROGRESS_BAR_WIDTH / 100;

        String bar =
                "=".repeat(filled) +
                " ".repeat(PROGRESS_BAR_WIDTH - filled);

        System.out.printf(
                "\r[%s] %3d%% (%,d/%,d)",
                bar,
                percent,
                processed,
                TOTAL_RECORDS
        );
    }

    private static int getInt(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static double getDouble(String value) {
        if (value == null || value.isBlank()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private static boolean getBoolean(String value) {
        if (value == null) {
            return false;
        }
        return value.trim().equalsIgnoreCase("true");
    }
}