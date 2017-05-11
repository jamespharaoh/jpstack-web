package wbs.apn.chat.report.console;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenGetAction;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import org.joda.time.Interval;
import org.joda.time.LocalDate;

import wbs.console.forms.context.FormContext;
import wbs.console.forms.context.FormContextBuilder;
import wbs.console.module.ConsoleManager;
import wbs.console.module.ConsoleModule;
import wbs.console.part.AbstractPagePart;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDatabase;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectManager;

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

import wbs.utils.time.TextualInterval;

import wbs.apn.chat.affiliate.console.ChatAffiliateConsoleHelper;
import wbs.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.apn.chat.bill.console.ChatRouteConsoleHelper;
import wbs.apn.chat.bill.console.ChatUserCreditConsoleHelper;
import wbs.apn.chat.bill.model.ChatRouteNetworkRec;
import wbs.apn.chat.bill.model.ChatRouteRec;
import wbs.apn.chat.bill.model.ChatUserCreditRec;
import wbs.apn.chat.contact.model.ChatMessageObjectHelper;
import wbs.apn.chat.contact.model.ChatMessageRec;
import wbs.apn.chat.contact.model.ChatMessageSearch;
import wbs.apn.chat.core.console.ChatConsoleHelper;
import wbs.apn.chat.core.console.ChatMonthCostConsoleHelper;
import wbs.apn.chat.core.model.ChatMonthCostRec;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserSearch;

