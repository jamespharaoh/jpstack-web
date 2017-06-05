package wbs.imchat.api;

import static wbs.utils.collection.IterableUtils.iterableFilterToList;
import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.etc.NumberUtils.parseIntegerRequired;

import java.util.Collections;
import java.util.List;

import javax.inject.Provider;

import com.google.common.collect.Ordering;

import lombok.NonNull;

import org.apache.commons.lang3.tuple.Pair;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.currency.logic.CurrencyLogic;

import wbs.imchat.model.ImChatObjectHelper;
import wbs.imchat.model.ImChatPricePointObjectHelper;
import wbs.imchat.model.ImChatPricePointRec;
import wbs.imchat.model.ImChatRec;
import wbs.web.action.Action;
import wbs.web.context.RequestContext;
import wbs.web.responder.JsonResponder;
import wbs.web.responder.Responder;

@PrototypeComponent ("imChatPricePointListAction")
public
class ImChatPricePointListAction
	implements Action {

	// dependencies

	@SingletonDependency
	CurrencyLogic currencyLogic;

	@SingletonDependency
	Database database;

	@SingletonDependency
	ImChatApiLogic imChatApiLogic;

	@SingletonDependency
	ImChatObjectHelper imChatHelper;

	@SingletonDependency
	ImChatPricePointObjectHelper imChatPricePointHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <JsonResponder> jsonResponderProvider;

	// implementation

	@Override
	public
	Responder handle (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnly (
					logContext,
					parentTaskLogger,
					"handle");

		) {

			ImChatRec imChat =
				imChatHelper.findRequired (
					transaction,
					parseIntegerRequired (
						requestContext.requestStringRequired (
							"imChatId")));

			// retrieve price points

			List <ImChatPricePointRec> pricePoints =
				iterableFilterToList (
					pricePoint ->
						! pricePoint.getDeleted (),
					imChatPricePointHelper.findByParent (
						transaction,
						imChat));

			Collections.sort (
				pricePoints,
				Ordering.natural ().onResultOf (
					pricePoint ->
						Pair.of (
							pricePoint.getOrder (),
							pricePoint.getCode ())));

			// create response

			List <ImChatPricePointData> pricePointDatas =
				iterableMapToList (
					pricePoints,
					pricePoint ->
						imChatApiLogic.pricePointData (
							transaction,
							pricePoint));

			return jsonResponderProvider.get ()

				.value (
					pricePointDatas);

		}

	}

}
