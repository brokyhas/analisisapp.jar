# AnalyticaPyME Babahoyo 📊

Aplicación de escritorio en Java (Swing) para el análisis de ventas de pequeñas y medianas empresas en Babahoyo, Los Ríos, Ecuador. Permite registrar ventas por zona, visualizar tendencias, generar pronósticos simples y explorar los datos en un cartograma interactivo.

## Características

- **Carga de Ventas**: registro manual de ventas por zona, mes y año, con tabla editable.
- **Importar/Exportar Excel (.xlsx)**: carga y guarda los datos de ventas en formato Excel, sin depender de librerías externas.
- **Tendencias**: gráfico de líneas con el histórico de ventas mensuales por zona.
- **Pronósticos**: estimación del próximo mes mediante regresión lineal simple.
- **Gráficos Estadísticos**: gráfico de barras y gráfico circular (pastel) con la participación de cada zona.
- **Cartograma interactivo**: mapa esquemático con zoom, desplazamiento (pan) y zonas arrastrables, con tamaño y color proporcional a las ventas.
- **Modo claro / oscuro**: cambio de tema desde la barra superior.

## Requisitos

- [JDK](https://adoptium.net/) 11 o superior (usa únicamente librerías estándar de Java: Swing, AWT y `org.w3c.dom`, sin dependencias externas).

## Compilar y ejecutar

```bash
javac SalesAnalysisApp.java
jar cfe SalesAnalysisApp.jar SalesAnalysisApp *.class
java -jar SalesAnalysisApp.jar
```

En Windows (PowerShell), si `jar` no se reconoce como comando, usa la ruta completa a tu JDK, por ejemplo:

```powershell
& "C:\Program Files\Java\jdk-21.0.11\bin\jar.exe" cfe SalesAnalysisApp.jar SalesAnalysisApp *.class
```

## Uso

1. Abre la aplicación con `java -jar SalesAnalysisApp.jar` o haciendo doble clic sobre el `.jar` (requiere tener Java instalado).
2. Ve a la pestaña **Carga de Ventas** para registrar ventas o importar un archivo Excel existente.
3. Explora las pestañas **Tendencias**, **Pronósticos**, **Gráficos Estadísticos** y **Cartograma** para analizar los datos.

## Estructura del proyecto

Todo el código vive en un único archivo `SalesAnalysisApp.java`, que contiene la clase principal y varias clases de soporte:

| Clase | Responsabilidad |
|---|---|
| `SalesAnalysisApp` | Ventana principal y punto de entrada (`main`) |
| `DataStore` | Almacén de datos de ventas en memoria |
| `XlsxUtil` | Lectura y escritura de archivos `.xlsx` sin dependencias externas |
| `PanelCarga` | Registro e importación/exportación de ventas |
| `PanelTendencias` / `ChartLineaPanel` | Gráfico de tendencias mensuales |
| `PanelPronosticos` / `ChartLineaPronosticoPanel` | Pronóstico por regresión lineal |
| `PanelGraficos` / `ChartBarraPanel` / `ChartPastelPanel` | Gráficos estadísticos por zona |
| `PanelCartograma` / `CartogramaPanel` | Mapa interactivo de zonas |
| `AppConstants` | Colores, fuentes y constantes de la interfaz (modo claro/oscuro) |

## Licencia

Este proyecto está bajo la licencia MIT. Consulta el archivo [LICENSE](LICENSE) para más detalles.
