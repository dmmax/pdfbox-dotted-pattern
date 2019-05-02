package me.dmmax.pdfbox.dottedpattern.utility;

public class XYPoint {

    private float x;
    private float y;

    public XYPoint(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public static XYPoint from(float x, float y) {
        return new XYPoint(x, y);
    }

    public XYPoint plusX(float plus) {
        this.x += plus;
        return this;
    }

    public XYPoint plusY(float plus) {
        this.y += plus;
        return this;
    }

    public XYPoint minusX(float minus) {
        this.x -= minus;
        return this;
    }

    public XYPoint minusY(float minus) {
        this.y -= minus;
        return this;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }
}
