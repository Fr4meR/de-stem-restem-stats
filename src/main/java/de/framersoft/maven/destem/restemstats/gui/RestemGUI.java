package de.framersoft.maven.destem.restemstats.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.StringJoiner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
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
import de.framersoft.maven.destem.restemstats.core.RestemVoteValues;
import de.framersoft.maven.destem.restemstats.core.RestemVoteValuesEventListener;
import eu.bittrade.libs.steemj.SteemJ;
import eu.bittrade.libs.steemj.exceptions.SteemCommunicationException;
import eu.bittrade.libs.steemj.exceptions.SteemResponseException;
/**
 * the main gui window
 * @author fr4mer
 */
public class RestemGUI extends JFrame{
	private static final long serialVersionUID = -7266748948961079730L;
	
	private Properties voteValues = new Properties();
	
	private JDatePickerImpl dpStartDate;
	private JDatePickerImpl dpEndDate;
	
	JTextArea txtOutput;
	
	private JButton btnGenerate;
	private JButton btnCopySteemFormat;
	private JButton btnRefreshVoteValues;
	
	private JProgressBar progress;
	
	JTextField txtVoteSmall;
	JTextField txtVoteMedium;
	JTextField txtVoteBig;
	
	/**
	 * constructor
	 * @author fr4mer
	 */
	public RestemGUI() {
		try(InputStream is = new FileInputStream("data/voting-values.prop")){
			voteValues.load(is);
		} catch (IOException e1) {
			//do nothing here: is expected at first start / deleted properties file
		}
		
		setTitle("ReStem Statistiken");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel pane = new JPanel();
		pane.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		
		c.insets = new Insets(5, 5, 5, 5);
		
		//start date label
		JLabel lblStartDate = new JLabel("Start");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.weightx = 0.16;
		pane.add(lblStartDate, c);
		
		//start date date picker
		UtilDateModel modelStart = new UtilDateModel(getLastSaturday());
		Properties propStart = new Properties();
		propStart.put("text.today", "Heute");
		propStart.put("text.month", "Monat");
		propStart.put("text.year", "Jahr");
		
		JDatePanelImpl datePanelStart = new JDatePanelImpl(modelStart, propStart);
		dpStartDate = new JDatePickerImpl(datePanelStart, new RestemDatePickerFormatter());
		dpStartDate.getModel().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				onDatesChanged();
			}
		});
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 3;
		c.weightx = 0.34;
		pane.add(dpStartDate, c);
		
		//end date label
		JLabel lblEndDate = new JLabel("Ende");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 4;
		c.gridy = 0;
		c.gridwidth = 1;
		c.weightx = 0.16;
		pane.add(lblEndDate, c);
		
		//start date date picker
		UtilDateModel modelEnd = new UtilDateModel(getNextFriday());
		Properties propEnd = new Properties();
		propEnd.put("text.today", "Heute");
		propEnd.put("text.month", "Monat");
		propEnd.put("text.year", "Jahr");
		
		JDatePanelImpl datePanelEnd = new JDatePanelImpl(modelEnd, propEnd);
		dpEndDate = new JDatePickerImpl(datePanelEnd, new RestemDatePickerFormatter());
		dpEndDate.getModel().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				onDatesChanged();
			}
		});
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 5;
		c.gridy = 0;
		c.gridwidth = 2;
		c.weightx = 0.34;
		pane.add(dpEndDate, c);
		
		//vote values label
		JLabel lblVoteSmall = new JLabel("Vote-Werte");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		pane.add(lblVoteSmall, c);
		
		//vate value small
		txtVoteSmall = new JTextField();
		txtVoteSmall.setText(voteValues.getProperty("vote_small", "0"));
		txtVoteSmall.setHorizontalAlignment(SwingConstants.RIGHT);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;
		pane.add(txtVoteSmall, c);
		
		//vote value medium		
		txtVoteMedium = new JTextField();
		txtVoteMedium.setText(voteValues.getProperty("vote_medium", "0"));
		txtVoteMedium.setHorizontalAlignment(SwingConstants.RIGHT);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 2;
		c.gridy = 1;
		c.gridwidth = 1;
		pane.add(txtVoteMedium, c);
		
		//vote value big
		txtVoteBig = new JTextField();
		txtVoteBig.setText(voteValues.getProperty("vote_big", "0"));
		txtVoteBig.setHorizontalAlignment(SwingConstants.RIGHT);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 3;
		c.gridy = 1;
		c.gridwidth = 1;
		pane.add(txtVoteBig, c);
		
		//button to refresh vote values
		btnRefreshVoteValues = new JButton("Werte erneuern");
		btnRefreshVoteValues.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					reloadVoteValues();
				} catch (SteemCommunicationException | SteemResponseException e1) {
					e1.printStackTrace();
				}
			}
		});
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 4;
		c.gridy = 1;
		c.gridwidth = 3;
		pane.add(btnRefreshVoteValues, c);
		
		//generate button
		btnGenerate = new JButton("ReStem Statistiken erzeugen");
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
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 7;
		c.gridheight = 2;
		c.weightx = 1.0;
		pane.add(btnGenerate, c);
		
		//progress bar
		progress = new JProgressBar(0, 5);
		progress.setStringPainted(true);
		progress.setString("");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 7;
		c.gridheight = 1;
		pane.add(progress, c);
		
		//output field
		txtOutput = new JTextArea();
		JScrollPane scrollTextArea = new JScrollPane(txtOutput);
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 5;
		c.gridwidth = 7;
		c.gridheight = 1;
		c.weighty = 1.0;
		pane.add(scrollTextArea, c);
		
		//copy in steemit format
		btnCopySteemFormat = new JButton("In Zwischenablage kopieren");
		btnCopySteemFormat.setEnabled(false);
		btnCopySteemFormat.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				StringSelection selection = new StringSelection(txtOutput.getText());
				Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
				clip.setContents(selection, selection);
				
				JOptionPane.showMessageDialog(null, "In Zwischenablage kopiert.");
			}
		});
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 6;
		c.gridwidth = 7;
		c.gridheight = 1;
		c.weighty = 0;
		pane.add(btnCopySteemFormat, c);
		
		add(pane);
		
		//call this here to ensure all enabled/disabled states are set correctly
		onDatesChanged();
	}
	
	/**
	 * method that is called whenever a date changed.
	 * this is used to set the state of the generate button, so it is only
	 * active if we have valid dates.
	 */
	private void onDatesChanged() {
		//check if both dates are set and the end date is after the start date
		Date dateStart = getStartDate();
		Date dateEnd = getEndDate(); 
		
		//if both are set and the end date is after the start date
		//activate loading button
		boolean enableButton = dateStart != null && dateEnd != null && dateStart.getTime() < dateEnd.getTime();
		btnGenerate.setEnabled(enableButton);
	}
	
	/**
	 * @return
	 * 		the start date
	 */
	private Date getStartDate() {
		Date startDate = (Date) dpStartDate.getModel().getValue();
		return startDate;
	}
	
	/**
	 * @return
	 * 		the end date that is used for displaying purposes.
	 */
	private Date getEndDateDisplay() {
		Date endDate = (Date) dpEndDate.getModel().getValue();
		return endDate;
	}
	
	/**
	 * @return
	 * 		the end date that is used for internal computing.
	 */
	private Date getEndDate() {
		Date endDate = (Date) dpEndDate.getModel().getValue();
		if(endDate == null) return null;
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(endDate);
		cal.add(Calendar.DATE, 1);
		
		return cal.getTime();
	}
	
	/**
	 * sets if there is currently come loading process going.
	 * disabled button / textfields accordingly.
	 * @param loading
	 * 		is there some loading process?
	 */
	private void setLoading(boolean loading) {
		if(loading) {
			btnGenerate.setEnabled(false);
			btnRefreshVoteValues.setEnabled(false);
			dpStartDate.setEnabled(false);
			dpStartDate.getComponent(1).setEnabled(false);
			dpEndDate.setEnabled(false);
			dpEndDate.getComponent(1).setEnabled(false);
		}
		else {
			onDatesChanged();
			btnRefreshVoteValues.setEnabled(true);
			dpStartDate.getComponent(1).setEnabled(true);
			dpStartDate.setEnabled(true);
			dpEndDate.getComponent(1).setEnabled(true);
			dpEndDate.setEnabled(true);
		}
	}
	
	/**
	 * loads the restem stats and displays them as finished.
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws SteemCommunicationException
	 * @throws SteemResponseException
	 * @throws IOException
	 */
	private void loadRestemStats() throws JsonParseException, JsonMappingException, SteemCommunicationException, SteemResponseException, IOException {
		clearStats();
		
		saveVoteValues();
		
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
				progress.setString("Ermittle Vote-Werte...");
			}
			
			@Override
			public void onResultReady(RestemStatsResult result) {
				progress.setValue(5);
				progress.setString("Fertig");
				setSteemitFormattingText(result, dateStart, getEndDateDisplay());
				setLoading(false);
			}
			
			@Override
			public void onException(Exception e) {
				progress.setValue(progress.getMaximum());
				progress.setString("Fehler");
				setLoading(false);
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
		setLoading(true);
		progress.setMinimum(0);
		progress.setMaximum(5);
		progress.setValue(0);
		progress.setString("Lade Votes...");
		stats.start();
		
		//handle gui elements
		btnGenerate.setEnabled(false);
	}
	
	/**
	 * sets the restem text
	 * @param result
	 * 		the result of the restemstats calculation
	 * @param start
	 * 		the start date
	 * @param end
	 * 		the end date
	 */
	private void setSteemitFormattingText(RestemStatsResult result, Date start, Date end) {		
		//open template file
		File file = new File("data/restemStats.tmpl");
		try(FileInputStream fis = new FileInputStream(file)){
			byte[] data = new byte[(int) file.length()];
			fis.read(data);
			
			String restemStatsSteemitFormat = new String(data, "UTF-8");
			
			//replace placeholders
			DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
			
			restemStatsSteemitFormat = restemStatsSteemitFormat.replaceAll("#StartDate#", formatter.format(start));
			restemStatsSteemitFormat = restemStatsSteemitFormat.replaceAll("#EndDate#", formatter.format(end));
			
			restemStatsSteemitFormat = restemStatsSteemitFormat.replaceAll("#NumberOfVotes#", Integer.toString(result.getNumberOfPosts()));
			double meanPostsPerDay = Math.round(result.getMeanPostsPerDay() * 10.0) / 10.0;
			restemStatsSteemitFormat = restemStatsSteemitFormat.replaceAll("#MeanPostsPerDay#", Double.toString(meanPostsPerDay));
			restemStatsSteemitFormat = restemStatsSteemitFormat.replaceAll("#NumberOfAuthors#", Integer.toString(result.getUniqueAuthors().size()));
			
			restemStatsSteemitFormat = restemStatsSteemitFormat.replaceAll("#VoteSmall#", txtVoteSmall.getText());
			restemStatsSteemitFormat = restemStatsSteemitFormat.replaceAll("#VoteMedium#", txtVoteMedium.getText());
			restemStatsSteemitFormat = restemStatsSteemitFormat.replaceAll("#VoteBig#", txtVoteBig.getText());
			
			StringJoiner sj = new StringJoiner(System.lineSeparator());
			List<String> authors = result.getUniqueAuthors();
			for(String author : authors) {
				sj.add("@" + author);
			}
			restemStatsSteemitFormat = restemStatsSteemitFormat.replaceAll("#ListOfAuthors#", sj.toString());

			txtOutput.setText(restemStatsSteemitFormat);
			btnCopySteemFormat.setEnabled(true);
		} catch (Exception e) {
			//ignore
		}
	}
	
	/**
	 * restets displayed restem stats
	 */
	private void clearStats() {
		txtOutput.setText("");
		btnCopySteemFormat.setEnabled(false);
	}
	
	/**
	 * reloads the vote values and displays them as finished.
	 */
	private void reloadVoteValues() throws SteemCommunicationException, SteemResponseException {
		RestemVoteValues values = new RestemVoteValues(new SteemJ());
		values.addEventListener(new RestemVoteValuesEventListener() {
			
			@Override
			public void onTotalStepsCalculated(int totalSteps) {
				progress.setMinimum(0);
				progress.setMaximum(totalSteps);
			}
			
			@Override
			public void onStep() {
				int currentValue = progress.getValue() + 1;
				progress.setValue(currentValue);
				progress.setString("Lade Vote-Werte..." + currentValue + " / " + (progress.getMaximum()));
			}
			
			@Override
			public void onFinished(double voteSmall, double voteMedium, double voteBig) {								
				//round and convert to strings
				String small = Integer.toString((int)Math.round(voteSmall));
				String medium = Integer.toString((int)Math.round(voteMedium));
				String big = Integer.toString((int)Math.round(voteBig));
				
				txtVoteSmall.setText(small);
				txtVoteMedium.setText(medium);
				txtVoteBig.setText(big);
				progress.setString("Fertig");
				
				try {
					saveVoteValues();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				setLoading(false);
			}
			
			@Override
			public void onException(Exception e) {
				setLoading(false);
				progress.setString("Fehler");
				progress.setValue(progress.getMaximum());
				e.printStackTrace();
			}
		});
		
		progress.setString("Lade Vote-Werte...");
		progress.setValue(0);
		setLoading(true);
		values.start();
	}
	
	/**
	 * persists the vote values, so they can be reused
	 * @throws IOException
	 */
	private void saveVoteValues() throws IOException {
		voteValues.setProperty("vote_small", txtVoteSmall.getText());
		voteValues.setProperty("vote_medium", txtVoteMedium.getText());
		voteValues.setProperty("vote_big", txtVoteBig.getText());
		
		try(Writer writer = new FileWriter("data/voting-values.prop")){
			voteValues.store(writer, "voting values");
		}
	}
	
	/**
	 * @return
	 * 		the last saturday
	 */
	private Date getLastSaturday() {
		Calendar cal = Calendar.getInstance();
		int daysBack = cal.get(Calendar.DAY_OF_WEEK) * -1;
		cal.add(Calendar.DATE, daysBack);
		return cal.getTime();
	}
	
	/**
	 * @return
	 * 		the next friday
	 */
	private Date getNextFriday() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(getLastSaturday());
		cal.add(Calendar.DATE, 6);
		return cal.getTime();
	}
}
