package com.sancai.oa.utils;

import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.util.Iterator;

/**
 * 将一个Sheet复制到另一个Excel中
 *
 * @author fanjing
 * @date 2019/8/23
 */
public class POIUtils {
    public class XSSFDateUtil extends DateUtil {

    }

    public static void copyCellStyle(XSSFCellStyle fromStyle, XSSFCellStyle toStyle) {

        toStyle.cloneStyleFrom(fromStyle);

    }

    public static void mergeSheetAllRegion(XSSFSheet fromSheet, XSSFSheet toSheet,int totalNums) {//合并单元格
        int num = fromSheet.getNumMergedRegions();
        CellRangeAddress cellR = null;
        for (int i = 0; i < num; i++) {
            cellR = fromSheet.getMergedRegion(i);
            cellR.setFirstRow(cellR.getFirstRow()+totalNums+3);
            cellR.setLastRow(cellR.getLastRow()+totalNums+3);
            cellR.setFirstColumn(cellR.getFirstColumn());
            cellR.setLastColumn(cellR.getLastColumn());
            toSheet.addMergedRegion(cellR);
        }
    }

    public static void copyCell(XSSFWorkbook wb, XSSFCell fromCell, XSSFCell toCell) {
        XSSFCellStyle newstyle = wb.createCellStyle();
        copyCellStyle(fromCell.getCellStyle(), newstyle);
        //toCell.setEncoding(fromCell.getEncoding());
        //样式
        toCell.setCellStyle(newstyle);
        if (fromCell.getCellComment() != null) {
            toCell.setCellComment(fromCell.getCellComment());
        }
        // 不同数据类型处理
        int fromCellType = fromCell.getCellType();
        toCell.setCellType(fromCellType);
        if (fromCellType == XSSFCell.CELL_TYPE_NUMERIC) {
            if (XSSFDateUtil.isCellDateFormatted(fromCell)) {
                toCell.setCellValue(fromCell.getDateCellValue());
            } else {
                toCell.setCellValue(fromCell.getNumericCellValue());
            }
        } else if (fromCellType == XSSFCell.CELL_TYPE_STRING) {
            toCell.setCellValue(fromCell.getRichStringCellValue());
        } else if (fromCellType == XSSFCell.CELL_TYPE_BLANK) {
            // nothing21
        } else if (fromCellType == XSSFCell.CELL_TYPE_BOOLEAN) {
            toCell.setCellValue(fromCell.getBooleanCellValue());
        } else if (fromCellType == XSSFCell.CELL_TYPE_ERROR) {
            toCell.setCellErrorValue(fromCell.getErrorCellValue());
        } else if (fromCellType == XSSFCell.CELL_TYPE_FORMULA) {
            toCell.setCellFormula(fromCell.getCellFormula());
        } else { // nothing29
        }

    }

    public static void copyRow(XSSFWorkbook wb, XSSFRow oldRow, XSSFRow toRow) {
        toRow.setHeight(oldRow.getHeight());
        for (Iterator cellIt = oldRow.cellIterator(); cellIt.hasNext(); ) {
            XSSFCell tmpCell = (XSSFCell) cellIt.next();
            XSSFCell newCell = toRow.createCell(tmpCell.getColumnIndex());
            copyCell(wb, tmpCell, newCell);
        }
    }

    public static int copySheet(XSSFWorkbook wb, XSSFSheet fromSheet, XSSFSheet toSheet) {
        int num = toSheet.getLastRowNum();
        mergeSheetAllRegion(fromSheet, toSheet,num);
        //设置列宽
        for (int i = 0; i <= fromSheet.getRow(fromSheet.getFirstRowNum()).getLastCellNum(); i++) {
            toSheet.setColumnWidth(i, fromSheet.getColumnWidth(i));
        }
        int i = 3;
        for (Iterator rowIt = fromSheet.rowIterator(); rowIt.hasNext(); ) {
            XSSFRow oldRow = (XSSFRow) rowIt.next();
            //已有的sheet的最后一个单元格的行数
            XSSFRow newRow = toSheet.createRow(num+(i++));
            copyRow(wb, oldRow, newRow);
        }
        return toSheet.getLastRowNum();
    }

}
