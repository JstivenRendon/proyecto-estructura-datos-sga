# Manual técnico e instalación del SGA

## 1. Propósito

Este documento explica la organización del código y los pasos conocidos para abrir y ejecutar el Sistema de Gestión de Alojamiento (SGA).

## 2. Tecnologías identificadas

- Lenguaje: Java.
- Interfaz gráfica: Java Swing.
- Entorno de ejecución: Java Development Kit (JDK).
- Control de versiones: Git.
- Repositorio remoto: GitHub.
- Diagramas: PlantUML (para los archivos fuente incluidos en `diagramas/`).

La versión exacta del JDK debe confirmarse en el entorno del equipo; no se debe inventar un número de versión.

## 3. Organización del repositorio

La estructura fuente se encuentra en `src/sga/`. Se identifican los siguientes directorios:

| Directorio | Contenido |
|---|---|
| `src/sga/modelo` | Entidades del dominio. |
| `src/sga/estructuras` | Estructuras de datos genéricas propias. |
| `src/sga/servicios` | Servicios y reglas de negocio. |
| `src/sga/gui` | Ventanas y paneles Swing. |
| `src/sga/persistencia` | Lectura y carga de escenarios. |
| `src/sga/comparadores` | Comparadores para ordenamiento. |
| `src/sga/excepciones` | Excepciones específicas. |
| `src/sga/pruebas` | Código de pruebas. |
| `datos` | Archivos de escenario de prueba. |

## 4. Clases relevantes

### 4.1 Entrada de la aplicación

`gui.Main` contiene el método `main`. Inicia `VentanaPrincipal` mediante `SwingUtilities.invokeLater`, lo que programa la creación de la interfaz en el hilo de eventos de Swing.

### 4.2 Coordinación del dominio

`SistemaAlojamiento` coordina información de apartamentos, huéspedes, reservas, temporadas, solicitudes externas, tareas de preparación, turnos y mapa del edificio.

### 4.3 Servicios

Entre los servicios identificados se encuentran:
- `GestorReservas`
- `GestorTemporadas`
- `CotizadorEstancia`
- `ValidadorDisponibilidad`
- `AsignadorGruposBacktracking`
- `ServicioRecepcion`
- `ServicioEstadisticas`

La responsabilidad concreta de cada método debe consultarse en el archivo fuente correspondiente.

### 4.4 Persistencia de escenarios

`LectorEscenario` carga un archivo de texto plano. Cada línea representa un registro separado por punto y coma. Ignora líneas vacías y comentarios que empiezan por `#`.

Los tipos de registro documentados en el código son:

```text
APARTAMENTO;codigo;nombre;bloque;piso;dormitorios;capacidad;caracteristicas;tarifa;estado;motivo;horaLlegadaHoy
HUESPED;documento;nombre;fechaNacimiento;telefono;correo
RECEPCIONISTA;id;nombre
TEMPORADA;tipo;inicio;fin
NODO;id;tipo;codigoApartamento
PASILLO;origen;destino;peso
RESERVA;id;documento;codigoApartamento;entrada;salida;canal;estado;personas
FAVORITO;documento;codigoApartamento
SOLICITUD_EXTERNA;plataforma;documento;codigoApartamento;entrada;salida;personas
PREPARACION;codigoApartamento;entradaHoy;horaLlegada
```

Las fechas se aceptan como `AAAA-MM-DD` o como desplazamientos respecto al día de ejecución, por ejemplo `+2`, `-1` o `0`. Los registros deben aparecer en un orden que permita referenciar entidades previamente cargadas.

`CargadorEscenario` crea un escenario de prueba directamente desde código, sin depender del archivo de texto.

## 5. Cómo obtener el proyecto

1. Abrir el repositorio: `https://github.com/JstivenRendon/proyecto-estructura-datos-sga`.
2. Seleccionar la rama `estructura`.
3. Descargar el repositorio como ZIP o clonarlo con Git.

