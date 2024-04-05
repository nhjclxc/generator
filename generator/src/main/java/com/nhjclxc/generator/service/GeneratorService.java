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
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 *  代码生成服务层实现
 *
 * @author 罗贤超
 */
@Slf4j
@Service
public class GeneratorService {

//	// serverTimezone=Asia/Shanghai
//	private static final String jdbcUrl = "jdbc:mysql://127.0.0.1:3306/test?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=Asia/Shanghai";
//	private static final String username = "root";
//	private static final String password = "root123";



		/* 确保多人同时可用
			1、搞一个连接池 使用HashMap
			2、使用uuid返回给前端，作为标识
			3、前端保存在sessionStorage里面，除连接数据库的接口不带uuid，其余接口都要待uuid
			4、前端在请求拦截器里面加上对应的uuid头部
			5、vue组件销毁前发送一个请求将对应的连接池里面的连接删除，确保连接池不会发生只增不减
		 */

	private final Map<String, DBSession> dbConnectPool = new HashMap<>();

	public boolean removeSessionUuid(String sessionUuid){
		DBSession remove = null;
		if (dbConnectPool.containsKey(sessionUuid)) {
			remove = dbConnectPool.remove(sessionUuid);
		}
		return remove != null;
	}


	/** 连接数据库 */
	public String connect(JDBCObject jdbcObject) {
		try {
			Connection connection = DriverManager.getConnection(jdbcObject.getJdbcUrl(), jdbcObject.getUsername(), jdbcObject.getPassword());
			Statement statement = connection.createStatement();
			String sessionUuid = UUID.randomUUID().toString().replaceAll("-", "");
			DBSession dbSession = DBSession.builder().connct(connection).statement(statement).sessionUuid(sessionUuid).build();
			dbConnectPool.put(sessionUuid, dbSession);
			return sessionUuid;
		} catch (SQLException e) {
			throw new RuntimeException("数据库连接异常：" + e.getMessage());
		}
	}

	/**
	 * 关闭数据库连接
	 */
	public String closeConnect( ) {
		DBSession dbSession = getDbSession();
		try {
			// 关闭资源
			Statement statement = dbSession.getStatement();
			if (statement != null)
				statement.close();

			Connection connct = dbSession.getConnct();
			if (connct != null)
				connct.close();

			// 连接池里面移除改对象
			this.dbConnectPool.remove(dbSession.getSessionUuid());

		} catch (SQLException se) {
			throw new RuntimeException("databases session close happend exception：" + se.getMessage());
		}
		log.info("databases session was closed !!! --- {}", dbSession.getSessionUuid());

		return "databases session was closed !!!";
	}

	/**
	 * 获取当前的数据库连接对象
	 */
	private DBSession getDbSession() {
		String sessionUuid = ContextHolder.getAuthorization();
		DBSession dbSession = dbConnectPool.get(sessionUuid);
		if (dbSession == null){
			throw new RuntimeException("databases session error：会话不存在");
		}
		return dbSession;
	}

	/** 执行sql */
	public ResultSet executeSql(String sql) throws SQLException {
		// threadLocal里面获取会话数据库信息
		DBSession dbSession = getDbSession();

		Statement statement = dbSession.getStatement();
		if (statement == null){
			throw new RuntimeException("请先连接数据库");
		}
		log.info("executeSql {}：{}", LocalDateTime.now(), sql);
		return statement.executeQuery(sql);
	}

	/** 更新配置 */
	public static void flushGenConfig(GeneratorCodeDTO dto){
		GenConfig.setAuthor(dto.getAuthor());
		GenConfig.setPackageName(dto.getPackageName());
		GenConfig.setAutoRemovePre(dto.getAutoRemovePre());
		GenConfig.setTablePrefix(dto.getTablePrefix());
		GenConfig.setEnableSwagger(dto.getEnableSwagger());
		GenConfig.setEnableLombok(dto.getEnableLombok());
	}

