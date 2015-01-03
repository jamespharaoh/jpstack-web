
---------------------------------------- daemons

INSERT INTO daemon (class) VALUES ('com.pharaohsystems.txt2.netsize.NetsizeInbound');

---------------------------------------- ROUTE netsize test

INSERT INTO route (code, name, type, class)
VALUES ('netsize_test', 'Netsize test route', 1, 'com.pharaohsystems.txt2.netsize.NetsizeOutbound');

INSERT INTO netsize_endpoint (hostname, port, timeout, routeid, networkid,
	username, password, inbound, outbound, report)
VALUES ('uk.netsizeonline.com', 38000, 5000, route ('netsize_test'), 0,
	'allpointsnorth-test', 'i4k2i3e2',
	'NSGClientMO^Allpointsnorth-test',
	'NSGClientMT^Allpointsnorth-test',
	'NSGClientSR^Allpointsnorth-test');

---------------------------------------- dialogue 88211 route

INSERT INTO route (code, name, number, in_charge, out_charge, can_send, can_receive, sender_id)
VALUES ('dialogue_88211_500', 'Dialogue 88211', '88211', 0, 500, true, true, sender_id ('http'));

INSERT INTO httproute (routeid, networkid, post, url, params, param_encoding, successregex, permfailureregex)
VALUES (
    route_id ('dialogue_88211_500'),
    0,
    true,
    'http://sms.dialogue.co.uk/cgi-bin/messaging/messaging.mpl',
    'X-E3-HTTP-Login=nowuk.api&' ||
        'X-E3-HTTP-Password=kdkk3k3k&' ||
        'X-E3-Recipients={numto}&' ||
        'X-E3-Message={message}&' ||
        'X-E3-Originating-Address={numfrom}&' ||
        'X-E3-Reply-Path=HTTP%3ahades.apnuk.com%2fapi%2fdialogue%2froute%2f' || route_id ('dialogue_88211_500') || '%2freport&' ||
        'X-E3-Confirm-Delivery=on&' ||
        'X-E3-Validity-Period=24h&' ||
        'X-E3-Confirm-Error=on&' ||
        'X-E3-User-Key={id}&' ||
        'X-E3-Confirm-Submission=on',
    'utf-8',
    E'\\bE3_SUBMIT_RESULT=[0-9]+:success:([-0-9A-F]+)\\b',
    'X-E3-Submission-Report: "(40|45)"');

---------------------------------------- dialogue 89505 route

INSERT INTO route (code, name, number, in_charge, out_charge, can_send, can_receive, sender_id)
VALUES ('dialogue_89505_500', 'Dialogue 89505', '89505', 0, 500, true, true, sender_id ('http'));

DELETE FROM httproute WHERE routeid = route_id ('dialogue_89505_500');
INSERT INTO httproute (routeid, networkid, post, url, params, param_encoding, successregex, permfailureregex)
VALUES (
    route_id ('dialogue_89505_500'),
    0,
    true,
    'http://sms.dialogue.co.uk/cgi-bin/messaging/messaging.mpl',
    'X-E3-HTTP-Login=nowuk.api&' ||
        'X-E3-HTTP-Password=kdkk3k3k&' ||
        'X-E3-Recipients={numto}&' ||
        'X-E3-Message={message}&' ||
        'X-E3-Originating-Address={numfrom}&' ||
        'X-E3-Reply-Path=HTTP%3ahades.apnuk.com%2fapi%2fdialogue%2froute%2f' || route_id ('dialogue_89505_500') || '%2freport&' ||
        'X-E3-Confirm-Delivery=on&' ||
        'X-E3-Validity-Period=24h&' ||
        'X-E3-Confirm-Error=on&' ||
        'X-E3-User-Key={id}&' ||
        'X-E3-Confirm-Submission=on',
    'utf-8',
    E'\\bE3_SUBMIT_RESULT=[0-9]+:success:([-0-9A-F]+)\\b',
    'X-E3-Submission-Report: "(40|45)"');

---------------------------------------- dialogue 85722 route

INSERT INTO route (code, name, number, in_charge, out_charge, can_send, can_receive, sender_id)
VALUES ('dialogue_85722_500', 'Dialogue 85722', '85722', 100, 500, true, true, sender_id ('http'));

