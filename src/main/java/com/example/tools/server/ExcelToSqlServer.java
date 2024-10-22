package com.example.tools.server;


import com.example.tools.dto.ExcelToSqlPO;

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
