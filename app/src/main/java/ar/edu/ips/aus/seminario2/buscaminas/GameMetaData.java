package ar.edu.ips.aus.seminario2.buscaminas;

import java.io.Serializable;

public class GameMetaData implements Serializable {

    private String id;
    private String title;
    private String author;
    private long width;
    private long height;
    private long mines;
    private String status = "ready"; // ready/running/stoppped

    private long end_votes = 0;

    public String getId() {
        return id;
    }

    public void setId(String key) {
        id = key;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public long getWidth() {
        return this.width;
    }

    public void setWidth(long width) {
        this.width = width;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public long getBombNumber() {
        return mines;
    }

    public void setBombNumber(long bombNumber) {
        this.mines = bombNumber;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public long getMines() {
        return mines;
    }

    public void setMines(long mines) {
        this.mines = mines;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getEnd_votes() {
        return end_votes;
    }

    public void setEnd_votes(long end_votes) {
        this.end_votes = end_votes;
    }
}
