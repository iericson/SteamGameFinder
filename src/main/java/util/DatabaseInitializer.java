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
                release_date TEXT,
                estimated_owners TEXT,
                peak_ccu INT,
                required_age INT,
                price DOUBLE,
                discount DOUBLE,
                dlc_count INT,
                about_the_game TEXT,
                supported_languages TEXT,
                full_audio_languages TEXT,
                reviews TEXT,
                header_image TEXT,
                website TEXT,
                support_url TEXT,
                support_email TEXT,
                windows BOOLEAN,
                mac BOOLEAN,
                linux BOOLEAN,
                metacritic_score INT,
                metacritic_url TEXT,
                user_score INT,
                positive INT,
                negative INT,
                score_rank TEXT,
                achievements INT,
                recommendations INT,
                notes TEXT,
                average_playtime_forever INT,
                average_playtime_two_weeks INT,
                median_playtime_forever INT,
                median_playtime_two_weeks INT,
                developers TEXT,
                publishers TEXT,
                categories TEXT,
                genres TEXT,
                tags TEXT,
                screenshots TEXT,
                movies TEXT
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

            stmt.execute("DROP TABLE IF EXISTS mylist");
            stmt.execute("DROP TABLE IF EXISTS games");

            System.out.println("Database reset.");

        } catch(SQLException e) {
            e.printStackTrace();
        }
    }
}