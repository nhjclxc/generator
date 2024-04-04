package com.nhjclxc.generator.service;

import com.github.pagehelper.PageInfo;
import com.nhjclxc.generator.model.*;
import com.nhjclxc.generator.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 *  代码生成服务层实现
 *
 * @author LuoXianchao
 */
@Slf4j
@Service
public class GeneratorService {

//	// serverTimezone=Asia/Shanghai
//	private static final String jdbcUrl = "jdbc:mysql://127.0.0.1:3306/test?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=Asia/Shanghai";
//	private static final String username = "root";
//	private static final String password = "root123";

	private Connection conn = null;
	private Statement stmt = null;

	/** 连接数据库 */
	public void connect(JDBCObject jdbcObject) throws SQLException {
		if (this.conn != null){
			return;
		}
		this.conn = DriverManager.getConnection(jdbcObject.getJdbcUrl(), jdbcObject.getUsername(), jdbcObject.getPassword());
		this.stmt = conn.createStatement();
	}

	/** 执行sql */
	public ResultSet executeSql(String sql) throws SQLException {

		if (this.stmt == null){
			throw new RuntimeException("请先连接数据库");
		}
		log.info("executeSql {}：{}", LocalDateTime.now(), sql);
		return this.stmt.executeQuery(sql);
	}


	public PageInfo<GenTable> parse(JDBCObject jdbcObject, GenTable genTable, Integer pageNum, Integer pageSize) throws SQLException {

		// 连接数据库
		connect(jdbcObject);

		// 查询表信息
		String dbTableListSQL = getDBTableListSQL(genTable.getTableName(), genTable.getTableComment());
		ResultSet rs = executeSql(dbTableListSQL);

		// 处理结果集
		List<GenTable> tableList = new ArrayList<>();
		while (rs.next()) {
			tableList.add(GenTable.builder().tableName(rs.getString("table_name")).tableComment(rs.getString("table_comment"))
					.createTime(parseLocalDateTime(rs.getString("create_time"), YYYY_MM_DD_HH_MM_SS_LINE))
					.updateTime(parseLocalDateTime(rs.getString("update_time"), YYYY_MM_DD_HH_MM_SS_LINE))
					.build());
		}

		// 对已有数据进行分页
		pageNum = Optional.ofNullable(pageNum).orElse(1);
		pageSize = Optional.ofNullable(pageSize).orElse(10);
		return getPageInfoByDataList(pageNum, pageSize, tableList);
	}

	private static String getDBTableListSQL(String tableName, String tableComment){
		StringBuilder sql = new StringBuilder("\t\tselect table_name, table_comment, create_time, update_time from information_schema.tables\n" +
				"\t\twhere table_schema = (select database())\n" +
				"\t\tAND table_name NOT LIKE 'qrtz_%' AND table_name NOT LIKE 'gen_%'\n" +
				"\t\tAND table_name NOT IN (select table_name from gen_table)\n");

		if (StringUtils.isNotBlank(tableName)){
			sql.append("\t\t\tAND lower(table_name) like lower(concat('%").append(tableName).append("%'))\n");
		}
		if (StringUtils.isNotBlank(tableComment)){
			sql.append("\t\t\tAND lower(table_comment) like lower(concat('%").append(tableComment).append("%'))\n");
		}
		sql.append("        order by create_time desc");

		return sql.toString();
	}
	private static String getDBTableListSQL4Output(String tableNames){
		/*
 		select table_name, table_comment, create_time, update_time from information_schema.tables
 		where  table_schema = (select database())
 		and table_name in
	    <foreach collection="array" item="name" open="(" separator="," close=")">
 			#{name}
        </foreach>
		 */
		return " \t\tselect table_name, table_comment, create_time, update_time from information_schema.tables\n" +
				" \t\twhere  table_schema = (select database())\n" +
				" \t\tand table_name in " + tableNames;
	}
	private static String getDBTableColumnsByName(String tableNames){
		return "select table_name, column_name, ordinal_position as sort, column_comment, column_type, " +
				"(case when (is_nullable = 'no' && column_key != 'PRI') then '1' else '0' end) as is_required, " +
				"(case when column_key = 'PRI' then '1' else '0' end) as is_pk, " +
				"(case when extra = 'auto_increment' then '1' else '0' end) as is_increment\n" +
		"\t\tfrom information_schema.columns where table_schema = (select database()) and table_name in " + tableNames + "\n" +
		"\t\torder by ordinal_position";
	}

