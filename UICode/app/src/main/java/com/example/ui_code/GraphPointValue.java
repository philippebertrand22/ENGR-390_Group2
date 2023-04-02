package com.example.ui_code;

public class GraphPointValue {
    int xValue, yValue;
    public GraphPointValue(){}

    public GraphPointValue(int x, int y){
        this.xValue =x;
        this.yValue =y;
    }

    public void setxValue(int xValue) {
        this.xValue = xValue;
    }

    public void setyValue(int yValue) {
        this.yValue = yValue;
    }

    public int getxValue() {
        return xValue;
    }

    public int getyValue() {
        return yValue;
    }
}
