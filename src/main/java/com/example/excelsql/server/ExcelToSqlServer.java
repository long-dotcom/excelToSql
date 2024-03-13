package com.example.excelsql.server;


import com.example.excelsql.dto.ExcelToSqlPO;

import java.util.Map;


/**
 * @author LongShunli
 * @date 2023/09/06
 */
public interface ExcelToSqlServer {
    /**
     * @return {@link Object}
     */
    public Map<String, Object> excelToSql(ExcelToSqlPO po);
}