	public byte[] genCode(GeneratorCodeDTO dto) throws SQLException {

		GenConfig.author = dto.getAuthor();
		GenConfig.packageName = dto.getPackageName();
		GenConfig.autoRemovePre= dto.getAutoRemovePre();
		GenConfig.tablePrefix = dto.getTablePrefix();
		GenConfig.enableSwagger = dto.getEnableSwagger();

		String tables = dto.getTables();
		if (tables == null || "".equals(tables)){
			throw new RuntimeException("未选择表，无法导出");
		}
		String[] tableNames = tables.split(",");
		if (tableNames.length <= 0){
			throw new RuntimeException("未选择表，无法导出");
		}

		StringJoiner stringJoiner = new StringJoiner(", ", "(", ")");

		for (String tableName : tableNames) {
			stringJoiner.add("'" + tableName + "'");
		}


		// 第一步：生成表结构

		// 查询表信息
		String dbTableListSQL4Output = getDBTableListSQL4Output(stringJoiner.toString());
//		ResultSet tablesRes = this.stmt.executeQuery(dbTableListSQL4Output);
		ResultSet tablesRes = executeSql(dbTableListSQL4Output);

		// 处理结果集
		List<GenTable> tableList = new ArrayList<>();
		while (tablesRes.next()) {
			tableList.add(GenTable.builder().tableName(tablesRes.getString("table_name")).tableComment(tablesRes.getString("table_comment"))
					.createTime(parseLocalDateTime(tablesRes.getString("create_time"), YYYY_MM_DD_HH_MM_SS_LINE))
					.updateTime(parseLocalDateTime(tablesRes.getString("update_time"), YYYY_MM_DD_HH_MM_SS_LINE))
					.tplCategory(GenConstants.TPL_CRUD).build());
		}
//		List<String> tableNameList = tableList.stream().map(GenTable::getTableName).filter(Objects::nonNull).collect(Collectors.toList());

		String dbTableColumnsByNameSQL = getDBTableColumnsByName(stringJoiner.toString());
//		ResultSet columnsRes = this.stmt.executeQuery(dbTableColumnsByNameSQL);

		ResultSet columnsRes = executeSql(dbTableColumnsByNameSQL);

		List<GenTableColumn> genTableColumnsList = new ArrayList<>();
		while (columnsRes.next()) {
			genTableColumnsList.add(GenTableColumn.builder().tableName(columnsRes.getString("table_name"))
					.columnName(columnsRes.getString("column_name")).sort(columnsRes.getInt("sort"))
					.columnComment(columnsRes.getString("column_comment")).columnType(columnsRes.getString("column_type"))
					.isRequired(columnsRes.getString("is_required")).isPk(columnsRes.getString("is_pk"))
					.isIncrement(columnsRes.getString("is_increment")).build());
		}
		Map<String, List<GenTableColumn>> genTableColumnsMap = genTableColumnsList.stream().filter(e -> e.getTableName() != null).collect(Collectors.groupingBy(GenTableColumn::getTableName));

		for (GenTable table : tableList) {
			String tableName = table.getTableName();
			GenUtils.initTable(table, GenConfig.author);
			// 保存列信息
			List<GenTableColumn> genTableColumns = genTableColumnsMap.get(tableName);
			List<GenTableColumn> columns = new ArrayList<>();
			for (GenTableColumn column : genTableColumns) {
				GenUtils.initColumnField(column, table);
				columns.add(column);
			}
			table.setColumns(columns);
		}

		// 第二步：生成代码

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ZipOutputStream zip = new ZipOutputStream(outputStream);
		for (GenTable genTable : tableList) {
			generatorCode(genTable, zip);
		}
		IOUtils.closeQuietly(zip);
		return outputStream.toByteArray();
	}

	/**
	 * 查询表信息并生成代码
	 */
	private void generatorCode(GenTable table, ZipOutputStream zip) {
		// 表信息
		//GenTable table

		// 设置主子表信息
//		setSubTable(table);
		// 设置主键列信息
		setPkColumn(table);

		VelocityInitializer.initVelocity();

		VelocityContext context = VelocityUtils.prepareContext(table);

		// 获取模板列表
		List<String> templates = VelocityUtils.getTemplateList(table.getTplCategory(), table.getTplWebType());
		for (String template : templates)
		{
			// 渲染模板
			StringWriter sw = new StringWriter();
			Template tpl = Velocity.getTemplate(template, Constants.UTF8);
			tpl.merge(context, sw);
			try
			{
				// 添加到zip
				zip.putNextEntry(new ZipEntry(VelocityUtils.getFileName(template, table)));
				IOUtils.write(sw.toString(), zip, Constants.UTF8);
				IOUtils.closeQuietly(sw);
				zip.flush();
				zip.closeEntry();
			}
			catch (IOException e)
			{
				log.error("渲染模板失败，表名：" + table.getTableName(), e);
			}
		}
	}

	/**
	 * 设置主子表信息
	 *
	 * @param table 业务表信息
	 */
	public void setSubTable(GenTable table) {
		String subTableName = table.getSubTableName();
		if (StringUtils.isNotEmpty(subTableName))
		{
//			table.setSubTable(genTableMapper.selectGenTableByName(subTableName));
		}
	}

