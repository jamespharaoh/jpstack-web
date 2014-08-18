<?

function find_services () {

	$services = array ();
	$dir = opendir ("/home/james/wbs/services");

	while ($file = readdir ($dir)) {

		if (preg_match ("/~$|\\.|\\.\\./", $file))
			continue;

		$services [] = $file;

	}

	closedir ($dir);
	sort ($services);

	return $services;

}

function get_status ($service) {

	exec (
		"/home/james/wbs/service $service status",
		$output,
		$status);

	return $status;

}

function send_command ($service, $command) {

	global $messages;

	exec (
		"sh -c \"/home/james/wbs/service $service $command >/dev/null 2>&1 &\"",
		$output,
		$status);

	foreach ($output as $o)
		$messages [] = "<p class=\"output\">" . htmlentities ($o) . "</p>";

	return $status == 0;

}

function count_postgres () {

	exec (
		"ps ax | grep \"postgres: james txt2\"",
		$output);

	return count ($output);

}

$messages[] = "";

if ($_SERVER['REQUEST_METHOD'] == "POST") {

	global $messages;

	if ($_POST["special"] == "killall") {

		exec (
			"sudo kill -kill $(pidof java)",
			$output,
			$status);

	} else {

		$messages [] =
			send_command ($_POST['service'], $_POST['command'])
				? "<p class=\"notice\">Command executed successfully</p>"
				: "<p class=\"error\">Command failed</p>";

	}

}

$services = find_services ();

function do_button ($service, $command, $enabled) { ?>

	<form method="post" action="?">

		<input
			type="hidden"
			name="service"
			value="<?= htmlentities ($service) ?>"/>

		<input
			type="hidden"
			name="command"
			value="<?= htmlentities ($command) ?>"/>

		<input
			type="submit"
			value="<?= htmlentities ($command) ?>"
			<?= $enabled ? "" : "disabled" ?>/>

	</form>

<? }

// make sure we reload
header ("Expires: 0");
header ("Cache-Control: no-store, no-cache, must-revalidate");
header ("Pragma: no-cache");
header ("Refresh: 10");

?><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
	<title>Hades Control Panel</title>
	<style type="text/css">
		form { margin: 0; padding: 0; }
		tr.running td.yes { background-color: #a0ffa0; }
		tr.not-running td.no { background-color: #ffa0a0; }
		tr.part-running td.part { background-color: #ffffa0; }
		p.error { color: darkred; }
		p.output { color: darkgreen; }
		p.notice { color: darkblue; }
	</style>
</head>
<body>
	<h1>Hades Control Panel</h1>

	<? foreach ($messages as $message) {
		echo $message;
	} ?>

	<table border="1" cellpadding="5">
		<tr>
			<th>Service</th>
			<th>Running</th>
			<th>Start</th>
			<th>Stop</th>
			<th>Restart</th>
		</tr>
		<? $statuses = array (
			array ("running", "yes", "yes", false, true, true),
			array ("not-running", "no", "no", true, false, false),
			array ("part-running", "part", "partially", false, true, true));
		foreach ($services as $service) {
			$status = $statuses [get_status ($service)]; ?>
			<tr class="<?= $status [0] ?>">
				<td><?= htmlentities ($service) ?></td>
				<td class="<?= $status [1] ?>"><?= $status [2] ?></td>
				<td><? do_button ($service, "start", $status [3]); ?></td>
				<td><? do_button ($service, "stop", $status [4]); ?></td>
				<td><? do_button ($service, "restart", $status [5]); ?></td>
			</tr>
		<? } ?>
	</table>

	<p><a href="?">Reload page &gt;&gt;</a></p>

	<p><?= date ("Y-m-d H:i:s") ?></p>

	<p>Number of database processes: <?= count_postgres () ?></p>

	<form
		method="post"
		action="?">

		<input
			type="hidden"
			name="special"
			value="killall"/>

		<input
			type="submit"
			value="kill all"/>

	</form>

</body>
</html>
