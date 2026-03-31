# SMS Forwarder para MacroDroid + Render

Proyecto Spring Boot listo para desplegar en Render. Recibe mensajes desde MacroDroid, los guarda en PostgreSQL y los reenvía por correo.

## Qué hace

- Recibe un SMS por `POST /api/sms`
- Guarda el mensaje en PostgreSQL
- Lo reenvía por correo automáticamente
- Permite listar mensajes guardados por `GET /api/sms`
- Protege ambos endpoints con header `X-API-KEY`

## Requisitos

- Java 17
- Maven 3.9+
- Cuenta en Render
- Cuenta de correo SMTP (Gmail con App Password recomendado)

## Variables de entorno

Debes configurar estas variables en Render:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `APP_API_KEY`
- `APP_MAIL_TO`
- `APP_MAIL_FROM`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `MAIL_HOST` (por defecto `smtp.gmail.com`)
- `MAIL_PORT` (por defecto `587`)

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
  "message": "SMS guardado y reenviado",
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

> Los nombres exactos de las variables pueden cambiar según tu versión de MacroDroid. Toma la idea y selecciona las variables equivalentes del SMS recibido.

## Deploy en Render

### Opción rápida
1. Sube el proyecto a GitHub.
2. En Render, crea un **Blueprint** apuntando al repositorio.
3. Render leerá `render.yaml` y creará:
   - el servicio web
   - la base de datos PostgreSQL
4. Completa las variables faltantes de correo.
5. Despliega.

### Opción manual
1. Crea un Web Service.
2. Conecta tu repositorio.
3. Usa Dockerfile.
4. Crea una base de datos PostgreSQL en Render.
5. Carga las variables de entorno.

## Gmail App Password

Si usas Gmail:
1. Activa verificación en dos pasos en tu cuenta Google.
2. Genera una App Password.
3. Usa esa contraseña en `MAIL_PASSWORD`.

## Mejoras opcionales

- Agregar panel web con Thymeleaf
- Exportar mensajes a Excel
- Filtrar por remitente o fecha
- Reenviar también a otro webhook
- Agregar logs y auditoría