	public PageInfo<GenTable> parse(GeneratorCodeDTO dto, Integer pageNum, Integer pageSize) throws SQLException {

		// 刷新代码生成配置
		flushGenConfig(dto);

		// 查询表信息
		List<GenTable> tableList = genTableList(dto.getTableName(), dto.getTableComment(), false, false);

		// 对已有数据进行分页
		pageNum = Optional.ofNullable(pageNum).orElse(1);
		pageSize = Optional.ofNullable(pageSize).orElse(10);
		return getPageInfoByDataList(pageNum, pageSize, tableList);
	}

	/**
	 * 获取列数据
	 */
	private static String getDBTableColumnsByName(String tableNames){
		return "select table_name, column_name, ordinal_position as sort, column_comment, column_type, " +
				"(case when (is_nullable = 'no' && column_key != 'PRI') then '1' else '0' end) as is_required, " +
				"(case when column_key = 'PRI' then '1' else '0' end) as is_pk, " +
				"(case when extra = 'auto_increment' then '1' else '0' end) as is_increment\n" +
		"\t\tfrom information_schema.columns where table_schema = (select database()) and table_name in ( " + tableNames + " ) \n" +
		"\t\torder by ordinal_position";
	}

	/**
	 * 获取表结构
	 */
	public List<GenTable> genTableList(String tables, String tableComment, boolean isExport, boolean equal) throws SQLException {

		log.info("tables: {}", tables);
		if (isExport && (tables == null || "".equals(tables))){
			throw new RuntimeException("未选择表，无法导出");
		}

		// 查询表信息 sql
		/*
		select table_name, table_comment from information_schema.tables
		where  table_schema = (select database())
			and ( lower(table_name) like lower(concat('%','gen','%'))
			 		or lower(table_name) like lower(concat('%','dict','%'))
			 		or lower(table_comment) like lower(concat('%','任务','%'))
			 	)
		 */
		StringBuilder selectTableInfoSQL = new StringBuilder("select table_name, table_comment from information_schema.tables\n" +
				"\t\twhere  table_schema = (select database())");
		boolean hasTableNmaeFlag = (tables != null && !"".equals(tables));
		boolean hasTableCommentFlag = (tableComment != null && !"".equals(tableComment));
		if (hasTableNmaeFlag || hasTableCommentFlag){
			selectTableInfoSQL.append(" and ");

			if (hasTableNmaeFlag){
				if (equal){
					selectTableInfoSQL.append(" lower(table_name) = lower('").append(tables).append("')");
				}else {
					String[] tableNames = tables.split(",");
					if (isExport && tableNames.length <= 0){
						throw new RuntimeException("未选择表，无法导出");
					}
					StringJoiner stringJoiner = new StringJoiner(" or ", "", "");
					for (String tableName : tableNames) {
						if (isExport){
							stringJoiner.add(" table_name = '" + tableName +"'");
						}else {
							stringJoiner.add(" lower(table_name) like concat('%',lower('" + tableName +"'),'%')");
						}
					}
					selectTableInfoSQL.append(stringJoiner);
				}
			}

			if (hasTableNmaeFlag && hasTableCommentFlag) {
				selectTableInfoSQL.append(" and ");
			}

			if (hasTableCommentFlag) {
				selectTableInfoSQL.append(" lower(table_comment) like concat('%', lower('").append(tableComment).append("'),'%') ");
			}

		}

		ResultSet tablesRes = executeSql(selectTableInfoSQL.toString());

		// 处理结果集
		List<GenTable> tableList = new ArrayList<>();
		while (tablesRes.next()) {
			String tableName = tablesRes.getString("table_name");
			GenTable table = GenTable.builder().tableName(tableName).className(GenUtils.convertClassName(tableName))
					.tableComment(tablesRes.getString("table_comment"))
					.tplCategory(GenConstants.TPL_CRUD).build();
			GenUtils.initTable(table, GenConfig.author);
			tableList.add(table);
		}
		tablesRes.close();
		return tableList;
	}

