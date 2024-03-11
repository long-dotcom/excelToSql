package com.example.excelsql.controller;

import com.example.excelsql.dto.ExcelToSqlPO;
import com.example.excelsql.server.ExcelToSqlServer;
import jakarta.annotation.Resource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.*;

/**
 * excel到sql控制器
 *
 * @author LongShunli
 * @date 2023/09/06
 *
 */
@RestController
public class ExcelToSqlController {
    @Resource
    private ExcelToSqlServer excelToSqlServer;

    /**
     * 将转换后的sql以流的方式返回
     *
     * @param po po
     * @return {@link ResponseEntity}<{@link InputStreamResource}>
     */
    @PostMapping("/excelToSqlFile")
    public ResponseEntity<InputStreamResource> excelToSqlFile(ExcelToSqlPO po) {
        File sqlFile = excelToSqlServer.excelToSql(po);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "text/plain"); // 设置内容类型为纯文本

        InputStreamResource resource = null;
        try {
            resource = new InputStreamResource(new FileInputStream(sqlFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(sqlFile.length())
                .body(resource);
    }

    @GetMapping("/excelToSqlPage")
    public ModelAndView excelToSqlPage() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("html/excelToSql");
        return modelAndView;
    }
}
