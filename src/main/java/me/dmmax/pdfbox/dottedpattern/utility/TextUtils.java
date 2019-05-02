package me.dmmax.pdfbox.dottedpattern.utility;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.*;
import java.io.IOException;

public class TextUtils {

    public static void showText(PDPageContentStream contentStream, PDType1Font fontType, float fontSize, XYPoint startPos, String text) throws IOException {
        contentStream.beginText();
        contentStream.setFont(fontType, fontSize);
        contentStream.setNonStrokingColor(Color.BLACK);
        contentStream.newLineAtOffset(startPos.getX(), startPos.getY());
        contentStream.showText(text);
        contentStream.endText();
    }
}
