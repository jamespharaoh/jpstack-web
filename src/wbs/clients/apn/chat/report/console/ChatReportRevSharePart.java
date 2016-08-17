package wbs.clients.apn.chat.report.console;

import static wbs.framework.utils.etc.OptionalUtils.isNotPresent;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.Interval;
import org.joda.time.LocalDate;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;
import wbs.clients.apn.chat.affiliate.console.ChatAffiliateConsoleHelper;
import wbs.clients.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.clients.apn.chat.bill.console.ChatRouteConsoleHelper;
import wbs.clients.apn.chat.bill.console.ChatUserCreditConsoleHelper;
import wbs.clients.apn.chat.bill.model.ChatRouteNetworkRec;
import wbs.clients.apn.chat.bill.model.ChatRouteRec;
import wbs.clients.apn.chat.bill.model.ChatUserCreditRec;
import wbs.clients.apn.chat.contact.model.ChatMessageObjectHelper;
import wbs.clients.apn.chat.contact.model.ChatMessageRec;
import wbs.clients.apn.chat.contact.model.ChatMessageSearch;
import wbs.clients.apn.chat.core.console.ChatMonthCostConsoleHelper;
import wbs.clients.apn.chat.core.model.ChatMonthCostRec;
import wbs.clients.apn.chat.core.model.ChatObjectHelper;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.scheme.model.ChatSchemeRec;
import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.ChatUserSearch;
import wbs.console.forms.FormField.FormType;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.module.ConsoleManager;
import wbs.console.module.ConsoleModule;
import wbs.console.part.AbstractPagePart;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.hibernate.HibernateDatabase;
import wbs.framework.object.ObjectManager;
import wbs.framework.utils.TextualInterval;
import wbs.platform.affiliate.console.AffiliateConsoleHelper;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.scaffold.model.RootRec;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.sms.message.stats.console.MessageStatsConsoleHelper;
import wbs.sms.message.stats.model.MessageStatsData;
import wbs.sms.message.stats.model.MessageStatsRec;
import wbs.sms.message.stats.model.MessageStatsSearch;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.route.core.model.RouteRec;

