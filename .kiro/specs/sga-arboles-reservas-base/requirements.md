# Requirements Document

## Introduction

This document covers Phase 1 of the SGA (Sistema de Gestión de Alojamiento) module "Persona B: Árboles, Reservas y Liquidación". The scope is limited to the structural foundation: custom data structures, domain models, custom exceptions, and comparators in Java — without GUI, backtracking, or multi-season liquidation. All code lives under `src/com/sga/` with root package `com.sga`.

The defining constraint of the entire project is that the core must rely on **team-implemented generic data structures** with no use of `java.util` collections. Only `java.time.*`, `java.lang.Comparable`, `java.lang.Iterable`, `java.util.Iterator`, and `java.util.Comparator` are permitted from the standard library.

## Glossary

- **ABB (ArbolBinarioBusqueda)**: A generic binary search tree implemented by the team; supports recursive insert, search, delete, traversal, height, count, and in-order iteration without `java.util.Stack`.
- **NodoBST**: Internal node class of the ABB holding a typed datum and left/right child references.
- **Estancia**: A value object representing the stay interval [fechaEntrada, fechaSalida); iterable night by night, excluding the departure night.
- **Apartamento**: A rentable unit in the building, identified by a unique code, with its own ABB of reservations.
- **Huesped**: A guest, identified by their national document number.
- **Reserva**: A booking linking a Huesped to an Apartamento for an Estancia, with lifecycle state and a 24-hour expiry when created as PENDIENTE.
- **EstadoOperativo**: Operational state of an Apartamento (PREPARADO, OCUPADO, PENDIENTE_PREPARACION, EN_PREPARACION, FUERA_DE_SERVICIO).
- **Temporada**: Pricing season (BAJA, MEDIA, ALTA).
- **CanalReserva**: Booking channel (PORTAL, DIRECTA, EXTERNA).
- **EstadoReserva**: Lifecycle state of a Reserva (PENDIENTE, CONFIRMADA, EN_CURSO, FINALIZADA, CANCELADA, NO_SHOW).
- **MainTestFase1**: A standalone `main` class that exercises all Phase 1 deliverables and prints results to stdout.

---

## Requirements

### Requirement 1: Custom Exception Hierarchy

**User Story:** As a developer, I want a set of typed exceptions for domain-rule violations, so that callers can distinguish and handle each error category precisely.

#### Acceptance Criteria

1. THE `EstructuraVaciaException` SHALL extend `RuntimeException` and reside in package `com.sga.excepciones`.
2. THE `FechasInvalidasException` SHALL extend `Exception` (checked) and reside in package `com.sga.excepciones`.
3. THE `ReservaSolapadaException` SHALL extend `Exception` (checked) and reside in package `com.sga.excepciones`.
4. THE `CapacidadExcedidaException` SHALL extend `Exception` (checked) and reside in package `com.sga.excepciones`.
5. WHEN any exception is constructed with a non-null message String of 1–500 characters, THE exception SHALL store that message unchanged and return it unmodified via `getMessage()`.
6. THE exception classes SHALL NOT import or use any `java.util` collection type.
7. WHEN any exception is constructed using a no-arg constructor (if provided), THE `getMessage()` SHALL return `null`.

---

### Requirement 2: Generic Binary Search Tree (ABB)

**User Story:** As a developer, I want a generic BST with fully recursive operations and an in-order iterator that uses no `java.util` classes, so that domain entities can be stored, searched, and traversed efficiently.

#### Acceptance Criteria

