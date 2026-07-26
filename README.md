# analisisapp.jar
# AnalyticaPyME — Ventas de Babahoyo, Los Ríos

Aplicación de escritorio para el análisis de ventas: carga de datos, tendencias, pronósticos, gráficos estadísticos y cartograma por zonas.

## Requisitos

- Tener **Java** instalado (versión 17 o superior).
  - Si no lo tenés, descargalo gratis desde: https://adoptium.net
  - Para comprobar si ya lo tenés, abrí una terminal/consola y escribí:
    ```
    java -version
    ```

## Cómo ejecutar la aplicación

**Opción 1 — Doble clic**
Hacé doble clic sobre el archivo `SalesAnalysisApp.jar`. En Windows y Mac, si Java está bien instalado, la aplicación debería abrirse directamente.

**Opción 2 — Desde la terminal/consola**
Ubicate en la carpeta donde está el archivo y ejecutá:

```
java -jar SalesAnalysisApp.jar
```

## Notas

- La aplicación no requiere instalación ni conexión a internet: es un solo archivo (`.jar`) autocontenido.
- Al abrirse, ya trae datos de ejemplo cargados para que puedas explorar las pestañas sin necesidad de ingresar información primero.
- Incluye modo claro/oscuro (botón en la esquina superior derecha).

## Pestañas disponibles

| Pestaña | Descripción |
|---|---|
| 📥 Carga de Ventas | Ingreso y edición de registros de ventas por zona y mes |
| 📈 Tendencias | Visualización de la evolución de ventas en el tiempo |
| 🔮 Pronósticos | Estimaciones de ventas futuras basadas en los datos cargados |
| 📊 Gráficos Estadísticos | Gráficos comparativos (barras, circular, etc.) |
| 🗺️ Cartograma | Mapa esquemático con el tamaño/color de cada zona según sus ventas |

## Problemas comunes

- **"No se reconoce el comando java"**: Java no está instalado o no está en el PATH del sistema. Instalalo desde el enlace de arriba.
- **El doble clic no abre nada**: probá la Opción 2 (desde terminal) para ver el mensaje de error exacto, o verificá que el archivo `.jar` esté asociado a Java en tu sistema operativo.
