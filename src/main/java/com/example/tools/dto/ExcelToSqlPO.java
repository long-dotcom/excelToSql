package com.example.tools.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class ExcelToSqlPO {

    /**
     * 要上传的文件
     * */
    private MultipartFile file;

    /**
     * 要操作的表名
     * */
    private String tableName;

    /**
     * sheet编号，0开始*/
    private int sheetNum;

    /**
     * 操作类型，add、update、delete
     * */
    private String type;

    /**
     * 要操作的数据在excel的行数开始下标
     * */
    private int startIndex;

    /**
     * 要操作的数据在excel的行数结束下标
     * */
    private int endIndex;

    /**
     * 修改、删除时的条件列编号
     * */
    private List<Integer> conditionNums;

}
