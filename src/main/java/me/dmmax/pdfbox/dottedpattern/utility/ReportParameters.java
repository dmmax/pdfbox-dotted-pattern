package me.dmmax.pdfbox.dottedpattern.utility;

import lombok.Builder;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

@Builder
public class ReportParameters {

    private PDDocument document;
    private Map<SquareSize, BufferedImage> dotPatterImages;

    public PDDocument document() {
        return document;
    }

    public BufferedImage getDotPatternBySize(SquareSize sizeOfDotPattern) {
        if (dotPatterImages != null) {
            return dotPatterImages.get(sizeOfDotPattern);
        }
        return null;
    }

    public void addDotPatternImage(SquareSize sizeOfDotPattern, BufferedImage image) {
        if (dotPatterImages == null) {
            dotPatterImages = new HashMap<>();
        }
        dotPatterImages.put(sizeOfDotPattern, image);
    }
}