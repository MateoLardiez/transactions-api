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

Con la aplicación corriendo, la documentación de la API está disponible en:

- **Swagger UI** (interactiva, permite probar los endpoints): `http://localhost:8080/swagger-ui.html`
- **OpenAPI** (especificación en JSON): `http://localhost:8080/v3/api-docs`

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

Los errores del body (campos faltantes o con un tipo de dato incorrecto) agregan el campo `errors`, con el motivo de cada campo inválido:

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
- **Los tipos distinguen mayúsculas**. `cars` y `Cars` son tipos distintos. La consigna no lo especifica y la comparación exacta es la más predecible.
- **Suma calculada con recorrido iterativo** (BFS con una cola explícita) en lugar de recursión, para soportar cadenas de cualquier profundidad.
- **Java 25 y Spring Boot 4**. El enunciado pide Java 11 o superior; se eligió la última versión LTS de Java por tener el soporte más largo, junto con la versión estable actual de Spring Boot.
- **Maven con Maven Wrapper**. El proyecto incluye `./mvnw`, por lo que no hace falta tener Maven instalado para compilarlo.
- **`PUT` responde `201 Created` con `{ "status": "ok" }`**. El body respeta el ejemplo de la consigna. El código de estado no está especificado en la consigna, así que se sigue el estándar HTTP: un `PUT` que crea un recurso nuevo responde `201`. Como este `PUT` nunca reemplaza (los duplicados dan `409`), siempre responde `201`.
- **Errores con `ProblemDetail`**. Se usa el formato estándar de errores HTTP en lugar de uno propio. Spring lo soporta de forma nativa, así que los errores propios (404, 409, 400) y los internos de Spring (JSON mal formado, id no numérico) responden con la misma forma.
- **La imagen de Docker corre los tests al construirse**. El build ejecuta `./mvnw package` sin `-DskipTests`, así que si algún test falla no se genera la imagen. Tarda más en construir, pero garantiza que nunca se empaquete código con tests en rojo.
- **Sin Docker Compose**. Compose sirve para coordinar varios servicios (por ejemplo, la app junto a una base de datos). Este proyecto tiene un único servicio sin dependencias externas, así que solo serviría para juntar `docker build` y `docker run` en un comando. Si en el futuro se agregara una base de datos, ahí sí tendría sentido.
- **`amount` debe ser un número JSON**. Por defecto Jackson convierte strings numéricos (`"amount": "2"`) en números sin avisar. Se desactivó esa conversión porque el monto es el dato crítico de una transacción: un valor con formato ambiguo se rechaza con `400` en lugar de interpretarse en silencio.
- **Campos desconocidos rechazados**. Por defecto Jackson ignora los campos que no reconoce. Eso hacía que un `"parentId"` (en lugar de `"parent_id"`) se ignorara y la transacción se creara sin padre, sin ningún aviso al cliente. Ahora un campo desconocido responde `400` con `"errors": { "parentId": "unknown field" }`. Omitir `parent_id` sigue siendo válido.

## Modelo de datos

Una transacción es un `record` inmutable:

```java
public record Transaction(long id, double amount, String type, Long parentId) {}
```

`parentId` es `Long` (objeto) porque puede ser `null` cuando la transacción no tiene padre.

Cada transacción tiene como máximo un padre, y un padre puede tener muchos hijos. Como el padre debe existir antes que el hijo y una transacción nunca se reemplaza, las relaciones forman un conjunto de árboles sin ciclos.

El almacenamiento en memoria usa dos `ConcurrentHashMap`, con el ejemplo de la consigna:

```
transactionsById                 childrenIdsByParentId
10 → Transaction(10, 5000, ...)  10 → {11}
11 → Transaction(11, 10000, ...) 11 → {12}
12 → Transaction(12, 5000, ...)
```

- `transactionsById` busca una transacción por id.
- `childrenIdsByParentId` es un índice para obtener los hijos directos de una transacción sin recorrer todas. Cumple el mismo rol que un índice sobre la columna `parent_id` en una base de datos.

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

#### Requisitos

- Docker

#### Ejecutar

```bash
docker build -t transactions-api .
docker run --rm -p 8080:8080 transactions-api
```

La API queda disponible en `http://localhost:8080`.

El `Dockerfile` es multi-stage: la primera etapa compila el Jar y corre los tests con el JDK; la segunda solo contiene el JRE y el Jar, por lo que la imagen final es más liviana.

