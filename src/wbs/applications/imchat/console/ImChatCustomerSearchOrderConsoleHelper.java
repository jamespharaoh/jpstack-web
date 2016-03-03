package wbs.applications.imchat.console;

import wbs.applications.imchat.model.ImChatCustomerSearch;
import wbs.console.helper.EnumConsoleHelper;
import wbs.framework.application.annotations.SingletonComponent;

@SingletonComponent ("imChatCustomerSearchOrderConsoleHelper")

public
class ImChatCustomerSearchOrderConsoleHelper
	extends EnumConsoleHelper<ImChatCustomerSearch.Order> {

	{

		enumClass (
			ImChatCustomerSearch.Order.class);

		add (
			ImChatCustomerSearch.Order.timestampDesc,
			"most recent");

		add (
			ImChatCustomerSearch.Order.totalPurchaseDesc,
			"highest purchase");

		add (
			ImChatCustomerSearch.Order.balanceDesc,
			"highest balance");

	}

}
