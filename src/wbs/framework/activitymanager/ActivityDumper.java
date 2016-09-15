package wbs.framework.activitymanager;

import lombok.extern.log4j.Log4j;

import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;

@Log4j
@SingletonComponent ("activityDumper")
public
class ActivityDumper {

	// singleton dependencies

	@SingletonDependency
	ActivityManagerImplementation activityManager;

	// implementation

	@NormalLifecycleSetup
	public
	void init () {

		Runnable runnable =
			new Runnable () {

			@Override
			public
			void run () {
				ActivityDumper.this.run ();
			}

		};

		Thread thread =
			new Thread (
				runnable,
				"ActivityDumper");

		thread.start ();

	}

	void run () {

		try {

			runReal ();

		} catch (InterruptedException exception) {

			return;

		} catch (Exception exception) {

			log.fatal (
				"Error in activity dumper",
				exception);

		}

	}

	void runReal ()
		throws InterruptedException {

		Thread.sleep (
			5000);

		for (;;) {

			runOnce ();

			Thread.sleep (
				5000);

		}

	}

	void runOnce () {

		activityManager.logActiveTasks ();

	}

}
