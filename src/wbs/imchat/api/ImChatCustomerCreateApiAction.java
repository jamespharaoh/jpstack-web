package wbs.imchat.api;

import static wbs.utils.collection.MapUtils.mapIsNotEmpty;
import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.string.StringUtils.hyphenToUnderscore;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import wbs.api.mvc.ApiAction;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.data.tools.DataFromJson;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.text.model.TextObjectHelper;

import wbs.utils.random.RandomLogic;

import wbs.imchat.model.ImChatCustomerObjectHelper;
import wbs.imchat.model.ImChatCustomerRec;
import wbs.imchat.model.ImChatObjectHelper;
import wbs.imchat.model.ImChatRec;
import wbs.imchat.model.ImChatSessionObjectHelper;
import wbs.imchat.model.ImChatSessionRec;
import wbs.services.messagetemplate.model.MessageTemplateSetObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateSetRec;
import wbs.web.context.RequestContext;
import wbs.web.responder.JsonResponder;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("imChatCustomerCreateApiAction")
public
class ImChatCustomerCreateApiAction
	implements ApiAction {

	// dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ImChatApiLogic imChatApiLogic;

	@SingletonDependency
	ImChatCustomerObjectHelper imChatCustomerHelper;

	@SingletonDependency
	ImChatObjectHelper imChatHelper;

	@SingletonDependency
	ImChatSessionObjectHelper imChatSessionHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MessageTemplateSetObjectHelper messageTemplateSetHelper;

	@SingletonDependency
	RandomLogic randomLogic;

	@SingletonDependency
	RequestContext requestContext;

	@SingletonDependency
	TextObjectHelper textHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <JsonResponder> jsonResponderProvider;

	// implementation

	@Override
	public
	Optional <WebResponder> handle (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"handle");

		) {

			// decode request

			DataFromJson dataFromJson =
				new DataFromJson ();

			JSONObject jsonValue =
				(JSONObject)
				JSONValue.parse (
					requestContext.requestBodyString ());

			ImChatCustomerCreateRequest createRequest =
				dataFromJson.fromJson (
					ImChatCustomerCreateRequest.class,
					jsonValue);

			// lookup objects

			ImChatRec imChat =
				imChatHelper.findRequired (
					transaction,
					parseIntegerRequired (
						requestContext.requestStringRequired (
							"imChatId")));

			MessageTemplateSetRec messageTemplateSet =
				messageTemplateSetHelper.findByCodeRequired (
					transaction,
					imChat.getMessageTemplateDatabase (),
					hyphenToUnderscore (
						createRequest.messagesCode ()));

			// check for existing

			ImChatCustomerRec existingCustomer =
				imChatCustomerHelper.findByEmail (
					transaction,
					imChat,
					createRequest.email ());

			if (existingCustomer != null) {

				return optionalOf (
					imChatApiLogic.failureResponseFormat (
						transaction,
						"email-already-exists",
						"A customer with that email address already exists"));

			}

			// check email looks ok

			Matcher emailMatcher =
				emailPattern.matcher (
					createRequest.email ());

			if (! emailMatcher.matches ()) {

				return optionalOf (
					imChatApiLogic.failureResponseFormat (
						transaction,
						"email-invalid",
						"Please enter a valid email address"));

			}

			// check password looks ok

			if (createRequest.password ().length () < 6) {

				return optionalOf (
					imChatApiLogic.failureResponseFormat (
						transaction,
						"password-invalid",
						"Please enter a longer password"));

			}

			// create new

			ImChatCustomerRec newCustomer =
				imChatCustomerHelper.insert (
					transaction,
					imChatCustomerHelper.createInstance ()

				.setImChat (
					imChat)

				.setCode (
					randomLogic.generateNumericNoZero (8))

				.setEmail (
					createRequest.email ())

				.setPassword (
					createRequest.password ())

				.setMessageTemplateSet (
					messageTemplateSet)

				.setFirstSession (
					transaction.now ())

				.setLastSession (
					transaction.now ())

			);

			// update details

			Map <String, String> detailErrors =
				imChatApiLogic.updateCustomerDetails (
					transaction,
					newCustomer,
					createRequest.details ());

			if (
				mapIsNotEmpty (
					detailErrors)
			) {

				return optionalOf (
					imChatApiLogic.failureResponseFormat (
						transaction,
						"details-invalid",
						"One or more of the details provided are invalid"));

			}

			// create session

			ImChatSessionRec session =
				imChatSessionHelper.insert (
					transaction,
					imChatSessionHelper.createInstance ()

				.setImChatCustomer (
					newCustomer)

				.setSecret (
					randomLogic.generateLowercase (20))

				.setActive (
					true)

				.setStartTime (
					transaction.now ())

				.setUpdateTime (
					transaction.now ())

				.setUserAgentText (
					optionalOrNull (
						textHelper.findOrCreate (
							transaction,
							optionalFromNullable (
								createRequest.userAgent ()))))

				.setIpAddress (
					optionalOrNull (
						requestContext.realIp ()))

			);

			newCustomer

				.setActiveSession (
					session);

			// create response

			ImChatCustomerCreateSuccess successResponse =
				new ImChatCustomerCreateSuccess ()

				.sessionSecret (
					session.getSecret ())

				.customer (
					imChatApiLogic.customerData (
						transaction,
						newCustomer));

			// commit and return

			transaction.commit ();

			return optionalOf (
				jsonResponderProvider.get ()

				.value (
					successResponse)

			);

		}

	}

	final static
	Pattern emailPattern =
		Pattern.compile (
			"[^@]+@[^@]+");

}