INSERT INTO httproute (routeid, networkid, post, url, params, param_encoding, successregex, permfailureregex)
VALUES (
    route_id ('dialogue_85722_500'),
    0,
    true,
    'http://sms.dialogue.co.uk/cgi-bin/messaging/messaging.mpl',
    'X-E3-HTTP-Login=nowuk.api&X-E3-HTTP-Password=kdkk3k3k&X-E3-Recipients={numto}&X-E3-Message={message}&X-E3-Originating-Address={numfrom}&X-E3-Reply-Path=HTTP%3ahades.apnuk.com%2fapi%2fdialogue%2froute%2f21%2freport&X-E3-Confirm-Delivery=on&X-E3-Validity-Period=24h&X-E3-Confirm-Error=on&X-E3-User-Key={id}&X-E3-Confirm-Submission=on',
    'iso-8859-1',
    '\\bE3_SUBMIT_RESULT=[0-9]+:success:([-0-9A-F]+)\\b',
    'X-E3-Submission-Report: "(40|45)"');

---------------------------------------- unwired plaza free route

INSERT INTO route (code, name, can_receive, can_send)
VALUES ('unwiredplaza_free', 'Unwiredplaza Free', false, true);

INSERT INTO httproute (routeid, networkid, post, url, params, successregex, param_encoding)
VALUES (route_id('unwiredplaza_free'), 0, false,
	'http://system.unwiredplaza.com/send.php',
	'sn=F4A7JMI49X79PO0ATY&smsto={numto}&smsfrom={numfrom}&smsmsg={message}&type=2&network=BULK&charge=0&smstarif=2&account=APNUK',
	'id=([0-9]+)&charged=0&reason=OK&reason_code=0',
	'utf-8');

---------------------------------------- dialogue 83010 route

INSERT INTO route (code, name, type, class, number, outcharge)
VALUES ('dialogue_83010_25', 'Dialogue 83010 25p', 2, 'com.pharaohsystems.txt2.daemon.HttpOutbound', '83010', 25);

INSERT INTO httproute (routeid, networkid, post, url, params, successregex, param_encoding)
VALUES (route ('dialogue_83010_25'), 0, true,
	'http://sms.dialogue.co.uk/cgi-bin/messaging/messaging.mpl',
	'X-E3-HTTP-Login=nowuk-2WAYT&X-E3-HTTP-Password=0000uus&X-E3-Recipients={numto}&X-E3-Message={message}&X-E3-Originating-Address={numfrom}&X-E3-Charge-Band=5',
	'\\bE3_SUBMIT_RESULT=[0-9]+:success:([-0-9]+)\\b',
	'utf-8');

---------------------------------------- dialogue 85010 50p route

INSERT INTO route (code, name, type, class, number, outcharge)
VALUES ('dialogue_85010_50', 'Dialogue 83010 50p', 2, 'com.pharaohsystems.txt2.daemon.HttpOutbound', '85010', 50);

INSERT INTO httproute (routeid, networkid, post, url, params, successregex, param_encoding)
VALUES (route ('dialogue_85010_50'), 0, true,
	'http://sms.dialogue.co.uk/cgi-bin/messaging/messaging.mpl',
	'X-E3-HTTP-Login=nowuk-2WAYT&X-E3-HTTP-Password=0000uus&X-E3-Charge-Band=5&' ||
		'X-E3-Recipients={numto}&X-E3-Message={message}&X-E3-Originating-Address={numfrom}&' ||
		'X-E3-Reply-Path=HTTP%3ahades.apnuk.com%2fapi%2fdialogue%2froute%2f' || route ('dialogue_85010_50') || '%2freport&' ||
		'X-E3-Confirm-Delivery=on&X-E3-Confirm-Error=on&X-E3-User-Key={id}',
	'\\bE3_SUBMIT_RESULT=[0-9]+:success:([-0-9A-F]+)\\b',
	'utf-8');

---------------------------------------- dialogue 69005 route

INSERT INTO route (code, name, type, class, number, outcharge)
VALUES ('dialogue_69005', 'Dialogue 69005 3.00', 2, 'com.pharaohsystems.txt2.daemon.HttpOutbound', '69005', 300);

