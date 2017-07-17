package wbs.imchat.api;

import static wbs.utils.collection.IterableUtils.iterableFilterMapToList;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NumberUtils.notEqualToZero;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.Ordering;

import lombok.NonNull;

import wbs.api.mvc.ApiAction;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.data.tools.DataFromJson;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.utils.time.TimeFormatter;

import wbs.imchat.model.ImChatCustomerObjectHelper;
import wbs.imchat.model.ImChatCustomerRec;
import wbs.imchat.model.ImChatPurchaseObjectHelper;
import wbs.imchat.model.ImChatSessionObjectHelper;
import wbs.imchat.model.ImChatSessionRec;
import wbs.web.context.RequestContext;
import wbs.web.responder.JsonResponder;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("imChatPurchaseHistoryApiAction")
public
class ImChatPurchaseHistoryApiAction
	implements ApiAction {

	// dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ImChatApiLogic imChatApiLogic;

	@SingletonDependency
	ImChatCustomerObjectHelper imChatCustomerHelper;

	@SingletonDependency
	ImChatPurchaseObjectHelper imChatPurchaseHelper;

	@SingletonDependency
	ImChatSessionObjectHelper imChatSessionHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	@SingletonDependency
	TimeFormatter timeFormatter;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <JsonResponder> jsonResponderProvider;

	// implementation

	@Override
	public
	Optional <WebResponder> handle (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnly (
					logContext,
					parentTaskLogger,
					"handle");

		) {

			// decode request

			DataFromJson dataFromJson =
				new DataFromJson ();

			ImChatPurchaseHistoryRequest purchaseHistoryRequest =
				dataFromJson.fromJson (
					ImChatPurchaseHistoryRequest.class,
					requestContext.requestBodyString ());

			// lookup session and customer

			ImChatSessionRec session =
				imChatSessionHelper.findBySecret (
					transaction,
					purchaseHistoryRequest.sessionSecret ());

			if (
				session == null
				|| ! session.getActive ()
			) {

				return optionalOf (
					imChatApiLogic.failureResponseFormat (
						transaction,
						"session-invalid",
						"The session secret is invalid or the session is no ",
						"longer active"));

			}

			ImChatCustomerRec customer =
				session.getImChatCustomer ();

			// retrieve purchases

			List <ImChatPurchaseHistoryData> purchaseDatas =
				new ArrayList<> ();

			purchaseDatas.addAll (
				iterableFilterMapToList (
					customer.getPurchases (),
					purchase ->
						isNotNull (
							purchase.getCompletedTime ()),
					purchase ->
						imChatApiLogic.purchaseHistoryData (
							transaction,
							purchase)));

			purchaseDatas.addAll (
				iterableFilterMapToList (
					customer.getCredits (),
					credit ->
						notEqualToZero (
							credit.getBillAmount ()),
					credit ->
						imChatApiLogic.purchaseHistoryData (
							transaction,
							credit)));

			Collections.sort (
				purchaseDatas,
				Ordering.natural ().reverse ().onResultOf (
					ImChatPurchaseHistoryData::timestampString));

			// create response

			ImChatPurchaseHistorySuccess purchaseHistoryResponse =
				new ImChatPurchaseHistorySuccess ()

				.customer (
					imChatApiLogic.customerData (
						transaction,
						customer))

				.purchases (
					purchaseDatas)

			;

			return optionalOf (
				jsonResponderProvider.provide (
					transaction,
					jsonResponder ->
						jsonResponder

				.value (
					purchaseHistoryResponse)

			));

		}

	}

}