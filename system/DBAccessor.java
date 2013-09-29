package system;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import javax.swing.JOptionPane;

import entity.Column;

/**
 * DBAccessor
 * @author Daniel J Bray
 *
 * This class acts as a bridge between the GUI and the mySQL database by sending and receiving from
 * the database and passing it along to the GUI. All SQL syntax is stored here and hard coded into
 * methods to act as a black box for the user.
 */
public class DBAccessor {
	private DBConnector connector;
	private Connection conn;
	private int cols;
	private String table;
	private int colIndex;
	private String currCol;

	/**
	 * Constructs a new DBAccessor, initializes fields, and creates a new connection to the mySQL database.
	 */
	public DBAccessor(){
		table = "";
		cols = 0;
		colIndex = 0;
		connector = new DBConnector();
		conn = connector.connect();
	}

	/**
	 * removeRow
	 * 
	 * Removes a tuple from the selected table. The parameter 'data' is data stored in the tuple
	 * with index 0 being the data on the farthest left part of the table. The parameter 'colNames'
	 * contains a vector that stores the entirety of the names of the columns. 
	 * 
	 * PRECONDITION: colNames and data must have the same size. Both must contain values that range 
	 * the length of the table.
	 * @param colNames - names of the columns stored in a vector
	 * @param data - all the data in a row stored in a vector
	 * @throws SQLException
	 */
	public void removeRow(Vector<String> colNames, Vector<String> data) throws SQLException{
		Vector<String> d = preprocessData(data);
		String s = "DELETE FROM " + table.toUpperCase() + " WHERE ";
		for(int i=0; i<colNames.size(); i++){
			s += colNames.get(i) + "=" + d.get(i);
			if(i+1 < colNames.size())
				s += " AND ";
		}
		s+=";";
		Statement statement = conn.createStatement();
		statement.execute(s);
	}

	/**
	 * dropTable
	 * 
	 * Removes the current table from the database.
	 * @throws SQLException
	 */
	public void dropTable() throws SQLException{
		Statement s = conn.createStatement();
		if(table != null)
			s.execute("DROP TABLE " + table.toUpperCase() + ";");
		table = null;
	}

	/**
	 * hasTable
	 * 
	 * Returns a boolean specifying whether or not there is a table 
	 * in the database with the name stored in the parameter 'name.'
	 * @param name - the name of the table to be checked.
	 * @return true if there is a table by that name in the database
	 * 		   false otherwise
	 */
	public boolean hasTable(String name){
		try{
			Statement s = conn.createStatement();
			ResultSet res = s.executeQuery("SHOW TABLES;");
			while(res.next()){
				if(name.equalsIgnoreCase(res.getString(1))){
					return true;
				}
			}
			return false;
		}
		catch(SQLException e){
			System.err.println(e.getMessage());
			return false;
		}
	}

	/**
	 * createTableStatement
	 * 
	 * A helper method to take all the information given by the GUI for a new table and formulates it into
	 * a create table mySQL statement.
	 * 
	 * @param name - the name of the table
	 * @param columns - the Column data stored in a vector
	 * @param primaryKeyName - the name of the primary key column
	 * @param hasInnoDB - specifies whether or not the table is using InnoDB
	 * @return the create table statement in SQL syntax
	 */
	private String createTableStatement(String name, Vector<Column> columns, 
			String[] primaryKeyName, boolean hasInnoDB){
		String statement = "CREATE TABLE " + name + "(\n";
		for(int i=0; i<columns.size(); i++){
			statement+=columns.get(i).getColName()+" "+columns.get(i).getDataType();
			if(columns.get(i).isNotNull())
				statement+=" NOT NULL";
			if(i+1 != columns.size())
				statement+=",\n";
		}
		if(primaryKeyName != null){
			statement+=",\nPRIMARY KEY(";
			for(int i=0; i<primaryKeyName.length; i++){
				statement+=primaryKeyName[i];
				if(i+1<primaryKeyName.length)
					statement+=", ";
			}
			statement+=")";
		}
		statement+=")";
		if(hasInnoDB)
			statement+="ENGINE=InnoDB";
		statement+=";";

		return statement;
	}

	public void prevCol() throws SQLException{
		colIndex--;
		if(colIndex < 0){
			colIndex = -1;
			currCol = null;
		}
		else{
			Statement statement = conn.createStatement();
			String s = "SELECT * FROM " + table.toUpperCase() + ";";
			ResultSet resSet = statement.executeQuery(s);
			currCol = resSet.getMetaData().getColumnName(colIndex+1);
		}
	}

