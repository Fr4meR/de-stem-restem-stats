package de.framersoft.maven.destem.restemstats.gui;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JFormattedTextField.AbstractFormatter;

public class RestemDatePickerFormatter extends AbstractFormatter {

	private static final long serialVersionUID = -9184079794314043642L;
	private static final String DATE_PATTERN = "dd.MM.yyyy";
	private SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_PATTERN);
	
	@Override
	public Object stringToValue(String arg0) throws ParseException {
		return dateFormatter.parseObject(arg0);
	}

	@Override
	public String valueToString(Object arg0) throws ParseException {
		if(arg0 != null) {
			Calendar cal = (Calendar) arg0;
			return dateFormatter.format(cal.getTime());
		}
		
		return "";
	}

}
