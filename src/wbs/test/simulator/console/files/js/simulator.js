$(function () {

	$(".simulator").each (function () {

		var simulator = $(this);

		var networkSelect =
			simulator.find (".networkSelect");

		var numFromText =
			simulator.find (".numFromText");

		var numToText =
			simulator.find (".numToText");

		var messageText =
			simulator.find (".messageText");

		function send () {

			$.ajax ({
				data: {
					type: "sendMessage",
					networkId: networkSelect.val (),
					numFrom: numFromText.val (),
					numTo: numToText.val (),
					message: messageText.val ()
				},
				dataType: "json",
				type: "POST",
				url: "/simulator/simulator.createEvent"
			});

		}

		simulator
			.find (".sendButton")
			.click (send);

		var lastPollId = 0;

		function poll () {

			$.ajax ({
				data: {
					last: lastPollId,
					limit: 100,
				},
				dataType: "json",
				type: "GET",
				url: "/simulator/simulator.poll"
			})

			.done (function (events) {

				for (var i = 0; i < events.length; i++) {

					var event = events [i];

					var dataTd, actionsTd;

					var tr = $("<tr>")
						.append ($("<td>")
							.text (event.date))
						.append ($("<td>")
							.text (event.time))
						.append ($("<td>")
							.text (event.type))
						.append (dataTd = $("<td>"))
						.append (actionsTd = $("<td>"));

					if (event.type == "message_in")
						tr.addClass ("messageIn");

					if (event.type == "message_out" && event.data.route.outCharge == 0)
						tr.addClass ("messageOut");

					if (event.type == "message_out" && event.data.route.outCharge > 0)
						tr.addClass ("messageBill");

					if (event.type == "message_in"
						|| event.type == "message_out") {

						dataTd

							.append ($("<strong>").text (
								event.data.message.numFrom +
								" -> " +
								event.data.message.numTo +
								": " +
								event.data.message.text))

							.append ("<br>")

							.append (
								document.createTextNode (
									"Id: " +
									event.data.message.id +
									", Route: " +
									event.data.route.code +
									", Network: " +
									event.data.network.code));

					}

					if (event.type == "message_out") {

						actionsTd

							.append ($("<button>")
								.text ("reply")
								.click (event, function (ev) {
									var event = ev.data;
									networkSelect.val (event.data.network.id);
									numFromText.val (event.data.message.numTo);
									numToText.val (event.data.message.numFrom);
									messageText.val ("").focus ();
								}))

							.append ($("<button>")
								.text ("deliv")
								.click (event, function (ev) {
									var event = ev.data;
									$.ajax ({
										data: {
											type: "deliveryReport",
											messageId: event.data.message.id,
											success: true
										},
										dataType: "json",
										type: "POST",
										url: "/simulator/simulator.createEvent"
									});
								}))

							.append ($("<button>")
								.text ("fail")
								.click (event, function (ev) {
									var event = ev.data;
									$.ajax ({
										data: {
											type: "deliveryReport",
											messageId: event.data.message.id,
											success: false
										},
										dataType: "json",
										type: "POST",
										url: "/simulator/simulator.createEvent"
									});
								}));
					}

					if (event.type == "delivery_report") {

						dataTd

							.append (
								document.createTextNode (
									"Message id: " +
									event.data.deliveryReport.messageId +
									", Success: " +
									(event.data.deliveryReport.success
										? "yes" : "no")));

					}

					simulator.find (".events tbody").prepend (tr);

					lastPollId = event.id;

				}

			})

			.always (function () {
				setTimeout (poll, 1000);
			})

		}

		poll ();

	});

});
