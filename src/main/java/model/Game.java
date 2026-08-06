/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author Isaac Ericson
 */

import java.util.List;

public class Game {

    private int id;
    private String name;
    private String headerImage;
    private String aboutTheGame;
    private String releaseDate;
    private String developers;
    private String publishers;
    private List<String> tags;
    private List<String> screenshots;
    private int positiveReviews;
    private int negativeReviews;
    private int averagePlaytimeForever;
    private int averagePlaytimeTwoWeeks;
    private int medianPlaytimeForever;
    private int medianPlaytimeTwoWeeks;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHeaderImage() {
        return headerImage;
    }

    public void setHeaderImage(String headerImage) {
        this.headerImage = headerImage;
    }

    public String getAboutTheGame() {
        return aboutTheGame;
    }

    public void setAboutTheGame(String aboutTheGame) {
        this.aboutTheGame = aboutTheGame;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getDevelopers() {
        return developers;
    }

    public void setDevelopers(String developers) {
        this.developers = developers;
    }

    public String getPublishers() {
        return publishers;
    }

    public void setPublishers(String publishers) {
        this.publishers = publishers;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getScreenshots() {
        return screenshots;
    }

    public void setScreenshots(List<String> screenshots) {
        this.screenshots = screenshots;
    }
    
    public int getPositiveReviews() {
        return positiveReviews;
    }

    public void setPositiveReviews(int positiveReviews) {
        this.positiveReviews = positiveReviews;
    }

    public int getNegativeReviews() {
        return negativeReviews;
    }

    public void setNegativeReviews(int negativeReviews) {
        this.negativeReviews = negativeReviews;
    }

    public int getAveragePlaytimeForever() {
        return averagePlaytimeForever;
    }

    public void setAveragePlaytimeForever(int averagePlaytimeForever) {
        this.averagePlaytimeForever = averagePlaytimeForever;
    }

    public int getAveragePlaytimeTwoWeeks() {
        return averagePlaytimeTwoWeeks;
    }

    public void setAveragePlaytimeTwoWeeks(int averagePlaytimeTwoWeeks) {
        this.averagePlaytimeTwoWeeks = averagePlaytimeTwoWeeks;
    }

    public int getMedianPlaytimeForever() {
        return medianPlaytimeForever;
    }

    public void setMedianPlaytimeForever(int medianPlaytimeForever) {
        this.medianPlaytimeForever = medianPlaytimeForever;
    }

    public int getMedianPlaytimeTwoWeeks() {
        return medianPlaytimeTwoWeeks;
    }

    public void setMedianPlaytimeTwoWeeks(int medianPlaytimeTwoWeeks) {
        this.medianPlaytimeTwoWeeks = medianPlaytimeTwoWeeks;
    }
    
    public int getTotalReviews() {
        return positiveReviews + negativeReviews;
    }

    public int getPositivePercent() {
        int total = getTotalReviews();
        if (total == 0) {
            return 0;
        }

        return (int) Math.round((positiveReviews * 100.0) / total);
    }
    public String getReviewSummary() {
        int total = getTotalReviews();
        int percent = getPositivePercent();

        if (total < 10)
            return "Not Enough Reviews";
        if (percent >= 95 && total >= 500)
            return "Overwhelmingly Positive";
        if (percent >= 80)
            return "Very Positive";
        if (percent >= 70)
            return "Mostly Positive";
        if (percent >= 40)
            return "Mixed";
        if (percent >= 20)
            return "Mostly Negative";
        return "Overwhelmingly Negative";
    }
    
    public String getReviewSummaryClass() {
        return switch (getReviewSummary()) {
            case "Overwhelmingly Positive",
                 "Very Positive",
                 "Mostly Positive" -> "review-positive";

            case "Mixed" -> "review-mixed";

            case "Mostly Negative",
                 "Overwhelmingly Negative" -> "review-negative";

            default -> "review-none";
        };
    }

    private String formatPlaytime(int minutes) {
        if (minutes <= 0) {
            return "0 hrs";
        }

        if (minutes < 60) {
            return minutes + " min";
        }

        double hours = minutes / 60.0;

        if (hours >= 10) {
            return Math.round(hours) + " hrs";
        }

        return String.format("%.1f hrs", hours);
    }

    public String getAveragePlaytimeForeverFormatted() {
        return formatPlaytime(averagePlaytimeForever);
    }

    public String getAveragePlaytimeTwoWeeksFormatted() {
        return formatPlaytime(averagePlaytimeTwoWeeks);
    }

    public String getMedianPlaytimeForeverFormatted() {
        return formatPlaytime(medianPlaytimeForever);
    }

    public String getMedianPlaytimeTwoWeeksFormatted() {
        return formatPlaytime(medianPlaytimeTwoWeeks);
    }

}