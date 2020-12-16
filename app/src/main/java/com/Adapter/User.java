package com.Adapter;

import java.util.Objects;

public class User implements Comparable<User> {
    private String name;
    private int poziom;
    private int points;

    public User(String name, int poziom, int points) {
        this.name = name;
        this.poziom = poziom;
        this.points = points;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPoziom() {
        return poziom;
    }

    public void setPoziom(int poziom) {
        this.poziom = poziom;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }



    @Override
    public int compareTo(User o) {
        int compareResult = Integer.compare(this.getPoziom(), o.getPoziom());
        if(compareResult == 0){
            compareResult = Integer.compare(this.getPoints(),o.getPoints());
        }
        return -compareResult;
    }
}
