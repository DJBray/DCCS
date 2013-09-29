package gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import system.DBAccessor;

/**
 * RemoveData_Panel
 * @author Daniel J Bray
 *
 * A GUI extension to the ShowTable_Panel that allows rows to be removed from the table.
 */
public class RemoveData_Panel extends ShowTable_Panel{

	/**
	 * Initializes components
	 * @param database - the database to be used.
	 */
	public RemoveData_Panel(DBAccessor database) {
		super(database);
	}

	/**
	 * @Override Initialize
	 * 
	 * Overrides the initialize method in ShowTable_Panel to add a remove button.
	 */
	protected void initialize(){
		super.initialize();
		JButton btnConfirm = new JButton();
		btnConfirm.setText("Remove Row");
		btnConfirm.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				removeRow();
			}
		});
		frame.getContentPane().add(btnConfirm, BorderLayout.SOUTH);
	}
	
	/**
	 * removeRow
	 * 
	 * Removes the selected row from the database and the table. If no row is selected no action
	 * is performed.
	 */
	private void removeRow(){
		int rowIndex = table.getSelectedRow();
		if(rowIndex == -1){ //Asserts a row is selected
			return;
		}
		
		TableModel model = table.getModel();
		Vector<String> data = new Vector<String>();
		Vector<String> colNames = new Vector<String>();
		
		for(int i=0; i<table.getColumnCount(); i++){
			data.add((String)model.getValueAt(rowIndex, i)); //finds all the values of each field in the selected row
			colNames.add(table.getColumnName(i));			 //finds the column names of each field
		}
		
		try{
			database.removeRow(colNames, data);				//attempts to remove the row from the database
			((DefaultTableModel)model).removeRow(rowIndex);
		}
		catch(SQLException e){
			JOptionPane.showMessageDialog(frame, e.getMessage());
		}
	}
}
