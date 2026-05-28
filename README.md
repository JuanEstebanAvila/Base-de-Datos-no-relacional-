# Motor de Base de Datos NoRelacional con Arboles AVL

Motor de base de datos no relacional implementado en Java que utiliza **arboles AVL autobalanceados** para indexacion y busqueda eficiente. Incluye interfaz grafica (Swing).

---

## Caracteristicas

- Multiples colecciones independientes, cada una con su propio arbol AVL
- Operaciones CRUD completas con complejidad **O(log n)**
- Busqueda por campo exacto y por rango de IDs
- Persistencia automatica en archivos JSON
- Interfaz grafica minimalista con visualizacion del arbol AVL

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
| Apache Maven | 3.8+ |

---

## Instalacion y ejecucion

**1. Clonar el repositorio**
```bash
git clone https://github.com/tu-usuario/tu-repositorio.git
cd tu-repositorio
```

**2. Compilar el proyecto**
```bash
mvn compile
```

**3. Ejecutar las pruebas unitarias**
```bash
mvn test
```

**4. Ejecutar el programa**
```bash
mvn exec:java -Dexec.mainClass="com.gestorbd.Main"
```

O desde tu IDE (NetBeans, IntelliJ, VS Code) ejecutando directamente `Main.java`.

Al iniciar, se abre la interfaz grafica. La carpeta `data_db/` se crea automaticamente si no existe.

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

INSERTAR 1 {"nombre":"Juan","edad":25,"ciudad":"Bogota"}
OK: Documento [1] insertado en el AVL y persistido.

INSERTAR 2 {"nombre":"Ana","edad":30,"ciudad":"Medellin"}
OK: Documento [2] insertado en el AVL y persistido.

INSERTAR 3 {"nombre":"Pedro","edad":22,"ciudad":"Cali"}
OK: Documento [3] insertado en el AVL y persistido.
```

### Buscar por ID

```
BUSCAR 1
ID 1 ->
{
  "nombre" : "Juan",
  "edad" : 25,
  "ciudad" : "Bogota"
}
```

### Buscar por campo exacto

```
BUSCAR DONDE ciudad = "Bogota"
--- Resultados encontrados (1) ---
ID 1 -> {"nombre":"Juan","edad":25,"ciudad":"Bogota"}
```

### Buscar por rango de IDs

```
BUSCAR_RANGO 1 2
--- Documentos en rango [1 - 2] (ordenados por AVL) ---
ID 1 -> {"nombre":"Juan","edad":25,"ciudad":"Bogota"}
ID 2 -> {"nombre":"Ana","edad":30,"ciudad":"Medellin"}
```

### Actualizar un documento

```
ACTUALIZAR 1 {"nombre":"Juan","edad":26,"ciudad":"Bogota"}
OK: Documento [1] actualizado con exito.
```

### Eliminar un documento

```
ELIMINAR 3
OK: Documento [3] eliminado del AVL y del archivo JSON.
```

### Trabajar con varias colecciones

```
CREAR_COLECCION productos
OK: Coleccion 'productos' creada con exito.

USE productos
OK: Contexto cambiado a la coleccion: productos

INSERTAR 1 {"nombre":"Laptop","precio":2500000,"stock":10}
OK: Documento [1] insertado en el AVL y persistido.

USE usuarios
OK: Contexto cambiado a la coleccion: usuarios

LISTAR_COLECCIONES
Colecciones disponibles: [usuarios, productos]
```

---

## Persistencia

Cada coleccion se guarda automaticamente en un archivo JSON dentro de la carpeta `data_db/`:

```
data_db/
├── usuarios.json
└── productos.json
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
