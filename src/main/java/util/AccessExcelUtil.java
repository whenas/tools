package com.phxl.ysy.util;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;


/**
 * EXCEL文件操作类
 *
 * @author lusongjiong
 */
public class AccessExcelUtil {
    public final static Logger log = LoggerFactory.getLogger(AccessExcelUtil.class);
    private static final String STYLE_STRING = "string";
    private static final String STYLE_NUMBER = "number";
    private static final String STYLE_DATE = "date";
    /**
     * EXCEL 2003的后缀名(小写)：.XLS
     */
    public static final String XLS_SUFFIX = ".xls";

    /**
     * EXCEL 2003的后缀名(大写)：.XLS
     */
    public static final String U_XLS_SUFFIX = ".XLS";

    /**
     * EXCEL 2007的后缀名(小写)：.XLSX
     */
    public static final String XLSX_SUFFIX = ".xlsx";

    /**
     * EXCEL 2007的后缀名(大写)：.XLSX
     */
    public static final String U_XLSX_SUFFIX = ".XLSX";
    protected AccessExcelUtil() {
    }

    /**
     * 功能：取到cls的以head开头的方法并保存到hashMap对象中
     * 实现流程：先取到cls的以head开头后缀在vector中存在的的方法
     * 返回保存了方法hashMap的对象
     *
     * @param head          String
     * @param attributeList ArrayList<String>对象属性名列表集合
     * @param cls           Class
     * @param <T>
     * @return HashMap
     */
    public static <T> Map<String, Method> getMethod(String head,
                                                    List<String> attributeList,
                                                    Class<T> cls) {
        Map<String, Method> methodMap = new HashMap<String, Method>();
        // 取到cls的方法
        Method[] methods = cls.getMethods();
        for (int j = 0; j < attributeList.size(); j++) {
            for (int i = 0; i < methods.length; i++) {
                // 取到cls的以head开头后缀在vector中存在的的方法
                if (methods[i].getName().equalsIgnoreCase(head + attributeList.get(j))) {
                    methodMap.put(attributeList.get(j), methods[i]);
                }
            }
        }
        return methodMap;
    }

    /**
     * EXCEL文件导入
     *
     * @param xlsStream       文件路径
     * @param cls             Class
     * @param attributeList   ArrayList<String>对象属性名列表集合
     * @param dataStartRow    数据开始读取行下标(以0开始)
     * @param dataStartColumn 数据开始读取列下标(以0开始)
     * @param <T>
     * @return List
     */
    public static <T> List<T> parseExcel(InputStream xlsStream,
                                         Class<T> cls,
                                         List<String> attributeList,
                                         Integer dataStartRow,
                                         Integer dataStartColumn) {
        // 初始化一个解析后的结果集对象
        List<T> results = new ArrayList<T>();
        if (null == xlsStream) {
            return results;
        }

        // 默认第一行为模版说明与标题不是数据部分，从row＝1开始读取数据
        if (null == dataStartRow || dataStartRow < 0) {
            dataStartRow = 1;
        }
        if (null == dataStartColumn || dataStartColumn < 0) {
            dataStartColumn = 0;
        }

        int sheetIndex = 0;
        try {
            // 获取工作薄Workbook对象信息
            Workbook workbook = WorkbookFactory.create(xlsStream);
            if (null == workbook) {
                return results;
            }
            // 循环解析工作薄Workbook对象信息
            for (; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                // 解析Excel工作表数据
                parseSheetData(sheet, results, cls, attributeList, dataStartRow, dataStartColumn);
            }
        } catch (Exception ex) {
            throw new RuntimeException("excel文件导入; excel文件的[第"
                    + (sheetIndex + 1) + "工作表" + ex);
        } finally {
            // 关闭输入流
            AccessExcelUtil.closeInputStream(xlsStream);
        }
        // 将数据保存到数据库中
        return results;
    }

