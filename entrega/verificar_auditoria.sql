USE semana05_b;
SELECT id, accion, metodo, fecha, detalle, usuario FROM auditoria_log ORDER BY id DESC;
SELECT accion, COUNT(*) AS cantidad FROM auditoria_log GROUP BY accion;
