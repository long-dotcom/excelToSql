package com.example.excelsql.server;


import com.example.excelsql.dto.ExcelToSqlPO;

import java.io.File;


/**
 * @author LongShunli
 * @date 2023/09/06
 */
public interface ExcelToSqlServer {
    /**
     * @return {@link Object}
     */
    public File excelToSql( ExcelToSqlPO po);
}