INSERT INTO httproute (routeid, networkid, post, url, params, successregex, param_encoding)
VALUES (route ('dialogue_69005'), 0, true,
	'http://sms.dialogue.co.uk/cgi-bin/messaging/messaging.mpl',
	'X-E3-HTTP-Login=nowuk_sender&X-E3-HTTP-Password=n0wuk&X-E3-Recipients={numto}&X-E3-Message={message}&X-E3-Originating-Address={numfrom}&X-E3-Charge-Band=12',
	'\\bE3_SUBMIT_RESULT=[0-9]+:success:([-0-9]+)\\b',
	'utf-8');

---------------------------------------- dialogue 60155 route

INSERT INTO route (code, name, number, out_charge, can_receive, can_send, delivery_reports, expiry_secs, sender_id)
VALUES ('dialogue_60155', 'Dialogue 60155 £3.00', '60155', 300, true, true, true, 259200, sender_id ('http'));

INSERT INTO httproute (routeid, networkid, post, url, params, successregex, permfailureregex, param_encoding)
VALUES (route_id ('dialogue_60155'), 0, true,
	'http://sms.dialogue.co.uk/cgi-bin/messaging/messaging.mpl',
	'X-E3-HTTP-Login=nowuk_sender&X-E3-HTTP-Password=n0wuk&X-E3-Recipients={numto}&X-E3-Message={message}&X-E3-Originating-Address={numfrom}&X-E3-Charge-Band=12&X-E3-Reply-Path=HTTP%3ahades.apnuk.com%2fapi%2fdialogue%2froute%2f14%2freport&X-E3-Confirm-Delivery=on&X-E3-Confirm-Error=on&X-E3-User-Key={id}&X-E3-Confirm-Submission=on',
	'\\bE3_SUBMIT_RESULT=[0-9]+:success:([-0-9A-F]+)\\b',
	'X-E3-Submission-Report: "(40|45)"',
	'iso-8859-1');

---------------------------------------- dialogue mms

INSERT INTO route (code, name, number, can_receive, can_send, sender_id, mms)
VALUES ('dialogue_mms_str8', 'Dialogue MMS 447950080993', '447950080993', true, true, sender_id ('dialogue_mms'), true);

---------------------------------------- ROUTE mediaburst_str8_primary2

INSERT INTO route (code, name, can_receive, can_send)
VALUES ('mediaburst_str8_primary2', 'Mediaburst magic numbers str8 primary 2', true, false);

SELECT magic_number_set_insert ('mediaburst_str8_primary2', 'Media burst str8 primary 2');

---------------------------------------- ROUTE mediaburst_str8_backup2

INSERT INTO route (code, name, can_receive, can_send)
VALUES ('mediaburst_str8_backup2', 'Mediaburst magic numbers str8 backup 2', true, false);

SELECT magic_number_set_insert ('mediaburst_str8_backup2', 'Media burst str8 backup 2');

---------------------------------------- ROUTE mediaburst_69001

INSERT INTO mediaburst_proteus_route_out (id, url, username, password, serv_type)
VALUES ((SELECT id FROM route WHERE code = 'mediaburst_69001'),
	'https://sms.message-platform.com/xml/send.aspx', 'APNorth', 'AllPN', NULL);

SELECT route_message_type_insert ('mediaburst_69001', 'sms', 'in', true);
SELECT route_message_type_insert ('mediaburst_69001', 'sms', 'out', true);

---------------------------------------- ROUTE mediaburst_free_low

INSERT INTO route (code, name, type, class)
VALUES ('mediaburst_free_low', 'Media Burst 3.8p', 1, 'HttpOutbound');

SELECT route_message_type_insert ('mediaburst_free_low', 'sms', 'in', true);
SELECT route_message_type_insert ('mediaburst_free_low', 'sms', 'out', true);
SELECT route_message_type_insert ('mediaburst_free_low', 'wap_push', 'out', true);

