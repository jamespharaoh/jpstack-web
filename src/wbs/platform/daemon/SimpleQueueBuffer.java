package wbs.platform.daemon;

import java.util.Set;

public
class SimpleQueueBuffer<K> {

	QueueBuffer<K,K> queueBuffer;

	public
	SimpleQueueBuffer (
			int fullSize) {

		queueBuffer =
			new QueueBuffer<K,K> (
				fullSize);

	}

	public
	int getFullSize () {
		return queueBuffer.getFullSize();
	}

	public
	void add (
			K key) {

		queueBuffer.add (
			key,
			key);

	}

	public
	K next ()
		throws InterruptedException {

		return queueBuffer.next ();

	}

	public
	void remove (
			K key) {

		queueBuffer.remove (
			key);

	}

	public
	void waitNotFull ()
		throws InterruptedException {

		queueBuffer.waitNotFull ();

	}

	public
	Set<K> getKeys () {

		return queueBuffer.getKeys ();

	}

}
