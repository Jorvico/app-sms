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
