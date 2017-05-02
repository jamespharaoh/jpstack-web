package wbs.imchat.console;

import wbs.console.helper.enums.EnumConsoleHelper;

import wbs.framework.component.annotations.SingletonComponent;

import wbs.imchat.model.ImChatCustomerSearch;

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
