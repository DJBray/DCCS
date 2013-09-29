package gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;

import system.DBAccessor;

/**
 * Database_Panel
 * @author Daniel J Bray
 *
 * The Database_Panel is the main GUI for the program. This class also contains the Main method
 * of the program. All other panels can be access from this GUI. All data transfer to the server
 * is belayed to the DBAccessor class.
 */
public class Database_Panel {
	private static final String LBL_INSTRUCTIONS = "Please enter a value for:"; 

	private JDialog dialog;			//Pop-up window for choosing a new table
	private JList<String> list;		//the program log

	//GUI fields
	private JFrame frame;
	private JTextField tableNameField;
	private JTextField insertField;
	private JLabel lblAttribute;
	private JButton btnNext;
	private JButton btnDisplayTable;
	private JLabel lblInstructions;
	private JTextPane txtPane;
	private JButton btnInsertData;
	private JButton btnRemoveData;
	private JButton btnClearLog;
	private JButton btnDropTable;
	private JButton btnBack;
	private JLabel lblType;
	private JLabel lblDataType;
	private JLabel lblNull;
	private JLabel lblNotNull;

	private Vector<String> insertion;	//Row to be inserted into the database (probably should be localized)
	private DBAccessor database;		//Handles all Database insertions/queries/etc.

