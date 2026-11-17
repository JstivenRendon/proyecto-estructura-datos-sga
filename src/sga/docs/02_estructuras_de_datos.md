# Estructuras de datos y algoritmos del SGA

## 1. Propósito

Este documento explica las estructuras genéricas implementadas en el paquete `estructuras` y su relación con las necesidades del Sistema de Gestión de Alojamiento. Las descripciones se basan en las clases fuente disponibles en la rama `estructura`.

Una característica central del proyecto es que las estructuras son genéricas: pueden almacenar distintos tipos sin quedar ligadas exclusivamente a las clases del alojamiento.

## 2. Resumen

| Clase | Implementación interna | Uso en el sistema |
|---|---|---|
| `ListaEnlazada<T>` | Lista doblemente enlazada con nodos anterior/siguiente | Base para colecciones y otras estructuras. |
| `Pila<T>` | Lista enlazada | Historial de operaciones reversibles del folio. |
| `Cola<T>` | Lista enlazada | Solicitudes externas en orden FIFO. |
| `ColaPrioridad<T>` | Heap binario mínimo sobre arreglo | Preparaciones y vencimientos ordenados por `Comparator`. |
| `ArbolBinarioBusqueda<T>` | Árbol binario con enlaces izquierdo/derecho | Búsqueda ordenada y reservas por apartamento. |
| `ListaCircular<T>` | Nodos enlazados en ciclo | Turnos rotativos de recepción. |
| `Conjunto<T>` | Lista enlazada con control de duplicados | Favoritos y visitados. |
| `Grafo<T>` | Lista de vértices y listas de aristas | Mapa, conectividad y rutas. |

## 3. Lista doblemente enlazada: `ListaEnlazada<T>`

### Concepto

Una lista enlazada almacena elementos en nodos conectados. En esta implementación cada nodo mantiene referencias al nodo anterior y al siguiente, por lo que es una lista doblemente enlazada.

### Implementación observada

La clase mantiene:
- `cabeza`: primer nodo.
- `cola`: último nodo.
- `tamano`: cantidad de elementos.

Entre sus operaciones se encuentran `agregarFinal`, `agregarInicio`, `obtener`, `set`, `eliminar`, `contiene`, `primerElemento`, `ultimoElemento`, `estaVacia`, `tamano` y `limpiar`. Implementa `Iterable<T>`.

### Aplicación

Sirve como estructura base de `Pila`, `Cola` y otras colecciones. También almacena elementos en diferentes partes del modelo, por ejemplo movimientos de folio y datos de historial.

### Complejidad orientativa

- Agregar al inicio o al final: O(1).
- Obtener por índice: O(n) en el peor caso; la implementación empieza desde el extremo más cercano.
- Buscar o eliminar por valor: O(n).
- Recorrer todos los elementos: O(n).

## 4. Pila: `Pila<T>`

### Concepto

Una pila sigue el principio LIFO (*Last In, First Out*): el último elemento agregado es el primero en salir.

### Implementación observada

La clase utiliza internamente una `ListaEnlazada<T>`. Sus operaciones principales son `apilar`, `desapilar`, `cima`, `estaVacia` y `tamano`.

### Aplicación

`Folio` contiene una pila de movimientos para soportar la operación de deshacer. La regla de negocio indica que los movimientos contables no se borran; al deshacer se registra un movimiento contrario. La pila permite identificar cuál fue la última operación susceptible de reversión.

## 5. Cola FIFO: `Cola<T>`

### Concepto

Una cola sigue el principio FIFO (*First In, First Out*): el primer elemento agregado es el primero en salir.

### Implementación observada

Utiliza una `ListaEnlazada<T>` como almacenamiento. Ofrece `encolar`, `desencolar`, `frente`, `estaVacia` y `tamano`.

### Aplicación

El canal externo utiliza `Cola<SolicitudExterna>`. Las solicitudes se encolan y se procesan en orden de llegada. Si no hay solicitudes, el servicio de recepción lanza `EstructuraVaciaException`.

## 6. Cola de prioridad: `ColaPrioridad<T>`

### Concepto

Una cola de prioridad entrega primero el elemento que tiene mayor prioridad según un comparador. En esta implementación, el elemento considerado menor por el `Comparator` queda en la raíz.

### Implementación observada

- Usa un arreglo genérico (`heap`) que crece cuando se llena.
- Es un heap binario mínimo.
- `subir` restaura el orden después de insertar.
- `bajar` restaura el orden después de extraer la raíz.
- `encolar`, `desencolar` y `frente` son operaciones principales.
- Su iterador recorre los elementos por prioridad mediante una copia, sin modificar la cola original.

### Aplicación

El sistema utiliza colas de prioridad para:
- Tareas de preparación, de acuerdo con el comparador que establece la prioridad.
- Reservas pendientes organizadas por hora de vencimiento.

El significado exacto de la prioridad depende del `Comparator` entregado al constructor; no debe asumirse que todos los usos tienen el mismo criterio.

### Complejidad orientativa

- Consultar el frente: O(1).
- Insertar: O(log n).
- Extraer: O(log n).
- Construir el recorrido del iterador: realiza una copia y extracciones, por lo que cuesta O(n log n).

## 7. Árbol binario de búsqueda: `ArbolBinarioBusqueda<T extends Comparable<T>>`

### Concepto

Un árbol binario de búsqueda organiza los datos según el orden natural definido por `compareTo`. Los valores menores se ubican a la izquierda y los mayores a la derecha.

### Implementación observada

