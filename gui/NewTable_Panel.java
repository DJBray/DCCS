package gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import system.DBAccessor;
import entity.Column;

/**
 * NewTable_Panel
 * @author Daniel J Bray
 *
 * This class is the GUI for creating a new table in a mySQL database. It does not come with all 
 * the features implemented in mySQL for creating a new table but instead provides an easy interface
 * to understand for mySQL beginners who don't know the syntax.
 */
public class NewTable_Panel {

	private static final String[] dataTypes = {"INT", "DOUBLE", "DATE", "CHAR", "VARCHAR(45)", "TEXT"};
	private static final String[] notNull = {"Allow null", "Not Null"};

	//GUI components
	private JFrame frame;
	private JTextField tableNameField;
	private JTextField columnNameField;
	private JTextField primaryKeyField;
	private JList<String> list;
	private JComboBox<String> dataTypeComboBox;
	private JComboBox<String> notNullComboBox;
	private JCheckBox chckbxInnodb;

	private Vector<Column> cols;		//the columns of the new table
	private Vector<String> colsInText;  //the columns of the new table as a String vector
	private String[] primaryKeys;

	//Database
	private DBAccessor database;

	/**
	 * Create the application and initialize components.
	 * @param database - the database to be used
	 */
	public NewTable_Panel(DBAccessor database) {
		primaryKeys = null;
		this.database = database;
		cols = new Vector<Column>();
		colsInText = new Vector<String>();
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		//I apologize, it is messy
		frame = new JFrame();
		frame.setBounds(100, 100, 650, 296);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(null);

		JLabel lblTableName = new JLabel("Table Name");
		lblTableName.setBounds(26, 11, 76, 14);
		panel.add(lblTableName);

		tableNameField = new JTextField();
		tableNameField.setBounds(130, 8, 190, 20);
		panel.add(tableNameField);
		tableNameField.setColumns(10);

		list = new JList<String>();
		list.setBounds(130, 44, 311, 99);
		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setBounds(128, 42, 313, 101);
		panel.add(scrollPane);

		dataTypeComboBox = new JComboBox<String>(dataTypes);
		dataTypeComboBox.setBounds(130, 174, 109, 20);
		panel.add(dataTypeComboBox);

		JLabel lblColumns = new JLabel("Columns");
		lblColumns.setBounds(26, 45, 60, 14);
		panel.add(lblColumns);

		JLabel lblAllowNull = new JLabel("Allow Null?");
		lblAllowNull.setBounds(450, 154, 70, 20);
		panel.add(lblAllowNull);

		notNullComboBox = new JComboBox<String>(notNull);
		notNullComboBox.setBounds(450, 174, 60, 20);
		panel.add(notNullComboBox);

		JButton btnAddColumn = new JButton("Add Column");
		btnAddColumn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addColumn();
			}
		});
		btnAddColumn.setBounds(515, 173, 105, 23);
		panel.add(btnAddColumn);

		columnNameField = new JTextField();
		columnNameField.setBounds(264, 174, 177, 20);
		panel.add(columnNameField);
		columnNameField.setColumns(10);

		JLabel lblAddColumn = new JLabel("Add Column");
		lblAddColumn.setBounds(26, 177, 77, 14);
		panel.add(lblAddColumn);

		JLabel lblDataType = new JLabel("Data Type");
		lblDataType.setBounds(130, 154, 70, 14);
		panel.add(lblDataType);

		JLabel lblColumnName = new JLabel("Column Name");
		lblColumnName.setBounds(264, 154, 85, 14);
		panel.add(lblColumnName);

		JButton btnMakePrimaryKey = new JButton("Make Primary Key");
		btnMakePrimaryKey.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				makePrimaryKey();
			}
		});
		btnMakePrimaryKey.setBounds(464, 41, 137, 23);
		panel.add(btnMakePrimaryKey);

		JLabel lblCurrentPrimaryKey = new JLabel("Current Primary Key:");
		lblCurrentPrimaryKey.setBounds(465, 88, 121, 14);
		panel.add(lblCurrentPrimaryKey);

		primaryKeyField = new JTextField();
		primaryKeyField.setEditable(false);
		primaryKeyField.setBounds(464, 113, 117, 20);
		panel.add(primaryKeyField);
		primaryKeyField.setColumns(10);

		JButton btnFinish = new JButton("Finish");
		btnFinish.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				finish();
			}
		});
		btnFinish.setBounds(130, 221, 109, 23);
		panel.add(btnFinish);

		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}
		});
		btnCancel.setBounds(264, 221, 109, 23);
		panel.add(btnCancel);

		chckbxInnodb = new JCheckBox("InnoDB?");
		chckbxInnodb.setBounds(381, 221, 97, 23);
		panel.add(chckbxInnodb);
		frame.setVisible(true);

		frame.getRootPane().setDefaultButton(btnAddColumn);

		JButton btnRemoveColumn = new JButton("Remove Column");
		btnRemoveColumn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				removeColumn();
			}
		});
		btnRemoveColumn.setBounds(486, 198, 134, 25);
		panel.add(btnRemoveColumn);
	}

	/**
	 * addColumn
	 * 
	 * Adds a new column to the table by getting the necessary data from the GUI fields and then
	 * storing these fields in two Vectors. No data is actually sent to the database until finish()
	 * is called.
	 */
	private void addColumn(){
		String c_name = columnNameField.getText();
		String dataType = (String)dataTypeComboBox.getSelectedItem();
		String n_Null = (String)notNullComboBox.getSelectedItem();

		boolean nn = n_Null.equals(notNull[0]) ? false : true;
		Column newCol = new Column(c_name.toLowerCase(), dataType, nn);

		//Checking for user error
		if(c_name.trim().isEmpty())
			JOptionPane.showMessageDialog(frame, "Column name cannot be empty");
		else if(c_name.contains(" "))
			JOptionPane.showMessageDialog(frame, "Column name cannot containe spaces"); 
		else if(dataType.isEmpty())
			JOptionPane.showMessageDialog(frame, "Data Type must be selected");
		else if(n_Null.isEmpty())
			JOptionPane.showMessageDialog(frame, "Not null field must be selected");
		else{
			//Makes sure no other columns have the same name
			for(Column i: cols){
				if(i.getColName().equalsIgnoreCase(c_name)){
					JOptionPane.showMessageDialog(frame, "Table already contains a column with that name.");
					return;
				}
			}
			//Adds the column
			cols.add(newCol);
			colsInText.add(dataType + "       " + c_name + "       " + n_Null);
			updateList();

			//Reset the column name field to being blank
			columnNameField.setText("");
		}
	}

	/**
	 * removeColumn
	 * 
	 * Removes a column from the new table.
	 */
	public void removeColumn(){
		//Assert the there is a selected index in the list component
		if(list.getSelectedIndex() != -1){
			int selectedIndex = list.getSelectedIndex();

			//If removed column is primary key, set primary key to null
			if(primaryKeyField.getText().equals(cols.get(selectedIndex).getColName()))
				primaryKeyField.setText("");

			//Removes the column and updates the list component
			cols.remove(selectedIndex);
			colsInText.remove(selectedIndex);
			updateList();
		}
	}

	/**
	 * updateList
	 * 
	 * Updates the list component of the GUI to display all the current columns to be added to
	 * the new table.
	 */
	private void updateList(){
		list.setListData(colsInText);
	}

	/**
	 * makePrimaryKey
	 * 
	 * Sets the new primary key of the table to the selected value in the list component.
	 */
	private void makePrimaryKey(){
		//checks if null
		if(list.getSelectedValue()==null)
			return;
		
		primaryKeyField.setText("");

		primaryKeys = new String[list.getSelectedIndices().length];
		int primaryKeyIndex = 0;
		for(int i=0;i<list.getSelectedIndices().length;i++){
			Column c = cols.get(i);
			boolean notNull = c.isNotNull();

			//asserts the user wants to make this a primary key if "not null" is not specified
			if(!notNull){
				int result = JOptionPane.showConfirmDialog(frame, "Making " + c.getColName() + " a primary key will make " +
						"it not allow null. Proceed anyway?");
				if(result != JOptionPane.OK_OPTION){
					return;
				}
			}
			primaryKeys[primaryKeyIndex] = c.getColName();
			primaryKeyIndex++;
		}
		
		for(int i=0; i<primaryKeys.length; i++){
			primaryKeyField.setText(primaryKeyField.getText() + primaryKeys[i]);
			if(i+1 < primaryKeys.length)
				primaryKeyField.setText(primaryKeyField.getText() + ", ");
		}
	}

	/**
	 * finish
	 * 
	 * Checks to make sure all fields contain valid characters and verifies this by attempting
	 * to create a new table in the database. If the table is correctly inserted the window will close.
	 * Otherwise a JOptionPane will show displaying one of the user's errors.
	 */
	private void finish(){
		String t_name = tableNameField.getText();
		if(t_name.contains(" "))
			JOptionPane.showMessageDialog(frame, "Table name cannot contain spaces.");
		else if(t_name.isEmpty())
			JOptionPane.showMessageDialog(frame, "Table name must have a name.");
		else if(database.hasTable(t_name))
			JOptionPane.showMessageDialog(frame, "Table name already in use.");
		else if(cols.isEmpty())
			JOptionPane.showMessageDialog(frame, "Cannot create a table with no columns.");
		else{
			try{
				database.createTable(t_name, cols, primaryKeys, chckbxInnodb.isSelected());
				this.frame.dispose();
			}
			catch(SQLException e){
				JOptionPane.showMessageDialog(frame, e.getMessage());
			}
		}
	}
}
