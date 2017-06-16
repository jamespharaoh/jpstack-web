package wbs.integrations.clockworksms.daemon;

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

import wbs.integrations.clockworksms.model.ClockworkSmsInboundLogObjectHelper;
import wbs.integrations.clockworksms.model.ClockworkSmsInboundLogRec;

import wbs.platform.daemon.RemovalDaemon;

@Accessors (fluent = true)
@SingletonComponent ("clockworkSmsInboundLogRemovalDaemon")
public
class ClockworkSmsInboundLogRemovalDaemon
	implements RemovalDaemon <ClockworkSmsInboundLogRec> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ClockworkSmsInboundLogObjectHelper clockworkSmsInboundLogHelper;

	// details

	@Override
	public
	LogContext logContext () {
		return logContext;
	}

	@Override
	public
	String serviceName () {
		return "Clockwork SMS inbound log removal";
	}

	@Override
	public
	String backgroundProcessName () {
		return "clockwork-sms-inbound-log.removal";
	}

	@Override
	public
	Duration removalAge () {
		return Duration.standardDays (30l);
	}

	// public implementation

	@Override
	public
	List <ClockworkSmsInboundLogRec> findItemsForRemoval (
			@NonNull Transaction parentTransaction,
			@NonNull Instant timestamp,
			@NonNull Long maxItems) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findItemsForRemoval");

		) {

			return clockworkSmsInboundLogHelper.findOlderThanLimit (
				transaction,
				timestamp,
				maxItems);

		}

	}

	@Override
	public
	void removeItem (
			@NonNull Transaction parentTransaction,
			@NonNull ClockworkSmsInboundLogRec item) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"removeItem");

		) {

			clockworkSmsInboundLogHelper.remove (
				transaction,
				item);

		}

	}

}