@PrototypeComponent ("chatReportRevSharePart")
public
class ChatReportRevSharePart
	extends AbstractPagePart {

	// dependencies

	@Inject
	AffiliateConsoleHelper affiliateHelper;

	@Inject
	ChatAffiliateConsoleHelper chatAffiliateHelper;

	@Inject
	ChatMessageObjectHelper chatMessageHelper;

	@Inject
	ChatObjectHelper chatHelper;

	@Inject
	ChatMonthCostConsoleHelper chatMonthCostHelper;

	@Inject @Named
	ConsoleModule chatReportConsoleModule;

	@Inject
	ChatRouteConsoleHelper chatRouteHelper;

	@Inject
	ChatUserCreditConsoleHelper chatUserCreditHelper;

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	ConsoleManager consoleManager;

	@Inject
	ConsoleRequestContext consoleRequestContext;

	@Inject
	HibernateDatabase database;

	@Inject
	FormFieldLogic formFieldLogic;

	@Inject
	MessageStatsConsoleHelper messageStatsHelper;

	@Inject
	ObjectManager objectManager;

	//@Inject
	//TimeFormatter timeFormatter;

	@Inject
	UserConsoleLogic userConsoleLogic;

	// state

	FormFieldSet searchFields;
	FormFieldSet resultsFields;

	ChatReportRevShareForm form;

	LocalDate startDate;
	LocalDate endDate;

	ChatRec chat;

	Map<AffiliateRec,ChatReportRevShareItem> chatReportsByAffiliate;
	ChatReportRevShareItem totalReport;

	List<ChatReportRevShareItem> chatReportsSorted;

	String outputTypeParam;

	@Override
	public
	void prepare () {

		searchFields =
			chatReportConsoleModule.formFieldSets ().get (
				"monthReportSearch");

		resultsFields =
			chatReportConsoleModule.formFieldSets ().get (
				"simpleReportResults");

		// get search form

		LocalDate today =
			LocalDate.now ();

		form =
			new ChatReportRevShareForm ()

			.month (
				today.toString (
					"YYYY-MM"));

		formFieldLogic.update (
			requestContext,
			searchFields,
			form,
			ImmutableMap.of (),
			"search");

		chat =
			chatHelper.findRequired (
				requestContext.stuffInteger (
					"chatId"));

		totalReport =
			new ChatReportRevShareItem ()

			.setCurrency (
				chat.getCurrency ())

			.setPath (
				"TOTAL");

		startDate =
			LocalDate.parse (
				stringFormat (
					"%s-01",
					form.month ()));

		endDate =
			startDate.plusMonths (1);

		// add stat sources

		addSmsMessages ();
		addCredits ();
		addJoiners ();
		addChatMessages ();

		// sort chat reports

		List<ChatReportRevShareItem> chatReportsTemp =
			new ArrayList<ChatReportRevShareItem> (
				chatReportsByAffiliate.values ());

		Collections.sort (
			chatReportsTemp);

		chatReportsSorted =
			ImmutableList.copyOf (
				chatReportsTemp);

	}

	void addSmsMessages () {

		List<ServiceRec> services =
			objectManager.getChildren (
				chat,
				ServiceRec.class);

		ArrayList<Long> serviceIds =
			new ArrayList<> ();

		for (
			ServiceRec service
				: services
		) {

			serviceIds.add (
				service.getId ());

		}

		List<MessageStatsRec> allMessageStats =
			messageStatsHelper.search (
				new MessageStatsSearch ()

			.serviceIdIn (
				serviceIds)

			.dateAfter (
				startDate)

			.dateBefore (
				endDate)

			.group (
				true)

			.groupByAffiliate (
				true)

			.groupByRoute (
				true)

			.groupByNetwork (
				true)

		);

		// aggregate by affiliate

		chatReportsByAffiliate =
			new HashMap<AffiliateRec,ChatReportRevShareItem> ();

		List<Long> errorRoutes =
			new ArrayList<> ();

		for (
			MessageStatsRec messageStats
				: allMessageStats
		) {

			// find report

			AffiliateRec affiliate =
				messageStats.getMessageStatsId ().getAffiliate ();

			ChatReportRevShareItem currentReport =
				getReport (
					affiliate);

			// find chat route

			RouteRec route =
				messageStats.getMessageStatsId ().getRoute ();

			ChatRouteRec chatRoute =
				chat.getChatRoutes ().get (
					route.getId ());

			if (chatRoute == null) {

				if (! errorRoutes.contains (route.getId ())) {

					errorRoutes.add (
						route.getId ());

					consoleRequestContext.addError (
						stringFormat (
							"Unknown route: %s (%d)",
							route.getCode (),
							route.getId ()));

				}

				continue;

			}

			// find chat route network

			NetworkRec network =
				messageStats.getMessageStatsId ().getNetwork ();

			Optional<ChatRouteNetworkRec> chatRouteNetwork =
				Optional.fromNullable (
					chatRoute.getChatRouteNetworks ().get (
						network.getId ()));

			// collect stats

			MessageStatsData statsValue =
				messageStats.getStats ();

			addToReport (
				currentReport,
				chatRoute,
				chatRouteNetwork,
				statsValue);

			addToReport (
				totalReport,
				chatRoute,
				chatRouteNetwork,
				statsValue);

		}

	}

	void addCredits () {

		List<ChatUserCreditRec> chatUserCredits =
			chatUserCreditHelper.findByTimestamp (
				chat,
				new Interval (
					startDate.toDateTimeAtStartOfDay (),
					endDate.toDateTimeAtStartOfDay ()));

		for (
			ChatUserCreditRec chatUserCredit
				: chatUserCredits
		) {

			ChatUserRec chatUser =
				chatUserCredit.getChatUser ();

			AffiliateRec affiliate =
				chatUserLogic.getAffiliate (
					chatUser);

			ChatReportRevShareItem affiliateReport =
				getReport (
					affiliate);

			addToReport (
				affiliateReport,
				chatUserCredit);

			addToReport (
				totalReport,
				chatUserCredit);

		}

	}

	void addJoiners () {

		List<ChatUserRec> joiners =
			chatUserHelper.search (
				new ChatUserSearch ()

			.chatId (
				chat.getId ())

			.firstJoin (
				TextualInterval.forInterval (
					userConsoleLogic.timezone (),
					startDate,
					endDate))

		);

		for (
			ChatUserRec chatUser
				: joiners
		) {

			AffiliateRec affiliate =
				chatUserLogic.getAffiliate (
					chatUser);

			ChatReportRevShareItem affiliateReport =
				getReport (
					affiliate);

			affiliateReport.setJoiners (
				affiliateReport.getJoiners () + 1);

			totalReport.setJoiners (
				totalReport.getJoiners () + 1);

		}

	}

	void addToReport (
			@NonNull ChatReportRevShareItem report,
			@NonNull ChatRouteRec chatRoute,
			@NonNull Optional<ChatRouteNetworkRec> chatRouteNetwork,
			@NonNull MessageStatsData statsValue) {

		if (
			chatRouteNetwork.isPresent ()
			&& chatRouteNetwork.get ().getOutRev () > 0
			&& statsValue.getOutDelivered () > 0
		) {

			report

				.setOutRev (
					report.getOutRev ()
					+ statsValue.getOutDelivered ()
						* chatRouteNetwork.get ().getOutRev ())

				.setOutRevNum (
					report.getOutRevNum ()
					+ statsValue.getOutDelivered ());

		} else if (
			chatRoute.getOutRev () > 0
			&& statsValue.getOutDelivered () > 0
		) {

			report

				.setOutRev (
					report.getOutRev ()
					+ statsValue.getOutDelivered ()
						* chatRoute.getOutRev ())

				.setOutRevNum (
					report.getOutRevNum ()
					+ statsValue.getOutDelivered ());

		}

		if (
			chatRoute.getInRev () > 0
			&& statsValue.getInTotal () > 0
		) {

			report

				.setInRev (
					report.getInRev ()
					+ statsValue.getInTotal ()
						* chatRoute.getInRev ())

				.setInRevNum (
					report.getInRevNum ()
					+ statsValue.getInTotal ());
		}

		if (
			chatRoute.getSmsCost () > 0
			&& statsValue.getOutTotal () > 0
		) {

			report

				.setSmsCost (
					report.getSmsCost ()
					+ statsValue.getOutTotal ()
						* chatRoute.getSmsCost ())

				.setSmsCostNum (
					report.getSmsCostNum ()
					+ statsValue.getOutTotal ());

		}

		if (
			chatRoute.getMmsCost () > 0
			&& statsValue.getOutTotal () > 0
		) {

			report

				.setMmsCost (
					report.getMmsCost ()
					+ statsValue.getOutTotal ()
						* chatRoute.getMmsCost ())

				.setMmsCostNum (
					report.getMmsCostNum ()
					+ statsValue.getOutTotal ());

		}

	}

	void addToReport (
			@NonNull ChatReportRevShareItem report,
			@NonNull ChatUserCreditRec credit) {

		report

			.setCreditRev (
				report.getCreditRev ()
				+ credit.getBillAmount () * 100);

	}

	ChatReportRevShareItem getReport (
			@NonNull AffiliateRec affiliate) {

		ChatReportRevShareItem existingReport =
			chatReportsByAffiliate.get (
				affiliate);

		if (existingReport != null)
			return existingReport;

		Object affiliateParent =
			objectManager.getParent (
				affiliate);

		ChatReportRevShareItem newReport;

		if (affiliateParent instanceof ChatAffiliateRec) {

			ChatAffiliateRec chatAffiliate =
				(ChatAffiliateRec)
				affiliateParent;

			newReport =
				new ChatReportRevShareItem ()

				.setAffiliate (
					affiliate)

				.setPath (
					objectManager.objectPathMini (
						chatAffiliate,
						chat))

				.setDescription (
					chatAffiliate.getDescription ())

				.setCurrency (
					chat.getCurrency ());

		} else if (affiliateParent instanceof ChatSchemeRec) {

			ChatSchemeRec chatScheme =
				(ChatSchemeRec)
				affiliateParent;

			newReport =
				new ChatReportRevShareItem ()

				.setAffiliate (
					affiliate)

				.setPath (
					objectManager.objectPathMini (
						chatScheme,
						chat))

				.setDescription (
					chatScheme.getDescription ())

				.setCurrency (
					chat.getCurrency ());

		} else if (affiliateParent instanceof RootRec) {

			newReport =
				new ChatReportRevShareItem ()

				.setAffiliate (
					affiliate)

				.setPath (
					"system")

				.setDescription (
					"")

				.setCurrency (
					chat.getCurrency ());

		} else {

			throw new RuntimeException ();

		}

		chatReportsByAffiliate.put (
			affiliate,
			newReport);

		return newReport;

	}

	void addChatMessages () {

		Optional<ChatMonthCostRec> chatMonthCostOptional =
			chatMonthCostHelper.findByCode (
				chat,
				form.month ());

		if (
			isNotPresent (
				chatMonthCostOptional)
		) {
			return;
		}

		ChatMonthCostRec chatMonthCost =
			chatMonthCostOptional.get ();

		List<ChatMessageRec> chatMessages =
			chatMessageHelper.search (
				new ChatMessageSearch ()

			.hasSender (
				true)

			.timestampAfter (
				startDate.toDateTimeAtStartOfDay ().toInstant ())

			.timestampBefore (
				startDate.toDateTimeAtStartOfDay ().toInstant ())

		);

		long staffCostPerMessage =
			chatMessages.isEmpty ()
				? 0
				: chatMonthCost.getStaffCost ()
					/ chatMessages.size ();

		for (
			ChatMessageRec chatMessage
				: chatMessages
		) {

			AffiliateRec affiliate =
				chatUserLogic.getAffiliate (
					chatMessage.getToUser ());

			ChatReportRevShareItem affiliateReport =
				getReport (
					affiliate);

			affiliateReport.setStaffCost (
				affiliateReport.getStaffCost () + staffCostPerMessage);

			totalReport.setStaffCost (
				totalReport.getStaffCost () + staffCostPerMessage);

		}

	}

	@Override
	public
	void renderHtmlBodyContent () {

		goSearchForm ();
		goReport ();

	}

	void goSearchForm () {

		printFormat (
			"<form method=\"get\">\n");

		printFormat (
			"<table class=\"details\">\n");

		formFieldLogic.outputFormRows (
			requestContext,
			formatWriter,
			searchFields,
			Optional.absent (),
			form,
			ImmutableMap.of (),
			FormType.search,
			"search");

		printFormat (
			"<tr>\n",
			"<th>Actions</th>\n",
			"<td><input",
			" type=\"submit\"",
			" value=\"search\"",
			"></td>\n",
			"</tr>\n");

		printFormat (
			"</table>\n");

		printFormat (
			"</form>\n");

	}

	void goReport () {

		printFormat (
			"<h2>Stats</h2>\n");

		printFormat (
			"<table class=\"list\">\n");

		// header

		printFormat (
			"<tr>");

		formFieldLogic.outputTableHeadings (
			formatWriter,
			resultsFields);

		printFormat (
			"</tr>\n");

		// row

		for (
			ChatReportRevShareItem chatReport
				: chatReportsSorted
		) {

			printFormat (
				"<tr>\n");

			formFieldLogic.outputTableCellsList (
				formatWriter,
				resultsFields,
				chatReport,
				ImmutableMap.of (),
				true);

			printFormat (
				"</tr>\n");

		}

		printFormat (
			"<tr>\n");

		// total

		printFormat (
			"<tr>\n");
			formFieldLogic.outputTableCellsList (
				formatWriter,
				resultsFields,
				totalReport,
				ImmutableMap.of (),
				true);

		printFormat (
			"</tr>\n");

		printFormat (
			"</table>\n");

	}

	void goRates () {

		printFormat (
			"<h2>Rates</h2>\n");

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"</table>\n");

	}

}
