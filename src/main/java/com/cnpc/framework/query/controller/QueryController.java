package com.cnpc.framework.query.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jxl.Workbook;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.write.Label;
import jxl.write.NumberFormat;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.BooleanType;
import org.hibernate.type.DateType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.cnpc.framework.base.entity.User;
import com.cnpc.framework.base.pojo.PageInfo;
import com.cnpc.framework.query.entity.Call;
import com.cnpc.framework.query.entity.Column;
import com.cnpc.framework.query.entity.ColumnConfig;
import com.cnpc.framework.query.entity.Query;
import com.cnpc.framework.query.entity.QueryCondition;
import com.cnpc.framework.query.entity.QueryConfig;
import com.cnpc.framework.query.filter.ConditionOperator;
import com.cnpc.framework.query.filter.IFilter;
import com.cnpc.framework.query.filter.StringFilter;
import com.cnpc.framework.query.pojo.QueryDefinition;
import com.cnpc.framework.query.service.QueryService;
import com.cnpc.framework.utils.DateUtil;
import com.cnpc.framework.utils.JSonHelper;
import com.cnpc.framework.utils.JXLExcelUtil;
import com.cnpc.framework.utils.ObjectUtil;
import com.cnpc.framework.utils.SessionUtil;
import com.cnpc.framework.utils.SpringContextUtil;
import com.cnpc.framework.utils.StrUtil;

/**
 * 查询
 * 
 * @author jrn
 * 
 */
@Controller
@RequestMapping("/query")
public class QueryController {

    @Resource
    private QueryService queryService;

    /**
     * 第一次加载页面初始化
     * 
     * @param queryId
     * @return
     */
    @RequestMapping("/loadData")
    @ResponseBody
    public Map<String, Object> loadData(String reqObj, HttpSession session) throws Exception {

        QueryCondition queryCondition = JSON.parseObject(reqObj, QueryCondition.class);
        Query query = queryCondition.getQuery() != null ? queryCondition.getQuery() : QueryDefinition.getQueryById(queryCondition
                .getQueryId());
        Map<String, Object> map = new HashMap<String, Object>();
        String className = query.getClassName();
        Class<?> objClass = null;
        if (className != null) {
            objClass = Class.forName(className);
        }
        // 分页信息
        PageInfo pageInfo = new PageInfo();
        if (queryCondition.getPageInfo() == null) {
            pageInfo.setPageSize(query.getPagesize());
        } else {
            pageInfo = queryCondition.getPageInfo();
        }
        List objList;
        /**
         * 需要走自己的查询方法在query的xml中配置simpleSearch="false"
         */
        if (!query.getSimpleSearch()) {
            Object service = SpringContextUtil.getBean(query.getService());
            Class clazz = service.getClass();
            Method method = clazz.getDeclaredMethod(query.getMethod(), QueryCondition.class, PageInfo.class);
            objList = (List) method.invoke(service, queryCondition, pageInfo);
        }
        // sql 查询方式 (1=1 方式传值)
        else {
            // sql 查询方式 (1=1 方式传值)
            if (StrUtil.isNotBlank(query.getSql())) {
                // 查询过滤条件
                StringBuilder sqlBuilder = new StringBuilder(query.getSql());
                Object[] objArr = new Object[] {};
                Type[] typeArr = new Type[] {};
                String sql = sqlBuilder.toString();
                int conPos = sql.indexOf("1=1");
                // 1=1出现次数
                int countMatch = 0;
                if (conPos > -1) {
                    IFilter filter = generateFilter(queryCondition, query, session, sqlBuilder);
                    sql = sqlBuilder.toString();
                    if (filter != null) {
                        objArr = filter.getValues().toArray();
                        typeArr = new Type[objArr.length];
                        for (int i = 0; i < objArr.length; i++) {
                            Object obj = objArr[i];
                            if (obj instanceof java.lang.String) {
                                typeArr[i] = StringType.INSTANCE;
                            } else if (obj instanceof java.lang.Integer) {
                                typeArr[i] = IntegerType.INSTANCE;
                            } else if (obj instanceof java.lang.Boolean) {
                                typeArr[i] = BooleanType.INSTANCE;
                            } else if (obj instanceof java.util.Date) {
                                typeArr[i] = DateType.INSTANCE;
                            } else {
                                typeArr[i] = StringType.INSTANCE;
                            }
                        }
                        countMatch = StrUtil.countMatches(sql, "1=1");
                        sql = sql.replace("1=1", filter.getString());
                    }
                }
                Object[] objArrs = new Object[objArr.length * countMatch];
                Type[] typeArrs = new Type[typeArr.length * countMatch];
                for (int i = 0; i < countMatch; i++) {
                    System.arraycopy(objArr, 0, objArrs, i * objArr.length, objArr.length);
                    System.arraycopy(typeArr, 0, typeArrs, i * typeArr.length, typeArr.length);
                }
                if (query.getAllowPaging())
                    objList = queryService.findMapBySql(sql, query.getCountStr(), pageInfo, objArrs, typeArrs, objClass);
                else
                    objList = queryService.findMapBySql(sql, objArrs, typeArrs, objClass);
            }
            // sql 查询映射到类方式 (非1=1方式传值 变量替换方式)
            else if (StrUtil.isNotBlank(query.getVarSql())) {
                // 查询过滤条件
                StringBuilder sqlBuilder = new StringBuilder(query.getVarSql());
                Object[] objArr = new Object[] {};
                Type[] typeArr = new Type[] {};
                IFilter filter = generateFilter(queryCondition, query, session, sqlBuilder);
                String sql = sqlBuilder.toString();
                if (query.getAllowPaging())
                    objList = queryService.findMapBySql(sql, query.getCountStr(), pageInfo, objArr, typeArr, objClass);
                else
                    objList = queryService.findMapBySql(sql, objArr, typeArr, objClass);
            }
            // criteria 离线查询方式
            else {
                DetachedCriteria criteria = DetachedCriteria.forClass(objClass);
                criteria = generateCriteria(queryCondition, query, session, criteria);
                pageInfo.setCount(queryService.getCountByCriteria(criteria));
                if (query.getAllowPaging())
                    objList = queryService.getListByCriteria(criteria, pageInfo);
                else
                    objList = queryService.findByCriteria(criteria);
            }
        }
        List<Call> callList = getCallList(query);
        // 组装Map
        query.setCallList(callList);

        map.put("query", query);
        map.put("pageInfo", pageInfo);
        map.put("rows", objList);
        return map;
    }

