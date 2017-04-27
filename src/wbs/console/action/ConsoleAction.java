package wbs.console.action;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.TypeUtils.classNameSimple;
import static wbs.utils.string.StringUtils.stringFormat;

import javax.inject.Provider;
import javax.validation.ConstraintViolationException;

import lombok.NonNull;

import org.hibernate.exception.LockAcquisitionException;

import wbs.console.misc.ConsoleExceptionHandler;
import wbs.console.misc.ConsoleUserHelper;
import wbs.console.module.ConsoleManager;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.Log4jLogContext;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.web.action.Action;
import wbs.web.responder.Responder;

public abstract
class ConsoleAction
	implements Action {

	private final static
	LogContext logContext =
		Log4jLogContext.forClass (
			ConsoleAction.class);

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

	@SingletonDependency
	protected
	ConsoleRequestContext requestContext;

	// details

	public final static
	int maxTries = 3;

	protected
	Responder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return null;

	}

	// implementation

	protected
	Responder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		return null;

	}

	@Override
	public final
	Responder handle (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"handle");

		) {

			try {

				Responder responder =
					goWithRetry (
						taskLogger);

				if (responder != null)
					return responder;

				Responder backupResponder =
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
	Responder goWithRetry (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
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

	Responder handleException (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Throwable throwable) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"handleException");

		) {

			// if we have no backup page just die

			Responder backupResponder = null;

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

	protected
	Provider <Responder> reusableResponder (
			@NonNull String responderName) {

		return consoleManager.responder (
			responderName,
			true);

	}

	protected
	Responder responder (
			@NonNull String responderName) {

		Provider <Responder> responderProvider =
			consoleManager.responder (
				responderName,
				true);

		return responderProvider.get ();

	}

}