Las dependencias de Maven se descargan en una capa separada del código fuente, así que Docker las reutiliza de su caché mientras el `pom.xml` no cambie. Un cambio solo en el código reconstruye la imagen en unos 30 segundos en lugar de unos 2 minutos.

## Tests

```bash
./mvnw test
```

El proyecto se desarrolló con TDD: cada funcionalidad empezó con un test en rojo. Hay dos tipos de tests:

- **Unitarios**: prueban el modelo, el repositorio y el servicio de forma aislada, sin levantar Spring. Los del servicio usan el repositorio en memoria real en lugar de mocks, para verificar comportamiento y no detalles de implementación. Incluyen un caso con una cadena de 100.000 transacciones que verifica que la suma no provoca `StackOverflowError`.
- **De integración** (`TransactionControllerIntegrationTest`): levantan la aplicación completa y envían requests HTTP con `MockMvc`. Cubren los tres endpoints, todos los códigos de error y el ejemplo completo de la consigna. Cada test arranca con la aplicación limpia (`@DirtiesContext`), porque el repositorio en memoria es compartido.

## Estructura del sistema

La aplicación está organizada en capas. Cada capa solo conoce a la de abajo:

```
HTTP → Controller → Service → Repository
```

```
src/main/java/com/challenge/transactions/
├── config/
│   └── OpenApiConfig.java              # Información general de la documentación OpenAPI
├── controller/
│   ├── TransactionController.java      # Endpoints REST: traduce HTTP ↔ Java
│   ├── GlobalExceptionHandler.java     # Traduce excepciones a respuestas ProblemDetail
│   └── dto/                            # Formato de requests y responses de la API
├── service/
│   └── TransactionService.java         # Reglas de negocio y cálculo de la suma (BFS)
├── repository/
│   ├── TransactionRepository.java      # Contrato de acceso a datos
│   └── InMemoryTransactionRepository.java  # Implementación con ConcurrentHashMap
├── model/
│   └── Transaction.java                # Entidad de dominio
└── exception/                          # Excepciones de dominio, una por regla
```

- **Controller**: recibe el request, valida su forma (campos obligatorios, tipos) y delega en el servicio. No contiene lógica de negocio.
- **Service**: aplica las reglas que dependen del estado guardado (duplicados, padre existente, autorreferencia) y recorre el árbol para la suma.
- **Repository**: guarda y busca datos. El servicio depende de la interfaz `TransactionRepository`, no de la implementación, así que el almacenamiento se puede reemplazar (por ejemplo, por una base de datos) sin tocar la lógica de negocio.
- **Exceptions**: el servicio lanza errores en términos de negocio ("el padre no existe"), y `GlobalExceptionHandler` es el único lugar que decide qué código HTTP les corresponde.

## Posibles mejoras

- **Devolver el objeto creado en el `PUT`**. Es la convención habitual para un recurso recién creado y evita que el cliente tenga que consultarlo de nuevo. No se hizo para respetar el body `{ "status": "ok" }` que pide la consigna.
- **Usar `BigDecimal` para los montos**. `double` es punto flotante binario y no representa exactamente algunos decimales (`0.1 + 0.2` da `0.30000000000000004`). Para dinero real conviene `BigDecimal`, que es decimal exacto. Se usó `double` porque es el tipo que especifica la consigna. `BigDecimal` además resolvería el overflow: con montos extremos (dos transacciones de `1e308`) la suma supera el máximo de un `double` y la respuesta es `{ "sum": "Infinity" }`, un string donde el contrato promete un número.
- **Persistencia en una base de datos**. Hoy los datos se pierden al reiniciar la aplicación. Bastaría con una nueva implementación de `TransactionRepository`. Con SQL, la suma se podría resolver en una sola consulta recursiva (`WITH RECURSIVE`).
- **Índice por tipo**. `findIdsByType` recorre todas las transacciones (O(n)). Un segundo índice `type → ids`, como el de hijos, lo haría directo.
- **Rechazar números en campos de texto**. Hoy `"type": 123` se acepta y se guarda como `"123"`, porque la configuración que vuelve estricto a `amount` solo cubre la conversión de string a número. Se podría rechazar con un customizer de Jackson. No se priorizó porque no afecta los montos ni las sumas.
- **Correr el contenedor con un usuario sin privilegios**. Hoy la app corre como `root` dentro del contenedor. Por el principio de mínimo privilegio, conviene crear un usuario de sistema, así una vulnerabilidad en la app tendría un impacto más acotado.