1. THE `ArbolBinarioBusqueda<T extends Comparable<T>>` SHALL reside in package `com.sga.estructuras` and implement `Iterable<T>`.
2. THE `ArbolBinarioBusqueda` SHALL contain a private static inner class `NodoBST<T>` with fields: `T dato`, `NodoBST<T> izquierdo`, `NodoBST<T> derecho`.
3. WHEN `insertar(T dato)` is called with a non-null datum, THE `ArbolBinarioBusqueda` SHALL insert the datum using a fully recursive private helper, preserving BST order, such that a subsequent `buscar(T dato)` call with the same datum returns a non-null result.
4. WHEN `insertar(T dato)` is called with a datum already present in the tree (i.e., `dato.compareTo(existing) == 0`), THE `ArbolBinarioBusqueda` SHALL leave the tree unchanged and not insert a duplicate node.
5. WHEN `insertar(T dato)` is called with a null datum, THE `ArbolBinarioBusqueda` SHALL throw an `IllegalArgumentException` without modifying the tree.
6. WHEN `buscar(T dato)` is called with a datum present in the tree, THE `ArbolBinarioBusqueda` SHALL return that datum using a fully recursive private helper.
7. WHEN `buscar(T dato)` is called with a datum absent from the tree, THE `ArbolBinarioBusqueda` SHALL return `null`.
8. WHEN `buscar(T dato)` is called with a null datum, THE `ArbolBinarioBusqueda` SHALL throw an `IllegalArgumentException`.
9. WHEN `eliminar(T dato)` is called with a datum present in the tree, THE `ArbolBinarioBusqueda` SHALL remove exactly that node using a fully recursive private helper that correctly handles all three deletion cases: leaf node, node with one child, and node with two children (using the in-order successor), such that a subsequent `buscar(T dato)` call with the same datum returns `null` and `contarElementos()` decreases by exactly 1.
10. WHEN `eliminar(T dato)` is called with a datum absent from the tree, THE `ArbolBinarioBusqueda` SHALL leave the tree unchanged, with `contarElementos()` returning the same value as before the call.
11. WHEN `calcularAltura()` is called on a non-empty tree, THE `ArbolBinarioBusqueda` SHALL return the height as a non-negative integer via a fully recursive private helper, where height is defined as the number of edges on the longest path from the root to a leaf (a single-node tree returns 0).
12. WHEN `calcularAltura()` is called on an empty tree, THE `ArbolBinarioBusqueda` SHALL return `-1`.
13. WHEN `recorridoInorden()`, `recorridoPreorden()`, or `recorridoPostorden()` is called, THE `ArbolBinarioBusqueda` SHALL return a `ListaEnlazada<T>` containing all elements in the corresponding traversal order, computed recursively, with the returned list containing exactly `contarElementos()` elements.
14. WHEN `contarElementos()` is called, THE `ArbolBinarioBusqueda` SHALL return the total node count as a non-negative integer via a fully recursive private helper, returning `0` for an empty tree.
15. WHEN `estaVacio()` is called, THE `ArbolBinarioBusqueda` SHALL return `true` if and only if the root is `null`.
16. WHEN `iterator()` is called, THE `ArbolBinarioBusqueda` SHALL return an `Iterator<T>` that traverses all `contarElementos()` elements in ascending in-order, using an internal linked-node stack or parent pointers without importing or instantiating any `java.util` collection type other than `java.util.Iterator`.
17. THE `ArbolBinarioBusqueda` SHALL NOT import or use any `java.util` collection type except `java.util.Iterator` and `java.util.Comparator`.

---

### Requirement 3: ListaEnlazada for Traversal Results

**User Story:** As a developer, I want a minimal generic singly-linked list to serve as the return type for tree traversals, so that traversal results can be iterated without depending on `java.util`.

#### Acceptance Criteria

1. THE `ListaEnlazada<T>` SHALL reside in package `com.sga.estructuras` and implement `Iterable<T>`.
2. THE `ListaEnlazada` SHALL provide `agregarFinal(T dato)`, `tamano()` (returns current element count), and `estaVacia()` (returns true iff count equals zero) operations.
3. WHEN `iterator()` is called on a `ListaEnlazada`, THE iterator SHALL traverse elements from head to tail in `agregarFinal` call sequence order; calling `hasNext()` on an iterator of an empty list SHALL immediately return `false`.
4. WHEN `iterator().next()` is called after `hasNext()` returns `false`, THE iterator SHALL throw `java.util.NoSuchElementException`.
5. THE `ListaEnlazada` SHALL NOT import or use any `java.util` collection type except `java.util.Iterator` and `java.util.NoSuchElementException`.

