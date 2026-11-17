# Sistema de Gestión de Alojamiento (SGA)
## Documento de descripción general

**Proyecto:** Sistema de Gestión de Alojamiento (SGA)  
**Alojamiento de referencia:** Aparta-Suites UQ, Salento, Quindío  
**Asignatura:** [Completar]  
**Integrantes:** [Completar]  
**Docente:** [Completar]  
**Institución:** [Completar]  
**Fecha:** [Completar]

---

## 1. Introducción

El Sistema de Gestión de Alojamiento (SGA) es una aplicación de escritorio desarrollada en Java para apoyar la administración de un alojamiento turístico compuesto por apartamentos independientes. El proyecto toma como escenario de referencia Aparta-Suites UQ, ubicado en Salento, Quindío.

El sistema reúne operaciones relacionadas con apartamentos, huéspedes, reservas, temporadas, cobros, recepción, preparación de apartamentos y recorridos dentro del edificio. Su propósito académico es aplicar estructuras de datos genéricas implementadas por el equipo para resolver necesidades concretas del dominio.

## 2. Contexto y problema

La operación manual de un alojamiento puede producir reservas duplicadas, errores al calcular tarifas, falta de coordinación en la preparación de apartamentos y demoras al localizar información. También puede ser difícil planear recorridos del personal cuando existen varios apartamentos pendientes de limpieza o preparación.

El SGA plantea una solución centralizada para representar la información del alojamiento y ejecutar operaciones de consulta y gestión con apoyo de estructuras de datos.

## 3. Justificación

El proyecto permite estudiar cómo las estructuras de datos se aplican a un problema realista. En lugar de limitarse a implementar estructuras aisladas, cada una participa en una función del sistema: los árboles apoyan búsquedas y detección de solapamientos; las colas organizan solicitudes; las colas de prioridad ordenan tareas; la pila permite registrar operaciones reversas; y el grafo representa el edificio y sus recorridos.

Además, la aplicación integra lógica de negocio con una interfaz gráfica de escritorio.

## 4. Objetivos

### 4.1 Objetivo general

Desarrollar una aplicación de escritorio para apoyar la gestión de un alojamiento turístico, aplicando estructuras de datos genéricas implementadas por el equipo para administrar apartamentos, huéspedes, reservas, cobros y procesos operativos.

### 4.2 Objetivos específicos

- Representar apartamentos, huéspedes, reservas y otros elementos del dominio mediante clases Java.
- Consultar apartamentos disponibles para una estancia y una cantidad de ocupantes.
- Gestionar reservas recibidas por diferentes canales y controlar sus estados.
- Calcular cotizaciones por noche considerando temporadas y ocupantes que pagan.
- Registrar cargos y pagos en el folio de cada reserva.
- Organizar solicitudes externas y tareas de preparación mediante colas.
- Administrar turnos de recepción mediante una lista circular.
- Representar conexiones del edificio mediante un grafo y calcular rutas de menor peso.
- Aplicar árboles binarios de búsqueda, listas enlazadas, pilas, conjuntos y otras estructuras propias.

## 5. Alcance funcional

De acuerdo con las especificaciones del proyecto y los componentes identificados en el repositorio, el alcance contempla:

1. **Gestión de apartamentos:** identificación por código, características, capacidad, tarifa base y estado operativo.
2. **Gestión de huéspedes:** datos personales, favoritos e historial.
3. **Reservas:** consulta de disponibilidad, creación, consulta, cancelación y cambios de estado.
4. **Canales:** reservas de portal, directas y solicitudes externas simuladas.
5. **Cotización y cobros:** cálculo noche por noche, anticipo y movimientos de folio.
6. **Temporadas:** aplicación de multiplicadores de tarifa según la temporada.
7. **Recepción:** turnos de recepcionistas y procesamiento de solicitudes.
8. **Preparación:** cola de tareas priorizada según las condiciones definidas.
9. **Mapa:** nodos y pasillos representados con un grafo, cálculo de rutas y análisis de conectividad.
10. **Visualización:** interfaz Swing con pestañas para las áreas operativas, según la composición implementada en la interfaz.

