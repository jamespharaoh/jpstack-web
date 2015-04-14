package wbs.ticket.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.framework.utils.RandomLogic;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.menu.model.MenuItemRec;
import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.ticket.model.TicketFieldTypeObjectHelper;
import wbs.ticket.model.TicketFieldTypeRec;
import wbs.ticket.model.TicketFieldTypeType;
import wbs.ticket.model.TicketFieldValueObjectHelper;
import wbs.ticket.model.TicketFieldValueRec;
import wbs.ticket.model.TicketManagerObjectHelper;
import wbs.ticket.model.TicketManagerRec;
import wbs.ticket.model.TicketNoteObjectHelper;
import wbs.ticket.model.TicketNoteRec;
import wbs.ticket.model.TicketObjectHelper;
import wbs.ticket.model.TicketRec;
import wbs.ticket.model.TicketStateObjectHelper;
import wbs.ticket.model.TicketStateRec;
import wbs.ticket.model.TicketStateState;

@PrototypeComponent ("ticketFixtureProvider")
public class TicketFixtureProvider
	implements FixtureProvider {
	
	// dependencies
	
	@Inject
	MenuGroupObjectHelper menuGroupHelper;
	
	@Inject
	MenuItemObjectHelper menuHelper;
	
	@Inject
	TicketManagerObjectHelper ticketManagerHelper;
	
	@Inject
	TicketObjectHelper ticketHelper;
	
	@Inject
	TicketFieldTypeObjectHelper ticketFieldTypeHelper;
	
	@Inject
	TicketFieldValueObjectHelper ticketFieldValueHelper;
	
	@Inject
	TicketNoteObjectHelper ticketNoteHelper;
	
	@Inject
	TicketStateObjectHelper ticketStateHelper;
	
	@Inject
	ObjectTypeObjectHelper objectTypeHelper;
	
	@Inject
	SliceObjectHelper sliceHelper;
	
	@Inject
	RandomLogic randomLogic;
	
	// implementation
	
	@Override
	public
	void createFixtures () {
	
		menuHelper.insert (
			new MenuItemRec ()
	
			.setMenuGroup (
				menuGroupHelper.findByCode (
					GlobalId.root,
					"test",
					"facility"))
	
			.setCode (
				"ticket_manager")
				
			.setName (
				"Ticket Manager Chat")

			.setDescription (
				"Ticket manager description")
	
			.setLabel (
				"Ticket Manager")
	
			.setTargetPath (
				"/ticketManagers")
				
			.setLabel (
				"Ticket Manager")

			.setTargetFrame (
				"main")
	
		);
	
		TicketManagerRec ticketManager =
			ticketManagerHelper.insert (
				new TicketManagerRec ()
	
			.setSlice (
				sliceHelper.findByCode (
					GlobalId.root,
					"test"))
	
			.setCode (
				"ticket_manager")
			
			.setName (
				"My ticket manager")
			
			.setDescription (
				"Ticket manager description")
	
		);
		
		TicketStateRec submittedState =
			ticketStateHelper.insert (
				new TicketStateRec ()
		
				.setTicketManager (
					ticketManager)
						
				.setName("Submitted")
				
				.setCode (
					"submitted")
				
				.setState(TicketStateState.submitted)	
			
		);
				
		TicketRec ticket =
			ticketHelper.insert (
				new TicketRec ()
	
			.setTicketManager (
				ticketManager)
	
			.setCode (
				randomLogic.generateNumericNoZero (8))
				
			.setTicketState(
				submittedState)
	
		);
		
		TicketFieldTypeRec booleanType =
				ticketFieldTypeHelper.insert (
						new TicketFieldTypeRec ()
			
					.setTicketManager (
						ticketManager)
						
					.setName("Read")
					
					.setCode (
						"read")
					
					.setType(TicketFieldTypeType.bool)
					
					.setRequired(true)			
			
		);	
	
		TicketFieldTypeRec numberType =
			ticketFieldTypeHelper.insert (
					new TicketFieldTypeRec ()
		
				.setTicketManager (
					ticketManager)
						
				.setName("Number")
				
				.setCode (
					"number")
				
				.setType(TicketFieldTypeType.number)
				
				.setRequired(true)			
		
		);
		
		TicketFieldTypeRec stringType =
			ticketFieldTypeHelper.insert (
				new TicketFieldTypeRec ()
		
				.setTicketManager (
					ticketManager)
						
				.setName("Text")
				
				.setCode (
					"text")
				
				.setType(TicketFieldTypeType.string)
				
				.setRequired(true)			
		
			);	
		
		ticketFieldValueHelper.insert(
			new TicketFieldValueRec ()
				
				.setTicket (
					ticket)
		
				.setTicketFieldType (
					numberType)
					
				.setIntegerValue(10)
				
		);
		
		ticketFieldValueHelper.insert(
			new TicketFieldValueRec ()
					
				.setTicket (
					ticket)
		
				.setTicketFieldType (
					stringType)
					
				.setStringValue("Value")
				
		);
		
		ticketFieldValueHelper.insert(
				new TicketFieldValueRec ()
						
					.setTicket (
						ticket)
			
					.setTicketFieldType (
						booleanType)
						
					.setBooleanValue(true)
					
			);
			
		ticketNoteHelper.insert (
			new TicketNoteRec ()

				.setTicket (
					ticket)
		
				.setIndex (
					ticket.getNumNotes ())
	
		);
		
		ticket
			.setNumNotes (
				ticket.getNumNotes () + 1);
		
	
	}

}