INSERT INTO httproute (routeid, post, url, networkid, params, successregex)
VALUES (route ('mediaburst_free_low'), TRUE, 'http://sms.mediaburst.com/sms/http/send_msg', 0,
	'user=apnlopri&passwd=z3sim92&sendto={numto}&text={message}&from={numfrom}&msg_type=TEXT',
	'\\bID: (\\w+)\\b');

INSERT INTO mediaburst_proteus_route_out (id, url, username, password, serv_type)
VALUES ((SELECT id FROM route WHERE code = 'mediaburst_free_low'),
	'https://sms.message-platform.com/xml/send.aspx', 'APNorth', 'AllPN', 'normal');

---------------------------------------- ROUTE mediaburst_free_high

SELECT route_message_type_insert ('mediaburst_free_high', 'sms', 'in', true);
SELECT route_message_type_insert ('mediaburst_free_high', 'sms', 'out', true);
SELECT route_message_type_insert ('mediaburst_free_high', 'wap_push', 'out', true);

INSERT INTO httproute (routeid, post, url, networkid, params, successregex)
	VALUES (route ('mediaburst_free_high'), TRUE, 'http://sms.mediaburst.com/sms/http/send_msg', 0,
		'user=apnhipri&passwd=kd9spu3&sendto={numto}&text={message}&from={numfrom}&msg_type=TEXT',
		'\\bID: (\\w+)\\b');

INSERT INTO mediaburst_proteus_route_out (id, url, username, password, serv_type)
VALUES ((SELECT id FROM route WHERE code = 'mediaburst_free_high'),
	'https://sms.message-platform.com/xml/send.aspx', 'APNorth', 'AllPN', 'high');

---------------------------------------- g8wave 89016 route

INSERT INTO httproute (routeid, networkid, post, url, params, successregex, param_encoding)
VALUES (route ('g8wave_89016'), 1, false,
	'http://www.people2people.co.uk/webscripts/httpreader1.asp',
	'telno={numto}&shortcode={numfrom}&network=ORANGE&message={message}&msgtype=1&user=1003&pwd=txtfun&msgref={messageid}&dlr=0',
	'NOERROR',
	'utf-8');

INSERT INTO httproute (routeid, networkid, post, url, params, successregex, param_encoding)
VALUES (route ('g8wave_89016'), 2, false,
	'http://www.people2people.co.uk/webscripts/httpreader1.asp',
	'telno={numto}&shortcode={numfrom}&network=VODA&message={message}&msgtype=1&user=1003&pwd=txtfun&msgref={messageid}&dlr=0',
	'NOERROR',
	'utf-8');

INSERT INTO httproute (routeid, networkid, post, url, params, successregex, param_encoding)
VALUES (route ('g8wave_89016'), 3, false,
	'http://www.people2people.co.uk/webscripts/httpreader1.asp',
	'telno={numto}&shortcode={numfrom}&network=TMOB&message={message}&msgtype=1&user=1003&pwd=txtfun&msgref={messageid}&dlr=0',
	'NOERROR',
	'utf-8');

INSERT INTO httproute (routeid, networkid, post, url, params, successregex, param_encoding)
VALUES (route ('g8wave_89016'), 4, false,
	'http://www.people2people.co.uk/webscripts/httpreader1.asp',
	'telno={numto}&shortcode={numfrom}&network=O2&message={message}&msgtype=1&user=1003&pwd=txtfun&msgref={messageid}&dlr=0',
	'NOERROR',
	'utf-8');

INSERT INTO route (code, name, type, class, number, outcharge)
VALUES ('dialogue_83010_25', 'Dialogue 83010 25p', 2, 'com.pharaohsystems.txt2.daemon.HttpOutbound', '83010', 25);

INSERT INTO httproute (routeid, networkid, post, url, params, successregex, param_encoding)
VALUES (route ('dialogue_free'), 0, true,
	'http://sms.dialogue.co.uk/cgi-bin/messaging/messaging.mpl',
	'X-E3-HTTP-Login=nowuk_sender&X-E3-HTTP-Password=n0wuk&X-E3-Recipients={numto}&X-E3-Message={message}&X-E3-Originating-Address={numfrom}',
	'\\bE3_SUBMIT_RESULT=[0-9]+:success:([-0-9]+)\\b',
	'utf-8');

