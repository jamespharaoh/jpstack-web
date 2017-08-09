package wbs.framework.database;

import lombok.NonNull;

import org.apache.commons.lang3.tuple.Pair;

import wbs.framework.logging.LogContext;

import wbs.utils.etc.SafeCloseable;

public
interface CloseableTransaction
	extends
		SafeCloseable,
		Transaction {

	public static
	CloseableTransaction genericWrapper (
			@NonNull Pair <LogContext, String> logContextAndName,
			@NonNull CloseableTransaction parentTransaction) {

		return parentTransaction.nestTransaction (
			logContextAndName.getLeft (),
			logContextAndName.getRight ());

	}

}
