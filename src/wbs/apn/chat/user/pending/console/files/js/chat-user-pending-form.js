function useTemplate () {

	var templateId =
		document.getElementById ('templateId');

	var text =
		document.getElementById ('message');

	if (templateId.value == '') {
		return;
	}

	var template =
		chatHelpTemplates [templateId.value];

	if (template) {
		text.value = template;
	}

}

function showPhoto () {

	try {

		document.getElementById ('photoRow').style.display = 'table-row';

	} catch (e) {

		document.getElementById ('photoRow').style.display = block';

	}

	document.getElementById ('templateRow').style.display = 'none';
	document.getElementById ('messageRow').style.display = 'none';
	document.getElementById ('approveButton').style.display = 'inline';
	document.getElementById ('rejectButton').style.display = 'none';

	$('#classificationRow').show ();

}

function showReject () {

	document.getElementById ('photoRow').style.display = 'none';

	try {

		document.getElementById ('templateRow').style.display = 'table-row';
		document.getElementById ('messageRow').style.display = 'table-row';

	} catch (e) {

		document.getElementById ('templateRow').style.display = 'block';
		document.getElementById ('messageRow').style.display = 'block';

	}

	document.getElementById ('approveButton').style.display = 'none';
	document.getElementById ('rejectButton').style.display = 'inline';

	$('#classificationRow').hide ();

}

// ex: noet ts=4 filetype=javascript