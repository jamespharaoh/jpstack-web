package wbs.platform.lock.logic;

import static wbs.framework.utils.etc.NumberUtils.fromJavaInteger;

import java.util.Arrays;

import javax.inject.Inject;

import lombok.NonNull;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.platform.lock.model.LockObjectHelper;
import wbs.platform.lock.model.LockRec;

@SingletonComponent ("lockLogic")
public
class LockLogicImplementation
	implements LockLogic {

	@Inject
	LockObjectHelper lockHelper;

	@Override
	public
	void magicLock (
			@NonNull Object... objects) {

		Long lockId =
			fromJavaInteger (
				Arrays.hashCode (
					objects)
				& 0x3fff);

		LockRec lock =
			lockHelper.findRequired (
				lockId);

		lockHelper.lock (
			lock);

	}

}
