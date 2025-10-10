package com.example.attendance.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelExporter {

    /**
     * data rows expected as: [date, regNo, name, status]
     */
    public static void exportAttendanceToFile(List<String[]> data, String filePath) throws IOException {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("Attendance");

        // header style
        Font hf = wb.createFont();
        hf.setBold(true);
        hf.setFontHeightInPoints((short)12);
        CellStyle hs = wb.createCellStyle();
        hs.setFont(hf);

        Row header = sheet.createRow(0);
        String[] heads = {"Date", "RegNo", "Name", "Status"};
        for (int i = 0; i < heads.length; i++) {
            Cell c = header.createCell(i);
            c.setCellValue(heads[i]);
            c.setCellStyle(hs);
        }

        for (int r = 0; r < data.size(); r++) {
            Row row = sheet.createRow(r + 1);
            String[] rowData = data.get(r);
            for (int c = 0; c < rowData.length; c++) {
                row.createCell(c).setCellValue(rowData[c]);
            }
        }

        for (int i = 0; i < heads.length; i++) sheet.autoSizeColumn(i);

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            wb.write(fos);
        }
        wb.close();
    }
}