	/**
	 * Create the application and give default values to a few variables.
	 */
	@SuppressWarnings("serial")
	public Database_Panel() {
		frame = new JFrame(){
			@Override
			public void dispose(){
				super.dispose();
				database.close();
			}
		};

		insertion = new Vector<String>();
		database = new DBAccessor();
		lblAttribute = new JLabel("Attribute");
		tableNameField = new JTextField();
		insertField = new JTextField();
		btnNext = new JButton("Next");

		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		//I apologize, this is messy.

		frame.setBounds(100, 100, 700, 460);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JSplitPane splitPane = new JSplitPane();
		splitPane.setDividerLocation(0.5);
		frame.getContentPane().add(splitPane, BorderLayout.CENTER);

		JPanel panel = new JPanel();
		splitPane.setLeftComponent(panel);
		panel.setLayout(new BorderLayout(0, 0));

		JPanel panel_1 = new JPanel();
		panel.add(panel_1, BorderLayout.NORTH);

		JLabel lblTable = new JLabel("Table");
		panel_1.add(lblTable);

		tableNameField.setEditable(false);
		tableNameField.setText("");      
		panel_1.add(tableNameField);
		tableNameField.setColumns(10);

		JButton btnBrowse = new JButton("Browse...");
		btnBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showFileChooserDialog();
			}
		});
		panel_1.add(btnBrowse);

		JButton btnCreateNewTable = new JButton("Create New Table");
		panel_1.add(btnCreateNewTable);
		btnCreateNewTable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new NewTable_Panel(database);
			}
		});

		JPanel panel_2 = new JPanel();
		panel.add(panel_2, BorderLayout.CENTER);
		panel_2.setLayout(null);

		insertField.setBounds(10, 149, 257, 20);
		insertField.setVisible(false);
		panel_2.add(insertField);
		insertField.setColumns(10);

		btnNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				performInsertion();
			}
		});
		btnNext.setBounds(162, 182, 89, 23);
		btnNext.setVisible(false);
		panel_2.add(btnNext);

		lblAttribute.setBounds(167, 76, 216, 14);
		lblAttribute.setVisible(false);
		panel_2.add(lblAttribute);

		btnDisplayTable = new JButton("Display Table");
		btnDisplayTable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new ShowTable_Panel(database);
			}
		});
		btnDisplayTable.setBounds(10, 353, 114, 23);
		btnDisplayTable.setVisible(false);
		panel_2.add(btnDisplayTable);

		lblInstructions = new JLabel(LBL_INSTRUCTIONS);
		lblInstructions.setVisible(false);
		lblInstructions.setBounds(10, 76, 159, 14);
		panel_2.add(lblInstructions);

		btnClearLog = new JButton("Clear Log");
		btnClearLog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtPane.setText("");
			}
		});
		btnClearLog.setBounds(268, 353, 115, 23);
		panel_2.add(btnClearLog);


		txtPane = new JTextPane();
		txtPane.setText("Select a table");

		JScrollPane sp = new JScrollPane(txtPane);
		splitPane.setRightComponent(sp);

		frame.getRootPane().setDefaultButton(btnNext);

		btnInsertData = new JButton("Insert Data");
		btnInsertData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				database.resetColIndex();
				resetNextPanel();
				updateCompsAfterInsertion();
				txtPane.setText(txtPane.getText() + "\nData ready to be entered.");
				setInsertCompVisible(true);
			}
		});
		btnInsertData.setBounds(10, 11, 114, 23);
		btnInsertData.setVisible(false);
		panel_2.add(btnInsertData);

		btnRemoveData = new JButton("Remove Data");
		btnRemoveData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new RemoveData_Panel(database);
				setInsertCompVisible(false);
			}
		});
		btnRemoveData.setBounds(136, 11, 115, 23);
		btnRemoveData.setVisible(false);
		panel_2.add(btnRemoveData);

		btnDropTable = new JButton("Delete Table");
		btnDropTable.setBounds(278, 10, 105, 25);
		btnDropTable.setVisible(false);
		panel_2.add(btnDropTable);
		btnDropTable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete this table?");
				if(confirm == JOptionPane.YES_OPTION){
					try{
						database.dropTable();
						tableNameField.setText("");
						setInsertCompVisible(false);
						setTableSelectedComponentsVisible(false);
					}
					catch(SQLException ex){
						JOptionPane.showMessageDialog(frame, ex.getMessage());
					}	
				}
			}
		});

		lblType = new JLabel("Data Type:");
		lblType.setBounds(10, 89, 80, 16);
		lblType.setVisible(false);
		panel_2.add(lblType);

		lblDataType = new JLabel("Data Type");
		lblDataType.setBounds(167, 89, 216, 16);
		lblDataType.setVisible(false);
		panel_2.add(lblDataType);

		lblNull = new JLabel("Not Null:");
		lblNull.setBounds(10, 103, 56, 16);
		lblNull.setVisible(false);
		panel_2.add(lblNull);

		lblNotNull = new JLabel("bool");
		lblNotNull.setVisible(false);
		lblNotNull.setBounds(167, 103, 56, 16);
		panel_2.add(lblNotNull);

		btnBack = new JButton("Back");
		btnBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				back();
			}
		});
		btnBack.setBounds(28, 182, 89, 23);
		btnBack.setVisible(false);
		panel_2.add(btnBack);

		frame.setVisible(true);
	}

	/**
	 * back
	 * 
	 * Goes back a column.
	 */
	private void back(){
		try{
			database.prevCol();
			updateCompsAfterInsertion();
			insertField.setText(insertion.get(database.getColIndex()));
			if(database.peekPrevColIndex() == -1){
				btnBack.setVisible(false);
			}
		}
		catch(SQLException e){
			JOptionPane.showMessageDialog(frame, "System error: " + e.getMessage());
		}
	}

	/**
	 * performInsertion
	 * 
	 * Takes the data entered by the user and attempts to add it to the list of items to be inserted into the database.
	 */
	private void performInsertion(){
		//Do nothing if the insertion fields aren't visible.
		if(!btnNext.isVisible())
			return;

		//Get the data type for the current column
		String dataType = lblDataType.getText();
		try{
			//If any of these conversions throw an exception then the user entered the wrong data type.
			if(dataType.contains("INT") || dataType.contains("int")){
				Integer.parseInt(insertField.getText());
			}
			else if(dataType.contains("DOUBLE") || dataType.contains("double")){
				Double.parseDouble(insertField.getText());
			}
			else if(dataType.contains("DATE") || dataType.contains("date")){
				DateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
				format.parse(insertField.getText());
			}

			//Since no exception was thrown we assume the data to be in the correct format and add it to the list
			if(database.getColIndex() < insertion.size()){
				insertion.remove(database.getColIndex());
				insertion.insertElementAt(insertField.getText(), database.getColIndex());
			}
			else
				insertion.add(insertField.getText());

			//If the next column index is -1 then the data is ready to be inserted into the database
			if(database.peekColIndex() == -1){
				performDatabaseInsertionCall();
			}
			//Otherwise increment the column index and update the insertion components to their new values.
			else{
				database.nextCol();
				updateCompsAfterInsertion();
				txtPane.setText(txtPane.getText() + "\nField added.");
				btnBack.setVisible(true);
			}
		}
		catch(Exception e){
			//The user put in the wrong data type for that field.
			JOptionPane.showMessageDialog(frame, "Error: field not added\n" + e.getMessage() + "\nCheck your data type.");
		}
	}

	/**
	 * performDatabaseInsertionCall
	 * 
	 * Takes the items in the insertion list and attempts to insert the data into the database.
	 */
	private void performDatabaseInsertionCall(){
		try{
			database.insert(insertion);
			String out = "| ";
			for(String item: insertion){
				out+= item + " | ";
			}
			txtPane.setText(txtPane.getText() + "\nInserting data:\n" + out +"\nData Inserted\n");
			resetNextPanel();
		}
		catch(SQLException ex){
			JOptionPane.showMessageDialog(frame, ex.getMessage());
			txtPane.setText(txtPane.getText() + "\nInsertion failed.");
		}
	}

	/**
	 * updateCompsAfterInsertion
	 * 
	 * Updates the insertion components' values. Usually called after adding a new
	 * item to the insertion list.
	 */
	private void updateCompsAfterInsertion(){
		try{
			if(database.getColIndex() < insertion.size()){
				insertField.setText(insertion.get(database.getColIndex()));
			}
			else{
				insertField.setText("");
			}

			lblAttribute.setText(database.getCol());
			lblDataType.setText(database.getColDataType());
			lblNotNull.setText(database.getColNotNull().toString());

			//If next index is -1 then we know we have reached last column
			if(database.peekColIndex() == -1){
				btnNext.setText("Confirm");
			}
			else{
				btnNext.setText("Next");
			}
		}
		catch(SQLException e){
			JOptionPane.showMessageDialog(frame, "System error: \n" + e.getMessage() + "\nAborting insertion.");
			setInsertCompVisible(false);
		}
	}

	/**
	 * showFileChooserDialog
	 * 
	 * Invokes a new, custom File chooser dialog that lists all the tables
	 * in the database and allows the user to select one.
	 */
	private void showFileChooserDialog(){
		//initialize frame
		String[] tables = database.getTables();
		dialog = new JDialog(this.frame,"Select a table");    
		dialog.setBounds(100, 100, 306, 191);
		dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		dialog.getContentPane().setLayout(null);
		dialog.setLocationRelativeTo(this.frame);

		//add label
		JLabel lblTable = new JLabel("Table");
		lblTable.setBounds(11, 46, 46, 14);
		dialog.getContentPane().add(lblTable);

		//add selection list
		list = new JList<String>(tables);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setBounds(47, 45, 233, 62);

		//make the list scroll enabled
		JScrollPane sp = new JScrollPane(list);
		sp.setBounds(45, 43, 235, 64);
		dialog.getContentPane().add(sp);

		//gives it a select button
		JButton btnAccept = new JButton("Accept");
		btnAccept.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				tableNameField.setText(list.getSelectedValue());
				if(!tableNameField.getText().isEmpty()){
					try{
						setInsertCompVisible(false);
						database.setTable(tableNameField.getText());
						setTableSelectedComponentsVisible(true);
						txtPane.setText(txtPane.getText() + "\nTable '" 
								+ tableNameField.getText() + "' selected.");
					}
					catch(SQLException e){
						JOptionPane.showMessageDialog(frame, e.getMessage());
					}
				}
				else{
					setInsertCompVisible(false);
					setTableSelectedComponentsVisible(false);
				}
				dialog.dispose();
			}
		});
		btnAccept.setBounds(11, 118, 89, 23);
		dialog.getContentPane().add(btnAccept);

		//gives it a cancel button
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		});
		btnCancel.setBounds(110, 118, 89, 23);
		dialog.getContentPane().add(btnCancel);

		//set the dialog visible
		dialog.setVisible(true);
	}

	/**
	 * setTableSelectedComponentsVisible
	 * 
	 * Simply sets the visibility attribute of components that are invalid
	 * if no table is present. 
	 * 
	 * @param bool - true enables the components as visible
	 * 				 false disables the components' visibility
	 */
	private void setTableSelectedComponentsVisible(boolean bool){
		btnDisplayTable.setVisible(bool);
		btnInsertData.setVisible(bool);
		btnRemoveData.setVisible(bool);
		btnDropTable.setVisible(bool);
	}

	/**
	 * setInsertCompVisible
	 * 
	 * Sets the components' visibility attribute that are associated with inserting 
	 * data into the table
	 * 
	 * @param bool - true enables the components as visible
	 * 				 false disables the components' visibility
	 */
	private void setInsertCompVisible(boolean bool){
		lblInstructions.setVisible(bool);
		insertField.setVisible(bool);
		btnNext.setVisible(bool);
		lblAttribute.setVisible(bool);
		lblType.setVisible(bool);
		lblDataType.setVisible(bool);
		lblNull.setVisible(bool);
		lblNotNull.setVisible(bool);
	}

	/**
	 * resetNextPanel
	 * 
	 * After inserting data into database this method resets the insertion components
	 * such that another tuple can be inserted immediately after.
	 */
	private void resetNextPanel(){
		btnBack.setVisible(false);
		database.resetColIndex();
		insertField.setText("");
		btnNext.setText("Next");
		insertion = new Vector<String>();
		updateCompsAfterInsertion();
		txtPane.setText(txtPane.getText()+"\nInsertion panel reset.");
	}
}