---

### Requirement 4: Estancia Value Object

**User Story:** As a developer, I want an Estancia object that validates its date range and iterates night by night, so that all date-interval logic is centralized and consistent.

#### Acceptance Criteria

1. THE `Estancia` SHALL reside in package `com.sga.modelo` and implement `Iterable<LocalDate>`.
2. WHEN an `Estancia` is constructed with `fechaEntrada` that is not strictly before `fechaSalida` (i.e., `fechaEntrada >= fechaSalida`), THE `Estancia` SHALL throw `FechasInvalidasException` with a message indicating that check-in must be strictly before check-out.
3. WHEN an `Estancia` is constructed with a null `fechaEntrada` or null `fechaSalida`, THE `Estancia` SHALL throw `FechasInvalidasException` with a message identifying which argument is null.
4. WHEN `iterator()` is called, THE `Estancia` SHALL return an `Iterator<LocalDate>` that yields `fechaEntrada`, then each subsequent calendar day, up to and excluding `fechaSalida`, advancing exactly one calendar day per `next()` call; `hasNext()` SHALL return `false` when the current position equals `fechaSalida`.
5. WHEN `iterator().next()` is called after `hasNext()` returns `false`, THE iterator SHALL throw `java.util.NoSuchElementException`.
6. THE `Estancia` SHALL expose `getFechaEntrada()` and `getFechaSalida()` returning the stored `LocalDate` values unchanged from construction.
7. WHEN `getNochesACobrar()` is called, THE `Estancia` SHALL return the number of nights as a positive `long` equal to `ChronoUnit.DAYS.between(fechaEntrada, fechaSalida)`.
8. THE `Estancia` SHALL NOT import or use any `java.util` collection type except `java.util.Iterator` and `java.util.NoSuchElementException`.

---

### Requirement 5: Domain Enums

**User Story:** As a developer, I want typed enums for every categorical value in the domain, so that invalid states cannot be represented and comparisons are safe.

#### Acceptance Criteria

1. THE `EstadoOperativo` enum SHALL reside in package `com.sga.modelo` and declare exactly the values: `PREPARADO`, `OCUPADO`, `PENDIENTE_PREPARACION`, `EN_PREPARACION`, `FUERA_DE_SERVICIO`.
2. THE `Temporada` enum SHALL reside in package `com.sga.modelo` and declare exactly the values: `BAJA`, `MEDIA`, `ALTA`.
3. THE `CanalReserva` enum SHALL reside in package `com.sga.modelo` and declare exactly the values: `PORTAL`, `DIRECTA`, `EXTERNA`.
4. THE `EstadoReserva` enum SHALL reside in package `com.sga.modelo` and declare exactly the values: `PENDIENTE`, `CONFIRMADA`, `EN_CURSO`, `FINALIZADA`, `CANCELADA`, `NO_SHOW`.
5. THE system SHALL guarantee that each enum type is the sole permitted type for its corresponding domain field, such that assigning a value outside the declared enum constants produces a compile-time error.
6. IF any code attempts to obtain an enum constant by name using a string that does not match any declared constant (e.g., via `valueOf`), THEN THE system SHALL throw a runtime exception and reject the operation without modifying any domain object state.

---

### Requirement 6: Apartamento Entity

**User Story:** As a developer, I want an Apartamento class that holds all unit data and its own reservation tree, so that searches and overlap checks are scoped per apartment.

#### Acceptance Criteria