    /**
     * 指定工作表的EXCEL文件导入
     *
     * @param xlsStream         文件路径
     * @param cls               Class
     * @param attributeList     ArrayList<String>对象属性名列表集合
     * @param dataStartRow      数据开始读取行下标(以0开始)
     * @param dataStartColumn   数据开始读取列下标(以0开始)
     * @param appointSheetIndex 工作表下标(以0开始)
     * @param <T>
     * @return List
     */
    public static <T> List<T> parseExcel(InputStream xlsStream,
                                         Class<T> cls,
                                         List<String> attributeList,
                                         Integer dataStartRow,
                                         Integer dataStartColumn,
                                         Integer appointSheetIndex) {
        // 初始化一个解析后的结果集对象
        List<T> results = new ArrayList<T>();
        if (null == xlsStream || null == appointSheetIndex || appointSheetIndex < 0) {
            return results;
        }

        // 默认第一行为模版说明与标题不是数据部分，从row＝1开始读取数据
        if (null == dataStartRow || dataStartRow < 0) {
            dataStartRow = 1;
        }
        if (null == dataStartColumn || dataStartColumn < 0) {
            dataStartColumn = 0;
        }

        try {
            // 获取工作薄Workbook对象信息
            Workbook workbook = WorkbookFactory.create(xlsStream);
            if (null == workbook) {
                return results;
            }

            // 解析Excel工作表数据
            parseSheetData(workbook.getSheetAt(appointSheetIndex), results, cls, attributeList, dataStartRow, dataStartColumn);
        } catch (Exception ex) {
            throw new RuntimeException("excel文件导入; excel文件的[第"
                    + (appointSheetIndex + 1) + "工作表" + ex);
        } finally {
            // 关闭输入流
            AccessExcelUtil.closeInputStream(xlsStream);
        }
        // 将数据保存到数据库中
        return results;
    }

