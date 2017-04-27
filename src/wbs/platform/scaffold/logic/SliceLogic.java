package wbs.platform.scaffold.logic;

import com.google.common.base.Optional;

import org.joda.time.Instant;

import wbs.framework.logging.TaskLogger;

import wbs.platform.scaffold.model.SliceRec;

public
interface SliceLogic {

	void updateSliceInactivityTimestamp (
			TaskLogger parentTaskLogger,
			SliceRec slice,
			Optional <Instant> timestamp);

}
