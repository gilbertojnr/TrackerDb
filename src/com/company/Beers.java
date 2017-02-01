package com.company;


import java.util.ArrayList;
/**
 * Created by gilbertakpan on 1/31/17.
 */
public class Beers extends ArrayList<Beers> {
    int beerId;
    int userId;
    String beerName;
    String brewery;
    boolean tasted;
    String year;
    String state;

    public void setBeerId(int beerId) {
        this.beerId = beerId;
    }

    public Beers() {

    }


    public Beers(int beerId,int userId, String beerName, String brewery, boolean tasted, String state, String year){
        this.beerId = beerId;
        this.userId = userId;
        this.beerName = beerName;
        this.brewery = brewery;
        this.tasted = tasted;
        this.year = year;
        this.state = state;
    }

    public int getBeerId() {
        return beerId;
    }



    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getBeerName() {
        return beerName;
    }

    public void setBeerName(String beerName) {
        this.beerName = beerName;
    }

    public String getBrewery() {
        return brewery;
    }

    public void setBrewery(String brewery) {
        this.brewery = brewery;
    }

    public boolean isTasted() {
        return tasted;
    }

    public void setTasted(boolean tasted) {
        this.tasted = tasted;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
