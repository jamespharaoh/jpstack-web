package wbs.platform.daemon;

import java.util.List;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;

public
interface RemovalDaemon <Type extends Record <Type>> {

	String serviceName ();
	String backgroundProcessName ();

	Duration removalAge ();

	default
	Long itemsPerBatch () {
		return 1000l;
	}

	default
	Duration sleepTime () {
		return Duration.millis (100l);
	}

	List <Type> findItemsForRemoval (
			Transaction parentTransaction,
			Instant timestamp,
			Long maxItems);

	void removeItem (
			Transaction parentTransaction,
			Type item);

}
