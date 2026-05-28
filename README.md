# Motor de Base de Datos NoSQL con Arboles AVL

Motor de base de datos no relacional implementado en Java que utiliza **arboles AVL autobalanceados** para indexacion y busqueda eficiente. Incluye interfaz grafica (Swing) e interfaz de linea de comandos (REPL).

---

## Caracteristicas

- Multiples colecciones independientes, cada una con su propio arbol AVL
- Operaciones CRUD completas con complejidad **O(log n)**
- Busqueda por campo exacto y por rango de IDs
- Persistencia automatica en archivos JSON
- Interfaz grafica minimalista con visualizacion del arbol AVL
- Interfaz de linea de comandos (REPL)

---

## Estructura del proyecto

```
src/
└── main/java/com/gestorbd/
    ├── Main.java                          # Punto de entrada
    ├── arbol/
    │   ├── AVLArbol.java                  # Arbol AVL autobalanceado
    │   └── AVLNodo.java                   # Nodo del arbol
    ├── modelo/
    │   └── Documento.java                 # Unidad de almacenamiento
    ├── persistencia/
    │   ├── RepositorioDocumentos.java      # Interfaz de persistencia
    │   └── AlmacenamientoArchivo.java      # Implementacion en archivo JSON
    ├── motor/
    │   ├── GestorBaseDatos.java            # Motor principal CRUD
    │   └── GestorColecciones.java          # Administrador de colecciones
    └── interfazusuario/
        ├── ReplBaseDatos.java              # Interfaz de linea de comandos
        └── GuiBaseDatos.java               # Interfaz grafica Swing

src/
└── test/java/com/gestorbd/
    ├── arbol/
    │   └── AVLArbolTest.java
    └── motor/
        └── GestorBaseDatosTest.java
```

Los archivos JSON de cada coleccion se guardan en la carpeta `data_db/` en la raiz del proyecto.

---

## Requisitos

| Herramienta | Version minima |
|---|---|
| Java JDK | 21 |
| NetBeans / IntelliJ / VS Code | Cualquier version reciente |

---

## Instalacion y ejecucion

**1. Descargar el proyecto**

Descarga el archivo `.zip` del repositorio desde GitHub haciendo clic en `Code` → `Download ZIP` y descomprimelo en la ubicacion que prefieras.

**2. Importar en el IDE**

- **NetBeans**: `File` → `Open Project` → selecciona la carpeta descomprimida → `Open Project`
- **IntelliJ IDEA**: `File` → `Open` → selecciona la carpeta descomprimida → `OK`
- **VS Code**: `File` → `Open Folder` → selecciona la carpeta descomprimida

El IDE detectara automaticamente el proyecto Maven y descargara las dependencias necesarias.

**3. Ejecutar el programa**

Abre el archivo `Main.java` ubicado en `src/main/java/com/gestorbd/` y ejecutalo:

- **NetBeans**: clic derecho sobre `Main.java` → `Run File`
- **IntelliJ IDEA**: clic en el boton verde ▶ junto al metodo `main`
- **VS Code**: clic en `Run` sobre el metodo `main`

Al iniciar se abre la interfaz grafica automaticamente.

---

## Datos de prueba incluidos

El proyecto incluye dos colecciones de ejemplo listas para usar en la carpeta `data_db/`:

| Archivo | Coleccion | Campos |
|---|---|---|
| `prueba 1.json` | prueba 1 | nombre, edad, codigo |
| `prueba 2.json` | prueba 2 | nombre, edad, codigo |

Al abrir el programa estas colecciones ya apareceran disponibles en el selector. Para explorarlas selecciona una con el boton `USE` y prueba los comandos de busqueda:

```
USE prueba 1
BUSCAR 1
LISTAR_COLECCIONES
BUSCAR_RANGO 1 5
BUSCAR DONDE nombre = "valor"
```

---

## Comandos disponibles

### Gestion de colecciones

| Comando | Descripcion |
|---|---|
| `CREAR_COLECCION <nombre>` | Crea una nueva coleccion |
| `BORRAR_COLECCION <nombre>` | Elimina una coleccion y su archivo JSON |
| `LISTAR_COLECCIONES` | Lista todas las colecciones existentes |
| `USE <nombre>` | Selecciona la coleccion activa para operar |

### Operaciones CRUD

