package wbs.framework.component.tools;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public
class EasyReadWriteLock {

	private
	ReadWriteLock internalLock =
		new ReentrantReadWriteLock ();

	public static
	EasyReadWriteLock instantiate () {

		return new EasyReadWriteLock ();

	}

	private
	EasyReadWriteLock () {
	}

	public
	HeldLock read () {

		return new HeldLock (
			internalLock.readLock ());

	}

	public
	HeldLock write () {

		return new HeldLock (
			internalLock.readLock ());

	}

	public static
	class HeldLock {

		private
		Lock lock;

		private
		boolean held = true;

		private
		HeldLock (
				Lock lock) {

			this.lock =
				lock;

			lock.lock ();

		}

		public
		void close () {

			if (! held) {
				return;
			}

			lock.unlock ();

			held = false;

		}

	}

}
