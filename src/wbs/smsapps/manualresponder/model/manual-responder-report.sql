---------------------------------------- VIEW manual_responder_report

CREATE VIEW manual_responder_report
AS SELECT
       req.id AS manual_responder_request_id,
       mr.id AS manual_responder_id,
       u.id AS user_id,
       (
               SELECT count (*)
               FROM message
               WHERE thread_message_id = req.message_id
                       AND direction = 1
                       AND charge > 0

       ) AS num
FROM
       manual_responder_request AS req
       INNER JOIN manual_responder AS mr ON req.manual_responder_id = mr.id
       INNER JOIN queue_item AS qi ON req.queue_item_id = qi.id
       INNER JOIN "user" AS u ON qi.processed_user_id = u.id

-- WHERE req.timestamp >= '2014-07-01 00:00:00'
--      AND req.timestamp < '2014-07-31 23:59:59'
;