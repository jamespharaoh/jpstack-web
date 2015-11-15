package wbs.platform.lock.logic;

import java.util.Arrays;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
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
			Object... objects) {

		int lockId =
			Arrays.hashCode (
				objects)
			& 0x3fff;

		LockRec lock =
			lockHelper.find (
				lockId);

		lockHelper.lock (
			lock);

	}

}