La clase utiliza nodos con dato, hijo izquierdo e hijo derecho. La inserción, búsqueda, eliminación, cálculo de altura, conteo y recorridos del árbol se implementan de forma recursiva. Los recorridos disponibles son inorden, preorden y postorden.

La clase implementa `Iterable<T>`. El iterador utiliza una clase interna auxiliar de pila de nodos; no utiliza la clase pública `Pila<T>` para ese recorrido.

También contiene `existeSolapamiento(Estancia)`, que examina reservas almacenadas en el árbol y considera sus estados para detectar conflictos de fechas.

### Aplicación

En el dominio, `Apartamento`, `Huesped` y `Reserva` implementan `Comparable`. El sistema utiliza árboles para organizar apartamentos, huéspedes y reservas asociadas a apartamentos. En particular, el árbol de reservas permite verificar si una nueva estancia comparte noches con una reserva activa.

### Complejidad orientativa

La búsqueda, inserción y eliminación cuestan O(h), donde h es la altura del árbol. En un árbol equilibrado h puede aproximarse a log n; en el peor caso un árbol degenerado puede llegar a O(n). La implementación no es un árbol AVL ni garantiza balanceo automático.

## 8. Lista circular: `ListaCircular<T>`

### Concepto

En una lista circular, el último nodo enlaza nuevamente con el primero. Esto permite recorrer los elementos repetidamente.

### Implementación observada

La clase mantiene una referencia `actual` y un contador `tamano`. Incluye operaciones como `agregar`, `turnoActual`, `avanzarTurno`, `estaVacia` y `tamano`.

### Aplicación

`ServicioRecepcion` usa `ListaCircular<Recepcionista>` para consultar el recepcionista actual y avanzar al siguiente turno.

## 9. Conjunto: `Conjunto<T>`

### Concepto

Un conjunto representa una colección sin elementos repetidos.

### Implementación observada

La implementación se apoya en `ListaEnlazada<T>` y proporciona operaciones para agregar, eliminar, consultar pertenencia y obtener el tamaño. El método `agregar` permite controlar si el elemento ya existe.

### Aplicación

- `Huesped` utiliza `Conjunto<String>` para sus apartamentos favoritos.
- `Grafo<T>` utiliza un conjunto de visitados en el recorrido DFS y en BFS.

## 10. Grafo: `Grafo<T>`

### Concepto

Un grafo está compuesto por vértices y aristas. En el SGA, los vértices representan puntos del edificio (apartamentos, recepción y puntos de servicio) y las aristas representan pasillos con un peso, por ejemplo una distancia.

### Implementación observada

- La lista `vertices` contiene los vértices.
- Cada vértice guarda su dato y una lista enlazada de aristas.
- Cada arista contiene un destino y un peso.
- `agregarArista` registra la conexión en ambos sentidos, por lo que la conexión implementada es no dirigida.
- No admite pesos negativos.
- Incluye `adyacentes`, `contiene`, `cantidadVertices`, `dfs`, `bfs`, `alcanzable` y `dijkstra`.

### Algoritmos

**DFS (búsqueda en profundidad):** visita un vecino y continúa profundizando recursivamente antes de retroceder. Usa un conjunto para no visitar dos veces el mismo vértice.

**BFS (búsqueda en amplitud):** utiliza una cola FIFO y visita los vértices por niveles, según el número de aristas desde el origen.

**Dijkstra:** calcula una ruta de menor suma de pesos entre un origen y un destino. La implementación usa una cola de prioridad y registros de distancias/predecesores. Como el grafo rechaza pesos negativos, se respeta una condición necesaria del algoritmo.

### Aplicación

El grafo representa los pasillos y puntos del edificio. `PanelMapa` dibuja nodos y conexiones, muestra estados de apartamentos, permite simular el cierre de un pasillo y visualizar qué apartamentos quedan inalcanzables desde recepción. También puede resaltar una ruta calculada.

## 11. Conceptos de Java relacionados

### Genéricos

Las estructuras declaran parámetros como `<T>` para reutilizar la misma implementación con diferentes tipos.

### Comparable

Define el orden natural de objetos. En el proyecto se utiliza para que determinadas entidades puedan almacenarse en árboles de búsqueda.

### Comparator

Permite definir órdenes alternativos sin modificar la clase del objeto. El proyecto dispone de comparadores para ordenar apartamentos y establecer prioridades.

### Iterable e Iterator

Permiten recorrer estructuras mediante iteradores y ciclos `for-each`. `Estancia` también es iterable para recorrer las noches de una reserva.

### Recursividad

Se utiliza en operaciones del árbol, DFS, detección de solapamientos y cotización noche por noche, entre otras funciones del proyecto.

## 12. Consideraciones para la sustentación

Para cada estructura, el estudiante debería poder explicar:
1. Qué problema resuelve.
2. Cómo está implementada internamente.
3. Qué operaciones ofrece.
4. En qué clase o servicio del SGA se utiliza.
5. Qué complejidad tiene cada operación importante.
6. Por qué esa estructura es adecuada para ese caso de uso.

## 13. Verificación frente al enunciado

El enunciado académico menciona requisitos de implementación y uso. No se debe afirmar que todos están cumplidos solamente porque aparezcan en la especificación. La comprobación debe realizarse revisando el código y ejecutando pruebas. En particular, validar:
- Que las estructuras requeridas sean propias y genéricas.
- Que los comparadores solicitados estén efectivamente conectados a las operaciones de interfaz.
- Que la detección de solapamientos respete los estados que se consideran activos.
- Que el algoritmo de rutas devuelva la ruta correcta con pesos.
- Que el comportamiento de vencimientos y deshacer coincida con las reglas de negocio.
