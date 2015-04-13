package wbs.applications.imchat.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import wbs.applications.imchat.model.ImChatObjectHelper;
import wbs.applications.imchat.model.ImChatPricePointObjectHelper;
import wbs.applications.imchat.model.ImChatPricePointRec;
import wbs.applications.imchat.model.ImChatRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Action;
import wbs.framework.web.JsonResponder;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
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
	ImChatApiLogic imChatApiLogic;

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
			database.beginReadOnly (
				this);

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
				imChatApiLogic.pricePointData (
					pricePoint));

		}

		return jsonResponderProvider.get ()
			.value (pricePointDatas);

	}

}
