package de.framersoft.maven.destem.restemstats;

import de.framersoft.maven.destem.restemstats.gui.RestemGUI;

/**
 * main class to start the application
 * @author fr4mer
 */
public class App {
	/**
	 * main method
	 * @param args
	 * 		no args used
	 */
	public static void main(String[] args) {
		//just show the gui here
		RestemGUI gui = new RestemGUI();
		gui.setSize(600, 400);
		gui.setLocationRelativeTo(null);
		gui.setVisible(true);
	}
}