Comando para clonar:

```bash
git clone --branch estructura https://github.com/JstivenRendon/proyecto-estructura-datos-sga.git
```

## 6. Cómo abrir y ejecutar

El `README.md` actual del repositorio indica este procedimiento para Visual Studio Code:

1. Abrir en VS Code la carpeta del proyecto.
2. Localizar la configuración de ejecución **SGA - gui.Main**.
3. Ejecutarla con `F5`.

Si esa configuración no aparece, revisar la configuración de Java de VS Code y el archivo de configuración del proyecto. El punto de entrada Java es `gui.Main`.

**No se debe asegurar que el proyecto compila en el estado actual sin probarlo.** En la versión de la rama `estructura` revisada para esta documentación, `src/sga/gui/VentanaPrincipal.java` contiene marcadores de conflicto de Git (`<<<<<<< HEAD`, `=======`, `>>>>>>> ...`). Es necesario resolver ese conflicto antes de compilar. Después de resolverlo, verificar que la inicialización de paneles y el escenario inicial sean coherentes.

## 7. Flujo general de ejecución

1. La JVM ejecuta `gui.Main.main`.
2. `SwingUtilities.invokeLater` programa la creación de la ventana principal.
3. `VentanaPrincipal` prepara el sistema y los paneles de interfaz.
4. Se carga un escenario desde `datos/escenario.txt` cuando el flujo configurado lo permite; existe un escenario alternativo construido por `CargadorEscenario`.
5. El usuario interactúa con los paneles.
6. Los paneles invocan operaciones de los servicios y del objeto `SistemaAlojamiento`.
7. La interfaz actualiza la información visible después de las operaciones.

Este flujo debe contrastarse con la versión final después de resolver los conflictos de integración.

## 8. Recomendaciones para desarrollo

- Mantener las estructuras genéricas independientes de las clases del dominio.
- Utilizar `Comparable` para el orden natural y `Comparator` para criterios alternativos.
- Validar fechas, capacidad, estados y solapamientos antes de confirmar operaciones.
- Mantener los movimientos del folio como registros inmutables y corregir mediante movimientos contrarios.
- Evitar introducir colecciones estándar en el núcleo cuando el requisito académico exige estructuras propias.
- Añadir JavaDoc a clases y métodos públicos relevantes.
- Ejecutar pruebas después de modificar algoritmos de árboles, colas, grafo y cotización.

## 9. Generación de JavaDoc

Para generar documentación JavaDoc:
1. Confirmar que el proyecto compile y que las rutas de fuentes estén configuradas.
2. Abrir la herramienta JavaDoc del JDK o utilizar la tarea equivalente del IDE.
3. Seleccionar los archivos fuente y el paquete de destino.
4. Configurar la carpeta de salida, por ejemplo `docs/javadoc`.
5. Generar y abrir `index.html` para comprobar el resultado.

La sintaxis exacta del comando depende de la estructura de fuentes y del JDK instalado. No se incluye un comando único porque primero debe confirmarse la configuración de compilación del repositorio.

## 10. Diagnóstico

| Problema | Revisión sugerida |
|---|---|
| Error de sintaxis con `<<<<<<<` | Resolver los conflictos de Git en `VentanaPrincipal.java`. |
| No se encuentra `gui.Main` | Revisar que se abra la raíz correcta y que `src` esté configurado como fuente. |
| No se encuentra un archivo de escenario | Revisar la ruta relativa `datos/escenario.txt`. |
| Error al cargar una línea | Verificar el número de campos, separadores `;`, tipos de datos y referencias previas. |
| Fechas inválidas | Usar `AAAA-MM-DD` o desplazamientos admitidos. |
| No se visualiza el mapa | Comprobar que existan nodos y pasillos en el escenario. |

## 11. Seguridad y datos de prueba

Utilizar información ficticia para demostraciones. No cargar documentos, teléfonos, correos o fotografías reales de huéspedes en repositorios públicos.
