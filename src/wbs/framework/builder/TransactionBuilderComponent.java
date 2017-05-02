package wbs.framework.builder;

import wbs.framework.database.Transaction;

public
interface TransactionBuilderComponent {

	void build (
			Transaction parentTransaction,
			Builder <Transaction> builder);

}
