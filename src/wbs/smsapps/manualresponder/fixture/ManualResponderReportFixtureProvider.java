package wbs.smsapps.manualresponder.fixture;

import javax.inject.Inject;

import java.util.Date;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.platform.user.model.UserRec;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.queue.model.QueueItemObjectHelper;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueItemState;
import wbs.platform.queue.model.QueueObjectHelper;
import wbs.platform.queue.model.QueueRec;
import wbs.platform.queue.model.QueueSubjectObjectHelper;
import wbs.platform.queue.model.QueueSubjectRec;
import wbs.platform.queue.model.QueueTypeObjectHelper;
import wbs.platform.queue.model.QueueTypeRec;
import wbs.smsapps.manualresponder.model.ManualResponderRec;
import wbs.smsapps.manualresponder.model.ManualResponderObjectHelper;
import wbs.smsapps.manualresponder.model.ManualResponderRequestRec;
import wbs.smsapps.manualresponder.model.ManualResponderRequestObjectHelper;
import wbs.platform.scaffold.model.SliceRec;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.scaffold.model.RootRec;
import wbs.platform.scaffold.model.RootObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.network.model.NetworkObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.sms.message.outbox.model.OutboxObjectHelper;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.affiliate.model.AffiliateObjectHelper;
import wbs.sms.message.batch.model.BatchObjectHelper;
import wbs.sms.message.core.model.MessageTypeRec;
import wbs.sms.message.core.model.MessageTypeObjectHelper;












import org.joda.time.LocalDate;

@PrototypeComponent ("manualResponderReportFixtureProvider")
public
class ManualResponderReportFixtureProvider
	implements FixtureProvider {

	// dependencies
	@Inject
	ManualResponderObjectHelper manualResponderHelper;

	@Inject
	QueueItemObjectHelper queueItemObjectHelper;

	@Inject
	UserObjectHelper userHelper;

	@Inject
	ManualResponderRequestObjectHelper manualResponderRequestHelper;

	@Inject
	SliceObjectHelper sliceHelper;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	RootObjectHelper rootHelper;

	@Inject
	NumberObjectHelper numberHelper;

	@Inject
	NetworkObjectHelper networkHelper;

	@Inject
	TextObjectHelper textHelper;

	@Inject
	RouteObjectHelper routeHelper;

	@Inject
	OutboxObjectHelper outboxHelper;

	@Inject
	BatchObjectHelper batchHelper;

	@Inject
	AffiliateObjectHelper affiliateHelper;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	MessageTypeObjectHelper messageTypeHelper;
	// implementation

	@Inject
	QueueObjectHelper queueHelper;

	@Inject
	QueueTypeObjectHelper queueTypeHelper;

	@Inject
	QueueSubjectObjectHelper queueSubjectHelper;
	@Inject
	QueueItemObjectHelper queueItemHelper;

	@Override
	public
	void createFixtures () {

		RootRec root =
			rootHelper.find (0);


		SliceRec testSlice =
			sliceHelper.findByCode (
				root,
				"test");

		UserRec dumieUser1=
				userHelper.insert (
					new UserRec ()
						.setUsername ("dumieUser1")
						.setPassword ("qUqP5cyxm6YcTAhz05Hph5gvu9M=")
						.setActive (true)
						.setSlice (testSlice));

		/*UserRec dumieUser2=
				userHelper.insert (
					new UserRec ()
						.setUsername ("dumieUser2")
						.setPassword ("qUqP5cyxm6YcTAhz05Hph5gvu9M=")
						.setActive (true)
						.setSlice (testSlice));
						*/

		NetworkRec networkBlue = networkHelper.findByCode (
							root,
							"blue");

		NumberRec numberExample =
				numberHelper.insert (
					new NumberRec()
					.setNumber("34607817033")
					.setNetwork(networkBlue)
					);


		ManualResponderRec manualResponder =
			manualResponderHelper.insert(
				new ManualResponderRec()

					.setSlice(testSlice)
					.setName ("ECAService")
					.setCode("ECAService")
					.setDescription("ECA_Service_Description"));

		/*ManualResponderRec manualResponder2 =
			manualResponderHelper.insert(
				new ManualResponderRec()
					.setSlice(sliceHelper.findByCode (
						GlobalId.root,
						"test"))
					.setName ("JFService")
					.setCode("JFService")
					.setDescription("JFMagazin_Service_Description"));
		*/




		TextRec textExample =
			textHelper.insert(
				new TextRec()
				.setText("EXAMPLE TEXT")
				);

		RouteRec route =
			routeHelper.insert(
				new RouteRec()
				.setSlice(testSlice)
				.setCode("routeText")
				.setName("routeText")
				.setDescription("RouteTextDescription")
				);


		AffiliateRec affiliate =
			affiliateHelper.findByCode (
					GlobalId.root,
					"system");
		MessageTypeRec messageType =
			messageTypeHelper.insert(
				new MessageTypeRec()
					.setCode("NewMessageType")
					.setDescription("NewMessageType")
					);

		MessageRec message =
			messageHelper.insert(
				new MessageRec()
					.setThreadId(1)
					.setText(textExample)
					.setNumFrom("999999999")
					.setNumTo("00000000000")
					.setDirection(MessageDirection.out)
					.setStatus(MessageStatus.pending)
					.setNumber(numberExample)
					.setRoute(route)
					.setService(serviceHelper.findByCode(manualResponder,"default"))
					.setNetwork(networkBlue)
					.setBatch(batchHelper.find(0))   //
					.setAffiliate(affiliate) //
					.setDate(new LocalDate())
					.setMessageType(messageType)
					.setCreatedTime(new Date())
					.setCharge(1)
					);
		QueueRec queue = queueHelper.find(1);

		@SuppressWarnings("unused")
		QueueTypeRec queuetye = this.queueTypeHelper.find(1);

		QueueSubjectRec queueSubject =
				queueSubjectHelper.insert (
					new QueueSubjectRec ()
						.setQueue (queue)
						.setObjectId (127));

		QueueItemRec queueItem =
				queueItemHelper.insert (
					new QueueItemRec ()

						.setQueueSubject (queueSubject)
						.setIndex (queueSubject.getTotalItems ())

						.setQueue (queue)

						.setSource ("source_exammplee")
						.setDetails ("details_example")
						.setRefObjectId (127)

						.setState (QueueItemState.pending)

						.setCreatedTime (new Date())
						.setPendingTime (new Date())
						.setProcessedUser(dumieUser1));

			// update queue subject

			queueSubject.setTotalItems (
				queueSubject.getTotalItems () + 1);

			queueSubject.setActiveItems (
				queueSubject.getActiveItems () + 1);

		@SuppressWarnings("unused")
		ManualResponderRequestRec manualResponderRequest =
			manualResponderRequestHelper.insert(
				new ManualResponderRequestRec()

					.setManualResponder(manualResponder)
					.setMessage(message)
					.setTimestamp(new Date())
					.setUser(dumieUser1)
					.setPending(true)
					.setNumber(numberExample)
					.setQueueItem(queueItem)
					);


	}

}
