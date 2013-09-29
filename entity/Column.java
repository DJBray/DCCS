package entity;

/**
 * Column
 * @author Daniel J Bray
 *
 * 	The column class is an entity class that is to be used for transporting table data. When creating
 * a new table in mySQL this allows for a table to add a new column through giving each column a 
 * name, data type, and specifying whether or not the field should be "not null." While there are
 * other features in mySQL for building tables, this is geared towards simplifying the program to 
 * make it easier to use for beginners or individuals without need of advanced databases.
 */
public class Column{
	private String colName;
	private String dataType;
	private boolean notNull;
	
	/**
	 * Constructor
	 * 
	 * Creates a column with an initialized name, data type, and specifies the "not null" field
	 * @param colName - Name of the column to be used in a new table in mySQL
	 * @param dataType - The data type of the column
	 * @param notNull - Specifies whether or not the field "not null" should be included
	 */
	public Column(String colName, String dataType, boolean notNull){
		this.colName = colName;
		this.dataType = dataType;
		this.notNull = notNull;
	}
	
	/**
	 * getColName
	 * 
	 * Returns the name of the Column
	 * @return - the name of the column
	 */
	public String getColName(){
		return colName;
	}
	
	/**
	 * getDataType
	 * 
	 * Returns the name of the Column's data type
	 * @return - the data type of the column
	 */
	public String getDataType(){
		return dataType;
	}
	
	/**
	 * isNotNull
	 * 
	 * Returns whether or not the "not null" field is included.
	 * @return true if "not null" is included
	 * 		   false otherwise
	 */
	public boolean isNotNull(){
		return notNull;
	}
	
	/**
	 * setColName
	 * 
	 * Sets the new name of the column.
	 * @param colName - the new name of the column
	 */
	public void setColName(String colName){
		this.colName = colName;
	}
	
	/**
	 * setDataType
	 * 
	 * Sets the new data type of the column.
	 * @param dataType - the new data type of the column
	 */
	public void setDataType(String dataType){
		this.dataType = dataType;
	}
	
	/**
	 * setNotNull
	 * 
	 * Sets the 'not null' field to be enabled or disabled.
	 * @param notNull - true if 'not null' is included
	 * 					false otherwise
	 */
	public void setNotNull(boolean notNull){
		this.notNull = notNull;
	}
}