INSERT INTO route (code, name, type, class)
VALUES ('g8wave_free', 'G8wave free', 2, 'com.pharaohsystems.txt2.daemon.HttpOutbound');

INSERT INTO httproute (routeid, networkid, post, url, params, successregex, param_encoding)
VALUES (route ('g8wave_free'), 0, false,
	'http://www.people2people.co.uk/webscripts/httpreader1.asp',
	'telno={numto}&shortcode={numfrom}&network=FREE&message={message}&msgtype=1&user=1003&pwd=txtfun&msgref={id}&dlr=0',
	'NOERROR',
	'utf-8');

------------------------------------------------------------ ROUTE hybyte_88211_500

SELECT hybyte_route_out_insert (
	'hybyte_88211_500',
	'https://secure.hybyte.net/apn/ami/',
	'88211',
	'password');

------------------------------------------------------------ ROUTE hybyte_87211_150

INSERT INTO route (code, name, number, outcharge, sender_id)
VALUES ('hybyte_87211_150', 'Hybyte 87211 1.50', '87211', 150, sender_id ('hybyte'));

SELECT hybyte_route_out_insert (
	'hybyte_87211_150',
	'https://secure.hybyte.net/apn/ami/',
	'87211',
	'password');

------------------------------------------------------------ ROUTE hybyte_86211_100

INSERT INTO route (code, name, number, outcharge, sender_id)
VALUES ('hybyte_86211_100', 'Hybyte 87211 1.00', '86211', 100, sender_id ('hybyte'));

SELECT hybyte_route_out_insert (
	'hybyte_86211_100',
	'https://secure.hybyte.net/apn/ami/',
	'86211',
	'password');

------------------------------------------------------------ ROUTE hybyte_85211_50

INSERT INTO route (code, name, number, outcharge, sender_id)
VALUES ('hybyte_60120_50', 'Hybyte 60120 50p', '60120', 150, sender_id ('hybyte'));

SELECT hybyte_route_out_insert (
	'hybyte_60120_50',
	'https://secure.hybyte.net/apn/ami/',
	'85211',
	'password');

------------------------------------------------------------ ROUTE hybyte_84915_25

INSERT INTO route (code, name, number, outcharge, sender_id)
VALUES ('hybyte_84915_25', 'Hybyte 84915 25p', '84915', 25, sender_id ('hybyte'));

SELECT hybyte_route_out_insert (
	'hybyte_84915_25',
	'https://secure.hybyte.net/apn/ami/',
	'84915',
	'password');

------------------------------------------------------------ ROUTE hybyte_84469

INSERT INTO route (code, name, number, outcharge, sender_id)
VALUES ('hybyte_84469_150', 'Hybyte 84469 £1.50', '84469', 150, sender_id ('hybyte'));

SELECT hybyte_route_out_insert (
	'hybyte_84469_150',
	'https://secure.hybyte.net/apn/ami/',
	'84469',
	'qwert1234');

------------------------------------------------------------ ROUTE hybyte_84469_free

INSERT INTO route (code, name, number, sender_id, can_send)
VALUES ('hybyte_84469_free', 'Hybyte 84469 free', '84469', sender_id ('hybyte'), true);

SELECT route_message_type_insert ('hybyte_84469_free', 'sms', 'out', true);
SELECT route_message_type_insert ('hybyte_84469_free', 'wap_push', 'out', true);

SELECT hybyte_route_out_insert (
	'hybyte_84469_free',
	'https://secure.hybyte.net/apn/ami/',
	'84469',
	'qwert1234');

------------------------------------------------------------ ROUTE hybyte_89452

INSERT INTO route (code, name, number, outcharge, sender_id)
VALUES ('hybyte_89452_150', 'Hybyte 89452 £1.50', '89452', 150, sender_id ('hybyte'));

SELECT hybyte_route_out_insert (
	'hybyte_89452_150',
	'https://secure.hybyte.net/apn/ami/',
	'89452',
	'amip4ss');

------------------------------------------------------------ ROUTE hybyte_89452_free

INSERT INTO route (code, name, number, sender_id, can_send, can_receive)
VALUES ('hybyte_89452_free', 'Hybyte 89452 free', '89452', sender_id ('hybyte'), true, false);

