package com.example.excelsql.server.impl;

import com.example.excelsql.dto.ExcelToSqlPO;
import com.example.excelsql.server.ExcelToSqlServer;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ExcelToSqlServerImpl implements ExcelToSqlServer {


    /**
     * @return {@link Object}
     */
    @Override
    public Map<String, Object> excelToSql(ExcelToSqlPO po) {
        MultipartFile file = po.getFile();
        InputStream inputStream = null;
        Sheet sheet;
        try {
            inputStream = file.getInputStream();
            Workbook workbook = new XSSFWorkbook(inputStream);
            sheet = workbook.getSheetAt(po.getSheetNum());
            switch (po.getType()) {
                case "add" -> {
                    // 处理增加操作
                    return add(sheet, po.getTableName());
                }
                case "delete" -> {
                    // 处理删除操作
                    return delete(sheet, po.getTableName(), po.getStartIndex(), po.getEndIndex(), po.getConditionNums());
                }
                case "update" ->
                // 处理修改操作
                {
                    return update(sheet, po.getTableName(), po.getStartIndex(), po.getEndIndex(), po.getConditionNums());
                }
                default ->
                    // 默认操作，可以是增加也可以是其他处理方式
                        System.out.println("执行默认操作（增加）");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // log the exception or handle it appropriately
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    private Map update(Sheet sheet, String tableName, int startIndex, int endIndex, List<Integer> conditionNums) {
        List<String> sqlStatements = new ArrayList<>();
        Map map = new HashMap<>();
        for (int rowNum = startIndex; rowNum <= endIndex; rowNum++) {
            StringBuilder updateSQL = new StringBuilder("UPDATE " + tableName + " SET ");
            Row row = sheet.getRow(rowNum);
            if (row != null) {
                for (int colNum = 0; colNum < row.getLastCellNum(); colNum++) {
                    Cell cell = row.getCell(colNum);

                    if (cell != null && isCellFilledWithColor(cell)) {
                        String columnName = getColumnName(colNum, sheet.getRow(0));
                        String value = getCellValueAsString(cell);
                        updateSQL.append(columnName).append(" = ").append("'").append(value).append("'").append(", ");
                    }
                }
                //去除流中的最后一个”,“
                updateSQL.setLength(updateSQL.length() - 2);
                updateSQL.append(buildCondition(sheet, conditionNums, rowNum));
            }
            sqlStatements.add(String.valueOf(updateSQL));
            if ((rowNum - startIndex) <= 26) {
                map.put("previewData", sqlStatements);
                List<String> subSqlStatements = new ArrayList<>(sqlStatements);
                map.put("previewData", subSqlStatements);
            }
        }

        StringBuilder sqlContent = new StringBuilder();
        for (String sqlStatement : sqlStatements) {
            sqlContent.append(sqlStatement).append("\n");
        }
        byte[] sqlBytes = sqlContent.toString().getBytes(StandardCharsets.UTF_8);
        String base64Sql = Base64.getEncoder().encodeToString(sqlBytes);
        map.put("base64Sql", base64Sql);
        return map;
    }

    /**
     * 构建条件
     *
     * @param sheet         床单
     * @param conditionNums 条件nums
     * @param rowNum        行号
     * @return {@link StringBuilder}
     */
    private StringBuilder buildCondition(Sheet sheet, List<Integer> conditionNums, int rowNum) {
        StringBuilder whereSQL = new StringBuilder(" WHERE 1=1 ");
        Row row0 = sheet.getRow(0);
        for (Integer conditionNum : conditionNums) {
            //条件字段
            String stringCellValue = row0.getCell(conditionNum).getStringCellValue();
            //字段值
            Row row = sheet.getRow(rowNum);
            Cell cell = row.getCell(conditionNum);
            String stringCellValue1 = getCellValueAsString(cell);
            whereSQL.append(" AND ").append(stringCellValue).append(" = ").append("'").append(stringCellValue1).append("'");
        }
        whereSQL.append(";");
        return whereSQL;
    }

    private boolean isCellFilledWithColor(Cell cell) {
        CellStyle cellStyle = cell.getCellStyle();
        Color fillForegroundColor = cellStyle.getFillForegroundColorColor();

        return fillForegroundColor != null;
    }

    private String getColumnName(int colNum, Row row) {
        // Replace this with your logic to convert column index to column name
        return row.getCell(colNum).getStringCellValue();
    }

    private Map delete(Sheet sheet, String tableName, int startIndex, int endIndex, List<Integer> conditionNums) {
        StringBuilder deleteSQL = new StringBuilder("DELETE FROM " + tableName);
        StringBuilder whereSQL = new StringBuilder(" WHERE ");
        String sql = "";
        if (conditionNums.size() <= 1) {
            for (Integer conditionNum : conditionNums) {
                Row row1 = sheet.getRow(0);
                String stringCellValue = row1.getCell(conditionNum).getStringCellValue();
                whereSQL.append(stringCellValue);
            }
            whereSQL.append(" IN ");
            StringBuilder valuesSQL = new StringBuilder();
            valuesSQL.append(" (");
            int start = startIndex;
            while (start <= endIndex) {
                saveData(sheet, conditionNums, valuesSQL, start);
                start++;
                if (start <= endIndex) {
                    valuesSQL.append(", ");
                }
            }
            valuesSQL.append(" );");
            whereSQL.append(valuesSQL);
            sql = deleteSQL.toString() + whereSQL.toString();
        } else {
            whereSQL.append("(");
            int index = 0;
            for (Integer conditionNum : conditionNums) {
                Row row1 = sheet.getRow(0);
                String stringCellValue = row1.getCell(conditionNum).getStringCellValue();
                whereSQL.append(stringCellValue);
                if (index < conditionNums.size() - 1) {
                    whereSQL.append(", ");
                }
                index++;
            }
            whereSQL.append(") IN (");
            StringBuilder valuesSQL = new StringBuilder();
            int start = startIndex;
            while (start <= endIndex) {
                valuesSQL.append("(");
                saveData(sheet, conditionNums, valuesSQL, start);
                valuesSQL.append(")");
                if (start < endIndex) {
                    valuesSQL.append(",");
                }
                start++;
            }
            whereSQL.append(valuesSQL).append(")");
            sql = deleteSQL.toString() + whereSQL.toString();
        }
        Map map = new HashMap<>();
        StringBuilder sqlContent = new StringBuilder();
        sqlContent.append(sql).append("\n");
        byte[] sqlBytes = sqlContent.toString().getBytes(StandardCharsets.UTF_8);
        String base64Sql = Base64.getEncoder().encodeToString(sqlBytes);
        map.put("base64Sql", base64Sql);
        List list = new ArrayList<>();
        list.add(sql);
        map.put("previewData", list);
        return map;
    }

    private void saveData(Sheet sheet, List<Integer> conditionNums, StringBuilder valuesSQL, int start) {
        for (int i = 0; i < conditionNums.size(); i++) {
            Row row = sheet.getRow(start);
            String cellValueAsString = getCellValueAsString(row.getCell(i));
            valuesSQL.append("'").append(cellValueAsString).append("'");
            if (i < conditionNums.size() - 1) {
                valuesSQL.append(", ");
            }
        }
    }

    private Map add(Sheet sheet, String tableName) {
        List<String> sqlStatements = new ArrayList<>();

        StringBuilder insertSQL = new StringBuilder("INSERT INTO " + tableName + " (");

        Row row1 = sheet.getRow(0);

        int numColumns = row1.getPhysicalNumberOfCells();

        for (int i = 0; i < numColumns; i++) {
            Cell cell = row1.getCell(i);
            // 获取单元格数据作为列名
            String columnName = cell.getStringCellValue();
            insertSQL.append(columnName);
            if (i < numColumns - 1) {
                insertSQL.append(", ");
            }
        }
        insertSQL.append(") ");

        int rowIndex = 0; // 用于跟踪当前行的索引
        Map returnData = new HashMap<String, Object>();
        for (Row row : sheet) {
            StringBuilder valuesSQL = new StringBuilder("VALUES (");
            if (rowIndex > 0) {
                for (int i = 0; i < numColumns; i++) {
                    Cell cell = row.getCell(i);
                    String cellValue = getCellValueAsString(cell);
                    //值为空时，直接为null
                    if ("".equals(cellValue)) {
                        valuesSQL.append("null");
                    } else {
                        valuesSQL.append("'").append(cellValue).append("'");
                    }
                    if (i < numColumns - 1) {
                        valuesSQL.append(", ");
                    }
                }
                valuesSQL.append(");");
                String finalSQL = insertSQL.toString() + valuesSQL.toString();
                sqlStatements.add(finalSQL);
            }
            rowIndex++;
            if (rowIndex == 26) {
                List<String> subSqlStatements = new ArrayList<>(sqlStatements);
                returnData.put("previewData", subSqlStatements);
            }
        }


        // 生成 .sql 文件并将 SQL 语句写入其中
        StringBuilder sqlContent = new StringBuilder();
        for (String sqlStatement : sqlStatements) {
            sqlContent.append(sqlStatement).append("\n");
        }
        byte[] sqlBytes = sqlContent.toString().getBytes(StandardCharsets.UTF_8);
        String base64Sql = Base64.getEncoder().encodeToString(sqlBytes);

        returnData.put("base64Sql", base64Sql);
        return returnData;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    // 如果是日期格式，可以按照需要格式化日期并返回字符串
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    return sdf.format(cell.getDateCellValue());
                } else {
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        // 如果是整数，不显示小数点
                        return String.valueOf((long) numericValue);
                    } else {
                        // 如果是小数，保留小数点和末尾的零
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }


}
