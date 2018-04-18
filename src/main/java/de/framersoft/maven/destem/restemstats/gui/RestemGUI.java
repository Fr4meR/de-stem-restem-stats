package de.framersoft.maven.destem.restemstats.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

public class RestemGUI extends JFrame{
	private static final long serialVersionUID = -7266748948961079730L;
	
	public RestemGUI() {
		setTitle("ReStem Statistiken");
		
		JPanel pane = new JPanel();
		pane.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		
		c.insets = new Insets(5, 5, 5, 5);
		
		//start date
		JLabel lblStartDate = new JLabel("Start (inklusive)");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		pane.add(lblStartDate, c);
		
		UtilDateModel modelStart = new UtilDateModel();
		Properties propStart = new Properties();
		propStart.put("text.today", "Heute");
		propStart.put("text.month", "Monat");
		propStart.put("text.year", "Jahr");
		
		JDatePanelImpl datePanelStart = new JDatePanelImpl(modelStart, propStart);
		JDatePickerImpl dpStartDate = new JDatePickerImpl(datePanelStart, new RestemDatePickerFormatter());
		
		dpStartDate.setPreferredSize(new Dimension(200, 25));
		dpStartDate.setMinimumSize(new Dimension(200, 25));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 0;
		pane.add(dpStartDate, c);
		
		//end date
		JLabel lblEndDate = new JLabel("Ende (exklusive)");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		pane.add(lblEndDate, c);
		
		UtilDateModel modelEnd = new UtilDateModel();
		Properties propEnd = new Properties();
		propEnd.put("text.today", "Heute");
		propEnd.put("text.month", "Monat");
		propEnd.put("text.year", "Jahr");
		
		JDatePanelImpl datePanelEnd = new JDatePanelImpl(modelEnd, propEnd);
		JDatePickerImpl dpEndDate = new JDatePickerImpl(datePanelEnd, new RestemDatePickerFormatter());
		
		dpEndDate.setPreferredSize(new Dimension(200, 25));
		dpEndDate.setMinimumSize(new Dimension(200, 25));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 1;
		pane.add(dpEndDate, c);
		
		//generate button
		JButton btnGenerate = new JButton("Laden...");
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 2;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 2;
		c.weightx = 1.0;
		pane.add(btnGenerate, c);
		
		//progress bar
		JProgressBar progress = new JProgressBar(0, 3);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 3;
		c.gridheight = 1;
		pane.add(progress, c);
		
		//output field
		JTextArea txtOutput = new JTextArea();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 3;
		c.gridheight = 1;
		c.weighty = 1.0;
		pane.add(txtOutput, c);
		
		//copy in steemit format
		JButton btnCopySteemFormat = new JButton("Im Steemit-Format kopieren");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 3;
		c.gridheight = 1;
		c.weighty = 0;
		pane.add(btnCopySteemFormat, c);
		
		add(pane);
	}
}