@PrototypeComponent ("chatReportRevSharePart")
public
class ChatReportRevSharePart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	AffiliateConsoleHelper affiliateHelper;

	@SingletonDependency
	ChatAffiliateConsoleHelper chatAffiliateHelper;

	@SingletonDependency
	ChatMessageObjectHelper chatMessageHelper;

	@SingletonDependency
	ChatConsoleHelper chatHelper;

	@SingletonDependency
	ChatMonthCostConsoleHelper chatMonthCostHelper;

	@SingletonDependency
	@NamedDependency
	ConsoleModule chatReportConsoleModule;

	@SingletonDependency
	ChatRouteConsoleHelper chatRouteHelper;

	@SingletonDependency
	ChatUserCreditConsoleHelper chatUserCreditHelper;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	ConsoleManager consoleManager;

	@SingletonDependency
	ConsoleRequestContext consoleRequestContext;

	@SingletonDependency
	HibernateDatabase database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MessageStatsConsoleHelper messageStatsHelper;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	@NamedDependency ("chatReportRevShareResultsFormContextBuilder")
	FormContextBuilder <ChatReportRevShareItem> resultsFormContextBuilder;

	@SingletonDependency
	@NamedDependency ("chatReportRevShareSearchFormContextBuilder")
	FormContextBuilder <ChatReportRevShareForm> searchFormContextBuilder;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// state

	FormContext <ChatReportRevShareForm> searchFormContext;
	FormContext <ChatReportRevShareItem> resultsFormContext;

	ChatReportRevShareForm searchForm;

	LocalDate startDate;
	LocalDate endDate;

	ChatRec chat;

	Map <AffiliateRec, ChatReportRevShareItem> chatReportsByAffiliate;
	ChatReportRevShareItem totalReport;

	List <ChatReportRevShareItem> chatReportsSorted;

	String outputTypeParam;

	@Override
	public
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			// prepare search form

			searchFormContext =
				searchFormContextBuilder.build (
					transaction,
					emptyMap ());

			searchForm =
				searchFormContext.object ();

			LocalDate today =
				LocalDate.now ();

			searchForm

				.month (
					today.toString (
						"YYYY-MM"))

			;

			searchFormContext.update (
				transaction);

			chat =
				chatHelper.findFromContextRequired (
					transaction);

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
						searchForm.month ()));

			endDate =
				startDate.plusMonths (1);

			// add stat sources

			addSmsMessages (
				transaction);

			addCredits (
				transaction);

			addJoiners (
				transaction);

			addChatMessages (
				transaction);

			// sort chat reports

			List <ChatReportRevShareItem> chatReportsTemp =
				new ArrayList<> (
					chatReportsByAffiliate.values ());

			Collections.sort (
				chatReportsTemp);

			chatReportsSorted =
				ImmutableList.copyOf (
					chatReportsTemp);

			// prepare results form

			resultsFormContext =
				resultsFormContextBuilder.build (
					transaction,
					emptyMap (),
					chatReportsSorted);

		}

	}

	private
	void addSmsMessages (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"addSmsMessages");

		) {

			List <ServiceRec> services =
				objectManager.getChildren (
					transaction,
					chat,
					ServiceRec.class);

			ArrayList <Long> serviceIds =
				new ArrayList<> ();

			for (
				ServiceRec service
					: services
			) {

				serviceIds.add (
					service.getId ());

			}

			List <MessageStatsRec> allMessageStats =
				messageStatsHelper.search (
					transaction,
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
				new HashMap<> ();

			List <Long> errorRoutes =
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
						transaction,
						affiliate);

				// find chat route

				RouteRec route =
					messageStats.getMessageStatsId ().getRoute ();

				ChatRouteRec chatRoute =
					chat.getChatRoutes ().get (
						route.getId ());

				if (chatRoute == null) {

					if (
						! errorRoutes.contains (
							route.getId ())
					) {

						errorRoutes.add (
							route.getId ());

						consoleRequestContext.addErrorFormat (
							"Unknown route: %s (%s)",
							route.getCode (),
							integerToDecimalString (
								route.getId ()));

					}

					continue;

				}

				// find chat route network

				NetworkRec network =
					messageStats.getMessageStatsId ().getNetwork ();

				Optional <ChatRouteNetworkRec> chatRouteNetwork =
					Optional.fromNullable (
						chatRoute.getChatRouteNetworks ().get (
							network.getId ()));

				// collect stats

				MessageStatsData statsValue =
					messageStats.getStats ();

				addToReport (
					transaction,
					currentReport,
					chatRoute,
					chatRouteNetwork,
					statsValue);

				addToReport (
					transaction,
					totalReport,
					chatRoute,
					chatRouteNetwork,
					statsValue);

			}

		}

	}

	private
	void addCredits (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"addCredits");

		) {

			List <ChatUserCreditRec> chatUserCredits =
				chatUserCreditHelper.findByTimestamp (
					transaction,
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
						transaction,
						chatUser);

				ChatReportRevShareItem affiliateReport =
					getReport (
						transaction,
						affiliate);

				addToReport (
					affiliateReport,
					chatUserCredit);

				addToReport (
					totalReport,
					chatUserCredit);

			}

		}

	}

	private
	void addJoiners (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"addJoiners");

		) {

			List <ChatUserRec> joiners =
				chatUserHelper.search (
					transaction,
					new ChatUserSearch ()

				.chatId (
					chat.getId ())

				.firstJoin (
					TextualInterval.forInterval (
						userConsoleLogic.timezone (
							transaction),
						startDate,
						endDate))

			);

			for (
				ChatUserRec chatUser
					: joiners
			) {

				AffiliateRec affiliate =
					chatUserLogic.getAffiliate (
						transaction,
						chatUser);

				ChatReportRevShareItem affiliateReport =
					getReport (
						transaction,
						affiliate);

				affiliateReport.setJoiners (
					affiliateReport.getJoiners () + 1);

				totalReport.setJoiners (
					totalReport.getJoiners () + 1);

			}

		}

	}

	private
	void addToReport (
			@NonNull Transaction parentTransaction,
			@NonNull ChatReportRevShareItem report,
			@NonNull ChatRouteRec chatRoute,
			@NonNull Optional <ChatRouteNetworkRec> chatRouteNetwork,
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
			@NonNull Transaction parentTransaction,
			@NonNull AffiliateRec affiliate) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getReport");

		) {

			ChatReportRevShareItem existingReport =
				chatReportsByAffiliate.get (
					affiliate);

			if (existingReport != null)
				return existingReport;

			Object affiliateParent =
				genericCastUnchecked (
					objectManager.getParentRequired (
						transaction,
						affiliate));

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
							transaction,
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
							transaction,
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

	}

	private
	void addChatMessages (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"addChatMessages");

		) {

			Optional <ChatMonthCostRec> chatMonthCostOptional =
				chatMonthCostHelper.findByCode (
					transaction,
					chat,
					searchForm.month ());

			if (
				optionalIsNotPresent (
					chatMonthCostOptional)
			) {
				return;
			}

			ChatMonthCostRec chatMonthCost =
				chatMonthCostOptional.get ();

			List <ChatMessageRec> chatMessages =
				chatMessageHelper.search (
					transaction,
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
						transaction,
						chatMessage.getToUser ());

				ChatReportRevShareItem affiliateReport =
					getReport (
						transaction,
						affiliate);

				affiliateReport.setStaffCost (
					affiliateReport.getStaffCost () + staffCostPerMessage);

				totalReport.setStaffCost (
					totalReport.getStaffCost () + staffCostPerMessage);

			}

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			goSearchForm (
				transaction);

			goReport (
				transaction);

		}

	}

	private
	void goSearchForm (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goSearchForm");

		) {

			htmlFormOpenGetAction (
				requestContext.resolveLocalUrl (
					"/chatReport.revShare"));

			htmlTableOpenDetails ();

			searchFormContext.outputFormRows (
				transaction);

			htmlTableDetailsRowWriteHtml (
				"Actions",
				stringFormat (
					"<input",
					" type=\"submit\"",
					" value=\"search\"",
					">"));

			htmlTableClose ();

			htmlFormClose ();

		}

	}

	void goReport (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goReport");

		) {

			htmlHeadingTwoWrite (
				"Stats");

			// table open

			htmlTableOpenList ();

			// header

			htmlTableRowOpen ();

			resultsFormContext.outputTableHeadings (
				transaction);

			htmlTableRowClose ();

			// row

			for (
				ChatReportRevShareItem chatReport
					: chatReportsSorted
			) {

				htmlTableRowOpen ();

				resultsFormContext.outputTableCellsList (
					transaction,
					chatReport,
					true);

				htmlTableRowClose ();

			}

			// total

			htmlTableRowOpen ();

			resultsFormContext.outputTableCellsList (
				transaction,
				totalReport,
				true);

			htmlTableRowClose ();

			// table close

			htmlTableClose ();

		}

	}

	void goRates () {

		htmlHeadingTwoWrite (
			"Rates");

		htmlTableOpenList ();

		htmlTableClose ();

	}

}
