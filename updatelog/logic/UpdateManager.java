package wbs.platform.updatelog.logic;

import static wbs.utils.etc.NumberUtils.integerEqualSafe;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;

import java.util.HashMap;
import java.util.Map;

import lombok.Cleanup;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.updatelog.model.UpdateLogObjectHelper;
import wbs.platform.updatelog.model.UpdateLogRec;

/**
 * Provides an efficient signalling system which works using the database.
 * Signals are categorised according to a name, and a reference integer (eg a
 * hash code). To cause a signal use signalUpdate (...). To receive signals
 * create a Watcher using makeWatcher (...) then call isUpdated (...) to check
 * for changes. This always returns true the first time for a given ref.
 *
 * The idea is that you can cache large chunks of a table and, so long as all
 * updaters are using the signalling system, ensure that the cache is always
 * kept up to date.
 *
 * The underlying mechanism is efficient requiring (assuming no updates) only a
 * single database hit for a single row, every few seconds (this time period is
 * configurable). On update three rows must be looked up and updated/created,
 * then an extra hit is needed for each named category being watched and every
 * unique ref in the affected named category being watched.
 *
 * TODO is this really the best way to do this?
 */
@SingletonComponent ("updateManager")
public
class UpdateManager {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UpdateLogObjectHelper updateLogHelper;

	// properties

	@Getter @Setter
	long intervalMs = 3000;

	long reloadTime = 0;

	long masterVersion = -2;

	Map<String,UpdateStuff> secondaryVersions =
		new HashMap<> ();

	Map<String,Map<Long,UpdateStuff>> tertiaryVersions =
		new HashMap<> ();

