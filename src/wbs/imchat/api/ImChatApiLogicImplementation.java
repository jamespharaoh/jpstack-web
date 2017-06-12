package wbs.imchat.api;

import static wbs.utils.etc.Misc.doesNotContain;
import static wbs.utils.etc.Misc.lessThan;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.string.StringUtils.camelToHyphen;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringIsEmpty;
import static wbs.utils.string.StringUtils.underscoreToHyphen;
import static wbs.utils.time.TimeUtils.calculateAgeInYears;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Provider;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.currency.logic.CurrencyLogic;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.media.model.ContentRec;
import wbs.platform.media.model.MediaRec;

import wbs.sms.core.logic.DateFinder;

import wbs.utils.time.TimeFormatter;

import wbs.imchat.model.ImChatConversationRec;
import wbs.imchat.model.ImChatCustomerCreditRec;
import wbs.imchat.model.ImChatCustomerDetailTypeRec;
import wbs.imchat.model.ImChatCustomerDetailValueObjectHelper;
import wbs.imchat.model.ImChatCustomerDetailValueRec;
import wbs.imchat.model.ImChatCustomerRec;
import wbs.imchat.model.ImChatMessageRec;
import wbs.imchat.model.ImChatPricePointRec;
import wbs.imchat.model.ImChatProfileRec;
import wbs.imchat.model.ImChatPurchaseRec;
import wbs.imchat.model.ImChatRec;
import wbs.web.responder.JsonResponder;
import wbs.web.responder.WebResponder;