	/**
	 * 设置主键列信息
	 *
	 * @param table 业务表信息
	 */
	public void setPkColumn(GenTable table) {
		for (GenTableColumn column : table.getColumns())
		{
			if (column.isPk())
			{
				table.setPkColumn(column);
				break;
			}
		}
		if (StringUtils.isNull(table.getPkColumn()))
		{
			table.setPkColumn(table.getColumns().get(0));
		}
		if (GenConstants.TPL_SUB.equals(table.getTplCategory()))
		{
			for (GenTableColumn column : table.getSubTable().getColumns())
			{
				if (column.isPk())
				{
					table.getSubTable().setPkColumn(column);
					break;
				}
			}
			if (StringUtils.isNull(table.getSubTable().getPkColumn()))
			{
				table.getSubTable().setPkColumn(table.getSubTable().getColumns().get(0));
			}
		}
	}


	/**
	 * 手动分页
	 *
	 * @param pageNum 页码，默认0
	 * @param pageSize 页大小，默认10
	 * @param dataList 数据集
	 * @return 分页对象
	 * @param <T> 数据类型 泛型
	 * @author 罗贤超
	 */
	public static <T> PageInfo<T> getPageInfoByDataList(Integer pageNum, Integer pageSize, List<T> dataList) {
		PageInfo<T> pageInfo = new PageInfo<>();
		if (dataList == null || dataList.size() == 0){
			pageInfo.setList(new ArrayList<>());
			return pageInfo;
		}
		pageNum = Optional.ofNullable(pageNum).orElse(0);
		pageSize = Optional.ofNullable(pageSize).orElse(10);
		int total = dataList.size();
		if (total > pageSize) {
			int toIndex = pageSize * pageNum;
			if (toIndex > total) {
				toIndex = total;// 取小的
			}
			dataList = dataList.subList(pageSize * (pageNum - 1), toIndex);
		}
		pageInfo.setList(dataList);
		pageInfo.setPageNum(pageNum);
		pageInfo.setPageSize(pageSize);
		pageInfo.setPages((total + pageSize - 1) / pageSize);
		pageInfo.setTotal(total);

		int pages = pageInfo.getPages();
		pageInfo.setIsFirstPage(pageNum == 1);
		pageInfo.setIsLastPage(pageNum == pages);
		pageInfo.setHasPreviousPage(pageNum > 1);
		pageInfo.setHasNextPage(pageNum < pages);
		return pageInfo;
	}


	public static final String YYYY_MM_DD_HH_MM_SS_LINE = "yyyy-MM-dd HH:mm:ss";

	public static LocalDateTime parseLocalDateTime(String localDateTime, String pattern) {
		try {
			return LocalDateTime.parse(localDateTime, DateTimeFormatter.ofPattern(pattern));
		}catch (Exception ignored){ }
		return null;
	}

    /*


////#  datasource:
////#    driverClassName: com.alibaba.druid.proxy.DruidDriver   com.mysql.cj.jdbc.Driver
////#    url: jdbc:mysql://127.0.0.1:3306/test?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
////#    username: root
////#    password: root123
    	public static void main(String[] args) {
		Connection conn = null;
		Statement stmt = null;
		try {
			// 连接数据库
			conn = DriverManager.getConnection(jdbcUrl, username, password);
			// 创建Statement对象
			stmt = conn.createStatement();

			// 执行查询
			String sql = "\n" +
					" \t\tselect table_name, table_comment, create_time, update_time from information_schema.tables\n" +
					" \t\twhere  table_schema = (select database())\n" +
					" \t\tand table_name in ('sys_job')";
			ResultSet rs = stmt.executeQuery(sql);
			// 处理结果集
			while (rs.next()) {
				System.out.println(rs.getString("table_name") + " - " + rs.getString("table_comment") );
			}

			String sql2 = "\n" +
					"\t\tselect column_name, (case when (is_nullable = 'no' && column_key != 'PRI') then '1' else '0' end) as is_required, (case when column_key = 'PRI' then '1' else '0' end) as is_pk, ordinal_position as sort, column_comment, (case when extra = 'auto_increment' then '1' else '0' end) as is_increment, column_type\n" +
					"\t\tfrom information_schema.columns where table_schema = (select database()) and table_name = ('sys_job')\n" +
					"\t\torder by ordinal_position";
			ResultSet rs2 = stmt.executeQuery(sql2);
			// 处理结果集
			while (rs2.next()) {
				System.out.println(rs2.getString("column_name") + " - " + rs2.getString("is_required") );
			}



			// 关闭结果集、Statement和连接
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException se) {
			// 处理JDBC错误
			se.printStackTrace();
		} catch (Exception e) {
			// 处理Class.forName错误
			e.printStackTrace();
		} finally {
			// 关闭资源
			try {
				if (stmt != null) stmt.close();
			} catch (SQLException se2) {
			}
			try {
				if (conn != null) conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
	}
     */
}