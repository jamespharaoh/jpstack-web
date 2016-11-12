package wbs.framework.logging;

import lombok.NonNull;

public
class DefaultLogContext {

	public static
	LogContext forClass (
			@NonNull Class <?> contextClass) {

		return Log4jLogContext.forClass (
			contextClass);

	}

}