1. THE `Apartamento` SHALL reside in package `com.sga.modelo` and implement `Comparable<Apartamento>`.
2. THE `Apartamento` constructor SHALL accept exactly these parameters in order: `String codigo`, `String nombre`, `String bloque`, `int piso`, `int numeroDormitorios`, `int capacidadMaxima`, `String caracteristicas`, `double tarifaBasePorPersona`; where `codigo` is 1–20 characters, `nombre` is 1–100 characters, `bloque` is 1–20 characters, `piso` is 1–99, `numeroDormitorios` is 1–20, `capacidadMaxima` is 1–50, `caracteristicas` is 0–500 characters, and `tarifaBasePorPersona` is 0.01–999999.99.
3. THE `Apartamento` constructor SHALL throw `IllegalArgumentException` if any parameter violates the bounds defined in criterion 2 or if `capacidadMaxima` is less than `numeroDormitorios`.
4. THE `Apartamento` SHALL initialize `estadoOperativo` to `EstadoOperativo.PREPARADO` and `motivoFueraDeServicio` to `null` upon construction.
5. THE `Apartamento` SHALL contain a private `ArbolBinarioBusqueda<Reserva> reservas` field initialized to an empty tree upon construction.
6. WHEN `compareTo(Apartamento otro)` is called, THE `Apartamento` SHALL return the result of `this.codigo.compareToIgnoreCase(otro.getCodigo())`.
7. THE `Apartamento` SHALL expose `getCodigo()`, `getNombre()`, `getBloque()`, `getPiso()`, `getNumeroDormitorios()`, `getCapacidadMaxima()`, `getCaracteristicas()`, `getTarifaBasePorPersona()`, `getEstadoOperativo()`, `getMotivoFueraDeServicio()`, `getHoraLlegadaProgramadaHoy()`, and `getReservas()`, each returning the value of its corresponding field without modification.
8. IF `setEstadoOperativo(EstadoOperativo estado)` is called with `EstadoOperativo.FUERA_DE_SERVICIO`, THEN THE `Apartamento` SHALL throw `IllegalArgumentException` indicating that a motivo is required.
9. THE `Apartamento` SHALL provide `setEstadoOperativo(EstadoOperativo estado, String motivo)` where: if `estado` is `FUERA_DE_SERVICIO` and `motivo` is `null` or blank, it throws `IllegalArgumentException`; if `estado` is `FUERA_DE_SERVICIO` and `motivo` is 1–200 non-blank characters, it sets `motivoFueraDeServicio` to `motivo`; and for any other `estado`, it sets `motivoFueraDeServicio` to `null` regardless of the `motivo` argument.
10. THE `Apartamento` SHALL provide `setHoraLlegadaProgramadaHoy(java.time.LocalTime hora)` that accepts `null` to clear the value and any non-null `LocalTime` to set it, with no thrown exception in either case.
11. THE `Apartamento` SHALL NOT import or use any `java.util` collection type except `java.util.Comparator`.

---

### Requirement 7: Huesped Entity

**User Story:** As a developer, I want a Huesped class comparable by document number, so that guests can be stored in and retrieved from the BST by their ID.

#### Acceptance Criteria

1. THE `Huesped` SHALL reside in package `com.sga.modelo` and implement `Comparable<Huesped>`.
2. IF any of `nombre`, `documento`, `fechaNacimiento`, or `telefono` passed to the `Huesped` constructor is null or, for String parameters, blank (empty or whitespace-only), THEN THE `Huesped` constructor SHALL throw an `IllegalArgumentException` indicating which parameter is invalid; `correo` may be null or blank.
3. WHEN `compareTo(Huesped otro)` is called with a non-null argument, THE `Huesped` SHALL return the result of `this.documento.compareToIgnoreCase(otro.getDocumento())`; IF `otro` is null, THEN THE `Huesped` SHALL throw a `NullPointerException`.
4. THE `Huesped` SHALL expose `getDocumento()`, `getNombre()`, `getFechaNacimiento()`, `getTelefono()`, `getCorreo()`, and `getRutaFotografia()`; THE `Huesped` SHALL NOT expose setters for `nombre` or `documento`.
5. THE `Huesped` SHALL provide `setRutaFotografia(String ruta)` to store an optional photo path; `getRutaFotografia()` SHALL return `null` when `setRutaFotografia` has never been called or was last called with a `null` argument.
6. THE `Huesped` SHALL NOT import or use any `java.util` collection type.

