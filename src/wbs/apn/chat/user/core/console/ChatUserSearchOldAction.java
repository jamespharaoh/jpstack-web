package wbs.apn.chat.user.core.console;

import static wbs.utils.etc.LogicUtils.parseBooleanTrueFalse;
import static wbs.utils.etc.Misc.toEnum;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalMapRequired;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.string.StringUtils.emptyStringToAbsent;
import static wbs.utils.string.StringUtils.nullIfEmptyString;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.time.TimeUtils.dateToInstantNullSafe;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.Cleanup;
import lombok.NonNull;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.apn.chat.bill.model.ChatUserCreditMode;
import wbs.apn.chat.user.core.model.ChatUserDateMode;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserType;
import wbs.apn.chat.user.core.model.Gender;
import wbs.apn.chat.user.core.model.Orient;
import wbs.console.action.ConsoleAction;
import wbs.console.context.ConsoleContext;
import wbs.console.context.ConsoleContextType;
import wbs.console.module.ConsoleManager;
import wbs.console.param.EnumParamChecker;
import wbs.console.param.FixedParamChecker;
import wbs.console.param.IntegerParamChecker;
import wbs.console.param.ParamChecker;
import wbs.console.param.ParamCheckerSet;
import wbs.console.param.TimestampFromParamChecker;
import wbs.console.param.TimestampToParamChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;
import wbs.utils.etc.NumberUtils;
import wbs.web.responder.Responder;