	private
	long getDatabaseVersion (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String table,
			@NonNull Long ref) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"getDatabaseVersion");

		UpdateLogRec updateLog =
			updateLogHelper.findByTableAndRef (
				table,
				ref);

		long ret =
			updateLog != null
				? updateLog.getVersion ()
				: -1l;

		taskLogger.debugFormat (
			"getDatabaseVersion (\"%s\", %s) = %s",
			table,
			integerToDecimalString (
				ref),
			integerToDecimalString (
				ret));

		return ret;

	}

	private
	void refreshMaster (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"refreshMaster");

		// check it is time

		long now =
			System.currentTimeMillis ();

		if (now < reloadTime)
			return;

		reloadTime =
			+ now
			+ intervalMs;

		// hit the db

		long newMasterVersion =
			getDatabaseVersion (
				taskLogger,
				"master",
				0l);

		// if the version hasn't changed don't bother

		if (
			integerEqualSafe (
				masterVersion,
				newMasterVersion)
		) {
			return;
		}


		// remember the new version

		masterVersion =
			newMasterVersion;

		// markall secondary versions as dirty

		for (UpdateStuff updateStuff
				: secondaryVersions.values ()) {

			updateStuff.dirty = true;

		}

	}

	private
	void refreshSecondary (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String table) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"refreshSecondary");

		// if it isn't marked dirty don't bother

		UpdateStuff stuff1 =
			secondaryVersions.get (table);

		if (stuff1 == null) {

			stuff1 =
				new UpdateStuff ();

			stuff1.version = -2;
			stuff1.dirty = true;

			secondaryVersions.put (
				table,
				stuff1);

		}

		if (! stuff1.dirty)
			return;

		stuff1.dirty = false;

		// hit the db

		long newSecondaryVersion =
			getDatabaseVersion (
				taskLogger,
				table,
				0l);

		// if the version hasn't changed don't bother

		if (
			integerEqualSafe (
				stuff1.version,
				newSecondaryVersion)
		) {
			return;
		}

		// remember the new version

		stuff1.version =
			newSecondaryVersion;

		// mark all this table's tertiary versions as dirty

		Map<Long,UpdateStuff> map =
			tertiaryVersions.get (
				table);

		if (map == null)
			return;

		for (UpdateStuff stuff2 : map.values ())
			stuff2.dirty = true;

	}

	public
	long refreshTertiary (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String table,
			@NonNull Long ref) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"refreshTertiary");

		// if it's not dirty don't bother

		Map <Long, UpdateStuff> map =
			tertiaryVersions.get (
				table);

		if (map == null) {

			map =
				new HashMap<Long,UpdateStuff> ();

			tertiaryVersions.put (
				table,
				map);

		}

		UpdateStuff stuff =
			map.get (ref);

		if (stuff == null) {

			stuff =
				new UpdateStuff ();

			stuff.version = -2;
			stuff.dirty = true;

			map.put (
				ref,
				stuff);

		}

		if (! stuff.dirty)
			return stuff.version;

		stuff.dirty = false;

		// hit the db

		long newTertiaryVersion =
			getDatabaseVersion (
				taskLogger,
				table,
				ref);

		// if the version hasn't changed don't bother

		if (
			integerEqualSafe (
				stuff.version,
				newTertiaryVersion)
		) {
			return stuff.version;
		}

		// remember the new version

		return stuff.version =
			newTertiaryVersion;

	}

	public
	long getVersionDb (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String table,
			@NonNull Long ref) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"getVersionDb");

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				"UpdateManager.getVersionDb (table, ref)",
				this);

		refreshMaster (
			taskLogger);

		refreshSecondary (
			taskLogger,
			table);

		refreshTertiary (
			taskLogger,
			table,
			ref);

		Map <Long, UpdateStuff> map =
			tertiaryVersions.get (
				table);

		UpdateStuff stuff2 =
			map.get (ref);

		return stuff2.version;

	}

	public synchronized
	long getVersion (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String table,
			@NonNull Long ref) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"getVersion");

		// check master

		long now =
			System.currentTimeMillis ();

		if (reloadTime < now) {

			return getVersionDb (
				taskLogger,
				table,
				ref);

		}

		// check secondary

		UpdateStuff stuff1 =
			secondaryVersions.get (
				table);

		if (stuff1 == null || stuff1.dirty) {

			return getVersionDb (
				taskLogger,
				table,
				ref);

		}

		// check tertiary

		Map<Long,UpdateStuff> map =
			tertiaryVersions.get (
				table);

		if (map == null) {

			return getVersionDb (
				taskLogger,
				table,
				ref);

		}

		UpdateStuff stuff2 =
			map.get (ref);

		if (stuff2 == null || stuff2.dirty) {

			return getVersionDb (
				taskLogger,
				table,
				ref);

		}

		return stuff2.version;

	}

	private
	void realSignalUpdate (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String table,
			@NonNull Long ref) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"realSignalUpdate");

		UpdateLogRec updateLog =
			updateLogHelper.findByTableAndRef (
				table,
				ref);

		if (updateLog == null) {

			updateLog =
				updateLogHelper.insert (
					taskLogger,
					updateLogHelper.createInstance ()

				.setCode (
					table)

				.setRef (
					ref)

				.setVersion (
					0l)

			);

		}

		updateLog.setVersion (
			updateLog.getVersion () + 1);

	}

	public
	void signalUpdate (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String table,
			@NonNull Long ref) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"signalUpdate");

		realSignalUpdate (
			taskLogger,
			table,
			ref);

		realSignalUpdate (
			taskLogger,
			table,
			0l);

		realSignalUpdate (
			taskLogger,
			"master",
			0l);

	}

	public
	Watcher makeWatcher (
			@NonNull String table) {

		return new Watcher (
			table);

	}

	static
	class UpdateStuff {

		long version;
		boolean dirty;

	}

	public
	class Watcher {

		String table;

		Map<Long,Long> versions =
			new HashMap<> ();

		Watcher (
				String newTable) {

			table = newTable;

		}

		public
		boolean isUpdated (
				@NonNull TaskLogger parentTaskLogger,
				@NonNull Long ref) {

			TaskLogger taskLogger =
				logContext.nestTaskLoggerFormat (
					parentTaskLogger,
					"Watcher (%s).isUpdated (%s)",
					table,
					integerToDecimalString (
						ref));

			taskLogger.debugFormat (
				"Watcher (\"%s" + table + "\").isUpdated (" + ref + ")");

			long newVersion =
				getVersion (
					taskLogger,
					table,
					ref);

			if (versions.containsKey (ref)) {

				long oldVersion =
					versions.get (ref);

				if (oldVersion == newVersion)
					return false;

			}

			versions.put (
				ref,
				newVersion);

			return true;

		}

	}

	public <Type>
	UpdateGetter <Type> makeUpdateGetterAdaptor (
			ComponentProvider <? extends Type> getter,
			long reloadTimeMs,
			String table,
			long ref) {

		return new UpdateGetterAdaptor <Type> (
			getter,
			reloadTimeMs,
			table,
			ref);

	}

	public static
	interface UpdateGetter <T>
		extends ComponentProvider <T> {

		void forceUpdate ();

	}

	private
	class UpdateGetterAdaptor <T>
		implements UpdateGetter <T> {

		ComponentProvider <? extends T> getter;
		long reloadTimeMs;
		String table;
		long ref;

		boolean forceUpdate = false;

		@Override
		public synchronized
		void forceUpdate () {
			forceUpdate = true;
		}

		public
		UpdateGetterAdaptor (
				ComponentProvider <? extends T> newGetter,
				long newReloadTimeMs,
				String newTable,
				long newRef) {

			getter = newGetter;
			reloadTimeMs = newReloadTimeMs;
			table = newTable;
			ref = newRef;

		}

		T value;
		long lastReload = 0;
		long oldVersion = -1;

		@Override
		public synchronized
		T provide (
				@NonNull TaskLogger parentTaskLogger) {

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"UpdateGetterAdaptor.provide ()");

			long now =
				System.currentTimeMillis ();

			// check for a forced update

			if (forceUpdate) {

				value =
					getter.provide (
						parentTaskLogger);

				lastReload = now;
				forceUpdate = false;

				return value;

			}

			// check for an update trigger

			long newVersion =
				getVersion (
					taskLogger,
					table,
					ref);

			if (oldVersion != newVersion) {

				value =
					getter.provide (
						parentTaskLogger);

				lastReload = now;
				oldVersion = newVersion;

				return value;

			}

			// if not check for a timed update

			if (lastReload + reloadTimeMs < now) {

				value =
					getter.provide (
						parentTaskLogger);

				lastReload = now;
				oldVersion = newVersion;

				return value;

			}

			// or just return the cached value

			return value;

		}

	}

}