    /**
     * 解析Excel工作表数据
     *
     * @param sheet             Excel工作表
     * @param resultDataList    返回数据列表
     * @param cls               Class
     * @param attributeNameList ArrayList<String>对象属性名列表集合
     * @param dataStartRow      数据开始读取行下标(以0开始)
     * @param dataStartColumn   数据开始读取列下标(以0开始)
     * @param <T>
     * @return
     */
    private static <T> void parseSheetData(Sheet sheet,
                                           List<T> resultDataList,
                                           Class<T> cls,
                                           List<String> attributeNameList,
                                           Integer dataStartRow,
                                           Integer dataStartColumn) {
        if (null == sheet || sheet.getPhysicalNumberOfRows() <= 0) {
            return;
        }

        int currentRowIndex = dataStartRow; // 当前处理行号
        int currentColumnIndex = 0; // 当前处理列号
        try {
            // 总数据列
            int countColumn = attributeNameList.size() + dataStartColumn;
            // 取到所有的给定字段的set方法
            Map<String, Method> methodMap = getMethod("set", attributeNameList, cls);
            // 循环解析工作薄Workbook对象行数据
            for (; currentRowIndex < sheet.getPhysicalNumberOfRows(); currentRowIndex++) {
                Row sheetRow = sheet.getRow(currentRowIndex);
                if (null == sheetRow) {
                    break;
                }

                // 创建数据对象
                T dataObj = cls.newInstance();
                boolean rowDataIsNull = true;
                // 循环解析工作薄Workbook对象行上的列
                currentColumnIndex = dataStartColumn;
                for (; currentColumnIndex < countColumn; currentColumnIndex++) {
                    // 取出Excel表格中的具体一个单元格的数据
                    Cell cell = sheetRow.getCell(currentColumnIndex);
                    if (null != cell) {
                        rowDataIsNull = false;
                        // 要调用的方法
                        Method method = methodMap.get(attributeNameList.get(currentColumnIndex));

                        // 得到字段返回值参数类型
                        Class<?> paraType = method.getParameterTypes()[0];

                        // 解析单元格的数据值
                        Object cellData = parseCellValue(cell, paraType);
                        // 为对象方法赋值
                        method.invoke(dataObj, cellData);
                    }
                }
                // 如果是行上值都为空，跳出循环行
                if (rowDataIsNull) {
                    break;
                } else {
                    resultDataList.add(dataObj);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("解析第 " + (currentRowIndex + 1)
                    + "行,第" + (currentColumnIndex + 1) + "列 ]过程中出现异常: " + ex);
        }
    }

    /**
     * 解析单元格的数据值
     *
     * @param cell     单元格
     * @param paraType 数据类型
     */
    private static Object parseCellValue(Cell cell, Class<?> paraType) {
        Object cellData = new Object();
        // 当前的单元格的类型
        int current = -50;
        // 得到当前单元格的（字符类型）的值
        if (Cell.CELL_TYPE_STRING == cell.getCellType()) {
            current = Cell.CELL_TYPE_STRING;
            String labelValue = cell.getStringCellValue();
             if (Long.class.equals(paraType)) {
                cellData = new Long(labelValue);
            } else if (BigDecimal.class.equals(paraType)) {
                cellData = new BigDecimal(labelValue);
            } else if (BigInteger.class.equals(paraType)) {
                cellData = new BigInteger(labelValue);
            } else if (Integer.class.equals(paraType)) {
                cellData = new Integer(labelValue);
            } else {
                cellData = labelValue;
            }
        }

        // 得到当前单元格的（数值类型）的值
        if (Cell.CELL_TYPE_NUMERIC == cell.getCellType()) {
            current = Cell.CELL_TYPE_NUMERIC;
            Double numberValue = cell.getNumericCellValue();

            if (BigInteger.class.equals(paraType)) {
                // 返回值参数类型BigInteger
                cellData = new BigInteger(Long.toString(numberValue.longValue()));
            } else if (BigDecimal.class.equals(paraType)) {
                // 返回值参数类型BigDecimal
                cellData = new BigDecimal(numberValue.toString());
            } else if (Long.class.equals(paraType)) {
                // 返回值参数类型Long
                cellData = numberValue.longValue();
            } else if (Double.class.equals(paraType)) {
                // 返回值参数类型Double
                cellData = numberValue;
            } else if (Integer.class.equals(paraType)) {
                // 返回值参数类型Integer
                cellData = numberValue.intValue();
            } else if (Float.class.equals(paraType)) {
                // 返回值参数类型Float
                cellData = numberValue.floatValue();
            } else if (Date.class.equals(paraType)) {
                // 日期类型Date
                cellData = cell.getDateCellValue();
            } else if (Boolean.class.equals(paraType)) {
                // 布尔类型Boolean
                cellData = cell.getBooleanCellValue();
            } else {
                // 返回值参数类型String
                cellData = new BigDecimal(numberValue).toString();
                if (cellData != null) {
                    int index = ((String) cellData).indexOf('.');
                    if (index > 1) {
                        cellData = ((String) cellData).substring(0, index);
                    }
                }
            }
        }

        // 得到当前单元格的（日期类型）的值
        if (Cell.CELL_TYPE_FORMULA == cell.getCellType()) {
            current = Cell.CELL_TYPE_FORMULA;
            if (Date.class.equals(paraType)) {
                cellData = cell.getDateCellValue();
            } else {
                cellData = cell.getCellFormula();
            }
        }

        // 数据类型为空
        if (current == -50) {
            cellData = null;
        }
        return cellData;
    }

    /**
     * 功能：把实体对象保存到Excel文件中
     * 实现流程：先将取到对象中每个字段的值，保存到相应的cell 关闭workbook
     *
     * @param filePath          String 文件的路径
     * @param fileName          String 文件名
     * @param heads             String[] EXCEL文件列表头信息数组
     * @param attributeNameList ArrayList对象中存放Excel表格各列对应数据库表的中字段的名称
     * @param dataList          List 实体对象列表数据
     * @return void
     */
    public static void createExcel(String filePath,
                                   String fileName,
                                   List<String> heads,
                                   List<String> attributeNameList,
                                   List<?> dataList) {
        OutputStream fileOpStream = null;
        try {
            // 文件包创建
            File folder = new File(filePath);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            // 文件名
            String fullPath = filePath.concat(File.separator).concat(fileName);
            fileOpStream = new FileOutputStream(fullPath);
            createExcel(fileOpStream, fileName, heads, attributeNameList, dataList);
        } catch (FileNotFoundException fe) {
            throw new RuntimeException("导出Excel失败, 创建文件发生异常 :" + fe);
        } catch (Exception e) {
            throw new RuntimeException("导出Excel失败 :" + e);
        } finally {
            // 关闭输出流
            AccessExcelUtil.closeOutputStream(fileOpStream);
        }
    }

    /**
     * 功能：把实体对象保存到Excel文件中
     * 实现流程：先将取到对象中每个字段的值，保存到相应的cell 关闭workbook
     *
     * @param fileOpStream OutputStream 输出流
     * @param fileName     String 文件名
     * @param heads        String[] EXCEL文件列表头信息数组
     * @param vectorAttr   Vector 对象中存放Excel表格各列对应数据库表的中字段的名称
     * @param dataList     List 实体对象列表数据
     * @return void
     */
    public static void createExcel(OutputStream fileOpStream,
                                   String fileName,
                                   List<String> heads,
                                   List<String> vectorAttr,
                                   List<?> dataList) {
        // 如果保存的对象中没有数据
        if (dataList == null || dataList.isEmpty()) {
            return;
        }
        // 输出流为空
        if (fileOpStream == null) {
            return;
        }

        try {
            // 先取到要用到的get方法
            Object objCls = dataList.get(0);
            Map<String, Method> hm = getMethod("get", vectorAttr, objCls.getClass());

            // 获取文件后缀
            String prefix = fileName.substring(fileName.lastIndexOf('.'));

            // 根据文件后缀声明一个工作薄
            Workbook workbook = null;
            if (XLSX_SUFFIX.equals(prefix)
                    || U_XLSX_SUFFIX.equals(prefix)) {
                workbook = new XSSFWorkbook();
            } else if (XLS_SUFFIX.equals(prefix)
                    || U_XLS_SUFFIX.equals(prefix)) {
                workbook = new HSSFWorkbook();
            } else {
                return;
            }

            // 创建单元格样式
            CellStyle centerStyle = getTitleCellStyle(workbook, CellStyle.ALIGN_CENTER);
            CellStyle numberStyle = getNumberCellStyle(workbook);
            CellStyle strStyle = getStrCellStyle(workbook);
            CellStyle dateStyle = getDateCellStyle(workbook);
            Map<String, CellStyle> styleMap = new HashMap<String, CellStyle>();
            styleMap.put(STYLE_NUMBER, numberStyle);
            styleMap.put(STYLE_STRING, strStyle);
            styleMap.put(STYLE_DATE, dateStyle);

            // 生成一个表格
            Sheet sheet = workbook.createSheet();

            int rowId = 0;
            // 创建标题行
            if (heads != null && !heads.isEmpty()) {
                Row titleRow = sheet.createRow(rowId);
                for (int i = 0; i < heads.size(); i++) {
                    Cell cell = titleRow.createCell(i);
                    cell.setCellStyle(centerStyle);
                    cell.setCellValue(heads.get(i));
                }
                rowId = rowId + 1;
            }
            // 循环数据创建数据行
            for (int i = 0; i < dataList.size(); i++) {
                // 获取数据对象
                Object data = dataList.get(i);
                // 创建行
                createExcelRows(data, sheet, rowId, vectorAttr, hm, styleMap);
                rowId = rowId + 1;
            }
            // 数据写到workbook
            workbook.write(fileOpStream);
        } catch (Exception e) {
            throw new RuntimeException("导出Excel失败 :" + e);
        } finally {
            // 关闭输出流
            AccessExcelUtil.closeOutputStream(fileOpStream);
        }
    }

    /**
     * 功能：把实体对象保存到Excel文件中
     * 实现流程：先将取到对象中每个字段的值，保存到相应的cell 关闭workbook
     *
     * @param fileOpStream  OutputStream 输出流
     * @param fileName      String 文件名
     * @param path          String 文件存放路径
     * @param vectorAttr    Vector 对象中存放Excel表格各列对应数据库表的中字段的名称
     * @param dataList      List 实体对象列表数据
     * @param startRowIndex 数据写入工作表sheet开始行的下标(从零开始)
     */
    public static void writeInExcel(OutputStream fileOpStream,
                                    String fileName,
                                    String path,
                                    List<String> vectorAttr,
                                    List<?> dataList,
                                    int startRowIndex) {
        // 输出流为空
        if (fileOpStream == null) {
            return;
        }

        InputStream inStream = null;
        try {
            // 文件包创建
            File folder = new File(path);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            // 文件名
            String filePath = path.concat(File.separator).concat(fileName);
            // 获取文件输入流
            inStream = new FileInputStream(filePath);
            // 获取文件后缀
            String prefix = fileName.substring(fileName.lastIndexOf('.'));

            // 根据文件后缀声明一个工作薄
            Workbook workbook = null;
            if (XLSX_SUFFIX.equals(prefix)
                    || U_XLSX_SUFFIX.equals(prefix)) {
                workbook = new XSSFWorkbook(inStream);
            } else if (XLS_SUFFIX.equals(prefix)
                    || U_XLS_SUFFIX.equals(prefix)) {
                workbook = new HSSFWorkbook(inStream);
            } else {
                return;
            }

            if (dataList != null && !dataList.isEmpty()) {
                // 先取到要用到的get方法
                Object objCls = dataList.get(0);
                Map<String, Method> hm = getMethod("get", vectorAttr, objCls.getClass());

                // 创建单元格样式
                CellStyle numberStyle = getNumberCellStyle(workbook);
                CellStyle strStyle = getStrCellStyle(workbook);
                CellStyle dateStyle = getDateCellStyle(workbook);
                Map<String, CellStyle> styleMap = new HashMap<String, CellStyle>();
                styleMap.put(STYLE_NUMBER, numberStyle);
                styleMap.put(STYLE_STRING, strStyle);
                styleMap.put(STYLE_DATE, dateStyle);

                // 生成一个表格
                Sheet sheet = workbook.getSheetAt(0);
                if (sheet == null) {
                    sheet = workbook.createSheet();
                }
                // 循环数据创建数据行
                for (int i = 0; i < dataList.size(); i++) {
                    // 获取数据对象
                    Object data = dataList.get(i);
                    // 创建行
                    createExcelRows(data, sheet, startRowIndex + i, vectorAttr, hm, styleMap);
                }
            }
            // 数据写到workbook
            workbook.write(fileOpStream);
        } catch (Exception e) {
            throw new RuntimeException("导出Excel失败 :" + e);
        } finally {
            // 关闭输出流
            AccessExcelUtil.closeOutputStream(fileOpStream);
            // 关闭输入流
            AccessExcelUtil.closeInputStream(inStream);
        }
    }

    /**
     * 关闭输入流
     *
     * @param inputStream
     */
    private static void closeInputStream(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                log.error("关闭输入IO流操作过程发生异常： " + e.getMessage(), e);
            }
        }
    }

