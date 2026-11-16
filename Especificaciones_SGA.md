# SGA — Sistema de Gestión de Alojamiento
## Especificaciones y requisitos del Proyecto Final de Estructura de Datos (2026-2)

---

## 1. Resumen del proyecto

Aplicación **de escritorio** para administrar el alojamiento turístico **Aparta-Suites UQ** (Salento, Quindío): un único alojamiento con apartamentos independientes distribuidos en bloques y pisos. Cada apartamento se entrega completo y en exclusiva a un solo grupo; **los dormitorios nunca se venden por separado**.

**Problemas a resolver:** ventas dobles de la misma noche, cobros mal calculados, apartamentos entregados sin terminar la limpieza, recorridos de aseo sin orden y búsquedas lentas entre cientos de registros.

**Restricción central:** el núcleo del sistema debe apoyarse en **estructuras de datos genéricas implementadas por el equipo**, sin colecciones de `java.util`.

---

## 2. Reglas de negocio

### 2.1 Huésped
- Atributos: nombre, documento, fecha de nacimiento, teléfono, correo y fotografía (**opcional**).
- Al reservar, el huésped queda como **titular** y registra a los **ocupantes con su fecha de nacimiento**.

### 2.2 Tarifa y cobro
- Se cobra **por persona, por noche**.
- Los **menores de 6 años ocupan cupo pero no pagan**.
- La **noche de salida no se cobra ni se ocupa** (del 10 al 12 de marzo = 2 noches).
- Tres temporadas: **Baja** (base), **Media** y **Alta**.
- Una estancia que cruza varias temporadas se **liquida noche por noche**.

### 2.3 Apartamento
- Atributos: código único, nombre, bloque, piso, número de dormitorios, capacidad máxima, características, tarifa por persona por noche según temporada, estado operativo.
- Estados operativos:

| Estado | Color en GUI |
|---|---|
| Preparado | Verde |
| Ocupado | Azul |
| Pendiente de preparación | Amarillo |
| En preparación | Amarillo |
| Fuera de servicio | Rojo |

- Al pasar a **Fuera de servicio** se debe **registrar el motivo**.

### 2.4 Canales de reserva
| Canal | Comportamiento |
|---|---|
| **Portal** | El huésped reserva desde la aplicación; queda **Pendiente** hasta pagar un **anticipo del 30 %**. |
| **Directa** | La recepción registra reservas telefónicas o presenciales. |
| **Externa** | Plataforma de terceros (simulada) que envía solicitudes en orden; se procesan **una a una**. |

### 2.5 Ciclo de vida de la reserva
```
Pendiente → Confirmada → En curso → Finalizada
```
Puede terminar antes como **Cancelada** o **No-show**. Las transiciones inválidas lanzan excepción propia.

### 2.6 Folio (cuenta de la reserva)
- Cada reserva tiene un folio con **cargos y pagos**.
- Los movimientos **nunca se borran**; se corrigen con un **movimiento contrario**.

### 2.7 Reglas fundamentales
1. **No solapamiento:** un apartamento no puede tener dos reservas activas que compartan al menos una noche, **sin importar el canal**.
2. **Preparación:** al salir un grupo, el apartamento queda *Pendiente de preparación* y **no puede recibir un nuevo grupo** hasta quedar *Preparado*.
3. Los apartamentos con **entrada programada para hoy** se preparan primero.
4. El **check-in solo es posible si el apartamento está Preparado**.
5. Las reservas Pendientes **vencen a las 24 horas**: el sistema las cancela y libera sus noches.

---

## 3. Funcionalidades del sistema

| # | Funcionalidad | Descripción | Estructura / técnica |
|---|---|---|---|
| F1 | Mapa del edificio y rutas de preparación | Grafo con apartamentos, recepción, escaleras y cuartos de aseo como nodos; pasillos como aristas con peso (metros o segundos). Calcula la ruta más corta para recorrer los pendientes. | Grafo, Dijkstra o BFS |
| F2 | Cola de preparación inteligente | Prioridad 1: entrada hoy (ordenados por hora estimada de llegada del siguiente grupo). Prioridad 2: sin entrada hoy. | Cola de prioridad + `Comparator` con `thenComparing` |
| F3 | Procesamiento del canal externo | Cola FIFO; si la solicitud choca con una reserva vigente se **rechaza** y se registra como **conflicto de canal**. | Cola |
| F4 | Búsqueda eficiente | Huéspedes por documento y apartamentos por código en milisegundos. | Árbol binario de búsqueda |
| F5 | Detección de solapamientos | Reservas de cada apartamento en un árbol ordenado por fecha de entrada; verificación **recursiva** sin revisar todas las reservas. | ABB + recursividad |
| F6 | Deshacer en recepción | Cada operación del folio se apila; deshacer **registra el movimiento contrario** (no borra). | Pila |
| F7 | Vencimiento automático | Reservas Pendientes ordenadas por hora de vencimiento (creación + 24 h); al vencer se cancelan y liberan noches. | Cola de prioridad |
| F8 | Asignación de grupos grandes | Si el grupo no cabe en un apartamento, busca la combinación de apartamentos disponibles que lo aloje usando **el menor número de apartamentos**. | Recursividad con backtracking |
| F9 | Visualización gráfica | Mapa interactivo del edificio y calendario de ocupación por apartamento. | GUI |

