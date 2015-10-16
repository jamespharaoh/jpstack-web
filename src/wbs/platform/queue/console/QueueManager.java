package wbs.platform.queue.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;

import lombok.extern.log4j.Log4j;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.web.Responder;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueRec;
import wbs.platform.queue.model.QueueSubjectRec;
import wbs.platform.queue.model.QueueTypeRec;

@Log4j
@SingletonComponent ("queueManager")
public
class QueueManager {

	@Inject
	Map<String,Provider<QueueConsolePlugin>> queueHelpersByBeanName =
		Collections.emptyMap ();

	Map<String,QueueConsolePlugin> queueHelpers =
		new HashMap<String,QueueConsolePlugin>();

	@PostConstruct
	public
	void init () {

		// initialise queuePageFactories by querying each factory

		for (Map.Entry<String,Provider<QueueConsolePlugin>> entry
				: queueHelpersByBeanName.entrySet ()) {

			String beanName =
				entry.getKey ();

			QueueConsolePlugin queueHelper =
				entry.getValue ().get ();

			for (String queueTypeCode : queueHelper.queueTypeCodes ()) {

				if (queueHelpers.containsKey (queueTypeCode)) {

					throw new RuntimeException (
						"Duplicated queue page factory: " + queueTypeCode);

				}

				queueHelpers.put (
					queueTypeCode,
					queueHelper);

				log.debug (
					stringFormat (
						"Adding queue page factory %s from %s",
						queueTypeCode,
						beanName));

			}

		}

		log.info (
			stringFormat (
				"Added %s queue page factories for %s queue types",
				queueHelpersByBeanName.size (),
				queueHelpers.size ()));

	}

	public
	Responder getItemResponder (
			ConsoleRequestContext  requestContext,
			QueueItemRec queueItem) {

		QueueSubjectRec queueSubject =
			queueItem.getQueueSubject ();

		QueueRec queue =
			queueSubject.getQueue ();

		QueueTypeRec queueType =
			queue.getType ();

		String key =
			stringFormat (
				"%s.%s",
				queueType.getParentObjectType ().getCode (),
				queueType.getCode ());

		QueueConsolePlugin queuePageFactory =
			queueHelpers.get (key);

		if (queuePageFactory == null) {

			throw new RuntimeException (
				stringFormat (
					"Queue page factory not found: %s",
					key));

		}

		return queuePageFactory.makeResponder (
			queueItem);

	}

	public
	long getPreferredUserDelay (
			QueueRec queue) {

		QueueTypeRec queueType =
			queue.getType ();

		String key =
			stringFormat (
				"%s.%s",
				queueType.getParentObjectType ().getCode (),
				queueType.getCode ());

		QueueConsolePlugin queuePageFactory =
			queueHelpers.get (key);

		if (queuePageFactory == null) {

			throw new RuntimeException (
				stringFormat (
					"Queue page factory not found: %s",
					key));

		}

		return queuePageFactory.preferredUserDelay (queue);

	}

}
