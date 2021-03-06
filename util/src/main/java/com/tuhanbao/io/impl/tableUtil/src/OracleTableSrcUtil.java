package com.tuhanbao.io.impl.tableUtil.src;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.tuhanbao.io.base.Constants;
import com.tuhanbao.io.impl.classUtil.EnumClassInfo;
import com.tuhanbao.io.impl.classUtil.IEnumType;
import com.tuhanbao.io.impl.codeUtil.Xls2CodeUtil;
import com.tuhanbao.io.impl.sqlUtil.DBUtil;
import com.tuhanbao.io.impl.tableUtil.DBType;
import com.tuhanbao.io.impl.tableUtil.DataType;
import com.tuhanbao.io.impl.tableUtil.EnumManager;
import com.tuhanbao.io.impl.tableUtil.ImportColumn;
import com.tuhanbao.io.impl.tableUtil.ImportTable;
import com.tuhanbao.io.objutil.ArrayUtil;
import com.tuhanbao.io.objutil.StringUtil;
import com.tuhanbao.util.db.conn.DBSrc;
import com.tuhanbao.util.db.conn.MyConnection;
import com.tuhanbao.util.db.table.dbtype.DBDataType;
import com.tuhanbao.util.db.table.dbtype.MysqlDataType;

public class OracleTableSrcUtil implements ITableSrcUtil{
	
	private static final OracleTableSrcUtil instance = new OracleTableSrcUtil();
	
	private OracleTableSrcUtil() {
		
	}
	
	public static OracleTableSrcUtil getInstance() {
		return instance;
	}
	
	/**
     * 导出一个用户下面的表
     * 
     * 仅用于oracle数据库
     * @param src
     * @throws Exception 
     */
    public List<ImportTable> getTables(DBSrc src) throws Exception {
    	String getAllTableSql = "select table_name from user_tables";
    	MyConnection connection = src.getConnection();
    	
		ResultSet rs = connection.executeQuery(getAllTableSql);
		List<String> tableNames = new ArrayList<String>();
		List<ImportTable> tables = new ArrayList<ImportTable>();
		while (rs.next()) {
			tableNames.add(rs.getString(1));
		}
		DBUtil.close(rs);
		connection.release();
		
		for (String tableName : tableNames) {
			tables.add(getTable(src, tableName));
		}
    	tables.addAll(getViews(src));
    	return tables;
    }
    
    public ImportTable getTable(DBSrc src, String tableName) throws Exception {
		ImportTable table = new ImportTable(tableName);
		MyConnection connection = src.getConnection();
		
		String sql = "select t2.column_name from user_constraints t1 left join user_cons_columns t2 on t2.constraint_name = t1.constraint_name where t1.table_name = '" + tableName + "' and t1.constraint_type ='P'";
		ResultSet rs = connection.executeQuery(sql);
		String PK= null;
		//只支持单主键
		while (rs.next()) {
			PK = rs.getString(1);
		}
		DBUtil.close(rs);
		
		sql = "select col.column_name, col.data_type, col.data_length, col.nullable, cet.comments, col.data_precision, col.data_scale, col.data_default from user_tab_columns col left join user_col_comments cet on cet.table_name=col.table_name and cet.column_name = col.column_name where col.table_name='" + tableName + "'";
		
		rs = connection.executeQuery(sql);
		while (rs.next()) {
			ImportColumn column = getColumn(table, rs, src.getDBType());
			table.addColumn(column);
			if (column.getName().equalsIgnoreCase(PK)) {
				table.setPK(column);
			}
		}
		
		DBUtil.close(rs);
		connection.release();
    	
		if (table.getColumns() == null || table.getColumns().isEmpty()) return null;
    	return table;
    }
	
	/**
     * 导出一个用户下面的表
     * 
     * 仅用于oracle数据库
     * @param src
     * @throws Exception 
     */
    private List<ImportTable> getViews(DBSrc src) throws Exception {
    	String getAllTableSql = "select view_name from user_views";
    	MyConnection connection = src.getConnection();
    	
		ResultSet rs = connection.executeQuery(getAllTableSql);
		List<String> tableNames = new ArrayList<String>();
		List<ImportTable> tables = new ArrayList<ImportTable>();
		while (rs.next()) {
			tableNames.add(rs.getString(1));
		}
		DBUtil.close(rs);
		connection.release();
		
		for (String tableName : tableNames) {
			ImportTable table = getTable(src, tableName);
			table.setView(true);
			tables.add(table);
		}
    	
    	return tables;
    }
    
    public ImportTable getTable(String tableName, String[][] arrays, DBType dbType) throws Exception {
		ImportTable table = new ImportTable(tableName);
		int length = arrays.length;
		//首行是列标
		for (int i = 1; i < length; i++) {
			String[] array = arrays[i];
			if (Xls2CodeUtil.isEmptyLine(array)) continue;
			ImportColumn column = getColumn(table, array, dbType);
			if (column.isPK()) table.setPK(column);
			else table.addColumn(column);
		}
		
    	return table;
    }
    
