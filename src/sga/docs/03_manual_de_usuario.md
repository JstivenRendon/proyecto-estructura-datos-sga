# Manual de usuario del SGA

## 1. Objetivo

Este manual describe cómo utilizar las áreas principales del Sistema de Gestión de Alojamiento (SGA). Está dirigido a la persona que opera el programa desde su interfaz gráfica y no necesita conocer los detalles internos del código.

> **Importante:** el manual se preparó a partir de los paneles y controles encontrados en el código fuente. La ventana principal presenta actualmente marcadores de conflicto de Git. Por eso, los pasos deben verificarse en la versión que compile y ejecute el equipo antes de entregar este manual. Inserta capturas reales donde se indican.

## 2. Inicio de la aplicación

El punto de entrada identificado es `gui.Main`, ubicado en `src/sga/gui/Main.java`.

En el README del repositorio se indica que, en Visual Studio Code, se puede abrir la carpeta del proyecto y ejecutar la configuración **SGA - gui.Main** con `F5`.

Al iniciar, la aplicación crea la ventana principal y carga un escenario de datos. Según el código revisado, se contempla la lectura de `datos/escenario.txt`; si esa lectura falla, se utiliza el escenario de prueba definido en `CargadorEscenario`.

**Captura 1. Ventana principal:** guardar en `capturas/01_inicio.png`.

## 3. Áreas principales

La ventana principal hace referencia a las siguientes áreas:

- Inicio: tablero de apartamentos y carga/restablecimiento del escenario.
- Reservas: búsqueda de disponibilidad y operaciones de reserva.
- Calendario: visualización de ocupación.
- Recepción: turnos y solicitudes del canal externo.
- Preparación: tareas pendientes y su prioridad.
- Mapa: representación del edificio y sus conexiones.
- Estadísticas: información resumida de la operación.

La disponibilidad real de cada pestaña depende de resolver y probar la integración final de `VentanaPrincipal`.

## 4. Consulta de disponibilidad y reservas

El panel `PanelReservas` contiene campos para documento, fecha de entrada, fecha de salida, cantidad de personas, criterio de orden y apartamento disponible. También incluye un selector de canal con las opciones Portal y Directa.

### 4.1 Buscar disponibilidad

1. Abrir la pestaña **Reservas**.
2. Escribir el documento del huésped.
3. Indicar la fecha de entrada y la fecha de salida.
4. Escribir la cantidad de personas.
5. Elegir el criterio de orden: precio, capacidad o dormitorios.
6. Pulsar **Buscar disponibilidad**.
7. Revisar la lista de apartamentos disponibles.

Las fechas deben escribirse en el formato `AAAA-MM-DD`, por ejemplo `2026-11-20`.

**Captura 2. Búsqueda de disponibilidad:** `capturas/02_disponibilidad.png`.

### 4.2 Crear una reserva

1. Realizar la búsqueda de disponibilidad.
2. Seleccionar un apartamento disponible.
3. Confirmar que el documento y las fechas sean correctos.
4. Elegir el canal correspondiente.
5. Pulsar **Crear reserva**.
6. Leer el resultado que muestra el panel.

El panel incluye los canales Portal y Directa. El comportamiento de cada canal debe revisarse según la versión funcional entregada.

**Captura 3. Creación de reserva:** `capturas/03_crear_reserva.png`.

### 4.3 Pagar anticipo

1. Identificar la reserva pendiente.
2. Utilizar la acción **Pagar anticipo**.
3. Revisar el mensaje de confirmación o error.

Las especificaciones establecen un anticipo del 30 % para las reservas del portal. Verificar el cálculo con una reserva de prueba.

**Captura 4. Anticipo:** `capturas/04_anticipo.png`.

### 4.4 Cancelar una reserva

1. Escribir el identificador de la reserva en el campo **ID de reserva**.
2. Pulsar **Cancelar reserva**.
3. Revisar el resultado que presenta el sistema.

Las especificaciones indican que el usuario debe poder conocer el valor de devolución antes de confirmar la cancelación. Comprueba que el flujo implementado muestre esa información.

### 4.5 Procesar vencimientos

El panel incluye la acción **Procesar vencimientos**. Se utiliza para procesar reservas pendientes cuyo plazo haya vencido, de acuerdo con la lógica implementada.

### 4.6 Procesar solicitudes externas

La ventana principal contiene una acción para procesar el canal externo. Las solicitudes externas se representan mediante `SolicitudExterna` y se encolan para ser atendidas en orden FIFO.

