package wbs.platform.scaffold.logic;

import org.joda.time.Instant;

import com.google.common.base.Optional;

import wbs.platform.scaffold.model.SliceRec;

public
interface SliceLogic {

	void updateSliceInactivityTimestamp (
			SliceRec slice,
			Optional<Instant> timestamp);

}
