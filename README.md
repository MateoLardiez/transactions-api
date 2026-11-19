# transactions-api

API REST en Spring Boot que almacena transacciones en memoria, permite consultarlas por tipo y calcula el monto total de una transacción sumando el de todas sus descendientes vinculadas mediante `parent_id`.

## Requerimientos

### Requerimientos funcionales

- **Almacenar una transacción**: un usuario puede registrar una transacción indicando su `id` (long, en la URL), `amount` (double), `type` (string) y, opcionalmente, `parent_id` (long) que la vincula con una transacción padre.
- **Consultar por tipo**: un usuario puede obtener la lista de **ids** de todas las transacciones de un tipo determinado.
- **Calcular la suma transitiva**: un usuario puede obtener la suma de los montos de una transacción **y de todas sus descendientes transitivas** (hijos, nietos, etc.).
  - Incluye el monto de la propia transacción.
  - Solo recorre hacia los descendientes, nunca hacia los ancestros.
  - Aplica a cualquier transacción: una sin hijos devuelve su propio monto.

### Reglas de negocio

- **Ids únicos**: no puede haber dos transacciones con el mismo `id`.
- **Padre existente**: si se informa `parent_id`, debe corresponder a una transacción ya registrada.
- **Sin autorreferencia**: `parent_id` no puede ser igual al `id` de la propia transacción.
- **Campos obligatorios**: `amount` y `type` son obligatorios; `type` no puede estar vacío.

### Requerimientos no funcionales

- **Tecnología**: Java 11 o superior (se usa Java 25), Spring Boot.
- **Persistencia**: en memoria, sin SQL.
- **Despliegue**: aplicación dockerizada.
- **Testing**: tests de integración obligatorios; desarrollo guiado por TDD.
- **Arquitectura**: separación en capas (controller, service, repository) aplicando principios SOLID.
- **Concurrencia**: el almacenamiento debe ser seguro ante requests simultáneas (thread-safe).
- **Eficiencia**: obtener los hijos de una transacción no debe requerir recorrer todas las transacciones.
- **Robustez**: el cálculo de la suma no debe fallar con cadenas largas de transacciones.

## API

### `PUT /transactions/{transaction_id}`

Registra una transacción.

```json
{ "amount": 5000, "type": "cars", "parent_id": 10 }
```

| Respuesta | Caso |
|---|---|
| `201 Created` `{ "status": "ok" }` | Transacción creada |
| `400 Bad Request` | Body inválido, `parent_id` inexistente o igual al propio id |
| `409 Conflict` | Ya existe una transacción con ese id |

### `GET /transactions/types/{type}`

Devuelve los ids de las transacciones del tipo indicado.

| Respuesta | Caso |
|---|---|
| `200 OK` `[10, 11]` | Lista de ids (vacía `[]` si no hay ninguna) |

### `GET /transactions/sum/{transaction_id}`

Devuelve la suma de la transacción y todas sus descendientes.

| Respuesta | Caso |
|---|---|
| `200 OK` `{ "sum": 20000 }` | Suma calculada |
| `404 Not Found` | La transacción no existe |

### Ejemplo

```
PUT /transactions/10 { "amount": 5000,  "type": "cars" }                       => { "status": "ok" }
PUT /transactions/11 { "amount": 10000, "type": "shopping", "parent_id": 10 }  => { "status": "ok" }
PUT /transactions/12 { "amount": 5000,  "type": "shopping", "parent_id": 11 }  => { "status": "ok" }

GET /transactions/types/cars => [10]
GET /transactions/sum/10     => { "sum": 20000 }   (10 + 11 + 12)
GET /transactions/sum/11     => { "sum": 15000 }   (11 + 12)
```

### Errores

Todos los errores se responden con el formato estándar `ProblemDetail` y `Content-Type: application/problem+json`:

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "Transaction with id 99 does not exist",
  "instance": "/transactions/sum/99"
}
```

Los errores de validación del body agregan el campo `errors`, con el motivo de cada campo inválido:

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Invalid request content.",
  "instance": "/transactions/10",
  "errors": { "amount": "must not be null" }
}
```

## Decisiones tomadas

- **PUT no reemplaza transacciones existentes (409)**. Aunque `PUT` suele significar "crear o reemplazar", el enunciado describe el id como el de una *nueva* transacción. Además, permitir reemplazos habilitaría cambiar el `parent_id` de una transacción existente y formar ciclos (por ejemplo 10 → 12 → 11 → 10), lo que rompería el cálculo de la suma.
- **El padre debe existir previamente (400)**. Junto con la decisión anterior, garantiza que las relaciones forman un arbol sin ciclos, por construcción.
- **Montos negativos permitidos**. El enunciado no los restringe y pueden representar devoluciones o ajustes.
- **Tipo inexistente devuelve `[]`**. Consultar un tipo sin transacciones no es un error, simplemente no hay resultados.
- **Suma calculada con recorrido iterativo** (BFS con una cola explícita) en lugar de recursión, para soportar cadenas de cualquier profundidad.
- **Java 25 y Spring Boot 4**. El enunciado pide Java 11 o superior; se eligió la última versión LTS de Java por tener el soporte más largo, junto con la versión estable actual de Spring Boot.
- **Maven con Maven Wrapper**. El proyecto incluye `./mvnw`, por lo que no hace falta tener Maven instalado para compilarlo.
- **`PUT` responde `201 Created` con `{ "status": "ok" }`**. El body respeta el ejemplo de la consigna. El código de estado no está especificado en la consigna, así que se sigue el estándar HTTP: un `PUT` que crea un recurso nuevo responde `201`. Como este `PUT` nunca reemplaza (los duplicados dan `409`), siempre responde `201`.
- **Errores con `ProblemDetail`**. Se usa el formato estándar de errores HTTP en lugar de uno propio. Spring lo soporta de forma nativa, así que los errores propios (404, 409, 400) y los internos de Spring (JSON mal formado, id no numérico) responden con la misma forma.

## Modelo de datos

A completar

## Compilar y ejecutar

### Local

#### Requisitos

- Java 25

#### Ejecutar

```bash
./mvnw spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

### Docker

A completar

## Tests

```bash
./mvnw test
```

## Estructura del sistema

A completar

## Posibles mejoras

- **Devolver el objeto creado en el `PUT`**. Es la convención habitual para un recurso recién creado y evita que el cliente tenga que consultarlo de nuevo. No se hizo para respetar el body `{ "status": "ok" }` que pide la consigna.