## 5. Calendario de ocupación

La interfaz incluye `PanelCalendarioOcupacion`, asociado a la visualización de ocupación por apartamento y fechas.

**Procedimiento:** abrir la pestaña Calendario, seleccionar o consultar el periodo disponible en la versión ejecutable y revisar las noches ocupadas, bloqueadas o libres que muestre la pantalla.

**Captura 5. Calendario:** `capturas/05_calendario.png`.

Completa este apartado con los controles exactos que aparezcan en la aplicación final.

## 6. Recepción y turnos

El servicio de recepción administra una lista circular de recepcionistas. Permite consultar el turno actual y avanzar al siguiente. También administra la cola de solicitudes externas.

**Procedimiento general:**
1. Abrir la pestaña Recepción.
2. Consultar el recepcionista del turno actual.
3. Usar la acción de avanzar turno cuando corresponda.
4. Consultar o procesar las solicitudes externas disponibles.

**Captura 6. Recepción:** `capturas/06_recepcion.png`.

## 7. Preparación de apartamentos

Las tareas de preparación se representan mediante `TareaPreparacion` y se organizan en una cola de prioridad. El criterio de prioridad se define mediante un comparador. Las especificaciones indican que los apartamentos con entrada programada para hoy deben atenderse primero.

**Procedimiento general:**
1. Abrir la pestaña Preparación.
2. Consultar la lista de tareas.
3. Revisar la prioridad y la hora estimada de llegada, si se muestra.
4. Seleccionar o despachar la siguiente tarea mediante el control disponible.
5. Confirmar que la tarea figure como terminada.

**Captura 7. Preparación:** `capturas/07_preparacion.png`.

## 8. Mapa del edificio

El panel `PanelMapa` representa el edificio como un grafo. Los nodos pueden representar apartamentos, recepción y puntos de servicio. Las conexiones representan pasillos con un peso.

El código de `PanelMapa` contempla:
- Dibujar nodos y pasillos.
- Mostrar los estados de los apartamentos.
- Resaltar una ruta calculada.
- Simular el cierre de un pasillo mediante un clic sobre la conexión.
- Identificar apartamentos que quedan inalcanzables desde recepción.
- Volver a abrir el pasillo.

**Procedimiento general:**
1. Abrir la pestaña Mapa.
2. Revisar los nodos y conexiones.
3. Consultar la ruta que se encuentre resaltada, si hay una seleccionada.
4. Probar el cierre de un pasillo en un escenario de prueba.
5. Observar los apartamentos que quedan aislados.
6. Restablecer la conexión antes de continuar.

**Captura 8. Mapa:** `capturas/08_mapa.png`.

## 9. Estadísticas

El repositorio contiene `ServicioEstadisticas` y la ventana principal hace referencia a una pestaña de estadísticas. Abre esa pestaña en la versión ejecutable y registra qué indicadores presenta realmente.

**Captura 9. Estadísticas:** `capturas/09_estadisticas.png`.

No describas indicadores que no aparezcan en la versión final.

## 10. Recomendaciones de uso

- Verificar las fechas antes de crear una reserva.
- Confirmar que la cantidad de personas no exceda la capacidad del apartamento.
- No asignar un apartamento con noches ya reservadas.
- Verificar que el apartamento esté preparado antes de realizar check-in.
- Revisar los mensajes de error cuando una operación no pueda completarse.
- Usar datos de prueba y no información personal real durante la demostración académica.

## 11. Problemas frecuentes

| Situación | Qué revisar |
|---|---|
| No inicia la aplicación | Revisar la configuración de Java, el punto de entrada y errores de compilación. |
| No aparece el escenario esperado | Verificar la ruta `datos/escenario.txt` y el formato de los registros. |
| No hay apartamentos disponibles | Revisar fechas, capacidad, estado operativo y reservas existentes. |
| Una reserva no se crea | Revisar documento del huésped, código del apartamento, fechas, capacidad y solapamientos. |
| No se procesa una solicitud externa | Confirmar que la cola tenga solicitudes pendientes. |
| No se muestra el mapa | Confirmar que el escenario haya cargado nodos y pasillos. |

## 12. Evidencias pendientes

Antes de entregar:
- Sustituir cada indicación de captura por una imagen real.
- Ajustar los pasos a los nombres exactos de botones de la versión final.
- Confirmar que cada operación descrita se puede ejecutar.
- Eliminar los apartados de funcionalidades que no estén disponibles en la versión final.
