package cn.bctools.teamwork.common.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author admin
 */
public class ExcelUtils {

    private static final String EXCEL_XLS = "xls";
    private static final String EXCEL_XLSX = "xlsx";

    /**
     * 判断excel的版本，并根据文件流数据获取workbook
     *
     * @throws Exception
     */
    public static Workbook getWorkBook(InputStream is, String fileName) throws Exception {

        Workbook workbook = null;
        if (fileName.endsWith(EXCEL_XLS)) {
            workbook = new HSSFWorkbook(is);
        } else if (fileName.endsWith(EXCEL_XLSX)) {
            workbook = new XSSFWorkbook(is);
        }
        return workbook;
    }

    public static List<List<Object>> getListInfo(MultipartFile file) {
        ExcelReader excelReader = null;
        InputStream input = null;
        try {
            if (file != null) {
                String fileExt = FileUtil.extName(file.getOriginalFilename());
                if (EXCEL_XLS.equals(fileExt) || EXCEL_XLSX.equals(fileExt)) {
                    input = file.getInputStream();
                    List<List<Object>> info = new ArrayList<>();
                    ExcelUtil.readBySax(input, 0, (i, i1, list) -> {
                        if (i1 >= 2) {
                            info.add(list);
                        }
                    });
                    return info;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (excelReader != null) {
                    excelReader.close();
                }
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