	public int peekPrevColIndex(){
		return (colIndex-1 < 0) ? -1 : colIndex-1;
	}

	/**
	 * createTable
	 * 
	 * Creates a new table using the specified information and stores it in the database.
	 * 
	 * @param name - name of the table
	 * @param columns - column information of the table
	 * @param primaryKeyName - column name for the primary key
	 * @param hasInnoDB - true if the table uses InnoDB; false otherwise
	 * @throws SQLException
	 */
	public void createTable(String name, Vector<Column> columns, 
			String[] primaryKeys, boolean hasInnoDB) throws SQLException{
		String stringStatement = createTableStatement(name, columns, primaryKeys, hasInnoDB);

		Statement s = conn.createStatement();
		s.execute(stringStatement);
	}

	/**
	 * selectTable
	 * 
	 * Sets the current table to be used in the database.
	 * @param name - the table name
	 * @throws SQLException
	 */
	public void setTable(String name) throws SQLException{
		table = name;
		Statement s = conn.createStatement();
		ResultSet rs = s.executeQuery("SELECT * FROM "+table.toUpperCase());
		cols = rs.getMetaData().getColumnCount();
	}

	/**
	 * getColNames
	 * 
	 * Gets the column names in the current table and returns it in a String array.
	 * 
	 * @return the column names in the current table
	 * @throws SQLException
	 */
	public String[] getColNames() throws SQLException{
		if(table == null)
			throw new SQLException("No Table Selected.");

		Statement statement = conn.createStatement();
		String s = "SELECT * FROM " + table.toUpperCase() + ";";
		ResultSet resSet = statement.executeQuery(s);
		ResultSetMetaData rsmd = resSet.getMetaData();

		String [] columnNames = new String[cols];
		for(int i=0; i<cols; i++){
			columnNames[i] = rsmd.getColumnName(i+1);
		}
		return columnNames;
	}

	/**
	 * getCol
	 * 
	 * Returns the current column specified used for insertion.
	 * 
	 * @return the current column name.
	 */
	public String getCol(){
		return currCol;
	}

	/**
	 * getColDataType
	 * 
	 * Gets and returns the data type for the current column selected during the insertion
	 * process. Returns null if column index does not exist.
	 * @return the current column's data type
	 * @throws SQLException if the system encounters an SQL syntax error.
	 */
	public String getColDataType() throws SQLException{
		if(table == null)
			throw new SQLException("No table selected.");

		Statement statement = conn.createStatement();
		String s = "SELECT DATA_TYPE FROM information_schema.columns WHERE table_name = '" + table + 
				"' AND COLUMN_NAME = '" + currCol + "';";
		ResultSet rs = statement.executeQuery(s);
		if(!rs.next())
			return null;
		else{
			return rs.getNString(1);
		}
	}

	/**
	 * getColNotNull
	 * 
	 * Returns a Boolean signifying if the current column has NotNull enabled. Returns null
	 * if column index does not exist.
	 * @return True if field is NotNull, false if it allows null, and null if column not found
	 * @throws SQLException if the system encounters a syntax error in the mysql query
	 */
	public Boolean getColNotNull() throws SQLException{
		if(table == null)
			throw new SQLException("No table selected.");

		Statement statement = conn.createStatement();
		String s = "SHOW COLUMNS FROM " + table + " WHERE Field='" + currCol + "';";
		ResultSet rs = statement.executeQuery(s);
		if(!rs.next())
			return null;
		else{
			return rs.getNString(3).equalsIgnoreCase("YES");
		}
	}

	/**
	 * nextCol
	 * 
	 * Sets the current column index to the next column. Also updates the currCol
	 * string identifier of the current column.
	 * 
	 * @throws SQLException if no table is selected or if the identifier for the
	 * number of columns in the database is wrong and causes the index to go beyond
	 * the number of columns without resetting to -1;
	 */
	public void nextCol() throws SQLException{
		if(table == null)
			throw new SQLException("No table selected.");

		colIndex++;
		if(colIndex>=cols){
			colIndex = -1;
			currCol = null;
		}
		else{
			Statement statement = conn.createStatement();
			String s = "SELECT * FROM " + table.toUpperCase() + ";";
			ResultSet resSet = statement.executeQuery(s);
			currCol = resSet.getMetaData().getColumnName(colIndex+1);
		}
	}

