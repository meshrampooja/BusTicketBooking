package com.booking.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import com.booking.entities.User;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

public class PDFGenerator {

    public static ByteArrayInputStream generateUserReport(List<User> users) {

        Document document = new Document(PageSize.A4, 50, 50, 50, 50);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph("Users"));

            for (User user : users) {
                document.add(new Paragraph(user.getFirstName() + " " + user.getLastName()));
                document.add(new Paragraph(user.getEmail()));
                document.add(new Paragraph(user.getPhoneNumber()));
                document.add(new Paragraph(" "));
            }

            document.close();

        } catch (DocumentException ex) {

            ex.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
}
