package wbs.framework.database;

import static wbs.utils.string.StringUtils.stringFormatLazyArray;

import lombok.NonNull;

import org.hibernate.Session;
import org.joda.time.Instant;

import wbs.framework.logging.LogContext;

public
interface TransactionMethods {

	OwnedTransaction ownedTransaction ();

	default
	long transactionId () {
		return ownedTransaction ().transactionId ();
	}

	default
	Instant now () {
		return ownedTransaction ().now ();
	}

	default
	Session hibernateSession () {
		return ownedTransaction ().hibernateSession ();
	}

	default
	void flush () {
		ownedTransaction ().flush ();
	}

	default
	void clear () {
		ownedTransaction ().clear ();
	}

	default
	void refresh (
			@NonNull Object ... objects) {

		ownedTransaction ().refresh (
			objects);

	}

	default
	boolean contains (
			@NonNull Object ... objects) {

		return ownedTransaction ().contains (
			objects);

	}

	default
	void setMeta (
			@NonNull String key,
			@NonNull Object value) {

		ownedTransaction ().setMeta (
			key,
			value);

	}

	default
	Object getMeta (
			@NonNull String key) {

		return ownedTransaction ().getMeta (
			key);

	}

	default
	void fetch (
			@NonNull Object ... objects) {

		ownedTransaction ().fetch (
			objects);

	}

	NestedTransaction nestTransaction (
			LogContext logContext,
			CharSequence dynamicContext);

	default
	NestedTransaction nestTransactionFormat (
			@NonNull LogContext logContext,
			@NonNull CharSequence ... dynamicContextArguments) {

		return nestTransaction (
			logContext,
			stringFormatLazyArray (
				dynamicContextArguments));

	}

	class IdGenerator {

		private static
		long nextId = 0;

		public synchronized static
		long nextId () {
			return nextId ++;
		}

	}

}
