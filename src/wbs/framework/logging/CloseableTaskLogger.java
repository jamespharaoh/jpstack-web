package wbs.framework.logging;

import lombok.NonNull;

import org.apache.commons.lang3.tuple.Pair;

import wbs.utils.etc.SafeCloseable;

public
interface CloseableTaskLogger
	extends
		SafeCloseable,
		TaskLogger {

	@Override
	default
	void close () {

		realTaskLogger ().close ();

	}

	public static
	CloseableTaskLogger genericWrapper (
			@NonNull Pair <LogContext, String> logContextAndName,
			@NonNull CloseableTaskLogger parentTaskLogger) {

		return logContextAndName.getLeft ().nestTaskLogger (
			parentTaskLogger,
			logContextAndName.getRight ());

	}

}
