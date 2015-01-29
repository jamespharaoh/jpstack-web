package wbs.imchat.core.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Action;
import wbs.framework.web.JsonResponder;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.imchat.core.model.ImChatObjectHelper;
import wbs.imchat.core.model.ImChatPricePointObjectHelper;
import wbs.imchat.core.model.ImChatPricePointRec;
import wbs.imchat.core.model.ImChatRec;
import wbs.platform.currency.logic.CurrencyLogic;

@PrototypeComponent ("imChatPricePointListAction")
public
class ImChatPricePointListAction
	implements Action {

	// dependencies

	@Inject
	CurrencyLogic currencyLogic;

	@Inject
	Database database;

	@Inject
	ImChatObjectHelper imChatHelper;

	@Inject
	ImChatPricePointObjectHelper imChatPricePointHelper;

	@Inject
	RequestContext requestContext;

	// prototype dependencies

	@Inject
	Provider<JsonResponder> jsonResponderProvider;

	// implementation

	@Override
	public
	Responder handle () {

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadOnly ();

		ImChatRec imChat =
			imChatHelper.find (
				Integer.parseInt (
					(String)
					requestContext.request (
						"imChatId")));

		// retrieve price points

		List<ImChatPricePointRec> pricePoints =
			imChatPricePointHelper.findByParent (
				imChat);

		Collections.sort (
			pricePoints);

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
				new ImChatPricePointData ()

				.id (
					pricePoint.getId ())

				.name (
					pricePoint.getName ())

				.price (
					currencyLogic.formatText (
						imChat.getCurrency (),
						pricePoint.getPrice ()))

				.value (
					currencyLogic.formatText (
						imChat.getCurrency (),
						pricePoint.getValue ()))

			);

		}

		return jsonResponderProvider.get ()
			.value (pricePointDatas);

	}

}
