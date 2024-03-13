package com.example.excelsql.dto;

import lombok.Data;

import java.io.File;
import java.util.List;

@Data
public class ExcelToSqlResponse {

    private String sqlFile;
    private List previewData;

    public ExcelToSqlResponse(String sqlFile, List previewData) {
        this.sqlFile = sqlFile;
        this.previewData = previewData;
    }
}
