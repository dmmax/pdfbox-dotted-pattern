package me.dmmax.pdfbox.dottedpattern.utility;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SquareSize that = (SquareSize) o;
        return Float.compare(that.width, width) == 0 &&
                Float.compare(that.height, height) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height);
    }
}
