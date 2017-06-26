package wbs.smsapps.broadcast.console;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

import wbs.console.helper.core.ConsoleHooks;

import wbs.framework.component.annotations.SingletonComponent;

import wbs.smsapps.broadcast.model.BroadcastRec;

@SingletonComponent ("broadcastConsoleHooks")
public
class BroadcastConsoleHooks
	implements ConsoleHooks <BroadcastRec> {

	// public implementation

	@Override
	public
	Ordering <BroadcastRec> defaultOrdering () {

		return Ordering.compound (
			ImmutableList.of (
				Ordering.natural ().reverse ().onResultOf (
					BroadcastRec::getCreatedTime),
				Ordering.natural ().reverse ().onResultOf (
					BroadcastRec::getId)));

	}

}
