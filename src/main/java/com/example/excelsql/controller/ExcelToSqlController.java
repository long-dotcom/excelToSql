package com.example.excelsql.controller;

import com.example.excelsql.dto.ExcelToSqlPO;
import com.example.excelsql.dto.ExcelToSqlResponse;
import com.example.excelsql.server.ExcelToSqlServer;
import jakarta.annotation.Resource;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.*;
import java.util.Base64;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<ExcelToSqlResponse> excelToSqlFile(ExcelToSqlPO po) {
        Map<String, Object> result = excelToSqlServer.excelToSql(po);
        File sqlFile = (File) result.get("sqlFile");
        List<String> previewDataList = (List<String>) result.get("previewData");

        // 将文件内容转换为Base64字符串
        String base64Content = null;
        try {
            base64Content = Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(sqlFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 创建 ExcelToSqlResponse 对象
        ExcelToSqlResponse response = new ExcelToSqlResponse(base64Content, previewDataList);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return ResponseEntity.ok()
                .headers(headers)
                .body(response);
    }

    @GetMapping("/excelToSqlPage")
    public ModelAndView excelToSqlPage() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("index");
        return modelAndView;
    }
}