El alcance no implica que exista integración real con plataformas externas de reservas o pasarelas bancarias: el canal externo se maneja como una simulación dentro del proyecto.

## 6. Reglas de negocio principales

Las siguientes reglas proceden del archivo de especificaciones del repositorio:

- Cada apartamento se reserva completo para un solo grupo; no se venden dormitorios por separado.
- La estancia se define con fecha de entrada y fecha de salida.
- La noche de salida no se cobra ni se ocupa. Por ejemplo, del día 10 al 12 se cuentan dos noches.
- El cobro se calcula por persona y por noche.
- Los ocupantes menores de seis años ocupan capacidad, pero no pagan.
- Se manejan temporadas baja, media y alta; las tarifas pueden variar entre temporadas.
- Las reservas activas de un apartamento no pueden compartir noches.
- Una reserva de portal queda pendiente hasta el pago del anticipo del 30 %.
- Las solicitudes externas se reciben en una cola FIFO y se procesan en orden de llegada.
- Al realizar check-out, el apartamento debe pasar por preparación antes de recibir un nuevo grupo.
- El check-in solo debe permitirse cuando el apartamento esté preparado.
- Los movimientos del folio no se eliminan; una corrección se registra mediante un movimiento contrario.
- Las reservas pendientes tienen un vencimiento definido de 24 horas en las especificaciones.

**Nota de verificación:** la existencia de una regla en las especificaciones no demuestra por sí sola que todos sus detalles estén implementados y probados en la versión actual. La sección de pruebas debe registrar el resultado observado para cada regla.

## 7. Requisitos funcionales

| Código | Requisito |
|---|---|
| RF-01 | Registrar y consultar apartamentos. |
| RF-02 | Registrar y consultar huéspedes. |
| RF-03 | Buscar apartamentos disponibles para fechas y cantidad de personas. |
| RF-04 | Ordenar resultados de disponibilidad por criterios alternativos, como precio, capacidad o dormitorios. |
| RF-05 | Crear reservas por los canales contemplados en el sistema. |
| RF-06 | Validar que una reserva no se solape con otra reserva activa del mismo apartamento. |
| RF-07 | Consultar y actualizar el estado de una reserva respetando sus transiciones permitidas. |
| RF-08 | Calcular el valor de una estancia con desglose por noche y temporada. |
| RF-09 | Registrar anticipos, cargos y pagos en el folio. |
| RF-10 | Registrar y procesar solicitudes externas en orden FIFO. |
| RF-11 | Gestionar tareas de preparación mediante una cola de prioridad. |
| RF-12 | Administrar los turnos de recepción. |
| RF-13 | Representar el edificio como un grafo y calcular rutas. |
| RF-14 | Cargar un escenario de prueba desde archivo de texto o desde el escenario definido en código. |
| RF-15 | Presentar información mediante una interfaz gráfica Swing. |

## 8. Requisitos no funcionales

- **Usabilidad:** la interfaz debe organizar las operaciones en pantallas comprensibles.
- **Mantenibilidad:** las responsabilidades se distribuyen en paquetes y clases.
- **Reutilización:** las estructuras de datos se implementan con genéricos.
- **Integridad:** las operaciones deben respetar las reglas de fechas, capacidad, estados y disponibilidad.
- **Portabilidad:** al estar desarrollado en Java, puede ejecutarse en entornos con un JDK compatible y configuración adecuada.
- **Trazabilidad:** el repositorio Git permite registrar cambios y versiones del proyecto.

## 9. Arquitectura y organización del código

El código fuente se encuentra bajo `src/sga/` y se organiza en los siguientes paquetes (los nombres de paquete declarados por los archivos Java son `gui`, `modelo`, etc.):

