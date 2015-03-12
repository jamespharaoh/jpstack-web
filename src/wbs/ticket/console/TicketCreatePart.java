package wbs.ticket.console;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.joda.time.LocalDate;

import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.part.AbstractPagePart;
import wbs.ticket.model.TicketFieldTypeObjectHelper;
import wbs.ticket.model.TicketFieldTypeRec;
import wbs.ticket.model.TicketObjectHelper;



public class TicketCreatePart 
	extends AbstractPagePart {
	
	// dependencies
	
	@Inject
	TicketObjectHelper ticketHelper;
	
	@Inject
	TicketFieldTypeObjectHelper ticketFieldTypeHelper;
	
	@Inject
	TimeFormatter timeFormatter;
	
	// state
	
	List<TicketFieldTypeRec> ticketFieldTypes =
		new ArrayList<TicketFieldTypeRec> ();
	
	String todayDate;
	
	// implementation
	
	@Override
	public
	void prepare () {
	
		ticketFieldTypes = 
			ticketFieldTypeHelper.findAll ();
		
		todayDate =
			timeFormatter.localDateToDateString (
				LocalDate.now ());
	
	}
	
	@Override
	public
	void goHeadStuff () {
	
		super.goHeadStuff ();
	
	}
	
	@Override
	public
	void goBodyStuff () {
	
		printFormat (
			"<h1>Hello World</h1>");
	
	}

}
