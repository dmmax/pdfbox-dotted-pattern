package me.dmmax.pdfbox.dottedpattern;


import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class Main {

    public static void main(String[] args) throws IOException {

        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        PDPageContentStream cs = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);

        printDottedWithImage(cs, document, 10, 650, 400);
        printDottedPattern(cs, 10, 600, 400, 15);
        printImage(cs, document, 10, 500, 200, 15);
        printDottedWithRadialShading(cs, 10, 550, 400, 15);

        cs.close();
        File file = new File("1.pdf");
        document.save(file);
        document.close();
    }

    private static void printDottedWithRadialShading(PDPageContentStream contentStream, float xStart, float yStart, float width, float height) throws IOException {

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

        int lines = (int) (height / spaceBetweenLines);

        float yPosition = yStart;
        for (int i = 0; i < lines; i++) {

            float xPosition = i % 2 == 0 ? xStart : xStart + 1.5F;
            System.out.println(i + "; xPosition: " + xPosition);
            for (; xPosition < xStart + width; xPosition += widthBetweenDots) {

                System.out.println(" " + xPosition);
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

    private static void printDottedWithImage(PDPageContentStream contentStream, PDDocument doc, float xStart, float yStart, float width) throws IOException {

        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("small_dot.png");
        BufferedImage image = ImageIO.read(inputStream);
        BufferedImage resize = resize(image, 1, 1);
        PDImageXObject imageObj = LosslessFactory.createFromImage(doc, resize);

        float space = 1.5F;
        for (int curPos = 0; curPos < width; curPos++) {
            contentStream.drawImage(imageObj, xStart + curPos, yStart);
            curPos += space;
        }
    }

    private static void printDottedPattern(PDPageContentStream contentStream, float xLeftTop, float yLeftTop, float width, float height) throws IOException {

        contentStream.setLineWidth(0.55F);
        contentStream.setLineCapStyle(2);
        contentStream.setStrokingColor(112, 112, 112);
        float lineSpaceDottedPattern = 1.25F;
        int lines = (int) (height / lineSpaceDottedPattern) + 1;

        for (int i = 0; i < lines; i++) {
            contentStream.setLineDashPattern(new float[]{0.05F, 2.33F}, i % 2 == 1 ? 1.17F : 0F);
            contentStream.moveTo(xLeftTop, yLeftTop);
            contentStream.lineTo(xLeftTop + width, yLeftTop);
            contentStream.stroke();
            yLeftTop -= lineSpaceDottedPattern;
        }
    }

    private static void printImage(PDPageContentStream contentStream, PDDocument doc, float startX, float startY, float width, float height) throws IOException {

        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("result.png");
        BufferedImage image = ImageIO.read(inputStream);
        Image tmp = image.getScaledInstance(((int) width), ((int) height), Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(((int) width), ((int) height), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        File resizedFile = new File("resized.png");
        resizedFile.deleteOnExit();

        ImageIO.write(resized, "png", resizedFile);

        PDImageXObject imageObj = PDImageXObject.createFromFile("resized.png", doc);
        contentStream.drawImage(imageObj, startX, startY);
    }

    private static BufferedImage resize(BufferedImage image, int width, int height) {
        Image tmp = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resized;
    }
}