---

### Requirement 8: Reserva Entity

**User Story:** As a developer, I want a Reserva class comparable by check-in date, so that reservations can be inserted into an apartment's BST and overlap detection works via tree order.

#### Acceptance Criteria

1. THE `Reserva` SHALL reside in package `com.sga.modelo` and implement `Comparable<Reserva>`.
2. THE `Reserva` constructor SHALL accept: `Huesped titular`, `Apartamento apartamento`, `Estancia estancia`, `CanalReserva canal`, `int cantidadOcupantes` (valid range: 1–20 inclusive).
3. WHEN any of `titular`, `apartamento`, `estancia`, or `canal` passed to the `Reserva` constructor is null, or when `cantidadOcupantes` is outside the range 1–20, THE `Reserva` constructor SHALL throw an `IllegalArgumentException` indicating which parameter is invalid.
4. THE `Reserva` constructor SHALL assign a unique `idReserva` using an auto-incremented static counter starting at 1 and incrementing by 1 per instance, formatted as `"RSV-%04d"`.
5. THE `Reserva` constructor SHALL set `fechaCreacion` to `LocalDateTime.now()`.
6. WHEN the `Reserva` is constructed with `canal == CanalReserva.PORTAL`, THE `Reserva` SHALL set `estado` to `EstadoReserva.PENDIENTE` and `fechaVencimiento` to `fechaCreacion.plusHours(24)`.
7. WHEN the `Reserva` is constructed with `canal != CanalReserva.PORTAL`, THE `Reserva` SHALL set `estado` to `EstadoReserva.CONFIRMADA` and `fechaVencimiento` to `null`.
8. WHEN `compareTo(Reserva otra)` is called with a non-null argument, THE `Reserva` SHALL return the result of `this.estancia.getFechaEntrada().compareTo(otra.getEstancia().getFechaEntrada())`; negative when this check-in is earlier, zero when equal, positive when later.
9. WHEN `compareTo(Reserva otra)` is called with a null argument, THE `Reserva` SHALL throw a `NullPointerException`.
10. THE `Reserva` SHALL expose `getIdReserva()`, `getTitular()`, `getApartamento()`, `getEstancia()`, `getCanal()`, `getEstado()`, `getFechaCreacion()`, `getFechaVencimiento()`, `getCantidadOcupantes()`.
11. THE `Reserva` SHALL NOT import or use any `java.util` collection type.

---

### Requirement 9: Apartment Comparators

**User Story:** As a developer, I want four `Comparator<Apartamento>` implementations so that apartment lists can be sorted by price, capacity, number of reservations, and preparation priority.

#### Acceptance Criteria

1. THE `ComparadorApartamentoPorPrecio` SHALL reside in package `com.sga.comparadores` and implement `Comparator<Apartamento>`, ordering apartments from lowest to highest `tarifaBasePorPersona`; when two apartments have equal `tarifaBasePorPersona`, the comparator SHALL order them by `codigo` in ascending lexicographic order.
2. THE `ComparadorApartamentoPorCapacidad` SHALL reside in package `com.sga.comparadores` and implement `Comparator<Apartamento>`, ordering apartments from highest to lowest `capacidadMaxima`; when two apartments have equal `capacidadMaxima`, the comparator SHALL order them by `codigo` in ascending lexicographic order.
3. THE `ComparadorApartamentoPorReservas` SHALL reside in package `com.sga.comparadores` and implement `Comparator<Apartamento>`, ordering apartments by ascending count of reservations returned by each apartment's BST `contarElementos()` method (fewer reservations first); when two apartments have equal reservation counts, the comparator SHALL order them by `codigo` in ascending lexicographic order.
4. THE `ComparadorPrioridadPreparacion` SHALL reside in package `com.sga.comparadores` and implement `Comparator<Apartamento>`, placing apartments whose `horaLlegadaProgramadaHoy` (`LocalTime`) is non-null before those where it is null, ordering non-null entries by earliest `horaLlegadaProgramadaHoy` first; when two non-null entries have equal `horaLlegadaProgramadaHoy`, the comparator SHALL order them by `codigo` in ascending lexicographic order.
5. THE comparator classes SHALL NOT import or reference any type from the `java.util` package within their source files except `java.util.Comparator`.