@PrototypeComponent ("chatUserSearchOldAction")
public
class ChatUserSearchOldAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ChatUserObjectHelper chatUserHelper;

	@SingletonDependency
	ConsoleManager consoleManager;

	@SingletonDependency
	Database database;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// details

	@Override
	protected
	Responder backupResponder () {

		return responder (
			"chatUserSearchOldResponder");

	}

	// implementation

	@Override
	protected
	Responder goReal (
			@NonNull TaskLogger taskLogger) {

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				"ChatUserSearchOldAction.goReal ()",
				this);

		// save session

		requestContext.session (
			"chatUserSearchParams",
			(Serializable)
			requestContext.parameterMapSimple ());

		// check params

		Map<String,Object> params =
			paramChecker.apply (requestContext);

		if (params == null)
			return null;

		// get parameters

		ChatUserType searchType =
			toEnum (
				ChatUserType.class,
				requestContext.parameterRequired (
					"type"));

		String searchCode =
			nullIfEmptyString (
				requestContext.parameterRequired (
					"code"));

		String searchNumber =
			nullIfEmptyString (
				requestContext.parameterRequired (
					"number"));

		boolean searchIncludeDeleted =
			optionalIsPresent (
				requestContext.parameter (
					"includeDeleted"));

		Gender searchGender =
			(Gender)
			params.get ("gender");

		Orient searchOrient =
			(Orient)
			params.get ("orient");

		String searchName =
			nullIfEmptyString (
				requestContext.parameterRequired (
					"name"));

		String searchLocation =
			nullIfEmptyString (
				requestContext.parameterRequired (
					"location"));

		String searchInfo =
			nullIfEmptyString (
				requestContext.parameterRequired (
					"info"));

		Boolean searchPicture =
			optionalOrNull (
				parseBooleanTrueFalse (
					requestContext.parameterRequired (
						"picture")));

		Boolean searchVideo =
			optionalOrNull (
				parseBooleanTrueFalse (
					requestContext.parameterRequired (
						"video")));

		Boolean searchAdultVerified =
			optionalOrNull (
				parseBooleanTrueFalse (
					requestContext.parameterRequired (
						"adultVerified")));

		ChatUserCreditMode searchCreditMode =
			toEnum (
				ChatUserCreditMode.class,
				requestContext.parameterRequired (
					"creditMode"));

		ChatUserDateMode searchDateMode =
			toEnum (
				ChatUserDateMode.class,
				requestContext.parameterRequired (
					"dateMode"));

		Optional <Long> searchOnline =
			optionalMapRequired (
				emptyStringToAbsent (
					requestContext.parameterRequired (
						"online")),
				NumberUtils::parseIntegerRequired);

		String searchOutput =
			nullIfEmptyString (
				requestContext.parameterRequired (
					"output"));

		String searchOrder =
			nullIfEmptyString (
				requestContext.parameterRequired (
					"order"));

		Long searchCreditFailedGte =
			(Long)
			params.get (
				"creditFailedGte");

		Long searchCreditFailedLte =
			(Long)
			params.get (
				"creditFailedLte");

		Long searchCreditNoReportGte =
			(Long)
			params.get (
				"creditNoReportGte");

		Long searchCreditNoReportLte =
			(Long)
			params.get (
				"creditNoReportLte");

		Long searchValueSinceEverGte =
			(Long)
			params.get (
				"valueSinceEverGte");

		Long searchValueSinceEverLte =
			(Long)
			params.get (
				"valueSinceEverLte");

		Instant searchFirstJoinGte =
			dateToInstantNullSafe (
				(Date)
				params.get (
					"firstJoinGte"));

		Instant searchFirstJoinLte =
			dateToInstantNullSafe (
				(Date)
				params.get (
					"firstJoinLte"));

		Long limit =
			(Long) params.get (
				"limit");

		// create basic criteria

		Map<String,Object> searchMap =
			new LinkedHashMap<> ();

		searchMap.put (
			"chatId",
			requestContext.stuffInteger (
				"chatId"));

		// check we are not being stupid

		if (
			stringEqualSafe (
				searchOutput,
				"imageZip")
		) {

			if (Boolean.FALSE.equals (searchPicture)) {

				requestContext.addError (
					stringFormat (
						"Search doesn't make sense, photo download for users ",
						"without a picture?"));

				return null;

			}

			searchPicture = true;

		}

		// assemble search criteria

		if (searchType != null) {

			searchMap.put (
				"type",
				searchType);

		}

		if (searchCode != null) {

			searchMap.put (
				"code",
				searchCode);

		}

		if (searchNumber != null && searchIncludeDeleted) {

			searchMap.put (
				"oldNumber",
				searchNumber);

		}

		if (searchNumber != null && ! searchIncludeDeleted) {

			searchMap.put (
				"number",
				searchNumber);

		}

		if (searchGender != null) {

			searchMap.put (
				"gender",
				searchGender);

		}

		if (searchLocation != null) {

			searchMap.put (
				"locPlace",
				searchLocation);

		}

		if (searchOrient != null) {

			searchMap.put (
				"orient",
				searchOrient);

		}

		if (searchName != null) {

			searchMap.put (
				"nameILike",
				"%" + searchName + "%");

		}

		if (searchInfo != null) {

			searchMap.put (
				"infoILike",
				"%" + searchInfo + "%");

		}

		if (searchPicture != null) {

			searchMap.put (
				"hasImage",
				searchPicture);

		}

		if (searchVideo != null) {

			searchMap.put (
				"hasVideo",
				searchVideo);

		}

		if (searchAdultVerified != null) {

			searchMap.put (
				"adultVerified",
				searchAdultVerified);

		}

		if (searchCreditMode != null) {

			searchMap.put (
				"creditMode",
				searchCreditMode);

		}

		if (searchDateMode != null) {

			searchMap.put (
				"dateMode",
				searchDateMode);

		}

		if (
			optionalIsPresent (
				searchOnline)
		) {

			searchMap.put (
				"onlineAfter",
				transaction.now ().minus (
					Duration.standardSeconds (
						searchOnline.get ())));

		}

		if (searchOrder != null) {

			searchMap.put (
				"orderBy",
				searchOrder);

		}

		if (searchCreditFailedGte != null) {

			searchMap.put (
				"creditFailedGte",
				searchCreditFailedGte);

		}

		if (searchCreditFailedLte != null) {

			searchMap.put (
				"creditFailedLte",
				searchCreditFailedLte);

		}

		if (searchCreditNoReportGte != null) {

			searchMap.put (
				"creditNoReportGte",
				searchCreditNoReportGte);

		}

		if (searchCreditNoReportLte != null) {

			searchMap.put (
				"creditNoReportLte",
				searchCreditNoReportLte);

		}

		if (searchValueSinceEverGte != null) {

			searchMap.put (
				"valueSinceEverGte",
				searchValueSinceEverGte);

		}

		if (searchValueSinceEverGte != null) {

			searchMap.put (
				"valueSinceEverLte",
				searchValueSinceEverLte);

		}

		if (searchFirstJoinGte != null) {

			searchMap.put (
				"firstJoinAfter",
				searchFirstJoinGte);

		}

		if (searchFirstJoinLte != null) {

			searchMap.put (
				"firstJoinBefore",
				searchFirstJoinLte);

		}

		if (limit != null) {

			searchMap.put (
				"limit",
				limit);

		}

		// and search!

		List <Long> chatUserIds =
			chatUserHelper.searchIds (
				searchMap);

		if (chatUserIds.size () == 0) {

			// no users found, back to search page

			requestContext.addError (
				"Search produced no results");

			return null;

		}

		if ("imageZip".equals (searchOutput)) {

			requestContext.request (
				"chatUserSearchResult",
				chatUserIds);

			return responder (
				"chatUserImageZipResponder");

		} else {

			if (chatUserIds.size () == 1) {

				// one user, go straight to details

				Long chatUserId =
					chatUserIds.get (
						0);

				requestContext.addNotice (
					"Found single user");

				ConsoleContextType targetContextType =
					consoleManager.contextType (
						"chatUser:combo",
						true);

				ConsoleContext targetContext =
					consoleManager.relatedContextRequired (
						taskLogger,
						requestContext.consoleContext (),
						targetContextType);

				consoleManager.changeContext (
					taskLogger,
					targetContext,
					"/" + chatUserId);

				return responder (
					"chatUserSummaryResponder");

			} else {

				// more than one user, show results page

				requestContext.addNotice (
					"Found " + chatUserIds.size () + " users");

				requestContext.request (
					"chatUserSearchResult",
					chatUserIds);

				return responder (
					"chatUserSearchOldResultsResponder");

			}

		}

	}

	static
	ParamCheckerSet paramChecker =
		new ParamCheckerSet (
			new ImmutableMap.Builder<String,ParamChecker<?>> ()

		.put (
			"gender",
			new EnumParamChecker<Gender> (
				"Invalid gender",
				false,
				Gender.class))

		.put (
			"orient",
			new EnumParamChecker<Orient> (
				"Invalid orient",
				false,
				Orient.class))

		.put (
			"creditFailedGte",
			new FixedParamChecker (
				"Invalid credit failed amount",
				false,
				2))

		.put (
			"creditFailedLte",
			new FixedParamChecker (
				"Invalid credit failed amount",
				false,
				2))

		.put (
			"creditNoReportGte",
			new FixedParamChecker (
				"Invalid credit no reports amount",
				false,
				2))

		.put (
			"creditNoReportLte",
			new FixedParamChecker (
				"Invalid credit no reports amount",
				false,
				2))

		.put (
			"valueSinceEverGte",
			new FixedParamChecker (
				"Invalid total spent amount",
				false,
				2))

		.put (
			"valueSinceEverLte",
			new FixedParamChecker (
				"Invalid total spent amount",
				false,
				2))

		.put (
			"firstJoinGte",
			new TimestampFromParamChecker (
				"Invalid first join date",
				false))

		.put (
			"firstJoinLte",
			new TimestampToParamChecker (
				"Invalid first join date",
				false))

		.put (
			"limit",
			new IntegerParamChecker (
				"Invalid max results",
				false))

		.build ());

}