| Comando | Descripcion | Complejidad |
|---|---|---|
| `INSERTAR <id> <json>` | Inserta un documento nuevo | O(log n) |
| `BUSCAR <id>` | Busca un documento por ID | O(log n) |
| `BUSCAR DONDE <campo> = <valor>` | Busca por valor exacto de un campo | O(n) |
| `BUSCAR_RANGO <id_min> <id_max>` | Busca documentos en un rango de IDs | O(log n + m) |
| `ACTUALIZAR <id> <json>` | Actualiza un documento existente | O(log n) |
| `ELIMINAR <id>` | Elimina un documento por ID | O(log n) |

---

## Ejemplos de uso

### Crear una coleccion e insertar documentos

```
CREAR_COLECCION usuarios
OK: Coleccion 'usuarios' creada con exito.

USE usuarios
OK: Contexto cambiado a la coleccion: usuarios

INSERTAR 1 {"nombre":"Juan","edad":25,"codigo":"EST001"}
OK: Documento [1] insertado en el AVL y persistido.

INSERTAR 2 {"nombre":"Ana","edad":30,"codigo":"EST002"}
OK: Documento [2] insertado en el AVL y persistido.

INSERTAR 3 {"nombre":"Pedro","edad":22,"codigo":"EST003"}
OK: Documento [3] insertado en el AVL y persistido.
```

### Buscar por ID

```
BUSCAR 1
ID 1 ->
{
  "nombre" : "Juan",
  "edad" : 25,
  "codigo" : "EST001"
}
```

### Buscar por campo exacto

```
BUSCAR DONDE nombre = "Juan"
--- Resultados encontrados (1) ---
ID 1 -> {"nombre":"Juan","edad":25,"codigo":"EST001"}
```

### Buscar por rango de IDs

```
BUSCAR_RANGO 1 2
--- Documentos en rango [1 - 2] (ordenados por AVL) ---
ID 1 -> {"nombre":"Juan","edad":25,"codigo":"EST001"}
ID 2 -> {"nombre":"Ana","edad":30,"codigo":"EST002"}
```

### Actualizar un documento

```
ACTUALIZAR 1 {"nombre":"Juan","edad":26,"codigo":"EST001"}
OK: Documento [1] actualizado con exito.
```

### Eliminar un documento

```
ELIMINAR 3
OK: Documento [3] eliminado del AVL y del archivo JSON.
```

### Trabajar con varias colecciones

```
CREAR_COLECCION docentes
OK: Coleccion 'docentes' creada con exito.

USE docentes
OK: Contexto cambiado a la coleccion: docentes

INSERTAR 1 {"nombre":"Carlos","edad":45,"codigo":"DOC001"}
OK: Documento [1] insertado en el AVL y persistido.

USE usuarios
OK: Contexto cambiado a la coleccion: usuarios

LISTAR_COLECCIONES
Colecciones disponibles: [usuarios, docentes, prueba 1, prueba 2]
```

---

## Persistencia

Cada coleccion se guarda automaticamente en un archivo JSON dentro de la carpeta `data_db/`:

```
data_db/
├── prueba 1.json
├── prueba 2.json
└── usuarios.json
```

Al reiniciar el programa, todas las colecciones y sus documentos se restauran automaticamente desde estos archivos. El arbol AVL se reconstruye en memoria a partir de los datos persistidos.

---

## Complejidad temporal

| Operacion | Complejidad |
|---|---|
| Insertar documento | O(log n) |
| Buscar por ID | O(log n) |
| Actualizar documento | O(log n) |
| Eliminar documento | O(log n) |
| Buscar por rango de IDs | O(log n + m) |
| Buscar por campo | O(n) |
| Listar todos los documentos | O(n) |
| Cargar coleccion desde archivo | O(k log k) |

Donde `n` es el numero de documentos, `m` es el numero de resultados del rango y `k` es el numero de documentos en el archivo al cargar.

---

## Tecnologias utilizadas

- **Java 21**
- **Maven** — gestion de dependencias y compilacion
- **Jackson** — serializacion y deserializacion JSON
- **Lombok** — reduccion de codigo boilerplate
- **JUnit 5** — pruebas unitarias
- **Swing** — interfaz grafica

---

## Autores

Desarrollado como proyecto academico para la asignatura de Estructuras de Datos.
