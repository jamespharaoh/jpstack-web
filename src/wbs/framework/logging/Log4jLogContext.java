package wbs.framework.logging;

import lombok.NonNull;

import org.apache.log4j.Logger;

public
class Log4jLogContext
	implements LogContext {

	private
	Logger logger;

	public
	Log4jLogContext (
			@NonNull Logger logger) {

		this.logger =
			logger;

	}

	@Override
	public
	TaskLogger createTaskLogger () {

		return new TaskLogger (
			logger);

	}

	public static
	LogContext forClass (
			@NonNull Class <?> contextClass) {

		return new Log4jLogContext (
			Logger.getLogger (
				contextClass));

	}

}