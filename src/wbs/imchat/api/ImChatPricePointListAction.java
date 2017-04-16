package wbs.imchat.api;

import static wbs.utils.etc.NumberUtils.parseIntegerRequired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Provider;

import lombok.NonNull;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
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

		// begin transaction

		try (

			Transaction transaction =
				database.beginReadOnly (
					"ImChatPricePointListAction.handle ()",
					this);

		) {

			ImChatRec imChat =
				imChatHelper.findRequired (
					parseIntegerRequired (
						requestContext.requestStringRequired (
							"imChatId")));

			// retrieve price points

			List<ImChatPricePointRec> pricePoints =
				imChatPricePointHelper.findByParent (
					imChat);

			Collections.sort (
				pricePoints,
				new Comparator<ImChatPricePointRec> () {

				@Override
				public
				int compare (
						ImChatPricePointRec left,
						ImChatPricePointRec right) {

					return new CompareToBuilder ()

						.append (
							left.getOrder (),
							right.getOrder ())

						.append (
							left.getCode (),
							right.getCode ())

						.toComparison ();

				}

			});

			// create response

			List<ImChatPricePointData> pricePointDatas =
				new ArrayList<ImChatPricePointData> ();

			for (
				ImChatPricePointRec pricePoint
					: pricePoints
			) {

				if (pricePoint.getDeleted ())
					continue;

				pricePointDatas.add (
					imChatApiLogic.pricePointData (
						pricePoint));

			}

			return jsonResponderProvider.get ()

				.value (
					pricePointDatas);

		}

	}

}
