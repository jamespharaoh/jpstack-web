package wbs.platform.console.action;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.ServletException;
import javax.validation.ConstraintViolationException;

import lombok.extern.log4j.Log4j;

import org.hibernate.exception.LockAcquisitionException;

import wbs.framework.application.context.ApplicationContext;
import wbs.framework.web.Action;
import wbs.framework.web.Responder;
import wbs.platform.console.misc.ConsoleExceptionHandler;
import wbs.platform.console.module.ConsoleManager;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.exception.logic.ExceptionLogic;

@Log4j
public abstract
class ConsoleAction
	implements Action {

	public final static
	int maxTries = 3;

	@Inject
	ApplicationContext applicationContext;

	@Inject
	ConsoleExceptionHandler consoleExceptionHandler;

	@Inject
	ConsoleManager consoleManager;

	@Inject
	ExceptionLogic exceptionLogic;

	@Inject
	ConsoleRequestContext requestContext;

	protected
	Responder backupResponder () {
		return null;
	}

	protected
	Responder goReal ()
		throws ServletException {

		return null;

	}

	@Override
	public final
	Responder go ()
		throws ServletException {

		try {

			Responder responder =
				goWithRetry ();

			if (responder != null)
				return responder;

			return backupResponder ();

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
			Throwable throwable)
		throws ServletException {

		// if we have no backup page just die

		Responder backupResponder = null;

		try {

			backupResponder =
				backupResponder ();

		} catch (Exception exceptionFromBackupResponder) {

			exceptionLogic.logException (
				"console",
				requestContext.requestPath (),
				exceptionFromBackupResponder,
				requestContext.userId (),
				false);

		}

		if (backupResponder == null) {

			if (throwable instanceof RuntimeException)
				throw (RuntimeException) throwable;

			if (throwable instanceof ServletException)
				throw (ServletException) throwable;

			throw new RuntimeException (throwable);

		}

		// record the exception

		log.error (
			stringFormat (
				"generated exception: %s",
				requestContext.requestPath ()),
			throwable);

		exceptionLogic.logException (
			"console",
			requestContext.requestPath (),
			throwable,
			requestContext.userId (),
			false);

		// give the user an error message

		requestContext.addError ("Internal error");

		// and go to backup page!

		return backupResponder;

	}

	protected
	Provider<Responder> reusableResponder (
			String responderName) {

		return consoleManager.responder (
			responderName,
			true);

	}

	protected
	Responder responder (
			String responderName) {

		Provider<Responder> responderProvider =
			consoleManager.responder (
				responderName,
				true);

		return responderProvider.get ();

	}

}