	/**
	 * resetColIndex
	 * 
	 * Resets the current column index to 0;
	 */
	public void resetColIndex(){
		colIndex = 0;
		try{
			Statement statement = conn.createStatement();
			String s = "SELECT * FROM " + table.toUpperCase() + ";";
			ResultSet resSet = statement.executeQuery(s);
			currCol = resSet.getMetaData().getColumnName(colIndex+1);
		}
		catch(SQLException e){
			JOptionPane.showMessageDialog(null, e.getMessage());
		}
	}

	/**
	 * getColIndex
	 * 
	 * Returns the current column index.
	 * @return the current column index.
	 */
	public int getColIndex(){
		return colIndex;
	}

	/**
	 * peekColIndex
	 * 
	 * Peeks at the next column index. If there is no next
	 * column index then returns -1.
	 * @return the next column index.
	 */
	public int peekColIndex(){
		return (colIndex+1>=cols) ? -1 : colIndex+1;
	}

	/**
	 * preprocessData
	 * 
	 * Takes a tuple of data in a table stored in a Vector and turns it into SQL syntax for ease of use.
	 * For example:
	 * 	if a column has the data "Dan Bray" stored as a VARCHAR
	 * 	"Dan Bray" will turn into " 'Dan Bray' " such that it can promptly be inserted into an SQL table
	 * 
	 * @param data the data in it's raw form
	 * @return data in a processed, ready to insert form.
	 */
	public Vector<String> preprocessData(Vector<String> data){
		Vector<String> processed = new Vector<String>();
		try{
			if(table == null)
				throw new SQLException("No table selected.");

			Statement s = conn.createStatement();
			ResultSet res = s.executeQuery("SHOW COLUMNS FROM " + table + ";");
			String type;
			res.next();
			for(int i=0; i<cols; ++i){
				type = res.getNString(2);
				if(type.contains("varchar") || type.contains("char")){ //|| type == Types.LONGNVARCHAR
					//|| type == Types.LONGVARCHAR || type == Types.NCHAR || type == Types.NVARCHAR){
					processed.add("'"+data.get(i)+"'");
				}
				//TODO: add more if statements for each data type
				else{
					processed.add(data.get(i));
				}
				res.next();
			}
			return processed;
		}
		catch(SQLException e){
			System.err.println("Error: " + e.getMessage());
		}
		return null;
	}

	/**
	 * insert
	 * 
	 * Inserts a tuple of data into the database stored in a Vector.
	 * 
	 * PRECONDITION: Requires that data is pre-processed s.t. data is in it's proper form.
	 * 
	 * @param data - A tuple of data to be inserted
	 * @throws SQLException
	 */
	public void insert(Vector<String> data) throws SQLException{
		if(table == null)
			throw new SQLException("No table selected.");

		Vector<String> p_data = preprocessData(data);
		Statement statement = conn.createStatement();

		String s = "INSERT INTO " + table.toUpperCase() + " VALUES(";
		for(int i=0; i<p_data.size(); i++){
			s+=p_data.get(i);
			if(i+1<p_data.size())
				s+=",";
		}
		s+=");";

		statement.executeUpdate(s);

	}

	/**
	 * getTables
	 * 
	 * Returns all tables currently stored in the database.
	 * @return the tables currently stored in the database.
	 */
	public String[] getTables(){
		try{
			Vector<String> results = new Vector<String>();
			Statement statement = conn.createStatement();
			ResultSet res = statement.executeQuery("SHOW TABLES;");
			while(res.next()){
				results.add(res.getString(1));
			}
			String[] results_arr = new String[results.size()];
			for(int i=0; i<results_arr.length;i++){
				results_arr[i] = results.get(i);
			}
			return results_arr;
		}
		catch(SQLException e){
			System.err.println(e.getMessage());
		}
		return null;
	}

	/**
	 * getTableData
	 * 
	 * Returns all the data stored in the table as a String
	 * @return the table data
	 * @throws SQLException
	 */
	public Vector<String[]> getTableData() throws SQLException{
		if(table == null)
			throw new SQLException("No table selected.");

		Statement statement = conn.createStatement();
		String s = "SELECT * FROM " + table.toUpperCase() + ";";
		ResultSet resSet = statement.executeQuery(s);
		Vector<String[]> t = new Vector<String[]>();
		while(resSet.next()){
			String[] row = new String[cols];
			for(int i = 0; i < cols; i++){
				row[i] = resSet.getString(1+i);
			}
			t.add(row);
		}
		return t;
	}

	/**
	 * close
	 * 
	 * Closes the connection with the database.
	 */
	public void close(){
		connector.disconnect();
	}
}
