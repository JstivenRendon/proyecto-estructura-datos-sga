# Proyecto Estructura de Datos — SGA: Sistema de Gestión de Alojamiento

## Contexto del negocio

El alojamiento turístico Aparta-Suites UQ, ubicado en Salento (Quindío), está formado por un conjunto de apartamentos independientes distribuidos en varios bloques y pisos de una misma edificación. Cada apartamento se entrega completo y en exclusiva a un solo grupo de huéspedes; los dormitorios nunca se venden por separado.

El crecimiento del turismo en la región ha desbordado la operación. Las reservas se anotan en un cuaderno y en WhatsApp, lo que ha provocado ventas dobles de la misma noche, cobros mal calculados, apartamentos entregados sin terminar la limpieza y personal de preparación que recorre el edificio sin un orden claro. Además, buscar un huésped o un apartamento entre cientos de registros toma demasiado tiempo.

Por esta razón, la administración ha decidido contratar a un equipo de ingenieros para desarrollar el SGA (Sistema de Gestión de Alojamiento), una aplicación de escritorio que administre las operaciones principales del alojamiento apoyándose en estructuras de datos desarrolladas por el propio equipo. El sistema administra un único alojamiento.

Cada huésped contará con un perfil que incluirá nombre, documento, fecha de nacimiento, teléfono, correo y, de manera opcional, una fotografía. Al reservar, el huésped queda como titular y registra a los ocupantes con su fecha de nacimiento. El alojamiento cobra por persona, por noche: los ocupantes menores de 6 años ocupan cupo pero no pagan.

Cada apartamento contará con un código único, nombre, bloque, piso, número de dormitorios, capacidad máxima, características, tarifa por persona por noche según la temporada y estado operativo. Los estados operativos posibles serán:

- Preparado
- Ocupado
- Pendiente de preparación
- En preparación
- Fuera de servicio

Cuando un apartamento pase a Fuera de servicio se deberá registrar el motivo.

Una estancia se expresa con una fecha de entrada y una fecha de salida; la noche de salida no se cobra ni se ocupa (del 10 al 12 de marzo son dos noches). El alojamiento maneja tres temporadas: Baja (temporada base), Media y Alta, y una estancia que cruza varias temporadas se liquida noche por noche.

El alojamiento recibe reservas por tres canales sobre los mismos apartamentos:

- **Portal:** el huésped reserva desde la aplicación y la reserva queda Pendiente hasta que pague un anticipo del 30 %.
- **Directa:** la recepción registra una reserva recibida por teléfono o de manera presencial.
- **Externa:** una plataforma de terceros (simulada) envía solicitudes que llegan en orden y deben procesarse una a una.

Toda reserva sigue el ciclo de vida Pendiente → Confirmada → En curso → Finalizada, y puede terminar antes como Cancelada o No-show. Cada reserva tiene un folio (su cuenta) con cargos y pagos; los movimientos nunca se borran, se corrigen con un movimiento contrario.

Una regla fundamental del sistema es que un apartamento no puede tener dos reservas activas que compartan al menos una noche, sin importar el canal. La segunda regla operativa clave es la preparación: cuando un grupo sale, el apartamento queda Pendiente de preparación y no puede recibir un nuevo grupo hasta que el personal lo deje Preparado. Los apartamentos con una entrada programada para hoy deben prepararse primero.

## Características del sistema

