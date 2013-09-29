package gui;

import java.awt.BorderLayout;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import system.DBAccessor;

/**
 * ShowTable_Panel
 * @author Daniel J Bray
 *
 * This class is a GUI for displaying a table taken from a mySQL database. The table is displayed
 * in a JTable and is simply used for display purposes without modification.
 */
public class ShowTable_Panel {

	protected JFrame frame;
	protected JTable table;
	protected DBAccessor database;

	/**
	 * Create the application.
	 * @param db - Database to be used.
	 */
	public ShowTable_Panel(DBAccessor db) {
		database = db;
		initialize();
		frame.setVisible(true);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	protected void initialize() {
		try{
			//creates the frame
			frame = new JFrame();
			frame.setBounds(100, 100, 735, 491);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			
			//adds a scrollPane 
			JScrollPane scrollPane = new JScrollPane();
			frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

			//Gets the table from the database
			Vector<String[]> data = database.getTableData();
			int dataSize = data.size()==0 ? 1 : data.size();

			//Creates a new 2d array of the table
			String[][] n_data = new String[dataSize][database.getColNames().length];
			for(int i=0; i<dataSize;i++){
				if(data.size() == 0)
					n_data[i] = null; //initializes the table with at lease 1 row if empty
				else
					n_data[i] = data.get(i); //otherwise fills the table with data
			}

			//Gets the names of the columns for the table
			String[] colNames = database.getColNames();

			//constructs the table and adds it to the frame
			table = new JTable();
			table.setModel(new DefaultTableModel(n_data,colNames));
			scrollPane.setViewportView(table);
		}
		catch(SQLException e){
			JOptionPane.showMessageDialog(frame, e.getMessage());
			frame.dispose(); //If an error occurs the frame closes.
		}
	}
}
