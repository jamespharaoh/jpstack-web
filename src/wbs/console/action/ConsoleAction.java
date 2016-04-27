package wbs.console.action;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.ServletException;
import javax.validation.ConstraintViolationException;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import org.hibernate.exception.LockAcquisitionException;

import wbs.console.misc.ConsoleExceptionHandler;
import wbs.console.misc.ConsoleUserHelper;
import wbs.console.module.ConsoleManager;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.context.ApplicationContext;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.web.Action;
import wbs.framework.web.Responder;

@Log4j
public abstract
class ConsoleAction
	implements Action {

	// dependencies

	@Inject
	protected
	ApplicationContext applicationContext;

	@Inject
	protected
	ConsoleExceptionHandler consoleExceptionHandler;

	@Inject
	protected
	ConsoleManager consoleManager;

	@Inject
	protected
	ConsoleUserHelper consoleUserHelper;

	@Inject
	protected
	ExceptionLogger exceptionLogger;

	@Inject
	protected
	ConsoleRequestContext requestContext;

	// details

	public final static
	int maxTries = 3;

	protected
	Responder backupResponder () {
		return null;
	}

	// implementation

	protected
	Responder goReal ()
		throws ServletException {

		return null;

	}

	@Override
	public final
	Responder handle () {

		try {

			Responder responder =
				goWithRetry ();

			if (responder != null)
				return responder;

			Responder backupResponder =
				backupResponder ();

			if (backupResponder == null) {

				throw new RuntimeException (
					stringFormat (
						"%s.backupResponder () returned null",
						getClass ().getSimpleName ()));

			}

			return backupResponder;

		} catch (Exception exception) {

			return handleException (
				exception);

		}

	}

	private
	Responder goWithRetry ()
		throws ServletException {

		int triesRemaining =
			maxTries;

		while (triesRemaining > 1) {

			Exception caught;

			// all but last try with catch

			try {

				return goReal ();

			} catch (ConstraintViolationException exception) {

				caught =
					exception;

			} catch (LockAcquisitionException exception) {

				caught =
					exception;

			}

			// caught an exception, log it and try again

			triesRemaining --;

			log.warn (
				stringFormat (
					"%s: caught %s, retrying, %s remaining",
					getClass ().getSimpleName (),
					caught.getClass ().getSimpleName (),
					triesRemaining));

		}

		// last try without catch

		return goReal ();

	}

	Responder handleException (
			@NonNull Throwable throwable) {

		// if we have no backup page just die

		Responder backupResponder = null;

		try {

			backupResponder =
				backupResponder ();

		} catch (Exception exceptionFromBackupResponder) {

			exceptionLogger.logThrowable (
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

		log.error (
			stringFormat (
				"generated exception: %s",
				requestContext.requestPath ()),
			throwable);

		exceptionLogger.logThrowable (
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

	protected
	Provider<Responder> reusableResponder (
			@NonNull String responderName) {

		return consoleManager.responder (
			responderName,
			true);

	}

	protected
	Responder responder (
			@NonNull String responderName) {

		Provider<Responder> responderProvider =
			consoleManager.responder (
				responderName,
				true);

		return responderProvider.get ();

	}

}