---

## 4. Roles y casos de uso

### 4.1 Huésped
- **Perfil:** registro con datos personales.
- **Búsqueda de disponibilidad:** apartamentos libres por rango de fechas y número de personas, ordenados por **precio, capacidad o número de dormitorios** (a elección).
- **Cotización:** valor de la estancia con **desglose noche por noche** (recorrido con `Iterator`).
- **Favoritos e historial:** favoritos **sin repetidos**; historial de estancias.
- **Reservas:** crear reservas por el portal, **pagar el anticipo** y **cancelar viendo antes cuánto se le devolvería**.

### 4.2 Recepcionista
- **Reservas directas y externas:** registrar reservas telefónicas/presenciales y procesar la cola del canal externo **en orden de llegada**.
- **Llegadas y salidas:** check-in (solo si *Preparado*) y check-out (el apartamento entra a la cola de preparación).
- **Atención prioritaria de preparación:** despachar la cola (primero entrada hoy) y consultar la ruta más corta (Dijkstra o BFS).
- **Folio:** registrar cargos y pagos; deshacer la última operación.
- **Turnos:** consultar el turno actual, que **rota entre los recepcionistas**.

### 4.3 Administrador del alojamiento
- **Análisis del grafo:** visualizar conectividad, **detectar apartamentos inalcanzables si se cierra un pasillo** y **agrupar apartamentos por bloque** (BFS/DFS).
- **Gestión jerárquica:** administrar empleados, apartamentos y huéspedes mediante árboles de búsqueda.
- **Temporadas y tarifas:** definir fechas de cada temporada y tarifas por apartamento.
- **Reportes avanzados:** ocupación, ingresos, reservas por canal, conflictos de canal y tiempos de preparación, mediante recorrido de las estructuras.

---

## 5. Estructuras de datos propias

> Todas deben ser **genéricas**, implementadas por el equipo y **sin usar `java.util`** (`ArrayList`, `LinkedList`, `PriorityQueue`, `TreeMap`, `HashSet`, `Stack`, etc.) en el núcleo. Las marcadas con ✱ son **obligatorias**.

| Estructura | Clase sugerida | Uso en el SGA |
|---|---|---|
| ✱ Grafo | `Grafo<T>` | Mapa del edificio; ruta de preparación con Dijkstra o BFS; DFS/BFS de conectividad. |
| ✱ Cola de prioridad | `ColaPrioridad<T>` | Cola de preparación (P1 entrada hoy por hora de llegada; P2 el resto) y reservas Pendientes por hora de vencimiento. |
| ✱ Cola | `Cola<T>` | Solicitudes del canal externo (FIFO). |
| ✱ Pila | `Pila<T>` | Deshacer en el folio; navegación "atrás" entre pantallas. |
| ✱ Lista doblemente enlazada | `ListaEnlazada<T>` | Historial de estancias por huésped, ocupantes de una reserva, desglose de noches liquidadas. |
| Lista circular | `ListaCircular<T>` | Rotación de turnos de recepcionistas. |
| ✱ Árbol binario de búsqueda | `ArbolBinarioBusqueda<T extends Comparable<T>>` | Catálogo de apartamentos por código, huéspedes por documento, reservas por apartamento por fecha de entrada. Inserción, búsqueda, eliminación y recorridos **recursivos**. |
| Conjunto | `Conjunto<T>` | Apartamentos favoritos de cada huésped (sin repetidos). |

**Restricción de diseño:** ninguna estructura puede estar atada a una clase del dominio.

---

## 6. Conceptos de Java obligatorios

> Cada concepto debe aparecer en el código y **poder explicarse en la sustentación**.

| Concepto | Dónde se exige |
|---|---|
| **Genéricos** | Todas las estructuras propias (`ListaEnlazada<T>`, `Cola<T>`, `Pila<T>`, `ColaPrioridad<T>`, `ArbolBinarioBusqueda<T extends Comparable<T>>`, `Grafo<T>`, `Conjunto<T>`). |
| **Comparable** (orden natural) | `Apartamento` por **código**, `Huesped` por **documento**, `Reserva` por **fecha de entrada** (permite guardarlos en el ABB). |
| **Comparator** (órdenes alternativos) | **Mínimo cuatro:** apartamentos por precio, por capacidad y por número de reservas; y la prioridad de la cola de preparación (entrada hoy primero, luego hora de llegada, combinando con `thenComparing`). |
| **Iterable / Iterator** | Todas las estructuras propias implementan `Iterable<T>` con su propio `Iterator<T>` (uso en for-each). La **estancia también es iterable**: `for (LocalDate noche : estancia)` recorre las noches a cobrar. |
| **Recursividad** | Inserción, búsqueda, eliminación, altura y recorridos del árbol; detección de solapamientos; DFS sobre el grafo; liquidación noche por noche; asignación de grupos grandes con backtracking. |
| **Excepciones propias** | Reserva solapada, capacidad excedida, fechas inválidas, transición de estado inválida y estructura vacía (p. ej. desencolar de cola vacía). |

