package me.dmmax.pdfbox.dottedpattern.utility;

import lombok.experimental.UtilityClass;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.function.PDFunctionType2;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.color.PDPattern;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDTilingPattern;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShading;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShadingType3;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.util.Charsets;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@UtilityClass
public class LineUtils {

    private static final float LINE_SPACE_BETWEEN_DOT_PATTERN_LINE = 1.2F;

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
        contentStream.setLineCapStyle(0);
        for (int i = 0; i < lines; i++) {
            contentStream.setLineDashPattern(new float[]{0.3F, 0.6F, 0.6F, 0.9F}, i % 2 == 0 ? 1.5F : 2.7F);
            contentStream.moveTo(startPoint.getX(), startPoint.getY());
            contentStream.lineTo(startPoint.getX() + width, startPoint.getY());
            contentStream.stroke();
            startPoint = startPoint.minusY(lineSpaceDashedPattern);
        }
    }

    public static void createDottedPattern(PDPageContentStream contentStream, XYPoint topLeftPoint, float width, int lines) throws IOException {
        createDottedPattern(contentStream, topLeftPoint, width, lines, 1);
    }


    private static void createDottedPattern(PDPageContentStream contentStream, XYPoint topLeftPoint, float width, int lines, float increment) throws IOException {

        contentStream.setLineWidth(0.3F * increment);
        contentStream.setLineCapStyle(0);

        contentStream.setStrokingColor(0, 0, 0);
        float lineSpaceDottedPattern = 1.2F * increment;

        float firstPhase = 1.2F * increment;
        float secondPhase = 0;

        for (int i = 0; i < lines; i++) {
            
            contentStream.setLineDashPattern(new float[]{0.3F * increment, 2.1F * increment}, i % 2 == 0 ? firstPhase : secondPhase);
            contentStream.moveTo(topLeftPoint.getX(), topLeftPoint.getY());
            contentStream.lineTo(topLeftPoint.getX() + width, topLeftPoint.getY());
            contentStream.stroke();
            topLeftPoint = topLeftPoint.minusY(lineSpaceDottedPattern);
        }
    }

    public static void createDottedPatternUsingTillingPattern(PDPage page, PDPageContentStream cs, XYPoint leftPoint, float width, int lines) throws IOException {

        PDTilingPattern tilingPattern = new PDTilingPattern();
        tilingPattern.setBBox(new PDRectangle(0, 0, 2.4F, 2.4F));
        tilingPattern.setPaintType(PDTilingPattern.PAINT_UNCOLORED);
        tilingPattern.setTilingType(PDTilingPattern.TILING_NO_DISTORTION);
        tilingPattern.setXStep(2.4F);
        tilingPattern.setYStep(2.4F);

        COSName patternName = page.getResources().add(tilingPattern);
        OutputStream os = tilingPattern.getContentStream().createOutputStream();

        String s = lines % 2 == 1
                ? "0.3 w 0 0.3 m 0.3 0.3 l s " + "0.3 w 1.2 1.5 m 1.5 1.5 l s"
                : "0.3 w 1.2 0.3 m 1.5 0.3 l s " + "0.3 w 0 1.5 m 0.3 1.5 l s ";

        os.write(s.getBytes(Charsets.US_ASCII));
        os.close();

        PDColorSpace patternColorSpace = new PDPattern(null, PDDeviceRGB.INSTANCE);
        PDColor patterColor = new PDColor(
                new float[]{0, 0, 0},
                patternName,
                patternColorSpace);


        cs.addRect(leftPoint.getX(), leftPoint.getY(), width, 1.2F * lines);
        cs.setNonStrokingColor(patterColor);
        cs.fill();
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
                coords.add(COSFloat.get("3.34")); // radius1
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

    public static void createDottedPatternUsingImageOfFullLine(ReportParameters reportParameters, PDPageContentStream contentStream, XYPoint leftDownPoint, float width, int lines) throws IOException {

        SquareSize sizeOfDotPattern = SquareSize.from(width, lines * LINE_SPACE_BETWEEN_DOT_PATTERN_LINE);
        BufferedImage image = reportParameters.getDotPatternBySize(sizeOfDotPattern);

        if (image == null) {
            image = generateDotPatternImageBySize(sizeOfDotPattern, lines);
            reportParameters.addDotPatternImage(sizeOfDotPattern, image);
        }

        PDImageXObject imageXObject = LosslessFactory.createFromImage(reportParameters.document(), image);

        contentStream.drawImage(imageXObject, leftDownPoint.getX(), leftDownPoint.getY(), width, lines * LINE_SPACE_BETWEEN_DOT_PATTERN_LINE);
    }

    private static BufferedImage generateDotPatternImageBySize(SquareSize sizeOfDotPattern, int lines) throws IOException {

        float increment = 3.34F;
        PDDocument imageDocument = new PDDocument();
        PDPage page = new PDPage(new PDRectangle(sizeOfDotPattern.width() * increment, sizeOfDotPattern.height() * increment));
        imageDocument.addPage(page);

        PDPageContentStream contentStream = new PDPageContentStream(imageDocument, page, PDPageContentStream.AppendMode.APPEND, true, true);
        createDottedPattern(contentStream, XYPoint.from(0.9F, lines * LINE_SPACE_BETWEEN_DOT_PATTERN_LINE * increment - 0.6F), sizeOfDotPattern.width() * increment, lines, increment);
        contentStream.close();

        PDFRenderer pdfRenderer = new PDFRenderer(imageDocument);

        BufferedImage image = pdfRenderer.renderImageWithDPI(0, 72 * 9, ImageType.BINARY);

        imageDocument.save(new File("123.pdf"));
        imageDocument.close();

        return image;
    }
}