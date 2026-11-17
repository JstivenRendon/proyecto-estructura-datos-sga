# Plan de pruebas y verificación del SGA

## 1. Objetivo

Este documento sirve para registrar pruebas funcionales y técnicas antes de la sustentación. Los resultados deben completarse ejecutando la versión final del programa. No se asignan resultados aprobados sin evidencia.

## 2. Formato de registro

Para cada prueba, registrar:
- Fecha.
- Persona que ejecutó la prueba.
- Versión o commit probado.
- Datos de entrada.
- Resultado esperado.
- Resultado obtenido.
- Estado: Pendiente / Aprobada / Fallida.
- Evidencia (captura, log o referencia).

## 3. Casos de prueba funcionales

| ID | Prueba | Resultado esperado | Estado |
|---|---|---|---|
| CP-01 | Iniciar la aplicación | Se abre la ventana principal sin errores. | Pendiente |
| CP-02 | Cargar escenario de prueba | Se cargan los datos previstos. | Pendiente |
| CP-03 | Buscar disponibilidad con fechas válidas | Se listan apartamentos que cumplen fechas y capacidad. | Pendiente |
| CP-04 | Buscar con fechas inválidas | Se informa el error y no se crea una reserva. | Pendiente |
| CP-05 | Intentar reservar un apartamento con solapamiento | La operación se rechaza. | Pendiente |
| CP-06 | Crear reserva por portal | La reserva queda en el estado definido por las reglas del portal. | Pendiente |
| CP-07 | Pagar anticipo de portal | Se registra el anticipo correspondiente al 30 % de la cotización. | Pendiente |
| CP-08 | Cotizar una estancia que cruza temporadas | El desglose aplica la tarifa de cada noche según su temporada. | Pendiente |
| CP-09 | Cotizar grupo con menores de seis años | Los menores ocupan capacidad, pero no generan cobro. | Pendiente |
| CP-10 | Procesar solicitudes externas | Se atienden en orden FIFO. | Pendiente |
| CP-11 | Consultar turnos de recepción | El turno avanza de forma circular. | Pendiente |
| CP-12 | Deshacer operación del folio | Se registra un movimiento contrario; no se elimina el original. | Pendiente |
| CP-13 | Procesar vencimientos | Las reservas pendientes vencidas se cancelan según la regla de 24 horas. | Pendiente |
| CP-14 | Registrar check-in | Solo se permite cuando el apartamento está preparado. | Pendiente |
| CP-15 | Registrar check-out | El apartamento pasa al flujo de preparación. | Pendiente |
| CP-16 | Despachar tarea de preparación | Se respeta el orden de prioridad configurado. | Pendiente |
| CP-17 | Calcular ruta en el mapa | Dijkstra devuelve una ruta de menor peso o lista vacía si no hay camino. | Pendiente |
| CP-18 | Cerrar un pasillo | Se identifican los apartamentos que quedan inalcanzables desde recepción. | Pendiente |
| CP-19 | Recorrer calendario | Se muestra la ocupación correspondiente al periodo seleccionado. | Pendiente |
| CP-20 | Consultar estadísticas | Se muestran los indicadores disponibles y coherentes con los datos de prueba. | Pendiente |

## 4. Pruebas de estructuras de datos

| ID | Estructura | Verificación | Estado |
|---|---|---|---|
| ED-01 | Lista enlazada | Insertar al inicio/final, consultar, eliminar y recorrer. | Pendiente |
| ED-02 | Pila | Apilar, consultar cima y desapilar en orden LIFO. | Pendiente |
| ED-03 | Cola | Encolar y desencolar en orden FIFO. | Pendiente |
| ED-04 | Cola de prioridad | Insertar y extraer respetando el comparador. | Pendiente |
| ED-05 | Árbol binario | Insertar, buscar, eliminar y recorrer inorden/preorden/postorden. | Pendiente |
| ED-06 | Lista circular | Avanzar turnos y regresar al primer elemento al completar el ciclo. | Pendiente |
| ED-07 | Conjunto | No permitir duplicados y comprobar pertenencia. | Pendiente |
| ED-08 | Grafo | Agregar vértices/aristas y comprobar DFS, BFS, alcanzabilidad y Dijkstra. | Pendiente |

## 5. Evidencias

Guardar capturas o archivos de evidencia en esta carpeta, por ejemplo:
- `capturas/prueba_busqueda.png`
- `capturas/prueba_reserva.png`
- `capturas/prueba_folio.png`
- `capturas/prueba_mapa.png`

No incluir datos personales reales.

## 6. Hallazgos técnicos iniciales

Durante la revisión del repositorio en la rama `estructura`, se encontraron marcadores de conflicto de Git dentro de `src/sga/gui/VentanaPrincipal.java`. Este hallazgo debe resolverse antes de ejecutar las pruebas de integración y de afirmar que la interfaz completa funciona.

También debe verificarse que los paneles integrados compartan la misma instancia de `SistemaAlojamiento`, que las notificaciones de cambios refresquen las vistas y que la carga de escenarios actualice todos los paneles que dependen del sistema.

## 7. Resumen de resultados

Completar después de ejecutar las pruebas:

- Pruebas funcionales aprobadas: [ ]
- Pruebas funcionales fallidas: [ ]
- Pruebas pendientes: [ ]
- Defectos encontrados: [ ]
- Commit probado: [ ]
- Conclusión: [Completar]
