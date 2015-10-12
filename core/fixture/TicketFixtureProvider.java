package wbs.services.ticket.core.fixture;

import javax.inject.Inject;

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
import wbs.services.ticket.core.model.TicketFieldDataType;
import wbs.services.ticket.core.model.TicketFieldTypeObjectHelper;
import wbs.services.ticket.core.model.TicketFieldTypeRec;
import wbs.services.ticket.core.model.TicketFieldValueObjectHelper;
import wbs.services.ticket.core.model.TicketFieldValueRec;
import wbs.services.ticket.core.model.TicketManagerObjectHelper;
import wbs.services.ticket.core.model.TicketManagerRec;
import wbs.services.ticket.core.model.TicketNoteObjectHelper;
import wbs.services.ticket.core.model.TicketNoteRec;
import wbs.services.ticket.core.model.TicketObjectHelper;
import wbs.services.ticket.core.model.TicketRec;
import wbs.services.ticket.core.model.TicketStateObjectHelper;
import wbs.services.ticket.core.model.TicketStateRec;
import wbs.services.ticket.core.model.TicketTemplateObjectHelper;

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

			.setName (
				"Submitted")

			.setCode (
				"submitted")

			.setShowInQueue (
				true)

			.setMinimum (
				0)

			.setMaximum (
				0)

		);

		// accepted state

		ticketStateHelper.insert (
			new TicketStateRec ()

			.setTicketManager (
				ticketManager)

			.setName("Accepted")

			.setCode (
				"accepted")

			.setShowInQueue (
				true)

			.setMinimum (
				60)

			.setMaximum (
				120)

		);

		// pending state
		ticketStateHelper.insert (
			new TicketStateRec ()

			.setTicketManager (
				ticketManager)

			.setCode (
				"pending")

			.setName (
				"Pending")

			.setShowInQueue (
				true)

			.setMinimum (
				300)

			.setMaximum (
				600)

		);

		// solved state

		ticketStateHelper.insert (
			new TicketStateRec ()

			.setTicketManager (
				ticketManager)

			.setCode (
				"solved")

			.setName (
				"Solved")

			.setShowInQueue (
				true)

			.setMinimum (
				60)

			.setMaximum (
				120)

		);

		// closed state

		ticketStateHelper.insert (
			new TicketStateRec ()

			.setTicketManager (
				ticketManager)

			.setCode (
				"closed")

			.setName (
				"Closed")

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

			.setCode (
				"read")

			.setName (
				"Read")

			.setDataType (
				TicketFieldDataType.bool)

			.setRequired (
				true)

			.setVisible (
				true)

		);

		TicketFieldTypeRec numberType =
			ticketFieldTypeHelper.insert (
				new TicketFieldTypeRec ()

			.setTicketManager (
				ticketManager)

			.setCode (
				"number")

			.setName (
				"Number")

			.setDataType (
				TicketFieldDataType.number)

			.setRequired (
				true)

			.setVisible (
				true)

		);

		TicketFieldTypeRec stringType =
			ticketFieldTypeHelper.insert (
				new TicketFieldTypeRec ()

			.setTicketManager (
				ticketManager)

			.setCode (
				"text")

			.setName (
				"Text")

			.setDataType (
				TicketFieldDataType.string)

			.setRequired (
				true)

			.setVisible (
				true)

		);

		TicketFieldTypeRec chatUserType =
			ticketFieldTypeHelper.insert (
				new TicketFieldTypeRec ()

			.setTicketManager (
				ticketManager)

			.setCode (
				"chat_user")

			.setName (
				"Chat user")

			.setDataType (
				TicketFieldDataType.object)

			.setObjectType (
				objectTypeHelper.findByCode (
					GlobalId.root,
					"chat_user"))

			.setRequired (
				true)

			.setVisible (
				true)

		);

		ticketFieldValueHelper.insert (
			new TicketFieldValueRec ()

			.setTicket (
				ticket)

			.setTicketFieldType (
				numberType)

			.setIntegerValue (
				10)

		);

		ticketFieldValueHelper.insert (
			new TicketFieldValueRec ()

			.setTicket (
				ticket)

			.setTicketFieldType (
				stringType)

			.setStringValue (
				"Value")

		);

		ticketFieldValueHelper.insert(
			new TicketFieldValueRec ()

			.setTicket (
				ticket)

			.setTicketFieldType (
				booleanType)

			.setBooleanValue (
				true)

		);

		ticketFieldValueHelper.insert(
			new TicketFieldValueRec ()

			.setTicket (
				ticket)

			.setTicketFieldType (
				chatUserType)

			.setIntegerValue (
				1)

		);

		ticketNoteHelper.insert (
			new TicketNoteRec ()

			.setTicket (
				ticket)

			.setIndex (
				ticket.getNumNotes ())

			.setNoteText (
				"Ticket note 1 text")

		);

		ticket
			.setNumNotes (
				ticket.getNumNotes () + 1);

		ticketNoteHelper.insert (
			new TicketNoteRec ()

			.setTicket (
				ticket)

			.setIndex (
				ticket.getNumNotes ())

			.setNoteText (
				"Ticket note 2 text")

		);

		ticket

			.setNumNotes (
				ticket.getNumNotes () + 1);

		// ticket template

		/*
		for (
			TicketStateState state
				: TicketStateState.values ()
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
						state.toString ()))

				.setTicketState (
					ticketStateHelper.findByCode (
						ticketManager,
						state.toString ()))

			);

			ticketManager.getTicketTemplates ().add (
				template);

		}
		*/

	}

}
