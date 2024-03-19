package com.example.excelsql.controller;

import com.example.excelsql.dto.ExcelToSqlPO;
import com.example.excelsql.dto.ExcelToSqlResponse;
import com.example.excelsql.server.ExcelToSqlServer;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.io.*;
import java.util.ArrayList;
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

    @GetMapping("/excelToSqlPage/preview")
    public ModelAndView excelToSqlPagePreview() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("preview");
        return modelAndView;
    }

    @PostMapping("/getSheetNames")
    public ResponseEntity<List<String>> getExcelSheetNames(@RequestParam("excelFile") MultipartFile file) throws IOException {
        // 创建输入流
        try (InputStream inputStream = file.getInputStream()) {
            // 创建一个工作簿对象来读取Excel文件
            Workbook workbook = new XSSFWorkbook(inputStream);

            // 获取所有工作表的名字
            List<String> sheetNames = new ArrayList<>();
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                sheetNames.add(sheet.getSheetName());
            }

            // 关闭工作簿以释放资源
            workbook.close();

            return ResponseEntity.ok(sheetNames);
        }
    }
}
