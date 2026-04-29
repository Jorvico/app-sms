# SMS Forwarder para MacroDroid + Render

Proyecto Spring Boot listo para desplegar en Render. Recibe mensajes desde MacroDroid, los guarda en PostgreSQL y los reenvía por correo usando API HTTPS con Resend.

## Qué hace

- Recibe un SMS por `POST /api/sms`
- Guarda el mensaje en PostgreSQL
- Lo reenvía por correo con Resend API HTTPS
- Permite listar mensajes guardados por `GET /api/sms`
- Protege ambos endpoints con header `X-API-KEY`

## Requisitos

- Java 17
- Maven 3.9+
- Cuenta en Render
- Cuenta en Resend con API key
- Dominio verificado en Resend para `APP_MAIL_FROM`

## Variables de entorno

Debes configurar estas variables en Render:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `APP_API_KEY`
- `RESEND_API_KEY`
- `APP_MAIL_TO`
- `APP_MAIL_FROM`
- `APP_MAIL_PROVIDER` = `resend`

Ejemplo de remitente:

```text
APP_MAIL_FROM=Notificaciones <notificaciones@tudominio.com>
```

## Ejecución local

```bash
mvn spring-boot:run
```

## Build local

```bash
mvn clean package
java -jar target/sms-forwarder-render-1.0.0.jar
```

## Endpoint para recibir SMS

### Request

`POST /api/sms`

Headers:

```http
Content-Type: application/json
X-API-KEY: TU_API_KEY
```

Body:

```json
{
  "sender": "Banco",
  "phoneNumber": "+573001234567",
  "message": "Compra aprobada por $50.000",
  "receivedAt": "2026-03-31T10:15:00"
}
```

### Response exitosa

```json
{
  "ok": true,
  "message": "SMS guardado y enviado por API HTTPS",
  "data": {
    "id": 1,
    "sender": "Banco",
    "phoneNumber": "+573001234567",
    "message": "Compra aprobada por $50.000",
    "receivedAt": "2026-03-31T10:15:00",
    "createdAt": "2026-03-31T10:15:05"
  }
}
```

## Endpoint para listar SMS

`GET /api/sms?page=0&size=20`

Header:

```http
X-API-KEY: TU_API_KEY
```

## Configuración sugerida en MacroDroid

### Trigger
- SMS Received

### Action
- HTTP Request
- Method: `POST`
- URL: `https://TU-SERVICIO.onrender.com/api/sms`
- Headers:
  - `Content-Type: application/json`
  - `X-API-KEY: TU_API_KEY`

Body JSON:

```json
{
  "sender": "[sender]",
  "phoneNumber": "[sender_number]",
  "message": "[sms_message]",
  "receivedAt": "[year]-[month]-[day]T[hour24]:[minute]:[second]"
}
```

## Deploy en Render

### Opción rápida
1. Sube el proyecto a GitHub.
2. En Render, crea un **Blueprint** apuntando al repositorio.
3. Render leerá `render.yaml` y creará el servicio y la base de datos.
4. Completa `RESEND_API_KEY`, `APP_MAIL_TO` y `APP_MAIL_FROM`.
5. Despliega.

### Opción manual
1. Crea un Web Service.
2. Conecta tu repositorio.
3. Usa el `Dockerfile`.
4. Crea una base de datos PostgreSQL en Render.
5. Carga las variables de entorno.

## Variables mínimas en Render

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://HOST:5432/DB
SPRING_DATASOURCE_USERNAME=USER
SPRING_DATASOURCE_PASSWORD=PASSWORD
APP_API_KEY=tu_api_key
RESEND_API_KEY=re_xxxxx
APP_MAIL_TO=tu_correo@dominio.com
APP_MAIL_FROM=Notificaciones <notificaciones@tudominio.com>
APP_MAIL_PROVIDER=resend
```

## Nota sobre Resend

Para enviar a otros destinatarios, `APP_MAIL_FROM` debe usar un dominio verificado en Resend.

## Cambios PRO v3 - sucursales y correo robusto

Esta versión incluye:

- Nueva tabla `branches` con sucursales iniciales:
  - `1 | GLOBAL FARMA PR | Prado`
  - `2 | GLOBAL FARMA QU | Quintanares`
  - `3 | GLOBAL FARMA LA | Fragua`
  - `4 | RED FARMA | Redfarma`
  - `5 | PORTAL DE SAN | San Ignacio`
- Seed automático de sucursales con `BranchDataInitializer`.
- Extracción automática por Regex de:
  - valor del pago: texto entre `por` y `con comision`.
  - sucursal: texto entre `, a` y `en la terminal`.
- Normalización automática:
  - `DROGUERIA GLOBAL FARMA PR` → `GLOBAL FARMA PR` → `Prado`.
  - `FARMACENTER PORTAL DE SAN` → `PORTAL DE SAN` → `San Ignacio`.
- Nuevo asunto de correo:
  - `Pago por $xx.xxx en FullName`
  - ejemplo: `Pago por $8.500 en Quintanares`.
- El SMS siempre se guarda primero en base de datos.
- El correo se intenta enviar en segundo plano con `@Async`.
- Si Resend falla o se demora, la API no debe devolver 500 por ese motivo.

### Nuevas columnas en `sms_messages`

Con `spring.jpa.hibernate.ddl-auto=update`, Hibernate agrega automáticamente:

- `payment_amount`
- `extracted_branch_name`
- `branch_id`

### Respuesta POST esperada

```json
{
  "ok": true,
  "message": "SMS guardado. El correo se intenta enviar en segundo plano",
  "data": {
    "id": 346,
    "paymentAmount": 8500,
    "branchName": "GLOBAL FARMA QU",
    "branchFullName": "Quintanares"
  }
}
```

## Cambios PRO v4 - cola de correo con reintentos y sin duplicados

Esta versión cambia el flujo de envío de correo para hacerlo más confiable:

```text
MacroDroid -> API -> guardar SMS -> responder 201 rápido
                         |
                         v
                scheduler reintenta correo
```

### Estados de correo

La tabla `sms_messages` ahora incluye:

- `email_status`: `PENDING`, `SENDING`, `SENT`, `FAILED`
- `email_retry_count`
- `email_last_error`
- `email_sent_at`
- `next_email_retry_at`

### Regla principal

- El SMS se guarda siempre.
- La API responde rápido a MacroDroid.
- El correo se envía por un scheduler cada 5 segundos.
- Si Resend falla, se marca `FAILED` y se reintenta.
- Si ya está `SENT`, no se vuelve a enviar.
- Se usa el estado `SENDING` para evitar duplicados mientras se procesa.

### Asunto del correo

Formato:

```text
Pago $8.500 en Prado [02:35 PM]
```

Esto ayuda a que Gmail no agrupe pagos del mismo valor en la misma conversación.

### Cuerpo del correo

El cuerpo del correo solo incluye:

- Mensaje original
- Fecha recibido

### Consulta SQL útil

```sql
SELECT id, email_status, email_retry_count, email_last_error, email_sent_at, next_email_retry_at, created_at
FROM sms_messages
ORDER BY created_at DESC;
```

### Configuración opcional

Puedes cambiar la frecuencia del scheduler en Render:

```text
APP_EMAIL_RETRY_FIXED_DELAY_MS=5000
```

Por defecto revisa cada 5 segundos.
