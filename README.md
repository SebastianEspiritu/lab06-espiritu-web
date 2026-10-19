# Laboratorio 06 — versión definitiva

Proyecto Spring Boot con auditoría AOP y control por usuario y rol, integrando las actividades de ambas guías.

## Ejecutar

1. Iniciar MySQL en localhost:3306.
2. Crear la base si aún no existe: `CREATE DATABASE IF NOT EXISTS semana05_b;`.
3. Ajustar usuario y contraseña en `src/main/resources/application.properties` si corresponde. La configuración actual usa root sin contraseña.
4. Desde esta carpeta ejecutar `mvn spring-boot:run` (o `./mvnw.cmd spring-boot:run`). Hibernate actualiza las tablas y añade usuario con `ddl-auto=update`.
5. Importar `entrega/Laboratorio06.postman_collection.json` en Postman y ejecutar sus solicitudes en orden. La variable productoId se completa al crear un producto.

## Permisos definitivos

| Usuario | Rol | Crear | Listar / consultar | Actualizar | Eliminar |
|---|---|---|---|---|---|
| Ricardo | ADMIN | Sí | Sí | Sí | Sí |
| Ana | USER | Sí | Sí | No | No |
| Luis | USER | Sí | Sí | No | No |

Enviar los encabezados Usuario y Rol respetando los valores anteriores. Son identidades simuladas para el ejercicio: estos encabezados no autentican realmente a una persona.

## Auditoría

La tabla auditoria_log guarda acción, método, fecha, detalle y usuario. Se registran CREAR con ID generado, ACTUALIZAR con ID, ELIMINAR con ID, LISTAR con cantidad, CONSULTAR con ID y ERROR con tipo y mensaje. El control de permisos se ejecuta antes de consultar o modificar productos. La auditoría usa una transacción independiente; si falla el registro de un error se conserva la excepción original y se informa por consola. Producto y auditoría de éxito no forman una operación atómica.

Los errores de validación del DTO y JSON rechazados por MVC ocurren antes del servicio: devuelven 400 y no pasan por el aspecto de errores del servicio.

## Verificar

`mvn test`

12 pruebas aprobadas: 11 pruebas HTTP con MockMvc y repositorios simulados, más una prueba con el contexto real de Spring, aspectos, JPA y persistencia H2 en modo MySQL. Incluyen CRUD, permisos, auditoría, validaciones, 401, 403, 404 y manejo de fallos al auditar errores. H2 solo se utiliza en pruebas.

No se ha validado la conexión con MySQL local, que no estaba escuchando en 3306. Las pruebas automatizadas no sustituyen las capturas de Postman y MySQL pedidas por la guía.

Tras ejecutar las peticiones de Postman, consultar:

```sql
SELECT id, accion, metodo, fecha, detalle, usuario
FROM auditoria_log ORDER BY id DESC;
```

El informe Word final incluye desarrollo, observaciones, conclusiones y alcance de la verificación; no presenta capturas de la guía como evidencia propia.