@SingletonComponent ("imChatApiLogic")
public
class ImChatApiLogicImplementation
	implements ImChatApiLogic {

	// singleton dependencies

	@SingletonDependency
	CurrencyLogic currencyLogic;

	@SingletonDependency
	Database database;

	@SingletonDependency
	EventLogic eventLogic;

	@SingletonDependency
	ImChatCustomerDetailValueObjectHelper imChatCustomerDetailValueHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	TimeFormatter timeFormatter;

	@SingletonDependency
	WbsConfig wbsConfig;

	// prototype dependencies

	@PrototypeDependency
	Provider <JsonResponder> jsonResponderProvider;

	// implementation

	@Override
	public
	ImChatPricePointData pricePointData (
			@NonNull Transaction parentTransaction,
			@NonNull ImChatPricePointRec pricePoint) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"pricePointData");

		) {

			ImChatRec imChat =
				pricePoint.getImChat ();

			return new ImChatPricePointData ()

				.code (
					underscoreToHyphen (
						pricePoint.getCode ()))

				.name (
					pricePoint.getPublicName ())

				.description (
					pricePoint.getPublicDescription ())

				.priceString (
					currencyLogic.formatText (
						imChat.getBillingCurrency (),
						pricePoint.getPrice ()))

				.valueString (
					currencyLogic.formatText (
						imChat.getCreditCurrency (),
						pricePoint.getValue ()));

		}

	}

	@Override
	public
	ImChatProfileData profileData (
			@NonNull Transaction parentTransaction,
			@NonNull ImChatProfileRec profile) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"profileData");

		) {

			if (profile.getDeleted ()) {
				throw new RuntimeException ();
			}

			MediaRec image =
				profile.getProfileImage ();

			ContentRec content =
				image.getContent ();

			Long hash =
				Math.abs (
					content.getHash ());

			long resizedWidth = 98;

			long resizedHeight =
				image.getHeight () * resizedWidth / image.getWidth ();

			return new ImChatProfileData ()

				.code (
					underscoreToHyphen (
						profile.getCode ()))

				.name (
					profile.getPublicName ())

				.description (
					profile.getPublicDescription ())

				.descriptionShort (
					profile.getPublicDescriptionShort ())

				.thumbnailImageLink (
					stringFormat (
						"%s",
						wbsConfig.apiUrl (),
						"/im-chat-media/%u",
						integerToDecimalString (
							image.getId ()),
						"/%u",
						integerToDecimalString (
							hash),
						"/thumbnail.jpg"))

				.thumbnailImageWidth (
					resizedWidth)

				.thumbnailImageHeight (
					resizedHeight)

				.miniatureImageLink (
					stringFormat (
						"%s",
						wbsConfig.apiUrl (),
						"/im-chat-media/%u",
						integerToDecimalString (
							image.getId ()),
						"/%u",
						integerToDecimalString (
							hash),
						"/miniature.jpg"))

				.miniatureImageWidth (
					24l)

				.miniatureImageHeight (
					24l);

		}

	}

	@Override
	public
	ImChatCustomerData customerData (
			@NonNull Transaction parentTransaction,
			@NonNull ImChatCustomerRec customer) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"customerData");

		) {

			ImChatRec imChat =
				customer.getImChat ();

			Long requiredBalance =
				customer.getBalance () < imChat.getMessageCost ()
					? imChat.getMessageCost () - customer.getBalance ()
					: 0;

			return new ImChatCustomerData ()

				.code (
					underscoreToHyphen (
						customer.getCode ()))

				.email (
					customer.getEmail ())

				.conditionsAccepted (
					customer.getAcceptedTermsAndConditions ())

				.detailsCompleted (
					customer.getDetailsCompleted ()
					|| ! imChat.getDetailsPageOnFirstLogin ())

				.balance (
					customer.getBalance ())

				.balanceString (
					currencyLogic.formatText (
						imChat.getCreditCurrency (),
						customer.getBalance ()))

				.minimumBalance (
					imChat.getMessageCost ())

				.minimumBalanceString (
					currencyLogic.formatText (
						imChat.getCreditCurrency (),
						imChat.getMessageCost ()))

				.requiredBalance (
					requiredBalance)

				.requiredBalanceString (
					currencyLogic.formatText (
						imChat.getCreditCurrency (),
						requiredBalance))

				.developerMode (
					customer.getDeveloperMode ())

				.details (
					customerDetailData (
						transaction,
						customer));

		}

	}

	@Override
	public
	List <ImChatCustomerDetailData> customerDetailData (
			@NonNull Transaction parentTransaction,
			@NonNull ImChatCustomerRec customer) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"customerDetailData");

		) {

			ImChatRec imChat =
				customer.getImChat ();

			ImmutableList.Builder<ImChatCustomerDetailData> returnBuilder =
				ImmutableList.<ImChatCustomerDetailData>builder ();

			for (
				ImChatCustomerDetailTypeRec customerDetailType
					: imChat.getCustomerDetailTypes ()
			) {

				ImChatCustomerDetailValueRec customerDetailValue =
					customer.getDetails ().get (
						customerDetailType.getId ());

				returnBuilder.add (
					new ImChatCustomerDetailData ()

					.code (
						underscoreToHyphen (
							customerDetailType.getCode ()))

					.label (
						customerDetailType.getLabel ())

					.help (
						customerDetailType.getHelp ())

					.required (
						customerDetailType.getRequired ())

					.requiredLabel (
						customerDetailType.getRequired ()
							? customerDetailType.getRequiredLabel ()
							: "")

					.dataType (
						camelToHyphen (
							customerDetailType.getDataType ().toString ()))

					.minimumAge (
						customerDetailType.getMinimumAge ())

					.value (
						customerDetailValue != null
							? customerDetailValue.getValue ()
							: null)

				);

			}

			return returnBuilder.build ();

		}

	}

	@Override
	public
	List <ImChatCustomerDetailData> createDetailData (
			@NonNull Transaction parentTransaction,
			@NonNull ImChatRec imChat) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createDetailData");

		) {

			return imChat.getCustomerDetailTypes ().stream ()

				.filter (
					ImChatCustomerDetailTypeRec::getWhenCreatingAccount)

				.map (
					detailType ->
						new ImChatCustomerDetailData ()

					.code (
						underscoreToHyphen (
							detailType.getCode ()))

					.label (
						detailType.getLabel ())

					.help (
						detailType.getHelp ())

					.required (
						detailType.getRequired ())

					.requiredLabel (
						detailType.getRequired ()
							? detailType.getRequiredLabel ()
							: "")

					.dataType (
						camelToHyphen (
							detailType.getDataType ().toString ()))

					.minimumAge (
						detailType.getMinimumAge ())

					.value (
						null)

					.requiredErrorTitle (
						detailType.getRequiredErrorTitle ())

					.requiredErrorMessage (
						detailType.getRequiredErrorMessage ())

					.invalidErrorTitle (
						detailType.getInvalidErrorTitle ())

					.invalidErrorMessage (
						detailType.getInvalidErrorMessage ())

					.ageErrorTitle (
						detailType.getAgeErrorTitle ())

					.ageErrorMessage (
						detailType.getAgeErrorMessage ())

				)

				.collect (
					Collectors.toList ());

		}

	}

	@Override
	public
	ImChatConversationData conversationData (
			@NonNull Transaction parentTransaction,
			@NonNull ImChatConversationRec conversation) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"conversationData");

		) {

			return new ImChatConversationData ()

				.index (
					conversation.getIndex ())

				.profile (
					profileData (
						transaction,
						conversation.getImChatProfile ()))

				.replyPending (
					conversation.getPendingReply ());

		}

	}

	@Override
	public
	ImChatMessageData messageData (
			@NonNull Transaction parentTransaction,
			@NonNull ImChatMessageRec message) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"messageData");

		) {

			ImChatConversationRec conversation =
				message.getImChatConversation ();

			ImChatCustomerRec customer =
				conversation.getImChatCustomer ();

			ImChatRec imChat =
				customer.getImChat ();

			return new ImChatMessageData ()

				.index (
					message.getIndex ())

				.sender (
					message.getSenderUser () != null
						? "operator"
						: "customer")

				.messageText (
					message.getMessageText ())

				.timestamp (
					message.getTimestamp ().getMillis ())

				.charge (
					ifNull (
						message.getPrice (),
						0l))

				.chargeString (
					currencyLogic.formatText (
						imChat.getCreditCurrency (),
						ifNull (
							message.getPrice (),
							0l)));

		}

	}

	@Override
	public
	ImChatPurchaseData purchaseData (
			@NonNull Transaction parentTransaction,
			@NonNull ImChatPurchaseRec purchase) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"purchaseData");

		) {

			ImChatCustomerRec customer =
				purchase.getImChatCustomer ();

			ImChatRec imChat =
				customer.getImChat ();

			return new ImChatPurchaseData ()

				.token (
					purchase.getToken ())

				.priceString (
					currencyLogic.formatText (
						imChat.getBillingCurrency (),
						purchase.getPrice ()))

				.valueString (
					currencyLogic.formatText (
						imChat.getCreditCurrency (),
						purchase.getValue ()));

		}

	}

	@Override
	public
	ImChatPurchaseHistoryData purchaseHistoryData (
			@NonNull Transaction parentTransaction,
			@NonNull ImChatPurchaseRec purchase) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"purchaseHistoryData");

		) {

			ImChatCustomerRec customer =
				purchase.getImChatCustomer ();

			ImChatRec imChat =
				customer.getImChat ();

			return new ImChatPurchaseHistoryData ()

				.priceString (
					currencyLogic.formatText (
						imChat.getBillingCurrency (),
						purchase.getPrice ()))

				.valueString (
					currencyLogic.formatText (
						imChat.getCreditCurrency (),
						purchase.getValue ()))

				.timestampString (
					timeFormatter.timestampString (
						timeFormatter.timezone (
							ifNull (
								imChat.getSlice ().getDefaultTimezone (),
								wbsConfig.defaultTimezone ())),
						purchase.getCreatedTime ()));

		}

	}

	@Override
	public
	ImChatPurchaseHistoryData purchaseHistoryData (
			@NonNull Transaction parentTransaction,
			@NonNull ImChatCustomerCreditRec credit) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"purchaseHistoryData");

		) {

			ImChatCustomerRec customer =
				credit.getImChatCustomer ();

			ImChatRec imChat =
				customer.getImChat ();

			return new ImChatPurchaseHistoryData ()

				.priceString (
					currencyLogic.formatText (
						imChat.getBillingCurrency (),
						credit.getBillAmount ()))

				.valueString (
					currencyLogic.formatText (
						imChat.getCreditCurrency (),
						credit.getCreditAmount ()))

				.timestampString (
					timeFormatter.timestampString (
						timeFormatter.timezone (
							ifNull (
								imChat.getSlice ().getDefaultTimezone (),
								wbsConfig.defaultTimezone ())),
						credit.getTimestamp ()))

			;

		}

	}

	@Override
	public
	Map <String, String> updateCustomerDetails (
			@NonNull Transaction parentTransaction,
			@NonNull ImChatCustomerRec customer,
			@NonNull Map <String, String> newDetails) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"updateCustomerDetails");

		) {

			ImChatRec imChat =
				customer.getImChat ();

			ImmutableMap.Builder <String, String> returnBuilder =
				ImmutableMap.builder ();

			for (
				ImChatCustomerDetailTypeRec detailType
					: imChat.getCustomerDetailTypes ()
			) {

				if (
					doesNotContain (
						newDetails.keySet (),
						underscoreToHyphen (
							detailType.getCode ()))
				) {
					continue;
				}

				String stringValue =
					newDetails.get (
						underscoreToHyphen (
							detailType.getCode ()));

				switch (detailType.getDataType ()) {

				case text:

					if (

						detailType.getRequired ()

						&& stringIsEmpty (
							stringValue.trim ())

					) {

						returnBuilder.put (
							underscoreToHyphen (
								detailType.getCode ()),
							"required");

						continue;

					}

					break;

				case dateOfBirth:

					if (

						detailType.getRequired ()

						&& stringIsEmpty (
							stringValue.trim ())

					) {

						returnBuilder.put (
							underscoreToHyphen (
								detailType.getCode ()),
							"required");

						continue;

					}

					Optional <LocalDate> dateOfBirthOptional =
						DateFinder.find (
							stringValue,
							1915);

					if (
						optionalIsNotPresent (
							dateOfBirthOptional)
					) {

						returnBuilder.put (
							underscoreToHyphen (
								detailType.getCode ()),
							"invalid");

						continue;

					}

					LocalDate dateOfBirth =
						optionalGetRequired (
							dateOfBirthOptional);

					Long ageInYears =
						calculateAgeInYears (
							dateOfBirth,
							transaction.now (),
							DateTimeZone.forID (
								wbsConfig.defaultTimezone ()));

					if (

						isNotNull (
							detailType.getMinimumAge ())

						&& lessThan (
							ageInYears,
							detailType.getMinimumAge ())

					) {

						returnBuilder.put (
							underscoreToHyphen (
								detailType.getCode ()),
							"below-minimum-age");

						continue;

					}

					break;

				default:

					throw new RuntimeException ("TODO");

				}

				ImChatCustomerDetailValueRec detailValue =
					imChatCustomerDetailValueHelper.insert (
						transaction,
						imChatCustomerDetailValueHelper.createInstance ()

					.setImChatCustomer (
						customer)

					.setImChatCustomerDetailType (
						detailType)

					.setValue (
						stringValue)

				);

				customer.getDetails ().put (
					detailType.getId (),
					detailValue);

				eventLogic.createEvent (
					transaction,
					"im_chat_customer_detail_updated",
					customer,
					detailType,
					stringValue);

			}

			return returnBuilder.build ();

		}

	}

	@Override
	public
	WebResponder failureResponse (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String reason,
			@NonNull String message) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"failureResponse");

		) {

			ImChatFailure failureResponse =
				new ImChatFailure ()

				.reason (
					reason)

				.message (
					message)

			;

			return jsonResponderProvider.get ()

				.value (
					failureResponse)

			;

		}

	}

}
