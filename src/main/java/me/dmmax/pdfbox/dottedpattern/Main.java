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
        printDottedWithRadialShading(cs);

        cs.close();
        File file = new File("1.pdf");
        document.save(file);
    }

    private static void printDottedWithRadialShading(PDPageContentStream contentStream) throws IOException {

        COSDictionary fdict = new COSDictionary();
        fdict.setInt(COSName.FUNCTION_TYPE, 2);
        COSArray domain = new COSArray();
        domain.add(COSInteger.get(0));
        domain.add(COSInteger.get(1));
        COSArray c0 = new COSArray();
        c0.add(COSFloat.get("0"));
        c0.add(COSFloat.get("0"));
        c0.add(COSFloat.get("0"));
        COSArray c1 = new COSArray();
        c1.add(COSFloat.get("0"));
        c1.add(COSFloat.get("0"));
        c1.add(COSFloat.get("0"));
        fdict.setItem(COSName.DOMAIN, domain);
        fdict.setItem(COSName.C0, c0);
        fdict.setItem(COSName.C1, c1);
        fdict.setInt(COSName.N, 1);
        PDFunctionType2 func = new PDFunctionType2(fdict);

        PDShadingType3 radialShading = new PDShadingType3(new COSDictionary());
        radialShading.setColorSpace(PDDeviceRGB.INSTANCE);
        radialShading.setShadingType(PDShading.SHADING_TYPE3);

        COSArray coords2 = new COSArray();
        coords2.add(COSInteger.get(700));
        coords2.add(COSInteger.get(1));
        coords2.add(COSInteger.get(1)); // radius1
        coords2.add(COSInteger.get(2));
        coords2.add(COSInteger.get(2));
        coords2.add(COSInteger.get(2)); // radius2

        radialShading.setCoords(coords2);
        radialShading.setFunction(func);

        contentStream.shadingFill(radialShading);
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