---

## 7. Interfaz gráfica (GUI)

| Pantalla | Contenido requerido |
|---|---|
| **Inicio** | Tablero con todos los apartamentos y su estado (verde Preparado, azul Ocupado, amarillo Pendiente/En preparación, rojo Fuera de servicio). |
| **Panel de reservas** | Búsqueda de disponibilidad con selector de orden, creación, confirmación y cancelación de reservas, procesamiento de la cola del canal externo. |
| **Calendario de ocupación** | Cuadrícula de apartamentos × noches del mes: noches **ocupadas, bloqueadas y libres**. |
| **Panel de usuario** | Reservas del huésped, saldo de cada folio, favoritos e historial. |
| **Panel de preparación** | Cola de preparación en orden de prioridad y ruta sugerida para recorrer los pendientes. |
| **Mapa interactivo** | Grafo del edificio (nodos = apartamentos y puntos de servicio; aristas = pasillos). Debe **destacar el estado de cada apartamento en tiempo real** y **dibujar la ruta calculada con Dijkstra**. |
| **Estadísticas** | Ocupación por apartamento y por temporada, ingresos por periodo, reservas por canal (incluidas canceladas y no-show), conflictos de canal y tiempo promedio de preparación. |
| **Carga de datos** | Botón para cargar un escenario de prueba (apartamentos, pasillos, huéspedes y reservas) desde un **archivo plano o serializado**. |

---

## 8. Requerimientos de desarrollo

- [ ] Uso de estructuras de datos desarrolladas por los estudiantes.
- [ ] Grupos de **hasta 4 integrantes**.
- [ ] Repositorio en **Git/GitHub**.
- [ ] **Mínimo 10 commits por integrante**, evidenciando trabajo colaborativo y trazabilidad.
- [ ] Cada commit descrito con **Conventional Commits** (`feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`…).
- [ ] **Diagrama de clases.**
- [ ] **Diagrama de estructuras propias.**

---

## 9. Condiciones generales de entrega

- [ ] Entrega con **demostración funcional** y **sustentación**.
- [ ] GUI **funcional e intuitiva**.
- [ ] **Visualización gráfica** del grafo del edificio y de los apartamentos.
- [ ] **Carga de datos iniciales** desde archivo o botón de prueba.

---

## 10. Evaluación

```
Nota final = Codificación (0 a 5) × Sustentación (factor de 0 a 1)
```

- La **sustentación es obligatoria para todos los integrantes**.
- El profesor **elige al sustentante en el momento de la presentación**, y esa persona influye en la calificación final de todo el grupo. → **Todos deben dominar todo el código.**

---

## 11. Lista de verificación final

### Estructuras y conceptos
- [ ] Grafo, Cola de prioridad, Cola, Pila, Lista doblemente enlazada y ABB implementados (genéricos, con `Iterable`).
- [ ] Lista circular y Conjunto implementados.
- [ ] Ninguna colección de `java.util` en el núcleo.
- [ ] `Comparable` en `Apartamento`, `Huesped` y `Reserva`.
- [ ] Al menos 4 `Comparator` (precio, capacidad, nº de reservas, prioridad de preparación con `thenComparing`).
- [ ] `Estancia` iterable por noches.
- [ ] Excepciones propias: reserva solapada, capacidad excedida, fechas inválidas, transición inválida, estructura vacía.

### Reglas y funcionalidades
- [ ] Sin solapamiento de reservas entre canales (ABB recursivo).
- [ ] Liquidación noche por noche con temporadas; menores de 6 años no pagan.
- [ ] Anticipo del 30 % en portal; vencimiento a las 24 h.
- [ ] Check-in solo con apartamento *Preparado*; check-out envía a cola de preparación.
- [ ] Folio inmutable con deshacer por movimiento contrario.
- [ ] Canal externo FIFO con registro de conflictos.
- [ ] Ruta más corta (Dijkstra/BFS) y análisis de conectividad / inalcanzables.
- [ ] Asignación de grupos grandes con backtracking (mínimo de apartamentos).
- [ ] Rotación de turnos de recepción.
- [ ] Reportes avanzados.

### Entrega
- [ ] GUI con las 8 pantallas.
- [ ] Mapa interactivo con estados en tiempo real y ruta dibujada.
- [ ] Carga de escenario de prueba desde archivo.
- [ ] Repositorio con ≥ 10 commits por integrante en Conventional Commits.
- [ ] Diagrama de clases y diagrama de estructuras propias.
- [ ] Preparación de la sustentación (todos los integrantes).
