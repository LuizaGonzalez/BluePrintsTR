**Objetivo del laboratorio**
Implementar colaboración en tiempo real para el caso de BluePrints. El Front consume la API CRUD de la Parte 3 (o equivalente) y habilita tiempo real usando Socket.IO o STOMP, para que múltiples clientes dibujen el mismo plano de forma simultánea.

Al finalizar, el equipo debe:

1. Integrar el Front con su API CRUD (listar/crear/actualizar/eliminar planos, y total de puntos por autor).
2. Conectar el Front a un backend de tiempo real (Socket.IO o STOMP) siguiendo los repos guía.
3. Demostrar colaboración en vivo (dos pestañas navegando el mismo plano).

----

## **Plan de seguimiento**

Se completo el CRUD del backend,  agregamos las operaciones que faltaban
para actualizar un plano completo (reemplazando todos sus puntos de una vez)
y para eliminarlo, en cada una de las capas del proyecto, la interfaz de persistencia, 
sus dos implementaciones (en memoria y en PostgreSQL), el Service, y finalmente el Controller,
donde creamos los endpoints PUT Y DELETE y un nuev record que seria el body al actualizar los puntos.

# Endpoints CRUD — ARSW Blueprints API

| Método | Ruta | Descripción |
|---|---|---|
| GET | /api/v1/blueprints | Obtener todos los blueprints |
| GET | /api/v1/blueprints/{author} | Obtener todos los blueprints de un autor |
| GET | /api/v1/blueprints/{author}/{bpname} | Obtener un blueprint específico por autor y nombre |
| POST | /api/v1/blueprints | Crear un nuevo blueprint |
| PUT | /api/v1/blueprints/{author}/{bpname} | Actualizar (reemplazar) los puntos de un blueprint existente |
| PUT | /api/v1/blueprints/{author}/{bpname}/points | Agregar un punto a un blueprint existente |
| DELETE | /api/v1/blueprints/{author}/{bpname} | Eliminar un blueprint existente |

### Por qué STOMP y no Socket.IO

Se eligió STOMP porque se integra nativamente con Spring Boot, permitiendo reutilizar todo el dominio existente (modelo Blueprint/Point, 
capa de persistencia, servicios) sin necesidad de un servidor adicional. Esto reduce la complejidad operativa a un solo proceso corriendo 
en el puerto 8080, en lugar de mantener dos servidores separados

### Flujo de mensajes

El cliente publica un punto dibujado:
- Destino: /app/draw
- Cuerpo: { "author": "luiza", "name": "casa1", "point": { "x": 100, "y": 200 } }

El servidor persiste el punto en PostgreSQL y retransmite a todos los suscritos:
- Tópico: /topic/blueprints.{author}.{name}
- Ejemplo: /topic/blueprints.juan.casa1

# Cambios realizados a nivel de código

## Completamos la interfaz de persistencia

Se agregaron dos metodos abstractos nuevas a **BlueprintPersistence.java**:

- **updateBlueprint(String author, String name, List<Point> points)** : Para reemplazar todos los puntos de un plano existente.
- **deleteBlueprint(String author, String name)** : Para eliminar un plano completo.

## Agregamos setPoints al modelo Blueprint y a BlueprintEntity

Se agregó el método **setPoints(List<Point> newPoints)** a **Blueprint.java**. Este método limpia la lista de puntos actual
y la reemplaza con la nueva lista recibida. Es el metodo que usa la capa de persistencia para actualizar un plano.

En BlueprintEntity usa el método **addPoint** existente para agregar cada punto nuevo, manteniendo correctamente la relación 
bidireccional entre **BlueprintEntity** y **PointEntity** que requiere JPA.

## Implementamos los métodos nuevos en InMemoryBlueprintPersistence y en PostgresBlueprintPersistence

- updateBlueprint
- deleteBlueprint
- updateBlueprint
- deleteBlueprint: verifica que el plano exista con **existsByAuthorAndName** y lo elimina con **deleteByAuthorAndName**. 
  Se marcó con **@Transactional** porque JPA requiere una transacción activa para operaciones de borrado.

## Agregamos los métodos al Service

Se agregaron **updateBlueprint** y **deleteBlueprint** a **BlueprintsServices.java**. Estos métodos simplemente delegan la 
llamada a la capa de persistencia, siguiendo el mismo patrón del resto del Service.

## Agregamos los endpoints al Controller

- **PUT /api/v1/blueprints/{author}/{bpname}** — recibe un cuerpo JSON con la lista de puntos nueva y llama 
  a **services.updateBlueprint**. Se creó el record **UpdateBlueprintRequest** para definir la forma del body esperado.
- **DELETE /api/v1/blueprints/{author}/{bpname}** — elimina el plano y responde con código 204 (No Content).

## Configurar CORS

Se creó **WebConfig.java** en el paquete config. Esta clase le dice a Spring que permita peticiones HTTP 
desde **http://localhost:5173** (el front en desarrollo) para todos los métodos: GET, POST, PUT, DELETE y OPTIONS. 
Sin esta clase, el navegador bloquea todas las peticiones del front al backend.

## Agregamos la dependencia de WebSocket

Se agregó **spring-boot-starter-websocket** al **pom.xml**. Esta dependencia incluye todo lo necesario para habilitar STOMP sobre WebSocket en Spring Boot.

## Configurar WebSocket y STOMP

Se creó **WebSocketConfig.java** en el paquete config. Esta clase habilita el broker de mensajería STOMP y define:

- El endpoint de conexión WebSocket: **/ws-blueprints**.
- El prefijo para mensajes del cliente al servidor: **/app**.
- El prefijo para mensajes del servidor a los clientes suscritos: **/topic**.

## Crear el controlador de tiempo real

Se creó **BlueprintWSController.java**. Esta clase recibe los mensajes de dibujo que llegan al destino **/app/draw**,
persiste el punto en PostgreSQL usando el Service existente, y retransmite el evento completo al 
tópico **/topic/blueprints.{author}.{name}** para que todos los clientes suscritos a ese plano lo reciban en tiempo real.