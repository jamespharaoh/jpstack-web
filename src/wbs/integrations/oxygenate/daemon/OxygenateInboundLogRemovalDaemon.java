package wbs.integrations.oxygenate.daemon;

import java.util.List;

import lombok.NonNull;
import lombok.experimental.Accessors;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.integrations.oxygenate.model.OxygenateInboundLogObjectHelper;
import wbs.integrations.oxygenate.model.OxygenateInboundLogRec;

import wbs.platform.daemon.RemovalDaemon;

@Accessors (fluent = true)
@SingletonComponent ("oxygenateInboundLogRemovalDaemon")
public
class OxygenateInboundLogRemovalDaemon
	implements RemovalDaemon <OxygenateInboundLogRec> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	OxygenateInboundLogObjectHelper oxygenateInboundLogHelper;

	// details

	@Override
	public
	String serviceName () {
		return "Oxygenate inbound log removal";
	}

	@Override
	public
	String backgroundProcessName () {
		return "oxygen8-inbound-log.removal";
	}

	@Override
	public
	Duration removalAge () {
		return Duration.standardDays (30l);
	}

	// public impleentation

	@Override
	public
	List <OxygenateInboundLogRec> findItemsForRemoval (
			@NonNull Transaction parentTransaction,
			@NonNull Instant timestamp,
			@NonNull Long maxItems) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findItemsForRemoval");

		) {

			return oxygenateInboundLogHelper.findOlderThanLimit (
				transaction,
				timestamp,
				maxItems);

		}

	}

	@Override
	public
	void removeItem (
			@NonNull Transaction parentTransaction,
			@NonNull OxygenateInboundLogRec item) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"removeItem");

		) {

			oxygenateInboundLogHelper.remove (
				transaction,
				item);

		}

	}

}
