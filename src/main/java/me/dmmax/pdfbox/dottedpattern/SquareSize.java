package me.dmmax.pdfbox.dottedpattern;

public class SquareSize {

    private float width, height;

    private SquareSize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public static SquareSize from(float width, float height) {
        return new SquareSize(width, height);
    }

    public SquareSize increaseHeightInTimes(int times) {
        return from(width, height * times);
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }
}