    /**
     * 关闭输出流
     *
     * @param outputStream
     */
    private static void closeOutputStream(OutputStream outputStream) {
        // 关闭输出流
        if (outputStream != null) {
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                log.error("关闭输出IO流操作过程发生异常： " + e.getMessage(), e);
            }
        }
    }

    /**
     * 创建EXCEL数据行
     *
     * @param data       数据对象
     * @param sheet      EXCEL工作表
     * @param rowIndex   创建工作表sheet行的下标
     * @param vectorAttr 数据对象的属性名
     * @param hm         数据对象的方法
     * @param styleMap   单元格样式MAP
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private static void createExcelRows(Object data,
                                        Sheet sheet,
                                        int rowIndex,
                                        List<String> vectorAttr,
                                        Map<String, Method> hm,
                                        Map<String, CellStyle> styleMap)
            throws IllegalAccessException, InvocationTargetException {
        Row dataRow = sheet.createRow(rowIndex);
        for (int column = 0; column < vectorAttr.size(); column++) {
            Cell cell = dataRow.createCell(column);
            // 取出对象中各个字段的值存放到Cell
            Object value = (hm.get(vectorAttr.get(column))).invoke(data);
            // 空值
            if (value == null) {
                cell.setCellStyle(styleMap.get(STYLE_STRING));
            }
            // 值的类型为String, Character
            if (value instanceof String || value instanceof Character) {
                cell.setCellStyle(styleMap.get(STYLE_STRING));
                cell.setCellValue(value.toString());
            }
            // 值的类型为BigInteger
            if (value instanceof BigInteger) {
                double valueAftChg = ((BigInteger) value).doubleValue();
                cell.setCellStyle(styleMap.get(STYLE_NUMBER));
                cell.setCellValue(valueAftChg);
            }
            // 值的类型为BigDecimal
            if (value instanceof BigDecimal) {
                double valueAftChg = ((BigDecimal) value).doubleValue();
                cell.setCellStyle(styleMap.get(STYLE_NUMBER));
                cell.setCellValue(valueAftChg);
            }
            // 值的类型为数据类型Long
            if (value instanceof Long) {
                double valueAftChg = ((Long) value).doubleValue();
                cell.setCellStyle(styleMap.get(STYLE_NUMBER));
                cell.setCellValue(valueAftChg);
            }
            // 值的类型为数据类型Integer
            if (value instanceof Integer) {
                double valueAftChg = ((Integer) value).doubleValue();
                cell.setCellStyle(styleMap.get(STYLE_NUMBER));
                cell.setCellValue(valueAftChg);
            }
            // 值的类型为数据类型Short
            if (value instanceof Short) {
                double valueAftChg = ((Short) value).doubleValue();
                cell.setCellStyle(styleMap.get(STYLE_NUMBER));
                cell.setCellValue(valueAftChg);
            }
            // 值的类型为数据类型Double
            if (value instanceof Double) {
                double valueAftChg = ((Double) value).doubleValue();
                cell.setCellStyle(styleMap.get(STYLE_NUMBER));
                cell.setCellValue(valueAftChg);
            }
            // 值的类型为数据类型Float
            if (value instanceof Float) {
                double valueAftChg = ((Float) value).doubleValue();
                cell.setCellStyle(styleMap.get(STYLE_NUMBER));
                cell.setCellValue(valueAftChg);
            }
            // 值的类型为数据类型Boolean
            if (value instanceof Boolean) {
                cell.setCellStyle(styleMap.get(STYLE_STRING));
                cell.setCellValue((Boolean) value);
            }
            // 值的类型为Date日期类型
            if (value instanceof Date) {
                cell.setCellStyle(styleMap.get(STYLE_DATE));
                cell.setCellValue((Date) value);
            }
            // 值的类型为Calendar日期类型
            if (value instanceof Calendar) {
                cell.setCellStyle(styleMap.get(STYLE_DATE));
                cell.setCellValue((Calendar) value);
            }
        }
    }

    /**
     * 获取 Excel 头单元格样式
     *
     * @param wb
     * @param align
     * @return
     */
    private static CellStyle getTitleCellStyle(Workbook wb, short align) {
        CellStyle style = wb.createCellStyle();

        style.setFillBackgroundColor(HSSFColor.AQUA.index);
        style.setFillPattern(CellStyle.BIG_SPOTS);
        style.setFillForegroundColor(HSSFColor.GREY_50_PERCENT.index);
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);

        style.setAlignment(align); // 对齐方式

        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setWrapText(true);

        // 字体
        Font titleFont = wb.createFont();
        titleFont.setColor(HSSFColor.WHITE.index);
        titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD); // 加粗
        style.setFont(titleFont);
        return style;
    }

    /**
     * 获取 Excel 普通字符单元格样式
     *
     * @param wb
     * @return
     */
    private static CellStyle getStrCellStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();

        style.setAlignment(CellStyle.ALIGN_LEFT); // 左对齐

        // 字体
        Font font = wb.createFont();
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setFont(font);
        return style;
    }

    /**
     * 获取 Excel 数字单元格样式
     *
     * @param wb
     * @return
     */
    private static CellStyle getNumberCellStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();

        style.setAlignment(CellStyle.ALIGN_RIGHT); // 右对齐

        // 字体
        Font font = wb.createFont();
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setFont(font);
        return style;
    }

    /**
     * 获取 Excel 日期单元格样式
     *
     * @param wb
     * @return
     */
    private static CellStyle getDateCellStyle(Workbook wb) {
        CreationHelper createHelper = wb.getCreationHelper();
        CellStyle style = wb.createCellStyle();
        style.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));

        style.setAlignment(CellStyle.ALIGN_RIGHT); // 右对齐

        // 字体
        Font font = wb.createFont();
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setFont(font);
        return style;
    }

}
