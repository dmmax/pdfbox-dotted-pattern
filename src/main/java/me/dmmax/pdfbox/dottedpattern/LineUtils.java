package me.dmmax.pdfbox.dottedpattern;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.function.PDFunctionType2;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShading;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShadingType3;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class LineUtils {

    public static void printSolidLine(PDPageContentStream cs, float lineWidth, XYPoint startPoint, XYPoint endPoint) throws IOException {

        cs.setStrokingColor(Color.BLACK);
        cs.setLineWidth(lineWidth);
        cs.setLineDashPattern(new float[]{}, 0);
        cs.moveTo(startPoint.getX(), startPoint.getY());
        cs.lineTo(endPoint.getX(), endPoint.getY());
        cs.stroke();
    }

    public static void createDashedPattern(PDPageContentStream contentStream, XYPoint startPoint, float width, int lines) throws IOException {

        float lineSpaceDashedPattern = 1.2F;

        contentStream.setStrokingColor(0, 0, 0);
        contentStream.setLineWidth(0.3F);
        contentStream.setLineCapStyle(2);
        for (int i = 0; i < lines; i++) {
            contentStream.setLineDashPattern(new float[]{0, 0.9F, 0.3F, 1.2F}, i % 2 == 0 ? 1.2F : 2.4F);
            contentStream.moveTo(startPoint.getX(), startPoint.getY());
            contentStream.lineTo(startPoint.getX() + width, startPoint.getY());
            contentStream.stroke();
            startPoint = startPoint.minusY(lineSpaceDashedPattern);
        }
        contentStream.setStrokingColor(0, 0, 0);
    }

    public static void createDottedPattern(PDPageContentStream contentStream, XYPoint topLeftPoint, float width, int lines) throws IOException {
        createDottedPattern(contentStream, topLeftPoint, width, lines, false);
    }

    public static void createDottedPatternInvertStartLine(PDPageContentStream contentStream, XYPoint topLeftPoint, float width, int lines) throws IOException {
        createDottedPattern(contentStream, topLeftPoint, width, lines, true);
    }

    private static void createDottedPattern(PDPageContentStream contentStream, XYPoint topLeftPoint, float width, int lines, boolean invertStartLine) throws IOException {

        contentStream.setLineWidth(0.3F);
        contentStream.setLineCapStyle(2);
        contentStream.setStrokingColor(0, 0, 0);
        float lineSpaceDottedPattern = 1.2F;

        float firstPhase = invertStartLine ? 0 : 1.2F;
        float secondPhase = invertStartLine ? 1.2F : 0;

        for (int i = 0; i < lines; i++) {

            contentStream.setMiterLimit(1);
            contentStream.setLineDashPattern(new float[]{0, 2.4F}, i % 2 == 0 ? firstPhase : secondPhase);
            contentStream.moveTo(topLeftPoint.getX(), topLeftPoint.getY());
            contentStream.lineTo(topLeftPoint.getX() + width, topLeftPoint.getY());
            contentStream.stroke();
            topLeftPoint = topLeftPoint.minusY(lineSpaceDottedPattern);
        }
        contentStream.setStrokingColor(0, 0, 0);
    }

    public static void createDottedPatternUsingRadialShading(PDPageContentStream contentStream, XYPoint topLeftPoint, SquareSize boxSize) throws IOException {

        COSDictionary dictionary = new COSDictionary();
        dictionary.setInt(COSName.FUNCTION_TYPE, 2);

        COSArray domain = new COSArray();
        domain.add(COSInteger.get(0));
        domain.add(COSInteger.get(1));
        dictionary.setItem(COSName.DOMAIN, domain);

        COSArray colorCenter = new COSArray();
        colorCenter.add(COSFloat.get("1"));
        colorCenter.add(COSFloat.get("1"));
        colorCenter.add(COSFloat.get("1"));
        dictionary.setItem(COSName.C0, colorCenter);

        COSArray colorEdge = new COSArray();
        colorEdge.add(COSFloat.get("0"));
        colorEdge.add(COSFloat.get("0"));
        colorEdge.add(COSFloat.get("0"));
        dictionary.setItem(COSName.C1, colorEdge);

        dictionary.setInt(COSName.N, 1);
        PDFunctionType2 function = new PDFunctionType2(dictionary);

        float widthBetweenDots = 2.5F;
        float spaceBetweenLines = 1.3F;

        int lines = (int) (boxSize.height() / spaceBetweenLines);

        float yPosition = topLeftPoint.getY();
        for (int i = 0; i < lines; i++) {

            float xPosition = i % 2 == 0 ? topLeftPoint.getX() : topLeftPoint.getX() + 1.5F;
            for (; xPosition < topLeftPoint.getX() + boxSize.width(); xPosition += widthBetweenDots) {

                PDShadingType3 radialShading = new PDShadingType3(new COSDictionary());
                radialShading.setColorSpace(PDDeviceRGB.INSTANCE);
                radialShading.setShadingType(PDShading.SHADING_TYPE3);
                radialShading.setFunction(function);

                COSArray coords = new COSArray();
                //center of dot with radius
                coords.add(COSFloat.get(String.valueOf(xPosition)));
                coords.add(COSFloat.get(String.valueOf(yPosition)));
                coords.add(COSFloat.get("0.33")); // radius1
                //edge of dot without radius
                coords.add(COSFloat.get(String.valueOf(xPosition)));
                coords.add(COSFloat.get(String.valueOf(yPosition)));
                coords.add(COSInteger.get(0)); // radius2

                radialShading.setCoords(coords);
                contentStream.shadingFill(radialShading);
            }

            yPosition += spaceBetweenLines;
        }
    }

    public static void createDottedPatternUsingImage(PDDocument doc, PDPageContentStream contentStream, XYPoint topLeftPoint, SquareSize boxSize) throws IOException {

        InputStream isImage = LineUtils.class.getResourceAsStream("classpath:small_dot.png");
        BufferedImage image = ImageIO.read(isImage);
        PDImageXObject imageObj = LosslessFactory.createFromImage(doc, image);

        SquareSize dotSize = SquareSize.from(0.225F, 0.225F);

        float widthBetweenDots = 2.4F;
        float spaceBetweenLines = 1.2F;

        int lines = (int) (boxSize.height() / spaceBetweenLines);
        float yPosition = topLeftPoint.getY();

        for (int i = 0; i < lines; i++) {

            float xPosition = i % 2 == 0 ? topLeftPoint.getX() + 1.2F : topLeftPoint.getX();
            for (; xPosition < topLeftPoint.getX() + boxSize.width(); xPosition += widthBetweenDots) {
                contentStream.drawImage(imageObj, xPosition, yPosition, dotSize.width(), dotSize.height());
            }

            yPosition += spaceBetweenLines;
        }

    }
}