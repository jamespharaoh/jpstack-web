<?

if ($_SERVER ["REQUEST_METHOD"] == "POST") {

	$dbhost="localhost";
	$dbuser="james";
	$dbpassword="james";
	$db="txt2";

	$conn = pg_connect("host=$dbhost port=5432 dbname=$db user=$dbuser password=$dbpassword");
	$result = pg_query($conn, "delete from exception;");

}

?>
<!DOCTYPE html>
<html>
<head>

	<title>Exception deletion</title>

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

	<h1>Exception deletion</h1>

	<form method="post">
		<input type="submit" value="delete exceptions"/>
	</form>

</body>
</html>