---

### Requirement 10: Integration Verification via MainTestFase1

**User Story:** As a developer, I want a runnable `MainTestFase1` class that exercises every Phase 1 deliverable, so that correctness can be verified by simply running `main`.

#### Acceptance Criteria

1. THE `MainTestFase1` SHALL reside in package `com.sga` and provide a `public static void main(String[] args)` method.
2. WHEN `main` is executed, THE `MainTestFase1` SHALL insert at least five `Apartamento` objects into an `ArbolBinarioBusqueda<Apartamento>` and print the in-order traversal to stdout in ascending order by the natural ordering of `Apartamento`.
3. WHEN `main` is executed, THE `MainTestFase1` SHALL search for an existing and a non-existing `Apartamento` and print each result as a labeled line indicating the search key and whether the element was found or not found.
4. WHEN `main` is executed, THE `MainTestFase1` SHALL delete one `Apartamento` from the BST and print the in-order traversal after deletion in ascending order.
5. WHEN `main` is executed, THE `MainTestFase1` SHALL print the height and element count of the BST immediately after all insertions and immediately after deletion, each labeled with the moment of measurement.
6. WHEN `main` is executed, THE `MainTestFase1` SHALL create an `Estancia` where the check-out date is at least one day after the check-in date and iterate it with `for (LocalDate noche : estancia)`, printing each night.
7. WHEN `main` is executed, THE `MainTestFase1` SHALL attempt to create an `Estancia` where the check-out date is not after the check-in date and print the caught `FechasInvalidasException` message.
8. WHEN `main` is executed, THE `MainTestFase1` SHALL create at least three `Reserva` objects, insert them into an `ArbolBinarioBusqueda<Reserva>`, and print pre-order and post-order traversals.
9. WHEN `main` is executed, THE `MainTestFase1` SHALL sort the same array of apartments using all four comparators — `ComparadorApartamentoPorPrecio`, `ComparadorApartamentoPorCapacidad`, `ComparadorApartamentoPorReservas`, and `ComparadorPrioridadPreparacion` — and print each sorted order labeled with the comparator used.
10. THE `MainTestFase1` SHALL NOT import or use any `java.util` collection type except `java.util.Comparator`.

---

### Requirement 11: Round-Trip and Structural Correctness of the BST Iterator

**User Story:** As a developer, I want to verify that the in-order iterator and the recursive in-order traversal produce identical sequences, so that both access paths are trustworthy.

#### Acceptance Criteria

1. THE `ArbolBinarioBusqueda<T extends Comparable<T>>` SHALL guarantee that, for any sequence of up to 10,000 insertions and deletions, the `ListaEnlazada<T>` returned by `recorridoInorden()` and the sequence of elements yielded by successive `iterator().next()` calls until `hasNext()` returns `false` are identical in content and order.
2. WHEN one or more elements are inserted into an `ArbolBinarioBusqueda<T extends Comparable<T>>`, THE `recorridoInorden()` SHALL return a `ListaEnlazada<T>` in which every element is less than or equal to the next element according to `T.compareTo()`, and the list contains exactly the same elements that were inserted.
3. WHEN an element is inserted into an `ArbolBinarioBusqueda<T>` and then deleted without any intervening insertions or deletions of other elements, THE subsequent `recorridoInorden()` SHALL return a `ListaEnlazada<T>` containing zero occurrences of that element; if the tree permitted duplicate values, only the occurrences matching the number of deletions performed SHALL be removed.
4. WHEN `estaVacio()` returns `true`, THE `iterator().hasNext()` SHALL return `false`, and a call to `iterator().next()` SHALL throw `NoSuchElementException`.