- **Mapa del edificio y rutas de preparación:** representación del alojamiento mediante un grafo donde los apartamentos, la recepción, las escaleras y los cuartos de aseo son nodos, y los pasillos son aristas con peso (metros o segundos). El sistema calcula la ruta más corta para que el personal recorra los apartamentos pendientes.
- **Cola de preparación inteligente:** los apartamentos que quedan Pendientes de preparación entran a una cola de prioridad. Prioridad 1: tienen una entrada programada para hoy (ordenados por la hora estimada de llegada del siguiente grupo). Prioridad 2: no tienen entrada hoy.
- **Procesamiento del canal externo:** las solicitudes de la plataforma de terceros llegan a una cola FIFO y se procesan en orden de llegada. Si una solicitud choca con una reserva vigente, se rechaza y se registra como conflicto de canal.
- **Búsqueda eficiente:** localización de huéspedes por documento y de apartamentos por código en milisegundos utilizando árboles binarios de búsqueda.
- **Detección de solapamientos:** las reservas de cada apartamento se guardan en un árbol ordenado por fecha de entrada; verificar si una nueva estancia choca con otra se resuelve recorriendo el árbol de forma recursiva, sin revisar todas las reservas del alojamiento.
- **Deshacer en recepción:** cada operación sobre un folio (cargo o pago) se apila. Deshacer la última operación no la borra: registra el movimiento contrario, respetando la regla de que los movimientos son inalterables.
- **Vencimiento automático:** las reservas Pendientes se organizan por su hora de vencimiento (24 horas después de creadas); al pasar el plazo, el sistema las cancela y libera sus noches.
- **Asignación de grupos grandes:** cuando llega un grupo que no cabe en un solo apartamento, el sistema busca, mediante recursividad con backtracking, la combinación de apartamentos disponibles que aloje a todo el grupo usando el menor número de apartamentos.
- **Visualización gráfica:** el sistema debe permitir visualizar un mapa interactivo del edificio y un calendario de ocupación por apartamento.

## Roles del sistema

### Huésped

- **Perfil:** registro con datos personales.
- **Búsqueda de disponibilidad:** consultar los apartamentos libres para un rango de fechas y un número de personas, ordenados según el criterio que elija: precio, capacidad o número de dormitorios.
- **Cotización:** ver el valor de la estancia con su desglose noche por noche (recorrido de la estancia con Iterator).
- **Favoritos e historial:** guardar apartamentos favoritos sin repetidos y consultar el historial de sus estancias.
- **Reservas:** crear reservas por el portal, pagar el anticipo y cancelar viendo antes cuánto se le devolvería.

### Recepcionista

- **Reservas directas y externas:** registrar reservas telefónicas o presenciales y procesar la cola de solicitudes del canal externo en orden de llegada.
- **Llegadas y salidas:** registrar el check-in (solo si el apartamento está Preparado) y el check-out (el apartamento entra a la cola de preparación).
- **Atención prioritaria de preparación:** despachar la cola de preparación atendiendo primero los apartamentos con entrada hoy, y consultar la ruta más corta para recorrerlos (Dijkstra o BFS).
- **Folio:** registrar cargos y pagos, y deshacer la última operación.
- **Turnos:** consultar el turno actual de recepción, que rota entre los recepcionistas.

### Administrador del alojamiento

- **Análisis del grafo:** visualizar la conectividad del edificio, detectar apartamentos inalcanzables si se cierra un pasillo y agrupar los apartamentos por bloque (recorridos BFS/DFS).
- **Gestión jerárquica:** administrar empleados, apartamentos y huéspedes mediante árboles de búsqueda.
- **Temporadas y tarifas:** definir las fechas de cada temporada y las tarifas por apartamento.
- **Reportes avanzados:** generar estadísticas de ocupación, ingresos, reservas por canal, conflictos de canal y tiempos de preparación mediante el recorrido de las estructuras.

## Estructuras de datos propias

Todas las estructuras deben ser genéricas e implementadas por el equipo, sin usar las colecciones de `java.util` (`ArrayList`, `LinkedList`, `PriorityQueue`, `TreeMap`, `HashSet`, `Stack`, etc.) en el núcleo del sistema. Las marcadas con \* son obligatorias.

| Estructura | Uso en el SGA |
|---|---|
| Grafo\* | Mapa del edificio. Nodos = apartamentos, recepción, escaleras, cuartos de aseo; aristas = pasillos con peso (metros o segundos). Ruta de preparación con Dijkstra o BFS. |
| Cola de prioridad\* | Cola de preparación. Prioridad 1: apartamentos con entrada hoy, por hora de llegada; prioridad 2: el resto. También las reservas Pendientes por hora de vencimiento. |
| Cola\* | Solicitudes del canal externo, procesadas en orden de llegada (FIFO). |
| Pila\* | Deshacer la última operación del folio; navegación "atrás" entre pantallas. |
| Lista doblemente enlazada\* | Historial de estancias de cada huésped, ocupantes de una reserva, desglose de noches liquidadas. |
| Lista circular | Rotación de turnos de los recepcionistas. |
| Árbol binario de búsqueda\* | Catálogo de apartamentos por código, huéspedes por documento, reservas de cada apartamento por fecha de entrada. Inserción, búsqueda, eliminación y recorridos recursivos. |
| Conjunto | Apartamentos favoritos de cada huésped (sin repetidos). |