| Paquete | Responsabilidad observada |
|---|---|
| `modelo` | Entidades del dominio: apartamento, huésped, reserva, estancia, folio, temporada, nodo del edificio, tarea de preparación y otras clases relacionadas. |
| `estructuras` | Implementaciones propias de listas, pilas, colas, árboles, conjuntos y grafos. |
| `servicios` | Lógica de gestión, validación de disponibilidad, cotización, reservas, recepción y estadísticas. |
| `gui` | Punto de entrada, ventana principal y paneles Swing. |
| `persistencia` | Lectura de escenarios desde archivo y carga de un escenario de prueba en código. |
| `comparadores` | Criterios de ordenamiento alternativos. |
| `excepciones` | Excepciones específicas del dominio y de las estructuras. |
| `pruebas` | Código de pruebas disponible en el repositorio; revisar los archivos concretos al preparar el informe final. |

La clase `SistemaAlojamiento` funciona como punto de coordinación de las entidades y servicios centrales. La interfaz gráfica utiliza una instancia del sistema para mostrar y ejecutar operaciones.

## 10. Estructuras de datos destacadas

| Estructura | Aplicación identificada |
|---|---|
| Lista doblemente enlazada | Colecciones de elementos del dominio y soporte para otras estructuras. |
| Pila | Operaciones de deshacer del folio. |
| Cola FIFO | Solicitudes del canal externo. |
| Cola de prioridad | Tareas de preparación y reservas pendientes por vencer. |
| Árbol binario de búsqueda | Organización y búsqueda de elementos comparables; reservas de un apartamento para detectar solapamientos. |
| Lista circular | Rotación de turnos de recepción. |
| Conjunto | Favoritos de huéspedes y elementos visitados en recorridos del grafo. |
| Grafo ponderado | Mapa del edificio, conectividad y rutas de menor peso. |

El detalle técnico de cada estructura se desarrolla en `02_estructuras_de_datos.md`.

## 11. Interfaz gráfica

El punto de entrada identificado es `gui.Main`. Este inicia `VentanaPrincipal` en el hilo de eventos de Swing.

En el código de la ventana principal se encuentran referencias a áreas como Inicio, Reservas, Calendario, Recepción, Preparación, Mapa y Estadísticas. Los paneles especializados incluyen `PanelReservas`, `PanelCalendarioOcupacion` y `PanelMapa`.

**Advertencia sobre el estado del repositorio revisado:** `src/sga/gui/VentanaPrincipal.java` contiene actualmente marcadores de conflicto de Git (`<<<<<<<`, `=======`, `>>>>>>>`). Antes de afirmar que todas las pestañas están integradas o que la aplicación compila, se debe resolver ese conflicto y ejecutar el programa.

## 12. Persistencia y carga de escenarios

Se identifican dos mecanismos:

- `CargadorEscenario.cargar()`: construye un escenario inicial de prueba directamente en código, con apartamentos, huéspedes, recepcionistas, temporadas, nodos, pasillos, reservas y tareas de preparación.
- `LectorEscenario.cargar(ruta)`: lee un archivo de texto plano con registros separados por punto y coma. Reconoce tipos de registro como `APARTAMENTO`, `HUESPED`, `RECEPCIONISTA`, `TEMPORADA`, `NODO`, `PASILLO`, `RESERVA`, `FAVORITO`, `SOLICITUD_EXTERNA` y `PREPARACION`.

No se debe describir este mecanismo como una base de datos relacional: lo observado es carga de escenarios desde memoria/código y archivo plano.

## 13. Limitaciones y aspectos por validar

- Resolver los marcadores de conflicto de `VentanaPrincipal.java`.
- Confirmar la integración final de todos los paneles y eventos.
- Ejecutar los flujos de usuario y documentar las diferencias entre requisitos y comportamiento real.
- Confirmar qué datos se conservan entre ejecuciones; no asumir persistencia permanente si no se implementó.
- Verificar la configuración exacta del JDK y del IDE usada por el equipo.
- Completar las capturas reales de la aplicación.

## 14. Conclusión

El SGA integra conceptos de programación orientada a objetos y estructuras de datos para modelar operaciones de un alojamiento. Su valor académico está en relacionar cada estructura con una necesidad del dominio y en permitir explicar cómo las decisiones de diseño contribuyen a la organización, búsqueda, priorización y recorrido de la información.

La documentación final debe acompañarse de pruebas de ejecución y evidencias visuales de la versión entregada.