SELECT route_message_type_insert ('hybyte_89452_free', 'sms', 'out', true);
SELECT route_message_type_insert ('hybyte_89452_free', 'wap_push', 'out', true);

SELECT hybyte_route_out_insert (
	'hybyte_89452_free',
	'https://secure.hybyte.net/apn/ami/',
	'89452',
	'amip4ss');

------------------------------------------------------------ ROUTE hybyte_89904_150

SELECT route_insert (
	'hybyte_89904_150', 'Hybyte 89904 £1.50', '89904', 150,
	'hybyte', true, true, 3 * 24 * 60 * 60);

SELECT route_message_type_insert ('hybyte_89904_150', 'sms', 'in', true);
SELECT route_message_type_insert ('hybyte_89904_150', 'sms', 'out', true);
SELECT route_message_type_insert ('hybyte_89904_150', 'wap_push', 'out', true);

INSERT INTO hybyte_route (id)
SELECT id FROM route WHERE code = 'hybyte_89904_150';

SELECT hybyte_route_out_insert (
	'hybyte_89904_150',
	'https://secure.hybyte.net/apn/ami/',
	'89904',
	't3mpout');

------------------------------------------------------------ ROUTE hybyte_89904_free

SELECT route_insert (
	'hybyte_89904_free', 'Hybyte 89904 free', '89904', 0,
	'hybyte', true, false, 3 * 24 * 60 * 60);

SELECT route_message_type_insert ('hybyte_89904_free', 'sms', 'out', true);
SELECT route_message_type_insert ('hybyte_89904_free', 'wap_push', 'out', true);

INSERT INTO hybyte_route (id)
SELECT id FROM route WHERE code = 'hybyte_89904_free';

SELECT hybyte_route_out_insert (
	'hybyte_89904_free',
	'https://secure.hybyte.net/apn/ami/',
	'89904',
	't3mpout');

------------------------------------------------------------ ROUTE hybyte_89975_free

SELECT route_insert (
	'hybyte_89975_free', 'Hybyte 89975 free', '89975', 0,
	'hybyte', true, false, 3 * 24 * 60 * 60);

SELECT route_message_type_insert ('hybyte_89975_free', 'sms', 'out', true);
SELECT route_message_type_insert ('hybyte_89975_free', 'wap_push', 'out', true);

INSERT INTO hybyte_route (id)
SELECT id FROM route WHERE code = 'hybyte_89975_free';

SELECT hybyte_route_out_insert (
	'hybyte_89975_free',
	'https://secure.hybyte.net/apn/ami/',
	'89975',
	'k1ngtut');

------------------------------------------------------------ ROUTE hybyte_free

SELECT hybyte_route_out_insert (
	'hybyte_free',
	'https://secure.hybyte.net/apn/ami/',
	'apn2',
	'password');

SELECT route_tester_insert (
	'hybyte_free',
	'APN',
	'447981920540',
	'rt',
	900);

------------------------------------------------------------ ROUTE mig_mms_gayx

SELECT route_insert (
	'mig_mms_gayx',
	'MIG MMS Gay Exchange',
	'447537401258');

UPDATE route
SET can_receive = true,
WHERE code = 'mig_mms_gayx';

INSERT INTO mig_route_in (route_id, set_network)
VALUES (route ('mig_mms_gayx', false);

------------------------------------------------------------ locator

SELECT dialogue_locator_insert ('uk_test', 'UK test account', 'http://lbs.dev.dialogue.co.uk/API-0-1', 'nowuk', 'james', 'jd7wn21', 'iso-8859-1');

------------------------------------------------------------- ROUTE dialogue_84010_150

INSERT INTO route (code, name, number, out_charge, sender_id,can_receive,can_send) VALUES ('dialogue_84010_150', 'Dialogue 84010 £1.50', '84010',150, sender_id('http'),true,true);


------------------------------------------------------------- re-bill

select num_to, min(created_time) from message where route_id=70 and status=6 group by num_to order by min(created_time);


insert into message_rebill(message_id,last_bill_date)
select id,processed_time from message where route_id=70 and status=6;

------------------------------------------------------------- location

select number (number_id), longitude, latitude from locator_log order by id desc limit 20;
