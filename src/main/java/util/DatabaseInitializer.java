/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

/**
 *
 * @author Isaac Ericson
 */

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    public static void initialize() {

        createGamesTable();
        createGameDetailsTable();
        createTagsTable();
        createGameTagsTable();
        createMyListTable();

        System.out.println("Database tables initialized.");
    }


    private static Connection getConnection() throws SQLException {

        return DatabaseConnection.getConnection();
    }


    private static void createGamesTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS games (
                app_id INT PRIMARY KEY,
                name TEXT,
                header_image TEXT,
                release_date TEXT,
                developers TEXT,
                publishers TEXT,
                positive INT,
                negative INT,
                recommendations INT,
                primary_tag VARCHAR(100),

                INDEX idx_games_primary_tag (primary_tag)
            )
            """;

        try (
            Connection conn = getConnection();
            Statement stmt = conn.createStatement()
        ) {
            stmt.execute(sql);
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createGameDetailsTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS game_details (
                app_id INT PRIMARY KEY,
                about_the_game TEXT,
                tags TEXT,
                screenshots TEXT,
                movies TEXT,
                website TEXT,
                support_url TEXT,
                support_email TEXT,
                supported_languages TEXT,
                full_audio_languages TEXT,
                estimated_owners TEXT,
                peak_ccu INT,
                required_age INT,
                price DOUBLE,
                discount DOUBLE,
                dlc_count INT,
                windows BOOLEAN,
                mac BOOLEAN,
                linux BOOLEAN,
                metacritic_score INT,
                metacritic_url TEXT,
                user_score INT,
                score_rank TEXT,
                achievements INT,
                notes TEXT,
                average_playtime_forever INT,
                average_playtime_two_weeks INT,
                median_playtime_forever INT,
                median_playtime_two_weeks INT,

                FOREIGN KEY (app_id)
                    REFERENCES games(app_id)
                    ON DELETE CASCADE
            )
            """;

        try (
            Connection conn = getConnection();
            Statement stmt = conn.createStatement()
        ) {
            stmt.execute(sql);
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createTagsTable() {

        String sql = """
            CREATE TABLE IF NOT EXISTS tags (
                tag_id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(100) UNIQUE NOT NULL
            )
            """;

        try (
            Connection conn = getConnection();
            Statement stmt = conn.createStatement()
        ) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createGameTagsTable() {

        String sql = """
            CREATE TABLE IF NOT EXISTS game_tags (
                app_id INT NOT NULL,
                tag_id INT NOT NULL,

                PRIMARY KEY (app_id, tag_id),
                INDEX idx_game_tags_tag_id (tag_id),

                FOREIGN KEY (app_id)
                    REFERENCES games(app_id)
                    ON DELETE CASCADE,

                FOREIGN KEY (tag_id)
                    REFERENCES tags(tag_id)
                    ON DELETE CASCADE
            )
            """;

        try (
            Connection conn = getConnection();
            Statement stmt = conn.createStatement()
        ) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createMyListTable() {

        String sql = """
            CREATE TABLE IF NOT EXISTS mylist (
                id INT AUTO_INCREMENT PRIMARY KEY,
                game_id INT NOT NULL,
                status VARCHAR(50),

                FOREIGN KEY (game_id)
                REFERENCES games(app_id)
                ON DELETE CASCADE
            )
            """;


        try (
            Connection conn = getConnection();
            Statement stmt = conn.createStatement()
        ) {

            stmt.execute(sql);

        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public static void resetDatabase() {

        try (
            Connection conn = getConnection();
            Statement stmt = conn.createStatement()
        ) {

            stmt.execute("DROP TABLE IF EXISTS game_tags");
            stmt.execute("DROP TABLE IF EXISTS game_details");
            stmt.execute("DROP TABLE IF EXISTS tags");
            stmt.execute("DROP TABLE IF EXISTS mylist");
            stmt.execute("DROP TABLE IF EXISTS games");

            System.out.println("Database reset.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}