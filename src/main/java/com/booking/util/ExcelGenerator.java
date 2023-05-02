package com.booking.util;

import com.booking.payload.UserDTO;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

public class ExcelGenerator {

    public static ByteArrayInputStream usersToExcel(List<UserDTO> users) throws IOException {
        String[] COLUMN_HEADINGS = { "ID", "First Name", "Last Name", "Email", "Phone Number", "Profile Picture", "Created At", "Updated At" };

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream();) {

            Sheet sheet = workbook.createSheet("Users");
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 14);
            headerFont.setColor(IndexedColors.RED.getIndex());

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);

            // Create Header Row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < COLUMN_HEADINGS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(COLUMN_HEADINGS[i]);
                cell.setCellStyle(headerCellStyle);
            }

            // Create Rows and Cells for each User
            int rowNumber = 1;
            for (UserDTO user : users) {
                Row row = sheet.createRow(rowNumber++);
                row.createCell(0).setCellValue(user.getId());
                row.createCell(1).setCellValue(user.getFirstName());
                row.createCell(2).setCellValue(user.getLastName());
                row.createCell(3).setCellValue(user.getEmail());
                row.createCell(4).setCellValue(user.getPhoneNumber());
                row.createCell(5).setCellValue(user.getProfilePicture());
                row.createCell(6).setCellValue(user.getCreatedAt().toString());
                row.createCell(7).setCellValue(user.getUpdatedAt().toString());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}
