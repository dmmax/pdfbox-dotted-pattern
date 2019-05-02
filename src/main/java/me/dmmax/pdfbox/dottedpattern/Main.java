package me.dmmax.pdfbox.dottedpattern;

import me.dmmax.pdfbox.dottedpattern.utility.LineUtils;
import me.dmmax.pdfbox.dottedpattern.utility.ReportParameters;
import me.dmmax.pdfbox.dottedpattern.utility.XYPoint;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.io.File;
import java.io.IOException;

public class Main {

    private PDDocument document;

    public static void main(String[] args) throws IOException {

        PDDocument document = new PDDocument();
        int lines = 9;
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        PDPageContentStream cs = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);

        ReportParameters reportParameters = ReportParameters.builder()
                .document(document)
                .build();

        LineUtils.createDottedPattern(cs, XYPoint.from(10, 700), 575, 17);

        LineUtils.createDottedPatternUsingImageOfFullLine(reportParameters, cs, XYPoint.from(10, 650), 575, 17);

        cs.close();
        File file = new File("print.pdf");
        document.save(file);
        document.close();
    }
}
