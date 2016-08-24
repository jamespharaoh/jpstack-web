package wbs.applications.imchat.api;

import static wbs.framework.utils.etc.NumberUtils.parseIntegerRequired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Provider;

import org.apache.commons.lang3.builder.CompareToBuilder;

import lombok.Cleanup;
import wbs.applications.imchat.model.ImChatObjectHelper;
import wbs.applications.imchat.model.ImChatPricePointObjectHelper;
import wbs.applications.imchat.model.ImChatPricePointRec;
import wbs.applications.imchat.model.ImChatRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.PrototypeDependency;
import wbs.framework.application.annotations.SingletonDependency;
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
	Responder handle () {

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				"ImChatPricePointListAction.handle ()",
				this);

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
