/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

/**
 *
 * @author Isaac Ericson
 */

public class AdultContentFilter {

    private static final String[] BLOCKED_TAGS = {
        "Hentai", "NSFW", "Mature", "Nudity", "Sexual Content"
    };

    private static final int RANK_LIMIT = 5;

    public static boolean isShowAdult(String adultParam) {
        return "true".equalsIgnoreCase(adultParam);
    }

    // appIdColumn is the fully-qualified column to match against
    // game_details.app_id, since callers reference it through
    // different aliases depending on their query structure
    // (e.g. "g.app_id" or "ranked.app_id").
    public static String exclusionClause(String appIdColumn) {
        StringBuilder sql = new StringBuilder();
        sql.append("AND NOT EXISTS (\n");
        sql.append("    SELECT 1 FROM game_details gd\n");
        sql.append("    WHERE gd.app_id = ").append(appIdColumn).append("\n");
        sql.append("    AND (\n");

        for (int i = 0; i < BLOCKED_TAGS.length; i++) {
            sql.append("        ");
            if (i > 0) {
                sql.append("OR ");
            }
            sql.append("FIND_IN_SET('").append(BLOCKED_TAGS[i])
               .append("', gd.tags) BETWEEN 1 AND ").append(RANK_LIMIT).append("\n");
        }

        sql.append("    )\n");
        sql.append(")");

        return sql.toString();
    }
}
