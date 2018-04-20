package de.framersoft.maven.destem.restemstats.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.StringJoiner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import de.framersoft.maven.destem.restemstats.core.RestemStats;
import de.framersoft.maven.destem.restemstats.core.RestemStatsEventListener;
import de.framersoft.maven.destem.restemstats.core.RestemStatsResult;
import eu.bittrade.libs.steemj.SteemJ;
import eu.bittrade.libs.steemj.exceptions.SteemCommunicationException;
import eu.bittrade.libs.steemj.exceptions.SteemResponseException;

public class RestemGUI extends JFrame{
	private static final long serialVersionUID = -7266748948961079730L;
	
	private JDatePickerImpl dpStartDate;
	private JDatePickerImpl dpEndDate;
	
	JTextArea txtOutput;
	
	private JButton btnGenerate;
	private JButton btnCopySteemFormat;
	
	private JProgressBar progress;
	
	private String restemStatsSteemitFormat;
	 
	
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
		dpStartDate = new JDatePickerImpl(datePanelStart, new RestemDatePickerFormatter());
		dpStartDate.setPreferredSize(new Dimension(200, 25));
		dpStartDate.setMinimumSize(new Dimension(200, 25));
		dpStartDate.getModel().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				datesChanged();
			}
		});
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
		dpEndDate = new JDatePickerImpl(datePanelEnd, new RestemDatePickerFormatter());
		
		dpEndDate.setPreferredSize(new Dimension(200, 25));
		dpEndDate.setMinimumSize(new Dimension(200, 25));
		dpEndDate.getModel().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				datesChanged();
			}
		});
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 1;
		pane.add(dpEndDate, c);
		
		//generate button
		btnGenerate = new JButton("Laden...");
		btnGenerate.setEnabled(false);
		btnGenerate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					loadRestemStats();
				} catch (SteemCommunicationException | SteemResponseException | IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 2;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 2;
		c.weightx = 1.0;
		pane.add(btnGenerate, c);
		
		//progress bar
		progress = new JProgressBar(0, 5);
		progress.setStringPainted(true);
		progress.setString("");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 3;
		c.gridheight = 1;
		pane.add(progress, c);
		
		//output field
		txtOutput = new JTextArea();
		
		JScrollPane scrollTextArea = new JScrollPane(txtOutput);
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 3;
		c.gridheight = 1;
		c.weighty = 1.0;
		pane.add(scrollTextArea, c);
		
		//copy in steemit format
		btnCopySteemFormat = new JButton("Im Steemit-Format kopieren");
		btnCopySteemFormat.setEnabled(false);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 3;
		c.gridheight = 1;
		c.weighty = 0;
		pane.add(btnCopySteemFormat, c);
		
		add(pane);
	}
	
	private void datesChanged() {
		//check if both dates are set and the end date is after the start date
		Date dateStart = getStartDate();
		Date dateEnd = getEndDate(); 
		
		//if both are set and the end date is after the start date
		//activate loading button
		boolean enableButton = dateStart != null && dateEnd != null && dateStart.getTime() < dateEnd.getTime();
		btnGenerate.setEnabled(enableButton);
	}
	
	private Date getStartDate() {
		Date startDate = (Date) dpStartDate.getModel().getValue();
		return startDate;
	}
	
	private Date getEndDate() {
		Date endDate = (Date) dpEndDate.getModel().getValue();
		return endDate;
	}
	
	private void setLoadingStats(boolean loading) {
		if(loading) {
			btnGenerate.setEnabled(false);
			dpStartDate.setEnabled(false);
			dpStartDate.getComponent(1).setEnabled(false);
			dpEndDate.setEnabled(false);
			dpEndDate.getComponent(1).setEnabled(false);
		}
		else {
			btnGenerate.setEnabled(true);
			dpStartDate.getComponent(1).setEnabled(true);
			dpStartDate.setEnabled(true);
			dpEndDate.getComponent(1).setEnabled(true);
			dpEndDate.setEnabled(true);
		}
	}
	
	private void loadRestemStats() throws JsonParseException, JsonMappingException, SteemCommunicationException, SteemResponseException, IOException {
		clearStats();
		
		Date dateStart = getStartDate();
		Date dateEnd = getEndDate();
		
		if(dateStart == null || dateEnd == null) return;
		
		RestemStats stats = new RestemStats(new SteemJ(), dateStart, dateEnd);
		stats.addListener(new RestemStatsEventListener() {
			
			@Override
			public void onVotesLoaded(int deStemVotes, int steemStemVotes) {
				progress.setValue(1);
				progress.setString("Ermittle Restem-Votes...");
			}
			
			@Override
			public void onUniqueAuthorsFound(int uniqueAuthors) {
				progress.setValue(4);
			}
			
			@Override
			public void onResultReady(RestemStatsResult result) {
				progress.setValue(5);
				progress.setString("Fertig");
				displayRestemStats(result);
				setSteemitFormattingText(result, dateStart, dateEnd);
				setLoadingStats(false);
			}
			
			@Override
			public void onError(Exception e) {
				progress.setValue(5);
				progress.setString("Fehler");
				setLoadingStats(false);
				e.printStackTrace();
			}
			
			@Override
			public void onDiscussionsLoaded(int discussions) {
				progress.setValue(3);
				progress.setString("Lade Ermittle Autoren...");
			}

			@Override
			public void onRestemVotesLoaded(int restemVotes) {
				progress.setValue(2);
				progress.setString("Lade Posts...");
			}
		});
		setLoadingStats(true);
		progress.setValue(0);
		progress.setString("Lade Votes...");
		stats.start();
		
		//handle gui elements
		btnGenerate.setEnabled(false);
	}
	
	private void displayRestemStats(RestemStatsResult result) {
		StringJoiner sj = new StringJoiner(System.lineSeparator());
		sj.add("# Posts: " + result.getDiscussions().size());
		sj.add("Ø Posts / Tag: " + Math.round(result.getMeanPostsPerDay() * 10.0) / 10.0);
		sj.add("# Autoren: " + result.getUniqueAuthors().size());
		sj.add("");
		sj.add("Liste der Autoren: ");
		List<String> authors = result.getUniqueAuthors();
		for(String author : authors) {
			sj.add("@" + author);
		}
		txtOutput.setText(sj.toString());
	}
	
	private void setSteemitFormattingText(RestemStatsResult result, Date start, Date end) {		
		//open template file
		File file = new File("data/restemStats.tmpl");
		try(FileInputStream fis = new FileInputStream(file)){
			byte[] data = new byte[(int) file.length()];
			fis.read(data);
			
			restemStatsSteemitFormat = new String(data, "UTF-8");
			
			//replace placeholders
			DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
			
			restemStatsSteemitFormat = restemStatsSteemitFormat.replaceAll("#StartDate#", formatter.format(start));
			restemStatsSteemitFormat = restemStatsSteemitFormat.replaceAll("#EndDate#", formatter.format(end));
			
			restemStatsSteemitFormat = restemStatsSteemitFormat.replaceAll("#NumberOfVotes#", Integer.toString(result.getNumberOfPosts()));
			double meanPostsPerDay = Math.round(result.getMeanPostsPerDay() * 10.0) / 10.0;
			restemStatsSteemitFormat = restemStatsSteemitFormat.replaceAll("#MeanPostsPerDay#", Double.toString(meanPostsPerDay));
			restemStatsSteemitFormat = restemStatsSteemitFormat.replaceAll("#NumberOfAuthors#", Integer.toString(result.getUniqueAuthors().size()));
			
			StringJoiner sj = new StringJoiner(System.lineSeparator());
			List<String> authors = result.getUniqueAuthors();
			for(String author : authors) {
				sj.add("@" + author);
			}
			restemStatsSteemitFormat = restemStatsSteemitFormat.replaceAll("#ListOfAuthors#", sj.toString());
			System.out.println(restemStatsSteemitFormat);
			btnCopySteemFormat.setEnabled(true);
		} catch (Exception e) {
			//ignore
		}
	}
	
	private void clearStats() {
		txtOutput.setText("");
		restemStatsSteemitFormat = null;
		btnCopySteemFormat.setEnabled(false);
	}
}
