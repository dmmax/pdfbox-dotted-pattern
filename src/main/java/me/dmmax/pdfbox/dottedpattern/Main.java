package me.dmmax.pdfbox.dottedpattern;

import be.quodlibet.boxable.utils.FontUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {

        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        PDPageContentStream cs = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);

        LineUtils.createDottedPatternUsingTillingPattern(page, cs, XYPoint.from(10, 650), 550, 17);

        LineUtils.createDottedPattern(cs, XYPoint.from(10, 450), 550, 21);

        float leftMargin = 10;
        float yPosition = 600;
        float width = 550;
        LineUtils.printSolidLine(cs, 1.5F, XYPoint.from(leftMargin, yPosition),
                XYPoint.from(leftMargin + width, yPosition));

        yPosition -= 1.1F;
        LineUtils.createDashedPattern(cs, XYPoint.from(leftMargin, yPosition), width, 12);

        float fontTextSize12 = 12;
        PDType1Font fontBold = PDType1Font.HELVETICA_BOLD;

        yPosition = yPosition - FontUtils.getHeight(fontBold, fontTextSize12);
        TextUtils.showText(cs, fontBold, fontTextSize12, XYPoint.from(leftMargin, yPosition), "INFORMATION YOU SHOULD KNOW");

        cs.close();
        File file = new File("print.pdf");
        document.save(file);
        document.close();
    }
}
