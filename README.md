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

