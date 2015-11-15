package wbs.framework.database;

import java.util.Map;
import java.util.WeakHashMap;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;

@SingletonComponent ("transactionCacheFactory")
// TODO get rid of this abomination
public
class TransactionCacheFactory {

	@Inject
	Database database;

	public <T>
	TransactionCache<T> create (
			Initialiser<T> newInitialiser) {

		return new TransactionCacheImplementation<T> (
			newInitialiser);

	}

	private
	class TransactionCacheImplementation<T>
		implements TransactionCache<T> {

		Map<Transaction,T> cache =
			new WeakHashMap<Transaction,T> ();

		private final
		Initialiser<T> initialiser;

		private
		TransactionCacheImplementation (
				Initialiser<T> newInitialiser) {

			initialiser =
				newInitialiser;

		}

		@Override
		public synchronized
		T get () {

			Transaction transaction =
				database.currentTransaction ();

			if (! cache.containsKey (transaction) && initialiser != null)
				cache.put (transaction, initialiser.initialise ());

			return cache.get (transaction);

		}

		@Override
		public synchronized
		void set (
				T value) {

			Transaction transaction =
				database.currentTransaction ();

			cache.put (
				transaction,
				value);

		}

	}

	public static
	interface Initialiser<T> {

		T initialise ();

	}

}