	private static ImportColumn getColumn(ImportTable table, String[] array, DBType dbType) {
		String colName = array[0];
		String dataType = array[1];
		String dataLengthStr = ArrayUtil.indexOf(array, 2);
		String canFilterStr = ArrayUtil.indexOf(array, 3);
		int dataLength = 0;
		
		if (!StringUtil.isEmpty(dataLengthStr)) dataLength = Integer.valueOf(dataLengthStr);
		
		IEnumType enumInfo = EnumManager.getEnum(dataType);
		ImportColumn col = null;
		if (enumInfo != null) {
			int enumDt = enumInfo.getType();
			if (enumDt == EnumClassInfo.INT) {
				dataType = "int";
				dataLength = 4;
			}
			else {
				dataType = "String";
				dataLength = 63;
			}
			
			DataType dt = DataType.getDataType(dataType.toUpperCase());
			col = new ImportColumn(table, colName, dt, TableSrcUtilFactory.getDBDataType(dt, dbType), dataLength);
			col.setEnumInfo(enumInfo);
		}
		else {
			DataType dt = DataType.getDataType(dataType.toUpperCase());
			col = new ImportColumn(table, colName, dt, TableSrcUtilFactory.getDBDataType(dt, dbType), dataLength);
		}

		String temp = ArrayUtil.indexOf(array, 4);
		if (!StringUtil.isEmpty(temp)) {
			if (temp.startsWith("PK")) {
				col.setPK(true);
			} 
			else if (temp.startsWith("FK")) {
	//			FK(T_CABLES_ROLE.ROLE_ID)
				int start = temp.indexOf(Constants.LEFT_PARENTHESE);
				int end = temp.indexOf(Constants.RIGHT_PARENTHESE);
				String FK = temp.substring(start + 1, end);
				col.setFK(FK);
			}
		}
		col.setDefaultValue(ArrayUtil.indexOf(array, 5));
		col.setComment(ArrayUtil.indexOf(array, 6));
		col.setCanFilter("true".equalsIgnoreCase(canFilterStr) || "1".equals(canFilterStr));
		return col;
	}

	private static ImportColumn getColumn(ImportTable table, ResultSet rs, DBType dbType) throws SQLException {
		String colName = rs.getString(1);
		String dataType = rs.getString(2);
		String dataLengthStr = rs.getString(3);
		boolean isNullable = !"Y".equals(rs.getString(4));
		String comment = rs.getString(5);
		String defaultValue = rs.getString(8);
		
		int dataLength = 0;
		//oracle的number要特殊一些
		if ("NUMBER".equalsIgnoreCase(dataType)) {
			dataLength = Integer.valueOf(rs.getInt(6));
			int scale = Integer.valueOf(rs.getInt(7));
			if (scale == 0) {
				if (dataLength > 10) dataType = "LONG";
				else dataType = "INT";
			}
		}
		else if (!StringUtil.isEmpty(dataLengthStr)) {
			dataLength = Integer.valueOf(dataLengthStr);
		}
		
		int index = dataType.indexOf("(");
		if (index != -1) {
			dataType = dataType.substring(0, index);
		}
		ImportColumn ic = new ImportColumn(table, colName, TableSrcUtilFactory.getDBDataType(dataType, dbType), dataLength, isNullable, comment);
		if (!StringUtil.isEmpty(defaultValue)) ic.setDefaultValue(defaultValue);
		return ic;
	}
	
	public String getSql(ImportTable table) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("drop table if exists ").append(table.getTableName().toUpperCase()).append(Constants.SEMICOLON).append(Constants.ENTER);
        sb.append("create table ").append(table.getTableName().toUpperCase()).append(Constants.LEFT_PARENTHESE);
        ImportColumn pk = null;
        for (ImportColumn col : table.getColumns())
        {
            sb.append(getSqlColumn(col)).append(Constants.COMMA).append(Constants.BLANK);
            if (col.isPK())
            {
                pk = col;
            }
        }
        
        if (pk != null)
        {
            sb.append("PRIMARY KEY (").append(pk.getName()).append(")");
        }
        else
        {
            sb.delete(sb.length() - 2, sb.length());
        }
        sb.append(Constants.RIGHT_PARENTHESE).append(Constants.SEMICOLON).append(Constants.ENTER);
        sb.append(Constants.ENTER);
        
        return sb.toString();
    }
	
	private static String getSqlColumn(ImportColumn c)
    {
        StringBuilder sb = new StringBuilder(c.getName());
        sb.append(Constants.BLANK);
        DBDataType dt = c.getDBDataType();
        if (dt == MysqlDataType.VARCHAR)
        {
            sb.append("VARCHAR(").append(c.getLength()).append(") COLLATE utf8_unicode_ci");
        }
        else if (dt == MysqlDataType.TEXT)
        {
            sb.append("TEXT COLLATE utf8_unicode_ci");
        }
        else {
        	sb.append(dt.name().toUpperCase());
        }
        
        return sb.toString();
    }
}
