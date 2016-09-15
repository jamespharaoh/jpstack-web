package wbs.services.ticket.core.fixture;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.object.ObjectManager;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.services.ticket.core.model.TicketFieldDataType;
import wbs.services.ticket.core.model.TicketFieldTypeObjectHelper;
import wbs.services.ticket.core.model.TicketFieldTypeRec;
import wbs.services.ticket.core.model.TicketFieldValueObjectHelper;
import wbs.services.ticket.core.model.TicketManagerObjectHelper;
import wbs.services.ticket.core.model.TicketManagerRec;
import wbs.services.ticket.core.model.TicketNoteObjectHelper;
import wbs.services.ticket.core.model.TicketObjectHelper;
import wbs.services.ticket.core.model.TicketRec;
import wbs.services.ticket.core.model.TicketStateObjectHelper;
import wbs.services.ticket.core.model.TicketStateRec;
import wbs.services.ticket.core.model.TicketTemplateObjectHelper;
import wbs.utils.random.RandomLogic;

@PrototypeComponent ("ticketFixtureProvider")
public
class TicketFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	MenuItemObjectHelper menuHelper;

	@SingletonDependency
	TicketManagerObjectHelper ticketManagerHelper;

	@SingletonDependency
	TicketObjectHelper ticketHelper;

	@SingletonDependency
	TicketFieldTypeObjectHelper ticketFieldTypeHelper;

	@SingletonDependency
	TicketFieldValueObjectHelper ticketFieldValueHelper;

	@SingletonDependency
	TicketNoteObjectHelper ticketNoteHelper;

	@SingletonDependency
	TicketStateObjectHelper ticketStateHelper;

	@SingletonDependency
	TicketTemplateObjectHelper ticketTemplateHelper;

	@SingletonDependency
	ObjectTypeObjectHelper objectTypeHelper;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	@SingletonDependency
	RandomLogic randomLogic;

	// implementation

	@Override
	public
	void createFixtures () {

		createMenuItems ();

		createTicketManager ();

	}

	private
	void createMenuItems () {

		menuHelper.insert (
			menuHelper.createInstance ()

			.setMenuGroup (
				menuGroupHelper.findByCodeRequired (
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

	}

	private
	void createTicketManager () {

		Transaction transaction =
			database.currentTransaction ();

		TicketManagerRec ticketManager =
			ticketManagerHelper.insert (
				ticketManagerHelper.createInstance ()

			.setSlice (
				sliceHelper.findByCodeRequired (
					GlobalId.root,
					"test"))

			.setCode (
				"ticket_manager")

			.setName (
				"My ticket manager")

			.setDescription (
				"Ticket manager description")

		);

		database.flush ();

		TicketStateRec submittedState =
			ticketStateHelper.insert (
				ticketStateHelper.createInstance ()

			.setTicketManager (
				ticketManager)

			.setCode (
				"submitted")

			.setName (
				"Submitted")

			.setDescription (
				"Submitted")

			.setShowInQueue (
				true)

			.setMinimum (
				0l)

			.setMaximum (
				0l)

			.setPreferredQueueTime (
				15l)

		);

		// accepted state

		ticketStateHelper.insert (
			ticketStateHelper.createInstance ()

			.setTicketManager (
				ticketManager)

			.setCode (
				"accepted")

			.setName (
				"Accepted")

			.setDescription (
				"Accepted")

			.setShowInQueue (
				true)

			.setMinimum (
				60l)

			.setMaximum (
				120l)

			.setPreferredQueueTime (
				15l)

		);

		// pending state

		ticketStateHelper.insert (
			ticketStateHelper.createInstance ()

			.setTicketManager (
				ticketManager)

			.setCode (
				"pending")

			.setName (
				"Pending")

			.setDescription (
				"Pending")

			.setShowInQueue (
				true)

			.setMinimum (
				300l)

			.setMaximum (
				600l)

			.setPreferredQueueTime (
				15l)

		);

		// solved state

		ticketStateHelper.insert (
			ticketStateHelper.createInstance ()

			.setTicketManager (
				ticketManager)

			.setCode (
				"solved")

			.setName (
				"Solved")

			.setDescription (
				"Solved")

			.setShowInQueue (
				true)

			.setMinimum (
				60l)

			.setMaximum (
				120l)

			.setPreferredQueueTime (
				15l)

		);

		// closed state

		ticketStateHelper.insert (
			ticketStateHelper.createInstance ()

			.setTicketManager (
				ticketManager)

			.setCode (
				"closed")

			.setName (
				"Closed")

			.setDescription (
				"Closed")

			.setShowInQueue (
				true)

			.setMinimum (
				0l)

			.setMaximum (
				0l)

			.setPreferredQueueTime (
				15l)

		);

		database.flush ();

		TicketRec ticket =
			ticketHelper.insert (
				ticketHelper.createInstance ()

			.setTicketManager (
				ticketManager)

			.setCode (
				randomLogic.generateNumericNoZero (8))

			.setTicketState (
				submittedState)

			.setTimestamp (
				transaction.now ())

		);

		TicketFieldTypeRec booleanType =
			ticketFieldTypeHelper.insert (
				ticketFieldTypeHelper.createInstance ()

			.setTicketManager (
				ticketManager)

			.setCode (
				"read")

			.setName (
				"Read")

			.setDescription (
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
				ticketFieldTypeHelper.createInstance ()

			.setTicketManager (
				ticketManager)

			.setCode (
				"number")

			.setName (
				"Number")

			.setDescription (
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
				ticketFieldTypeHelper.createInstance ()

			.setTicketManager (
				ticketManager)

			.setCode (
				"text")

			.setName (
				"Text")

			.setDescription (
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
				ticketFieldTypeHelper.createInstance ()

			.setTicketManager (
				ticketManager)

			.setCode (
				"chat_user")

			.setName (
				"Chat user")

			.setDescription (
				"Chat user")

			.setDataType (
				TicketFieldDataType.object)

			.setObjectType (
				objectTypeHelper.findByCodeRequired (
					GlobalId.root,
					"chat_user"))

			.setRequired (
				true)

			.setVisible (
				true)

		);

		ticketFieldValueHelper.insert (
			ticketFieldValueHelper.createInstance ()

			.setTicket (
				ticket)

			.setTicketFieldType (
				numberType)

			.setIntegerValue (
				10l)

		);

		ticketFieldValueHelper.insert (
			ticketFieldValueHelper.createInstance ()

			.setTicket (
				ticket)

			.setTicketFieldType (
				stringType)

			.setStringValue (
				"Value")

		);

		ticketFieldValueHelper.insert (
			ticketFieldValueHelper.createInstance ()

			.setTicket (
				ticket)

			.setTicketFieldType (
				booleanType)

			.setBooleanValue (
				true)

		);

		ticketFieldValueHelper.insert (
			ticketFieldValueHelper.createInstance ()

			.setTicket (
				ticket)

			.setTicketFieldType (
				chatUserType)

			.setIntegerValue (
				1l)

		);

		ticketNoteHelper.insert (
			ticketNoteHelper.createInstance ()

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
			ticketNoteHelper.createInstance ()

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