	/**
	 * 生成代码
	 */
	public byte[] genCode(GeneratorCodeDTO dto) throws SQLException {

		flushGenConfig(dto);

		// 第一步：生成表结构
		List<GenTable> tableList = genTableList(dto.getTables(), null, true, false);

		// 生成字段数据
		StringJoiner stringJoiner = new StringJoiner(", ");
		for (String tableName : dto.getTables().split(",")) {
			stringJoiner.add("'" + tableName + "'");
		}
		List<GenTableColumn> genTableColumnsList = getColumnsList(stringJoiner.toString());
		Map<String, List<GenTableColumn>> genTableColumnsMap = genTableColumnsList.stream().filter(e -> e.getTableName() != null).collect(Collectors.groupingBy(GenTableColumn::getTableName));

		// 表数据与字段数据绑定
		for (GenTable table : tableList) {
			// 保存列信息
			table.setColumns(genTableColumnsMap.get(table.getTableName()));
		}

		// 第三步：生成代码
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ZipOutputStream zip = new ZipOutputStream(outputStream);
		for (GenTable genTable : tableList) {
			generatorCode(genTable, dto.getEnableVue3(), zip);
		}
		IOUtils.closeQuietly(zip);
		return outputStream.toByteArray();
	}

	/** 获取指定表的列数据
	 * 一张表 tableNames为 "'table1'"
	 * 多张表 tableNames为 "'table1','table2','table3'"
	 * */
	private List<GenTableColumn> getColumnsList(String tableNames) throws SQLException {
		String dbTableColumnsByNameSQL = getDBTableColumnsByName(tableNames);
		ResultSet columnsRes = executeSql(dbTableColumnsByNameSQL);

		List<GenTableColumn> genTableColumnsList = new ArrayList<>();
		while (columnsRes.next()) {
			GenTableColumn column = GenTableColumn.builder().tableName(columnsRes.getString("table_name"))
					.columnName(columnsRes.getString("column_name")).sort(columnsRes.getInt("sort"))
					.columnComment(columnsRes.getString("column_comment")).columnType(columnsRes.getString("column_type"))
					.isRequired(columnsRes.getString("is_required")).isPk(columnsRes.getString("is_pk"))
					.isIncrement(columnsRes.getString("is_increment")).build();
			GenUtils.initColumnField(column);
			genTableColumnsList.add(column);
		}
		columnsRes.close();
		return genTableColumnsList;
	}

	/**
	 * 根据表信息生成代码
	 */
	private void generatorCode(GenTable table, Boolean enableVue3, ZipOutputStream zip) {
		// 表信息
		//GenTable table

		// 设置主键列信息
		setPkColumn(table);

		VelocityInitializer.initVelocity();

		VelocityContext context = VelocityUtils.prepareContext(table);

		// 获取模板列表
		List<String> templates = VelocityUtils.getTemplateList(table.getTplCategory(), enableVue3);
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
	 * 预览代码
	 *
	 * @return 预览数据列表
	 */
	public Map<String, String> previewCode(GeneratorCodeDTO dto) throws SQLException {
		flushGenConfig(dto);

		Map<String, String> dataMap = new LinkedHashMap<>();
		// 查询表信息
		String tables = dto.getTables();
		List<GenTable> tableList = genTableList(tables, null, false, true);
		GenTable table = tableList.get(0);
		// 获取列信息
		List<GenTableColumn> columns = getColumnsList("'" + tables + "'");
		table.setColumns(columns);

		// 设置主键列信息
		setPkColumn(table);
		VelocityInitializer.initVelocity();

		VelocityContext context = VelocityUtils.prepareContext(table);

		// 获取模板列表
		List<String> templates = VelocityUtils.getTemplateList(table.getTplCategory(), dto.getEnableVue3());
		for (String template : templates)
		{
			// 渲染模板
			StringWriter sw = new StringWriter();
			Template tpl = Velocity.getTemplate(template, Constants.UTF8);
			tpl.merge(context, sw);
			dataMap.put(template, sw.toString());
		}
		return dataMap;
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