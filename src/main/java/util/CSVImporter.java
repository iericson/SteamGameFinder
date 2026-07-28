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

    private static final int BATCH_SIZE = 1000;
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

    private static final String INSERT_SQL = """
        INSERT IGNORE INTO games (
            app_id, name, release_date, estimated_owners,
            peak_ccu, required_age, price, discount, dlc_count,
            about_the_game, supported_languages, full_audio_languages,
            reviews, header_image, website, support_url, support_email,
            windows, mac, linux, metacritic_score, metacritic_url,
            user_score, positive, negative, score_rank,
            achievements, recommendations, notes,
            average_playtime_forever, average_playtime_two_weeks,
            median_playtime_forever, median_playtime_two_weeks,
            developers, publishers, categories, genres,
            tags, screenshots, movies
        )
        VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        """;

    public static void importGames(Connection conn) {
        boolean oldAutoCommit = true;
        int imported = 0;
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
                PreparedStatement ps = conn.prepareStatement(INSERT_SQL)
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
                        for (int i = 0; i < EXPECTED_COLUMNS; i++) {
                            setPreparedStatementValue(ps, i, row.get(i));
                        }
                        ps.addBatch();
                        batched++;
                    } catch (RuntimeException e) {
                        System.out.println("Skipping bad data at CSV line " + line + ": " + e.getMessage());
                        skippedBadData++;
                        continue;
                    }

                    if (batched % BATCH_SIZE == 0) {
                        imported += runBatch(conn, ps, line);
                        batched = 0;
                    }
                }

                if (batched > 0) {
                    imported += runBatch(conn, ps, line);
                }

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

    /**
     * Executes and commits the current batch. If the batch fails, it's rolled
     * back and skipped so one bad batch doesn't take down the whole import.
     * Returns the number of rows successfully imported in this batch.
     */
    private static int runBatch(Connection conn, PreparedStatement ps, int upToLine) {
        try {
            int[] results = ps.executeBatch();
            conn.commit();
            System.out.println("Committed batch ending near line " + upToLine);
            return results.length;
        } catch (SQLException e) {
            System.out.println("Batch failed ending near line " + upToLine + ": " + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            return 0;
        } finally {
            try {
                ps.clearBatch();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Confirms the CSV's header row matches EXPECTED_HEADERS in order.
     * The column type dispatch in setPreparedStatementValue relies on fixed
     * positions, so if the CSV's columns are ever reordered this catches it
     * up front instead of silently writing values into the wrong columns.
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

    private static void setPreparedStatementValue(PreparedStatement ps, int index, String value) throws SQLException {
        switch (index) {
            case WINDOWS, MAC, LINUX -> ps.setBoolean(index + 1, getBoolean(value));

            case APP_ID, PEAK_CCU, REQUIRED_AGE, DLC_COUNT, METACRITIC_SCORE,
                 USER_SCORE, POSITIVE, NEGATIVE, ACHIEVEMENTS, RECOMMENDATIONS,
                 AVG_PLAYTIME_FOREVER, AVG_PLAYTIME_2_WEEKS,
                 MEDIAN_PLAYTIME_FOREVER, MEDIAN_PLAYTIME_2_WEEKS -> ps.setInt(index + 1, getInt(value));

            case PRICE, DISCOUNT -> ps.setDouble(index + 1, getDouble(value));

            default -> ps.setString(index + 1, value);
        }
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