package wbs.console.action;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.TypeUtils.classNameSimple;
import static wbs.utils.string.StringUtils.stringFormat;

import javax.validation.ConstraintViolationException;

import lombok.NonNull;

import org.hibernate.exception.LockAcquisitionException;

import wbs.console.misc.ConsoleExceptionHandler;
import wbs.console.misc.ConsoleUserHelper;
import wbs.console.module.ConsoleManager;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.mvc.WebAction;
import wbs.web.responder.WebResponder;

public abstract
class ConsoleAction
	implements WebAction {

	// singleton dependencies

	@SingletonDependency
	protected
	ConsoleExceptionHandler consoleExceptionHandler;

	@SingletonDependency
	protected
	ConsoleManager consoleManager;

	@SingletonDependency
	protected
	ConsoleUserHelper consoleUserHelper;

	@SingletonDependency
	protected
	ExceptionLogger exceptionLogger;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	protected
	ConsoleRequestContext requestContext;

	// details

	public final static
	int maxTries = 3;

	protected
	WebResponder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return null;

	}

	// implementation

	protected
	WebResponder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		return null;

	}

	@Override
	public final
	WebResponder handle (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"handle");

		) {

			try {

				WebResponder responder =
					goWithRetry (
						taskLogger);

				if (responder != null)
					return responder;

				WebResponder backupResponder =
					backupResponder (
						taskLogger);

				if (backupResponder == null) {

					throw new RuntimeException (
						stringFormat (
							"%s.backupResponder () returned null",
							getClass ().getSimpleName ()));

				}

				return backupResponder;

			} catch (Exception exception) {

				return handleException (
					taskLogger,
					exception);

			}

		}

	}

	private
	WebResponder goWithRetry (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"goWithRetry");

		) {

			int triesRemaining =
				maxTries;

			while (triesRemaining > 1) {

				Exception caught;

				// all but last try with catch

				try {

					return goReal (
						taskLogger);

				} catch (ConstraintViolationException exception) {

					caught =
						exception;

				} catch (LockAcquisitionException exception) {

					caught =
						exception;

				}

				// caught an exception, log it and try again

				triesRemaining --;

				taskLogger.warningFormat (
					"%s: caught %s, retrying, %s remaining",
					classNameSimple (
						getClass ()),
					classNameSimple (
						caught.getClass ()),
					integerToDecimalString (
						triesRemaining));

			}

			// last try without catch

			return goReal (
				taskLogger);

		}

	}

	WebResponder handleException (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Throwable throwable) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"handleException");

		) {

			// if we have no backup page just die

			WebResponder backupResponder = null;

			try {

				backupResponder =
					backupResponder (
						taskLogger);

			} catch (Exception exceptionFromBackupResponder) {

				exceptionLogger.logThrowable (
					taskLogger,
					"console",
					requestContext.requestPath (),
					exceptionFromBackupResponder,
					consoleUserHelper.loggedInUserId (),
					GenericExceptionResolution.ignoreWithUserWarning);

			}

			if (backupResponder == null) {

				if (throwable instanceof RuntimeException) {

					throw (RuntimeException)
						throwable;

				}

				throw new RuntimeException (
					throwable);

			}

			// record the exception

			taskLogger.errorFormatException (
				throwable,
				"generated exception: %s",
				requestContext.requestPath ());

			exceptionLogger.logThrowable (
				taskLogger,
				"console",
				requestContext.requestPath (),
				throwable,
				consoleUserHelper.loggedInUserId (),
				GenericExceptionResolution.ignoreWithUserWarning);

			// give the user an error message

			requestContext.addError (
				"Internal error");

			// and go to backup page!

			return backupResponder;

		}

	}

}
