package system;
import gui.Database_Panel;

import java.awt.EventQueue;


public class Program {

	/**
	 * Launch the application
	 * @param args (unused)
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new Database_Panel();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