## Conceptos de Java obligatorios

Cada concepto debe aparecer en el código y poder explicarse en la sustentación.

| Concepto | Dónde se exige en el SGA |
|---|---|
| Genéricos | Todas las estructuras propias (`ListaEnlazada<T>`, `Cola<T>`, `Pila<T>`, `ColaPrioridad<T>`, `ArbolBinarioBusqueda<T extends Comparable<T>>`, `Grafo<T>`, `Conjunto<T>`). Ninguna estructura puede estar atada a una clase del dominio. |
| Comparable (orden natural) | `Apartamento` se compara por código, `Huesped` por documento y `Reserva` por fecha de entrada. Es lo que permite guardarlos en el árbol binario de búsqueda. |
| Comparator (órdenes alternativos) | Mínimo cuatro: apartamentos por precio, por capacidad y por número de reservas; la prioridad de la cola de preparación (entrada hoy primero, luego hora de llegada, combinando comparadores con `thenComparing`). |
| Iterable / Iterator | Todas las estructuras propias implementan `Iterable<T>` con su propio `Iterator<T>`, para poder usarlas en un for-each. La estancia también es iterable: `for (LocalDate noche : estancia)` recorre las noches a cobrar. |
| Recursividad | Inserción, búsqueda, eliminación, altura y recorridos del árbol; detección de solapamientos en el árbol de reservas; DFS sobre el grafo; liquidación del valor noche por noche; asignación de grupos grandes con backtracking. |
| Excepciones propias | Reserva solapada, capacidad excedida, fechas inválidas, transición de estado inválida y estructura vacía (por ejemplo, desencolar de una cola vacía). |

## Interfaz gráfica (GUI)

- **Inicio:** tablero con todos los apartamentos y su estado operativo (verde Preparado, azul Ocupado, amarillo Pendiente o En preparación, rojo Fuera de servicio).
- **Panel de reservas:** búsqueda de disponibilidad con selector de orden, creación de reservas, confirmación, cancelación y procesamiento de la cola del canal externo.
- **Calendario de ocupación:** cuadrícula de apartamentos por noches del mes que muestre las noches ocupadas, bloqueadas y libres.
- **Panel de usuario:** el huésped ve sus reservas, el saldo de cada folio, sus favoritos y su historial.
- **Panel de preparación:** la cola de preparación en orden de prioridad y la ruta sugerida para recorrer los apartamentos pendientes.
- **Mapa interactivo:** representación gráfica del grafo del edificio, con nodos para apartamentos y puntos de servicio, y aristas para los pasillos. Debe destacar el estado de cada apartamento en tiempo real y dibujar la ruta calculada con Dijkstra.
- **Estadísticas:** ocupación por apartamento y por temporada, ingresos por periodo, reservas por canal (incluidas canceladas y no-show), conflictos de canal y tiempo promedio de preparación.
- **Carga de datos:** botón para cargar un escenario de prueba inicial (apartamentos, pasillos, huéspedes y reservas) desde un archivo plano o serializado.

## Requerimientos de desarrollo

- Uso de estructuras de datos desarrolladas por los estudiantes.
- Grupos de hasta 4 integrantes.
- Repositorio en Git/GitHub. Cada integrante del grupo debe tener un mínimo de 10 commits, para evidenciar el trabajo colaborativo y la trazabilidad del proyecto, y cada commit debe estar descrito utilizando conventional commits.
- Diagrama de clases.
- Diagrama de estructuras propias.

## Condiciones generales

- Entrega con demostración funcional y sustentación.
- GUI funcional e intuitiva.
- Visualización gráfica del grafo del edificio y de los apartamentos.
- Carga de datos iniciales desde archivo o botón de prueba.

## Evaluación

La nota del proyecto consta de dos partes:

1. Codificación (0 a 5 puntos).
2. Sustentación (factor multiplicador de 0 a 1).
   - La persona seleccionada para sustentar influirá en la calificación final del grupo.

El resultado final se obtiene multiplicando la nota de codificación por la evaluación de la sustentación.

Recuerden: la sustentación es obligatoria para todos los integrantes del grupo y el profesor elegirá al sustentante en el momento de la presentación.
