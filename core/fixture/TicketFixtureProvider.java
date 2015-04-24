package wbs.services.ticket.core.fixture;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.GlobalId;
import wbs.framework.utils.RandomLogic;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.menu.model.MenuItemRec;
import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.services.ticket.core.model.TicketFieldTypeRec;
import wbs.services.ticket.core.model.TicketFieldTypeType;
import wbs.services.ticket.core.model.TicketFieldValueRec;
import wbs.services.ticket.core.model.TicketManagerRec;
import wbs.services.ticket.core.model.TicketNoteRec;
import wbs.services.ticket.core.model.TicketRec;
import wbs.services.ticket.core.model.TicketStateRec;
import wbs.services.ticket.core.model.TicketStateState;
import wbs.services.ticket.core.model.TicketFieldTypeObjectHelper;
import wbs.services.ticket.core.model.TicketFieldValueObjectHelper;
import wbs.services.ticket.core.model.TicketManagerObjectHelper;
import wbs.services.ticket.core.model.TicketNoteObjectHelper;
import wbs.services.ticket.core.model.TicketObjectHelper;
import wbs.services.ticket.core.model.TicketStateObjectHelper;
import wbs.services.ticket.core.model.TicketTemplateObjectHelper;
import wbs.services.ticket.core.model.TicketTemplateRec;

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
	TicketTemplateObjectHelper ticketTemplateHelper;
	
	@Inject
	ObjectTypeObjectHelper objectTypeHelper;
	
	@Inject
	ObjectManager objectManager;
	
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
					
					.setState (
						TicketStateState.submitted)	
					
					.setShowInQueue (
						true)
						
					.setMinimum (
						0)
						
					.setMaximum (
						0)
				
			);
		
		TicketStateRec acceptedState =
			ticketStateHelper.insert (
				new TicketStateRec ()
		
				.setTicketManager (
					ticketManager)
						
				.setName("Accepted")
				
				.setCode (
					"accepted")
				
				.setState (
					TicketStateState.accepted)	
				
				.setShowInQueue (
					true)
					
				.setMinimum (
					0)
					
				.setMaximum (
					0)
			
		);
		
		TicketStateRec pendingState =
				ticketStateHelper.insert (
					new TicketStateRec ()
			
					.setTicketManager (
						ticketManager)
							
					.setName("Pending")
					
					.setCode (
						"pending")
					
					.setState (
						TicketStateState.pending)	
					
					.setShowInQueue (
						true)
						
					.setMinimum (
						0)
						
					.setMaximum (
						0)
				
			);
		
		TicketStateRec solvedState =
				ticketStateHelper.insert (
					new TicketStateRec ()
			
					.setTicketManager (
						ticketManager)
							
					.setName("Solved")
					
					.setCode (
						"solved")
					
					.setState (
						TicketStateState.solved)	
					
					.setShowInQueue (
						true)
						
					.setMinimum (
						0)
						
					.setMaximum (
						0)
				
			);
		
		TicketStateRec closedState =
				ticketStateHelper.insert (
					new TicketStateRec ()
			
					.setTicketManager (
						ticketManager)
							
					.setName("Closed")
					
					.setCode (
						"closed")
					
					.setState (
						TicketStateState.closed)	
					
					.setShowInQueue (
						true)
						
					.setMinimum (
						0)
						
					.setMaximum (
						0)
				
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
					
					.setVisible(true)		
			
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
				
				.setVisible(true)		
		
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
				
				.setVisible(true)		
		
			);
				

		TicketFieldTypeRec chatUserType =
			ticketFieldTypeHelper.insert (
				new TicketFieldTypeRec ()
		
				.setTicketManager (
					ticketManager)
						
				.setName("chat_user")
				
				.setCode (
					"chat_user")
				
				.setType(TicketFieldTypeType.object)
				
				.setObjectType(
					objectTypeHelper.findById (
						objectManager.objectHelperForClass (
							ChatUserRec.class).objectTypeId()))
				
				.setRequired(true)	
				
				.setVisible(true)		
		
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
		
		ticketFieldValueHelper.insert(
			new TicketFieldValueRec ()
					
				.setTicket (
					ticket)
		
				.setTicketFieldType (
					chatUserType)
					
				.setIntegerValue(1)
				
		);
			
		ticketNoteHelper.insert (
			new TicketNoteRec ()

				.setTicket (
					ticket)
		
				.setIndex (
					ticket.getNumNotes ())
	
		);
		
		// ticket template

		for (
			TicketStateState state : TicketStateState.values()
		) {

		TicketTemplateRec template =
			ticketTemplateHelper.insert (
				new TicketTemplateRec ()

				.setTicketManager (
					ticketManager)

				.setCode (
					stringFormat (
						"template_%s",
						state.toString()))

				.setName (
					stringFormat (
						"Template %s",
						state.toString()))

				.setTicketState (
					ticketStateHelper.findByCode (
						ticketManager, state.toString()))

				.setTicketNote (null)

			);
			
			ticketManager.getTemplates().add(template);

		}
		
		ticket
			.setNumNotes (
				ticket.getNumNotes () + 1);
		
	
	}

}