    /*
     * JXL方式数据导出 支持多 sheet导出，支持表名、sheet页、标题、副标题单独命名
     */

    @SuppressWarnings({ "unchecked", "deprecation" })
    @RequestMapping(value = "/exportSheetsDataJXL")
    public void exportSheetsDataJXL(String reqObjs, String tableName, HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        long s = (new Date()).getTime();
        long s1 = 0;
        List<QueryCondition> queryConditions = JSON.parseArray(reqObjs, QueryCondition.class);
        int conCount = queryConditions.size();
        String tempfile = System.currentTimeMillis() + "";
        OutputStream fOut = new FileOutputStream(request.getRealPath("/") + File.separator + "templates" + File.separator + "temp"
                + File.separator + tempfile + ".xls");

        // 产生工作簿对象
        WritableWorkbook workbook = Workbook.createWorkbook(fOut);

        // 定义标题样式
        WritableFont titleFont = new WritableFont(WritableFont.TIMES, 18, WritableFont.BOLD, false);
        WritableCellFormat title = new WritableCellFormat(titleFont);
        title.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);
        title.setAlignment(jxl.format.Alignment.CENTRE);
        title.setBorder(Border.BOTTOM, BorderLineStyle.THIN);
        title.setBorder(Border.LEFT, BorderLineStyle.THIN);
        title.setBorder(Border.RIGHT, BorderLineStyle.THIN);
        title.setBorder(Border.TOP, BorderLineStyle.THIN);
        title.setWrap(true);
        // 定义表头样式
        WritableFont headfont = new WritableFont(WritableFont.TIMES, 12, WritableFont.BOLD, false);
        ;
        WritableCellFormat head = new WritableCellFormat(headfont);
        head.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);
        head.setAlignment(jxl.format.Alignment.CENTRE);
        head.setBorder(Border.BOTTOM, BorderLineStyle.THIN);
        head.setBorder(Border.LEFT, BorderLineStyle.THIN);
        head.setBorder(Border.RIGHT, BorderLineStyle.THIN);
        head.setBorder(Border.TOP, BorderLineStyle.THIN);

        WritableFont font = new WritableFont(WritableFont.TIMES, 12, WritableFont.NO_BOLD, false);
        ;
        // 定义居中样式
        WritableCellFormat center = new WritableCellFormat(font);
        center.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);
        center.setAlignment(jxl.format.Alignment.CENTRE);
        center.setBorder(Border.BOTTOM, BorderLineStyle.THIN);
        center.setBorder(Border.LEFT, BorderLineStyle.THIN);
        center.setBorder(Border.RIGHT, BorderLineStyle.THIN);
        center.setBorder(Border.TOP, BorderLineStyle.THIN);

        // 需要自动换行的样式
        WritableCellFormat huanHang = new WritableCellFormat(font);
        huanHang.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);
        huanHang.setAlignment(jxl.format.Alignment.CENTRE);
        huanHang.setBorder(Border.BOTTOM, BorderLineStyle.THIN);
        huanHang.setBorder(Border.LEFT, BorderLineStyle.THIN);
        huanHang.setBorder(Border.RIGHT, BorderLineStyle.THIN);
        huanHang.setBorder(Border.TOP, BorderLineStyle.THIN);
        huanHang.setWrap(true);

        // 定义小数数字样式
        NumberFormat nf = new NumberFormat("#.##");
        WritableCellFormat number = new WritableCellFormat(nf);
        number.setFont(font);
        number.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);
        number.setAlignment(jxl.format.Alignment.CENTRE);
        number.setBorder(Border.BOTTOM, BorderLineStyle.THIN);
        number.setBorder(Border.LEFT, BorderLineStyle.THIN);
        number.setBorder(Border.RIGHT, BorderLineStyle.THIN);
        number.setBorder(Border.TOP, BorderLineStyle.THIN);

        // 定义整数数字样式
        NumberFormat intnf = new NumberFormat("#");
        WritableCellFormat intNumber = new WritableCellFormat(intnf);
        intNumber.setFont(font);
        intNumber.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);
        intNumber.setAlignment(jxl.format.Alignment.CENTRE);
        intNumber.setBorder(Border.BOTTOM, BorderLineStyle.THIN);
        intNumber.setBorder(Border.LEFT, BorderLineStyle.THIN);
        intNumber.setBorder(Border.RIGHT, BorderLineStyle.THIN);
        intNumber.setBorder(Border.TOP, BorderLineStyle.THIN);

        // 定义居左样式
        WritableCellFormat left = new WritableCellFormat(font);
        left.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);
        left.setBorder(Border.BOTTOM, BorderLineStyle.THIN);
        left.setBorder(Border.LEFT, BorderLineStyle.THIN);
        left.setBorder(Border.RIGHT, BorderLineStyle.THIN);
        left.setBorder(Border.TOP, BorderLineStyle.THIN);
        left.setWrap(true);

        // CellView cv = new CellView();
        // cv.setAutosize(true);
        // cv.setSize(18);
        try {
            for (int qindex = 0; qindex < conCount; qindex++) {
                QueryCondition queryCondition = queryConditions.get(qindex);
                Query query = queryCondition.getQuery() != null ? queryCondition.getQuery() : QueryDefinition.getQueryById(queryCondition
                        .getQueryId());
                String className = query.getClassName();
                Class<?> objClass = null;
                if (className != null) {
                    objClass = Class.forName(className);
                }

                List objList;
                // criteria离线查询方式
                if (StrUtil.isNotBlank(query.getSql())) {// sql查询映射到vo方式
                    // 查询过滤条件
                    StringBuilder sqlBuilder = new StringBuilder(query.getSql());
                    Object[] objArr = new Object[] {};
                    Type[] typeArr = new Type[] {};
                    String sql = sqlBuilder.toString();
                    int conPos = sql.indexOf("1=1");
                    // 1=1出现次数
                    int countMatch = 0;
                    if (conPos > -1) {
                        IFilter filter = generateFilter(queryCondition, query, request.getSession(), sqlBuilder);
                        sql = sqlBuilder.toString();
                        if (filter != null) {
                            objArr = filter.getValues().toArray();
                            typeArr = new Type[objArr.length];
                            for (int i = 0; i < objArr.length; i++) {
                                Object obj = objArr[i];
                                if (obj instanceof java.lang.String) {
                                    typeArr[i] = StringType.INSTANCE;
                                } else if (obj instanceof java.lang.Integer) {
                                    typeArr[i] = IntegerType.INSTANCE;
                                } else if (obj instanceof java.lang.Boolean) {
                                    typeArr[i] = BooleanType.INSTANCE;
                                } else if (obj instanceof java.util.Date) {
                                    typeArr[i] = DateType.INSTANCE;
                                } else {
                                    typeArr[i] = StringType.INSTANCE;
                                }
                            }
                            countMatch = StrUtil.countMatches(sql, "1=1");
                            sql = sql.replace("1=1", filter.getString());
                        }
                    }
                    Object[] objArrs = new Object[objArr.length * countMatch];
                    Type[] typeArrs = new Type[typeArr.length * countMatch];
                    // if(countMatch>1){
                    for (int i = 0; i < countMatch; i++) {
                        System.arraycopy(objArr, 0, objArrs, i * objArr.length, objArr.length);
                        System.arraycopy(typeArr, 0, typeArrs, i * typeArr.length, typeArr.length);
                    }
                    objList = queryService.findMapBySql(sql, objArrs, typeArrs, objClass);
                }
                // sql 查询映射到类方式 (非1=1方式传值 变量替换方式)
                else if (StrUtil.isNotBlank(query.getVarSql())) {
                    // 查询过滤条件
                    StringBuilder sqlBuilder = new StringBuilder(query.getVarSql());
                    Object[] objArr = new Object[] {};
                    Type[] typeArr = new Type[] {};
                    IFilter filter = generateFilter(queryCondition, query, request.getSession(), sqlBuilder);
                    String sql = sqlBuilder.toString();
                    objList = queryService.findMapBySql(sql, objArr, typeArr, objClass);
                } else {
                    DetachedCriteria criteria = DetachedCriteria.forClass(objClass);
                    criteria = generateCriteria(queryCondition, query, request.getSession(), criteria);
                    objList = queryService.findByCriteria(criteria);
                }

                s1 = (new Date()).getTime();
                // 产生工作表对象
                WritableSheet sheet = workbook.createSheet(
                        queryCondition.getSheetName() == null ? tableName : queryCondition.getSheetName(), qindex);

                int rowLen = objList.size();
                List<Column> colList = query.getColumnList();
                int colLen = colList.size();

                // 创建表头
                int hempty = 0;

                // 填充标题
                Label label = new Label(0, 0, queryCondition.getSheetTitle() != null ? queryCondition.getSheetTitle()
                        : queryCondition.getSheetName(), title);
                int lastcol = -1;
                for (int h = 0; h < colLen; ++h) {
                    Column hcolumn = colList.get(h);
                    if ((!hcolumn.getIsServerCondition() && !hcolumn.getHidden() && hcolumn.getIsExport()) || hcolumn.getIsJustExport()) {
                        lastcol++;
                    }
                }
                sheet.addCell(label);
                // sheet.mergeCells(int col1,int row1,int col2,int
                // row2);//左上角到右下角
                sheet.setRowView(0, 1500);
                sheet.mergeCells(0, 0, lastcol, 0);
                int titleRowCount = 1;
                // 填充副标题
                if (queryCondition.getSheetSubTitle() != null) {
                    titleRowCount = 2;
                    label = new Label(0, 1, queryCondition.getSheetSubTitle(), left);
                    sheet.addCell(label);
                    sheet.setRowView(1, 600, false);
                    sheet.mergeCells(0, 1, lastcol, 1);
                }
                sheet.setRowView(titleRowCount, 600, false);
                for (int h = 0; h < colLen; ++h) {
                    Column hcolumn = colList.get(h);
                    if ("rowIndex".equals(hcolumn.getKey())) {
                        // 创建序号列
                        label = new Label(0, titleRowCount, "序号", head);
                        sheet.addCell(label);
                        if ("序号".getBytes().length > sheet.getColumnWidth(0))
                            sheet.setColumnView(h - hempty, "序号".getBytes().length + 2);
                        continue;
                    }
                    if ((!hcolumn.getIsServerCondition() && !hcolumn.getHidden() && hcolumn.getIsExport()) || hcolumn.getIsJustExport()) {
                        // 创建表头列
                        label = new Label(h - hempty, titleRowCount, hcolumn.getHeader(), head);
                        sheet.addCell(label);
                        if (hcolumn.getHeader().getBytes().length > sheet.getColumnWidth(h - hempty))
                            sheet.setColumnView(h - hempty, hcolumn.getHeader().getBytes().length + 2);
                    } else {
                        ++hempty;
                    }

                }

                // 创建数据列表
                for (int b = 1; b <= rowLen; ++b) {
                    // 创建一行数据列
                    sheet.setRowView(b + titleRowCount, 500);
                    int bempty = 0;
                    Object obj = objList.get(b - 1);
                    // Row datarow = new Row();
                    Map<String, Object> dataMap = null;
                    if (objClass == null) {
                        dataMap = (Map<String, Object>) obj;
                        // datarow.setId(dataMap.get(query.getKey()).toString());
                    } else {
                        Method method = objClass.getMethod(ObjectUtil.methodName(query.getKey(), "get"));
                        // datarow.setId((String) method.invoke(obj));
                    }

                    for (int i = 0; i < colLen; ++i) {
                        Column bcolumn = colList.get(i);
                        if ("rowIndex".equals(bcolumn.getKey())) {
                            // 设置当前列序号
                            jxl.write.Number num = new jxl.write.Number(0, b + titleRowCount, b, intNumber);
                            sheet.addCell(num);
                            continue;
                        }
                        String data = "";

                        if (objClass != null) {
                            data = ObjectUtil.ObjectToString(obj, bcolumn.getKey(), bcolumn.getDateFormat());
                        } else {
                            if (Date.class.getName().equals(bcolumn.getClassType()) || "date".equals(bcolumn.getClassType().toLowerCase())) {
                                data = bcolumn.getDateFormat() != null ? DateUtil.format(
                                        (Date) dataMap.get(bcolumn.getKey().toUpperCase()), bcolumn.getDateFormat()) : DateUtil
                                        .format((Date) dataMap.get(bcolumn.getKey().toUpperCase()));
                            } else {
                                if (dataMap.get(bcolumn.getKey().toUpperCase()) != null) {
                                    data = dataMap.get(bcolumn.getKey().toUpperCase()).toString();
                                }
                            }
                        }
                        if ((!bcolumn.getIsServerCondition() && !bcolumn.getHidden() && bcolumn.getIsExport()) || bcolumn.getIsJustExport()) {
                            // 设置当前列值
                            WritableCellFormat format = center;
                            // 单元格对齐
                            if (bcolumn.getAlign().equals("center")) {
                                format = center;
                            } else if (bcolumn.getAlign().equals("left")) {
                                format = left;
                            }
                            // bcell.setCellStyle(huanHang);
                            // 单元格赋值

                            if (bcolumn.getClassType().equals("java.lang.Double") && bcolumn.getSuffix() == null) {
                                if (!StrUtil.isBlankOrNull(data)) {
                                    jxl.write.Number num1 = new jxl.write.Number(i - bempty, b + titleRowCount, Double.parseDouble(data),
                                            number);
                                    sheet.addCell(num1);
                                } else {
                                    label = new Label(i - bempty, b + titleRowCount, "", number);
                                    sheet.addCell(label);
                                }
                                // sheet.addCell(null);
                                if (data != null && data.getBytes().length > sheet.getColumnWidth(i - bempty))
                                    sheet.setColumnView(i - bempty, data.getBytes().length + 4);
                            } else if (bcolumn.getClassType().equals("java.lang.Integer")) {
                                jxl.write.Number num2 = new jxl.write.Number(i - bempty, b + titleRowCount, Integer.parseInt(data),
                                        intNumber);
                                sheet.addCell(num2);
                                if (data != null && data.getBytes().length > sheet.getColumnWidth(i - bempty))
                                    sheet.setColumnView(i - bempty, data.getBytes().length + 4);
                            } else if (bcolumn.getSuffix() != null) {
                                label = new Label(i - bempty, b + titleRowCount, data + "%", format);
                                sheet.addCell(label);
                                if (data != null && data.getBytes().length > sheet.getColumnWidth(i - bempty))
                                    sheet.setColumnView(i - bempty, data.getBytes().length + 4);
                            } else {
                                label = new Label(i - bempty, b + titleRowCount, data, format);
                                sheet.addCell(label);
                                if (data != null && data.getBytes().length > sheet.getColumnWidth(i - bempty))
                                    sheet.setColumnView(i - bempty, data.getBytes().length + 4);
                            }
                        } else {
                            ++bempty;
                        }
                        // sheet.setColumnView(i-bempty,cv);
                    }

                }
                // 创建sheet完成后，根据前台参数执行 行合并、列合并、多表头等操作
                if (queryCondition.getSheetMethod() != null) {
                    String sheetMethod = queryCondition.getSheetMethod();
                    String[] params = sheetMethod.split("_");
                    // 正文内容考试的行号索引
                    int contentBeginIndex = 1;
                    // 含有副标题
                    if (queryCondition.getSheetSubTitle() != null)
                        contentBeginIndex = 2;
                    if (params[0].equals("rowSpan")) {
                        for (int i = 1; i < params.length; i++) {
                            sheet = JXLExcelUtil.rowSpan(sheet, Integer.valueOf(params[i]).intValue(), contentBeginIndex);
                        }
                    }
                    if (params[0].equals("display")) {
                        for (int i = 1; i < params.length; i++) {
                            sheet.setColumnView(Integer.valueOf(params[i]).intValue(), 0);
                        }
                    }
                }

            }
            // 导出excel
            workbook.write();
            workbook.close();
            response.getWriter().print(tempfile);
            System.out.println(((new Date().getTime() - s1) / 1000) + ">>>>>>>>>>>>>>>>>>>>>>>>>>>" + ((new Date()).getTime() - s) / 1000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fOut.flush();
                fOut.close();
            } catch (Exception e2) {
            }
        }
    }

    @RequestMapping("/downExport")
    public void downExport(HttpServletRequest request, HttpServletResponse response) {

        OutputStream out = null;
        try {
            String templateName = request.getParameter("tempfile");
            String fileName = request.getParameter("tableName");
            out = response.getOutputStream();
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/vnd.ms-excel;charset=UTF-8");
            response.setHeader("content-disposition", "attachment;filename=" + new String(fileName.getBytes("GBK"), "ISO-8859-1") + ".xls");
            File file = new File(request.getRealPath("/") + File.separator + "templates" + File.separator + "temp" + File.separator
                    + templateName + ".xls");
            FileInputStream inputStream = new FileInputStream(file);
            // 开始读取下载
            byte[] b = new byte[1024];
            int i = 0;
            while ((i = inputStream.read(b)) > 0) {
                out.write(b, 0, i);
            }
            inputStream.close();
            file.delete();
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            response.setContentType("text/html; charset=utf-8");
            try {
                out.write("数据表导出异常，请重试！".getBytes("utf-8"));
            } catch (IOException e1) {
            }
        } finally {
            try {
                out.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * 获取初始化表格方法Call
     * 
     * @param query
     *            表格xml定义
     * @return
     * @throws Exception
     */
    protected List<Call> getCallList(Query query) throws Exception {

        List<Call> callList = new ArrayList<Call>();
        List<Column> colList = query.getColumnList();
        int colLen = colList.size();
        // 列头名称
        String header = "";
        // 副表头
        String subHeader = "";
        // 列头Id
        String ids = "";
        // 列头 filter 字典Code
        String dicts = "";
        // 类类型
        String colTypes = "";
        // 类排序方式
        String colSorting = "";
        // 初始列宽
        String initWidths = "";
        // 列的对齐方式
        String colAlign = "";
        // 列颜色
        String columnColor = "";
        // 列的显示隐藏
        String columnVisibility = "";
        String enableTooltips = "";
        String enableMultiline = query.getEnableMultiline().toString();
        // 冻结列
        String splitAt = (query.getSplitAt() == null) ? null : query.getSplitAt().toString();
        // 列头操作
        for (int i = 0; i < colLen; i++) {
            Column column = colList.get(i);
            if (column.getIsServerCondition())
                continue;
            header = (i == 0) ? column.getHeader() : header + "," + column.getHeader();
            subHeader = (i == 0) ? column.getSubHeader() : subHeader + "," + column.getSubHeader();
            ids = (i == 0) ? (column.getId() == null ? column.getKey() : column.getId()) : ids + ","
                    + (column.getId() == null ? column.getKey() : column.getId());
            dicts = (i == 0) ? column.getDict() : dicts + "," + column.getDict();
            colTypes = (i == 0) ? column.getType() : colTypes + "," + column.getType();
            // colSorting = (i == 0) ? column.getSortType() : colSorting + "," +
            // column.getSortType();
            initWidths = (i == 0) ? column.getWidth() : initWidths + "," + column.getWidth();
            colAlign = (i == 0) ? column.getAlign() : colAlign + "," + column.getAlign();
            columnColor = (i == 0) ? column.getColor() : columnColor + "," + column.getColor();
            columnVisibility = (i == 0) ? column.getHidden().toString() : columnVisibility + "," + column.getHidden().toString();
            enableTooltips = (i == 0) ? column.getEnableTooltip().toString() : enableTooltips + "," + column.getEnableTooltip().toString();
        }
        String[] commands = { "setHeader", "setInitWidths", "setColumnIds", "setColumnDicts", "setColTypes", "setColAlign",
                "setColumnColor", "setColumnsVisibility", "enableTooltips", "enableMultiline", "splitAt" };
        String[] params = { header, initWidths, ids, dicts, colTypes, colAlign, columnColor, columnVisibility, enableTooltips,
                enableMultiline, splitAt };
        for (int i = 0; i < commands.length; i++) {
            if (params[i] == null || params[i] == "")
                continue;
            Call call = new Call();
            if (commands[i].equals("setInitWidths") && query.getWidthType().equals("%"))
                call.setCommand("setInitWidthsP");
            else
                call.setCommand(commands[i]);
            call.setParam(params[i]);
            callList.add(call);
        }
        if (query.getEnableMultiHeader()) {
            Call call = new Call();
            call.setCommand("attachHeader");
            call.setParam(subHeader);
            callList.add(call);
        }
        return callList;
    }

    protected DetachedCriteria generateCriteria(QueryCondition condition, Query query, HttpSession session, DetachedCriteria criteria)
            throws Exception {

        // 排序
        String[] orderPropertys = null;
        String[] ascOrDescs = null;
        String sortInfo = StrUtil.isNotBlank(condition.getSortInfo()) ? condition.getSortInfo() : query.getOrder();
        if (StrUtil.isNotBlank(sortInfo)) {
            if (query.getAllowPaging() && StrUtil.isNotBlank(query.getKey())) {
                sortInfo += "," + query.getKey() + " asc";
            }
            int len = sortInfo.split(",").length;
            orderPropertys = new String[len];
            ascOrDescs = new String[len];
            for (int i = 0; i < len; i++) {
                orderPropertys[i] = sortInfo.split(",")[i].split(" ")[0];
                ascOrDescs[i] = sortInfo.split(",")[i].split(" ")[1];
                criteria = ObjectUtil.getCriteriaWithAlias(criteria, orderPropertys[i]);
                if (ascOrDescs[i].toUpperCase().equals("ASC"))
                    criteria.addOrder(Order.asc(orderPropertys[i]));
                else
                    criteria.addOrder(Order.desc(orderPropertys[i]));

            }
        }
        // 检索条件
        /*
         * IFilter filter = null; if (StrUtil.isNotBlank(query.getFilter())) {
         * filter = new StringFilter(query.getFilter()); }
         */
        // 服务器端检索条件
        List<Column> colList = query.getColumnList();
        int colLen = colList.size();
        for (int i = 0; i < colLen; i++) {
            Column column = colList.get(i);
            if (!column.getIsServerCondition())
                continue;
            if (condition.getConditions() == null)
                condition.setConditions(new ArrayList<Map<String, Object>>());
            Map<String, Object> conMap = new HashMap<String, Object>();
            conMap.put("key", column.getKey());
            conMap.put("value", column.getValue());
            conMap.put("operator", column.getOperator());
            condition.getConditions().add(conMap);
        }

        if (condition.getConditions() == null)
            return criteria;
        for (Map map : condition.getConditions()) {
            String key = (String) map.get("key");
            Object value = map.get("value");
            Column column = query.getColumn(key);
            if (column == null) {
                column = new Column();
                column.setKey(key);
            }
            if (map.get("operator") == null || "".equals(map.get("operator")))
                map.put("operator", column.getOperator());
            if (((value != null) && (StrUtil.isNotBlank(value.toString())) && (column != null) && !value.toString().equals("%%"))
                    || (map.get("operator").toString().toUpperCase().equals("NOT_NULL")
                            || map.get("operator").toString().toUpperCase().equals("NULL")
                            || column.getOperator().toUpperCase().equals("NOT_NULL") || column.getOperator().toUpperCase().equals("NULL"))) {
                try {
                    ConditionOperator operator = getOperator(column, (String) map.get("operator"), value);
                    Class clazz = JSonHelper.getClass(column.getClassType());
                    Object newValue;
                    if (operator.equals(ConditionOperator.BETWEEN)) {
                        String[] values = (value.toString() + " ").split(",");
                        if ((values.length != 0) && ((StrUtil.isBlank(values[0])) && (StrUtil.isBlank(values[1])))) {
                            continue;
                        }
                        List list = new ArrayList();
                        for (String v : values) {
                            if (StrUtil.isBlank(v))
                                list.add(null);
                            else {
                                list.add(JSonHelper.getValue(clazz, v));
                            }
                        }
                        newValue = list;
                    } else {
                        if ((operator.equals(ConditionOperator.IN)) || (operator.equals(ConditionOperator.NOT_IN))) {
                            String[] values = value.toString().split(",");
                            List list = new ArrayList();
                            for (String v : values) {
                                if (StrUtil.isNotBlank(v)) {
                                    list.add(JSonHelper.getValue(clazz, v));
                                }
                            }
                            if (list.size() == 0) {
                                continue;
                            }
                            newValue = list;
                        } else {
                            newValue = JSonHelper.getValue(clazz, value);
                        }
                    }
                    IFilter simpleFilter = null;
                    if (((Date.class.equals(clazz)) || (Date.class.equals(clazz.getSuperclass()))) && (value.toString().indexOf(':') < 0)) {
                        if (operator.equals(ConditionOperator.BETWEEN)) {
                            List list = (List) newValue;
                            if (list.get(1) != null) {
                                Object nextValue = DateUtil.getLastMilliSecond(list.get(1));
                                list.set(1, nextValue);
                                criteria.add(Restrictions.between(key, list.get(0), list.get(1)));
                            }
                        } else if ((operator.equals(ConditionOperator.EQ)) || (operator.equals(ConditionOperator.GT))
                                || (operator.equals(ConditionOperator.GE))) {
                            Object nextValue = DateUtil.getLastMilliSecond(newValue);
                            if (operator.equals(ConditionOperator.EQ)) {
                                operator = ConditionOperator.BETWEEN;
                                newValue = Arrays.asList(new Object[] { newValue, nextValue });
                            } else {
                                newValue = nextValue;
                            }
                        }
                    }
                    criteria = ObjectUtil.getCriteriaWithAlias(criteria, key);
                    if (operator.equals(ConditionOperator.EQ)) {
                        criteria.add(Restrictions.eq(key, newValue));
                    }
                    if (operator.equals(ConditionOperator.GE)) {
                        criteria.add(Restrictions.ge(key, newValue));
                    }
                    if (operator.equals(ConditionOperator.NOT_EQ)) {
                        criteria.add(Restrictions.not(Restrictions.eq(key, newValue)));
                    }
                    if (operator.equals(ConditionOperator.LIKE)) {
                        criteria.add(Restrictions.like(key, newValue));
                    }
                    if (operator.equals(ConditionOperator.IN)) {
                        criteria.add(Restrictions.in(key, (List) newValue));
                    }
                    if (operator.equals(ConditionOperator.NOT_IN)) {
                        criteria = ObjectUtil.getCriteriaWithAlias(criteria, key);
                        criteria.add(Restrictions.not(Restrictions.in(key, (List) newValue)));
                    }
                    if (operator.equals(ConditionOperator.NULL)) {
                        criteria.add(Restrictions.isNull(key));
                    }
                    if (operator.equals(ConditionOperator.NOT_NULL)) {
                        criteria.add(Restrictions.isNotNull(key));
                    }
                    if (operator.equals(ConditionOperator.BETWEEN)
                            && !((Date.class.equals(clazz)) || (Date.class.equals(clazz.getSuperclass())))) {
                        criteria.add(Restrictions.between(key, Integer.parseInt((String) ((List) newValue).get(0)),
                                Integer.parseInt((String) ((List) newValue).get(1))));
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Fail occurs in generate Criteria", e);
                }
            }
        }
        return criteria;
    }

    protected IFilter generateFilter(QueryCondition condition, Query query, HttpSession session, StringBuilder sqlBuilder) throws Exception {

        // sql后拼接排序
        String sortInfo = condition.getSortInfo() != null ? condition.getSortInfo() : query.getOrder();
        String orderSql = sqlBuilder.toString();
        if (StrUtil.isNotBlank(sortInfo) && orderSql.toLowerCase().lastIndexOf("order ") > -1) {
            orderSql = orderSql.substring(0, orderSql.toLowerCase().lastIndexOf("order ")) + " order by " + sortInfo;
            if (query.getAllowPaging() && StrUtil.isNotBlank(query.getSortKey())) {
                orderSql = orderSql + "," + query.getSortKey();
            }
            sqlBuilder.delete(0, sqlBuilder.toString().length()).append(orderSql);
        }
        IFilter filter = null;
        if (StrUtil.isNotBlank(query.getFilter())) {
            filter = new StringFilter(query.getFilter());
        }
        if (condition.getConditions() == null)
            return filter;
        for (Map map : condition.getConditions()) {
            String key = (String) map.get("key");
            Object value = map.get("value");
            boolean isCondition = true;
            if (map.get("isCondition") != null)
                isCondition = Boolean.parseBoolean((map.get("isCondition")).toString());
            Column column = query.getColumn(key);
            if (column == null) {
                column = new Column();
                column.setKey(key);
            }
            if (map.get("operator") == null || "".equals(map.get("operator")))
                map.put("operator", column.getOperator());
            if (((value != null) && (StrUtil.isNotBlank(value.toString())) && (column != null) && !value.toString().equals("%%") && !value
                    .toString().equals(","))
                    || (map.get("operator").toString().toUpperCase().equals("NOT_NULL")
                            || map.get("operator").toString().toUpperCase().equals("NULL")
                            || column.getOperator().toUpperCase().equals("NOT_NULL") || column.getOperator().toUpperCase().equals("NULL"))) {
                // ||(map.get("operator").toString().toUpperCase().equals("NOT_NULL")||map.get("operator").toString().toUpperCase().equals("NULL")||column.getOperator().toUpperCase().equals("NOT_NULL")||column.getOperator().toUpperCase().equals("NULL"))
                try {
                    ConditionOperator operator = getOperator(column, (String) map.get("operator"), value);
                    Class clazz = JSonHelper.getClass(column.getClassType());
                    Object newValue;
                    if (operator.equals(ConditionOperator.BETWEEN)) {
                        String[] values = (value.toString() + " ").split(",");
                        if ((values.length != 0) && ((StrUtil.isBlank(values[0])) && (StrUtil.isBlank(values[1])))) {
                            continue;
                        }
                        List list = new ArrayList();
                        for (String v : values) {
                            if (StrUtil.isBlank(v))
                                list.add(null);
                            else {
                                list.add(JSonHelper.getValue(clazz, v));
                            }
                        }
                        newValue = list;
                    } else {
                        if ((operator.equals(ConditionOperator.IN)) || (operator.equals(ConditionOperator.NOT_IN))) {
                            String[] values = value.toString().split(",");
                            List list = new ArrayList();
                            for (String v : values) {
                                if (StrUtil.isNotBlank(v)) {
                                    list.add(JSonHelper.getValue(clazz, v));
                                }
                            }
                            if (list.size() == 0) {
                                continue;
                            }
                            newValue = list;
                        } else {
                            newValue = JSonHelper.getValue(clazz, value);
                        }
                    }
                    IFilter simpleFilter = null;
                    if (((Date.class.equals(clazz)) || (Date.class.equals(clazz.getSuperclass()))) && (value.toString().indexOf(':') < 0)) {
                        if (operator.equals(ConditionOperator.BETWEEN)) {
                            List list = (List) newValue;
                            if (list.get(1) != null) {
                                Object nextValue = DateUtil.getLastMilliSecond(list.get(1));
                                list.set(1, nextValue);
                            }
                        } else if ((operator.equals(ConditionOperator.EQ)) || (operator.equals(ConditionOperator.GT))
                                || (operator.equals(ConditionOperator.GE))) {
                            Object nextValue = DateUtil.getLastMilliSecond(newValue);
                            if (operator.equals(ConditionOperator.EQ)) {
                                operator = ConditionOperator.BETWEEN;
                                newValue = Arrays.asList(new Object[] { newValue, nextValue });
                            } else {
                                newValue = nextValue;
                            }
                        }
                    }
                    simpleFilter = operator.getFilter(key, newValue);
                    // 扩展 多层次sql 条件
                    String sql = sqlBuilder.toString();
                    if (sql != null) {
                        if (sql.indexOf("@" + key + "#") > -1) {
                            if (newValue instanceof java.lang.String) {
                                if (column.getIsQuote())
                                    sql = sql.replaceAll("@" + key + "#", "'" + newValue + "'");
                                else
                                    sql = sql.replaceAll("@" + key + "#", newValue.toString());
                            } else if (newValue instanceof java.lang.Integer)
                                sql = sql.replaceAll("@" + key + "#", newValue.toString());
                            else if (newValue instanceof ArrayList) {// 2015/4/6江日念扩展
                                StringBuilder sb = new StringBuilder();
                                for (int i = 0; i < ((List) newValue).size(); i++) {
                                    Object obj = ((List) newValue).get(i);
                                    if (obj == null)
                                        continue;
                                    if (obj instanceof java.lang.String) {
                                        if (column.getIsQuote())
                                            sb.append("'").append(obj.toString()).append("'").append(",");
                                        else
                                            sb.append(obj.toString()).append(",");
                                    }
                                    if (obj instanceof java.lang.Integer)
                                        sb.append(obj.toString()).append(",");
                                }
                                String newStr = sb.toString();
                                if (sb.length() > 0)
                                    newStr = sb.substring(0, sb.length() - 1);
                                sql = sql.replaceAll("@" + key + "#", newStr.toString());
                            }
                        }
                    }
                    sqlBuilder.delete(0, sqlBuilder.toString().length()).append(sql);
                    if (simpleFilter != null) {
                        if (isCondition)
                            filter = simpleFilter.appendAnd(filter);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Fail occurs in generate Filter ", e);
                }
            }
        }
        return filter;
    }

    private ConditionOperator getOperator(Column column, String operator, Object value) throws RuntimeException {

        ConditionOperator oper;
        if (StrUtil.isBlank(operator)) {
            if ("like_or_in".equalsIgnoreCase(column.getOperator())) {
                if ((value != null) && (value.toString().indexOf(",") > 0))
                    oper = ConditionOperator.IN;
                else
                    oper = ConditionOperator.LIKE;
            } else {
                oper = column.getOperatorObject();
            }
        } else {
            oper = ConditionOperator.getOperator(operator);
        }
        return oper;
    }

    // --------------------------------------------------------------------------------------
    // ----自定义列配置 江日念扩展
    // 2014-3-17--------------------------------------------------------
    // ---------------------------------------------------------------------------------------
    /**
     * 跳转到自定义表格配置界面
     */
    @RequestMapping(value = "tableConfig")
    public String tableConfig(String queryId, String pageName, Model model) {

        model.addAttribute("queryId", queryId);
        model.addAttribute("pageName", pageName);
        return "base/query/table_config";
    }

    /**
     * 跳转到自定义表格配置界面 配置其中一个query,与其绑定的query也随着改变 XXB
     * 
     * @param queryId
     *            主query
     * @param bindQueryId
     *            次query
     * @param pageName
     * @param model
     * @return
     */
    @RequestMapping(value = "tableConfigForMultQuery")
    public String tableConfigForMultQuery(String queryId, String bindQueryId, String pageName, Model model) {

        model.addAttribute("queryId", queryId);
        model.addAttribute("bindQueryId", bindQueryId);
        model.addAttribute("pageName", pageName);
        return "base/query/table_config_multquery";
    }

    @RequestMapping(value = "getConfigData")
    @ResponseBody
    public ColumnConfig getConfigData(String queryId, String pageName, HttpSession session) {

        ColumnConfig config = new ColumnConfig();
        // 得到已选择的Column
        User user = SessionUtil.getCurrentUser();
        DetachedCriteria criteria = DetachedCriteria.forClass(QueryConfig.class);
        criteria.add(Restrictions.eq("queryId", queryId));
        criteria.add(Restrictions.eq("pageName", pageName));
        criteria.add(Restrictions.eq("userid", user.getId()));
        List<QueryConfig> list = queryService.findByCriteria(criteria);
        // 如果数据库不为空，则取数据库，否则取xml配置文档
        Query query = QueryDefinition.getQueryById(queryId);
        Object[][] selected = new Object[][] {};
        Object[][] unSelected = new Object[][] {};
        List<Column> columnList = new ArrayList<Column>();
        for (Column column : query.getColumnList()) {
            columnList.add(column);
        }
        int left = 0;
        int right = 0;

        if (list.size() > 0) {
            QueryConfig configExist = list.get(0);
            List<String> columnNames = configExist.getColumns();
            // 选中的列
            int i = 0;
            for (String columnName : columnNames) {
                for (Column column : query.getColumnList()) {
                    if (columnName.equals(column.getId() != null ? column.getId() : column.getKey())) {
                        Object[] objs = new Object[2];
                        objs[0] = column.getHeader();
                        objs[1] = columnName;
                        selected[i++] = objs;
                        columnList.remove(column);
                    }
                }
            }
            config.setSelected(selected);
            // 未选中的列
            int j = 0;
            for (Column column : columnList) {
                if (!column.getHidden() && !column.getIsServerCondition()) {
                    Object[] objs = new Object[2];
                    objs[0] = column.getHeader();
                    objs[1] = column.getId() != null ? column.getId() : column.getKey();
                    unSelected[j++] = objs;
                }
            }
            config.setUnSelected(unSelected);
        } else {
            // 初始化选中列
            int i = 0;
            for (Column column : columnList) {
                if (!column.getHidden() && !column.getIsServerCondition()) {
                    Object[] objs = new Object[2];
                    objs[0] = column.getHeader();
                    objs[1] = column.getId() != null ? column.getId() : column.getKey();
                    selected[i++] = objs;
                }
            }
            config.setSelected(selected);
            config.setUnSelected(unSelected);
        }
        return config;
    }

    /**
     * 
     * @param queryId
     * @param pageName
     * @param session
     * @return
     */
    @RequestMapping(value = "getColumnConfig")
    @ResponseBody
    public Map<String, Object> getColumnConfig(String queryId, String pageName, HttpSession session) throws Exception {

        Query query = QueryDefinition.getQueryById(queryId);
        User user = SessionUtil.getCurrentUser();
        DetachedCriteria criteria = DetachedCriteria.forClass(QueryConfig.class);
        criteria.add(Restrictions.eq("queryId", queryId));
        criteria.add(Restrictions.eq("pageName", pageName));
        criteria.add(Restrictions.eq("userid", user.getId()));
        List<QueryConfig> list = queryService.findByCriteria(criteria);
        List<String> colsName = new ArrayList<String>();
        if (list.size() > 0)
            colsName = list.get(0).getColumns();
        Map<String, Object> map = new HashMap<String, Object>();
        query.setCallList(getCallList(query));
        map.put("query", query);
        map.put("columnName", colsName);
        return map;
    }

    /**
     * 
     * @param configObj
     * @param session
     * @return
     */
    @RequestMapping(value = "saveUserDefine")
    @ResponseBody
    public String saveUserDefine(String configObj, HttpSession session) {

        QueryConfig config = JSON.parseObject(configObj, QueryConfig.class);
        User user = SessionUtil.getCurrentUser();
        config.setUserid(user.getId());
        queryService.deleteAndSave(config);
        return "200";
    }

    /**
     * 恢复默认设置
     * 
     * @param configObj
     * @param session
     * @return
     */
    @RequestMapping(value = "setDefault")
    @ResponseBody
    public String setDefault(String configObj, HttpSession session) {

        QueryConfig config = JSON.parseObject(configObj, QueryConfig.class);
        User user = SessionUtil.getCurrentUser();
        config.setUserid(user.getId());
        queryService.delete(config);
        return "200";
    }

    @RequestMapping(value = "saveUserDefines")
    @ResponseBody
    public String saveUserDefines(String configList, HttpSession session) {

        List<QueryConfig> cfgList = JSON.parseArray(configList, QueryConfig.class);
        if (cfgList == null || cfgList.size() < 1)
            return "";
        User user = SessionUtil.getCurrentUser();
        for (QueryConfig cfg : cfgList) {
            cfg.setUserid(user.getId());
            queryService.deleteAndSave(cfg);
        }
        return "200";
    }

    /**
     * 恢复默认设置
     * 
     * @param configObj
     * @param session
     * @return
     */
    @RequestMapping(value = "setDefaults")
    @ResponseBody
    public String setDefaults(String configList, HttpSession session) {

        List<QueryConfig> cfgList = JSON.parseArray(configList, QueryConfig.class);
        if (cfgList == null || cfgList.size() < 1)
            return "";
        User user = SessionUtil.getCurrentUser();
        for (QueryConfig cfg : cfgList) {
            cfg.setUserid(user.getId());
            queryService.delete(cfg);
        }
        return "200";
    }

}
