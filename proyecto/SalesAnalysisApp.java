import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.GeneralPath;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.IntSupplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

//iniciacion del codigo pig

public class SalesAnalysisApp extends JFrame {

    private final DataStore store = new DataStore();
    private JComponent contenedorTabs;

    public SalesAnalysisApp() {
        super("AnalyticaPyME Babahoyo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1160, 760);
        setMinimumSize(new Dimension(980, 660));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        seedSampleData();
        construirInterfaz();
    }

    // Reconstruye toda la interfaz leyendo la paleta de colores actual (claro u oscuro).
    // Se llama una vez al iniciar y cada vez que el usuario alterna el modo de color.
    private void construirInterfaz() {
        // Refresca primero los valores globales de UIManager (combos, spinners, tablas,
        // tooltips, cuadros de diálogo, selector de archivos...) para que TODO el look and
        // feel nativo de Swing se adapte también al modo claro/oscuro, no solo los paneles
        // personalizados que pintamos a mano.
        AppConstants.actualizarUIManager();
        SwingUtilities.updateComponentTreeUI(this);

        getContentPane().removeAll();
        getContentPane().setBackground(AppConstants.BG);
        store.clearListeners();

        JPanel header = new HeaderPanel();
        header.setLayout(new BorderLayout());
        header.setPreferredSize(new Dimension(100, 68));
        header.setBorder(BorderFactory.createEmptyBorder(0, 26, 0, 22));

        JPanel west = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        west.setOpaque(false);
        JLabel logo = new JLabel("📊");
        logo.setFont(new Font("SansSerif", Font.PLAIN, 24));
        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        JLabel titulo = new JLabel("AnalyticaPyME — Ventas de Babahoyo, Los Ríos");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        JLabel subtitulo = new JLabel("Panel de control comercial");
        subtitulo.setForeground(new Color(255, 255, 255, 195));
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        textos.add(titulo);
        textos.add(subtitulo);
        west.add(logo);
        west.add(textos);
        header.add(west, BorderLayout.WEST);

        JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        east.setOpaque(false);

        String textoBoton = AppConstants.modoOscuro ? "☀  Modo claro" : "🌙  Modo oscuro";
        RoundedButton btnTema = new RoundedButton(textoBoton, new Color(255, 255, 255, 40), Color.WHITE);
        btnTema.addActionListener(e -> {
            AppConstants.alternarModo();
            construirInterfaz();
        });
        east.add(btnTema);

        header.add(east, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));
        tabs.setBackground(AppConstants.PANEL_BG);
        tabs.setForeground(AppConstants.TEXT_PRIMARY);
        tabs.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        tabs.addTab("📥  Carga de Ventas", new PanelCarga(store));
        tabs.addTab("📈  Tendencias", new PanelTendencias(store));
        tabs.addTab("🔮  Pronósticos", new PanelPronosticos(store));
        tabs.addTab("📊  Gráficos Estadísticos", new PanelGraficos(store));
        tabs.addTab("🗺️  Cartograma", new PanelCartograma(store));

        JPanel tabsWrapper = new JPanel(new BorderLayout());
        tabsWrapper.setBackground(AppConstants.BG);
        tabsWrapper.setBorder(BorderFactory.createEmptyBorder(10, 12, 12, 12));
        tabsWrapper.add(tabs, BorderLayout.CENTER);

        contenedorTabs = tabsWrapper;
        add(contenedorTabs, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    // Datos de ejemplo para que la aplicación no inicie vacía.
    private void seedSampleData() {
        int anio = 2026;
        double[] baseMonto = {15000, 6000, 4500, 5200, 4000, 3800};
        double[] crecimiento = {0.04, 0.02, -0.01, 0.03, 0.015, 0.01};
        String[] zonas = AppConstants.ZONAS;
        for (int zi = 0; zi < zonas.length; zi++) {
            double monto = baseMonto[zi];
            for (int mes = 1; mes <= 6; mes++) {
                monto = monto * (1 + crecimiento[zi]) + (Math.random() - 0.5) * monto * 0.05;
                store.getRegistros().add(new SalesRecord(zonas[zi], anio, mes, Math.round(monto * 100.0) / 100.0));
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Se usa Metal (multiplataforma) en lugar del look and feel nativo del sistema
                // porque Metal respeta los colores que definimos en UIManager. Así logramos que
                // TODOS los componentes (combos, listas desplegables, spinners, tooltips,
                // cuadros de diálogo y el selector de archivos) cambien de verdad entre modo
                // claro y oscuro, en vez de quedarse con los colores nativos del sistema operativo.
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                UIManager.put("swing.boldMetal", Boolean.FALSE);
            } catch (Exception ignored) {
                // Se usa el look and feel por defecto si falla.
            }
            AppConstants.actualizarUIManager();
            new SalesAnalysisApp().setVisible(true);
        });
    }
}

// Encabezado con degradado de color para un aspecto más moderno. Lee los colores de forma
// dinámica en cada pintado, por lo que se adapta automáticamente al modo claro/oscuro.
class HeaderPanel extends JPanel {
    HeaderPanel() {
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gp = new GradientPaint(0, 0, AppConstants.PRIMARY_DARK, getWidth(), 0, AppConstants.PRIMARY);
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
        super.paintComponent(g);
    }
}

// Botón plano con esquinas redondeadas y efecto hover.
class RoundedButton extends JButton {
    private final Color baseColor;
    private final Color hoverColor;
    private boolean hovering = false;

    RoundedButton(String text, Color baseColor, Color textColor) {
        super(text);
        this.baseColor = baseColor;
        this.hoverColor = baseColor.darker();
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setForeground(textColor);
        setFont(new Font("SansSerif", Font.BOLD, 13));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovering = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovering = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(hovering ? hoverColor : baseColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
        g2.dispose();
        super.paintComponent(g);
    }
}

// Tarjeta con esquinas redondeadas y una sombra suave, usada para agrupar controles.
// El color de fondo se lee de AppConstants en cada pintado, así se adapta al tema activo.
class CardPanel extends JPanel {
    CardPanel() {
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(0, 0, 0, AppConstants.modoOscuro ? 70 : 18));
        g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 4, 16, 16);
        g2.setColor(AppConstants.PANEL_BG);
        g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 6, 16, 16);
        g2.setColor(AppConstants.GRID);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, getWidth() - 5, getHeight() - 7, 16, 16);
        g2.dispose();
        super.paintComponent(g);
    }
}

// Pequeña tarjeta de estadística (número grande + etiqueta), usada como resumen rápido.
class StatCard extends JPanel {
    private final JLabel valorLbl;
    private final Color acento;

    StatCard(String etiqueta, String valorInicial, Color acento) {
        this.acento = acento;
        setOpaque(false);
        setLayout(new BorderLayout(0, 2));
        setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JLabel etiquetaLbl = new JLabel(etiqueta.toUpperCase(Locale.US));
        etiquetaLbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        etiquetaLbl.setForeground(AppConstants.SECONDARY);

        valorLbl = new JLabel(valorInicial);
        valorLbl.setFont(new Font("SansSerif", Font.BOLD, 20));
        valorLbl.setForeground(acento);

        add(etiquetaLbl, BorderLayout.NORTH);
        add(valorLbl, BorderLayout.CENTER);
    }

    void setValor(String valor) {
        valorLbl.setText(valor);
        valorLbl.setForeground(acento);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(AppConstants.PANEL_BG);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
        g2.setColor(acento);
        g2.fillRoundRect(0, 0, 5, getHeight() - 1, 14, 14);
        g2.fillRect(0, 0, 8, getHeight() - 1);
        g2.setColor(AppConstants.GRID);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
        g2.dispose();
        super.paintComponent(g);
    }
}

//Constantes de la aplicación: zonas, meses, colores (con soporte de modo claro/oscuro) y
//posiciones del cartograma.
class AppConstants {
    static final String[] ZONAS = {
            "ubicacion A", "ubicacion B", "ubicacion C", "ubicacion D", "ubicacion E", "ubicacion F"
    };

    static final String[] MESES = {
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    // Posiciones esquemáticas (0..1) de cada zona para el cartograma. No son coordenadas GPS reales.
    static final Map<String, double[]> POSICIONES = new LinkedHashMap<>();
    static {
        POSICIONES.put("ubicacion A", new double[]{0.50, 0.50});
        POSICIONES.put("ubicacion B", new double[]{0.52, 0.16});
        POSICIONES.put("ubicacion C", new double[]{0.16, 0.62});
        POSICIONES.put("ubicacion D", new double[]{0.84, 0.32});
        POSICIONES.put("ubicacion E", new double[]{0.30, 0.85});
        POSICIONES.put("ubicacion F", new double[]{0.80, 0.80});
    }

    // ---- Paleta "flat" moderna, con variante clara y oscura ----
    static boolean modoOscuro = false;

    static Color PRIMARY;
    static Color PRIMARY_DARK;
    static Color PRIMARY_LIGHT;
    static Color ACCENT;
    static Color SECONDARY;
    static Color GRAY_TEXT;
    static Color TEXT_PRIMARY;
    static Color BG;
    static Color PANEL_BG;
    static Color GRID;
    static Color ROW_ALT_BG;
    static Color ROW_HOVER_BG;
    static Color MAP_BG;
    static Color RIO_COLOR;
    static Color RIO_LABEL;
    static Color[] PALETA_GRAFICOS;

    static {
        aplicarPaleta();
    }

    // Alterna entre modo claro y oscuro y recalcula todos los colores.
    static void alternarModo() {
        modoOscuro = !modoOscuro;
        aplicarPaleta();
    }

    private static void aplicarPaleta() {
        if (modoOscuro) {
            PRIMARY = new Color(0x81, 0x8C, 0xF8);
            PRIMARY_DARK = new Color(0x3F, 0x38, 0xB5);
            PRIMARY_LIGHT = new Color(0x2A, 0x2C, 0x45);
            ACCENT = new Color(0xF8, 0x71, 0x71);
            SECONDARY = new Color(0x9C, 0xA9, 0xBC);
            GRAY_TEXT = new Color(0xC7, 0xD0, 0xDD);
            TEXT_PRIMARY = new Color(0xEA, 0xEC, 0xF2);
            BG = new Color(0x14, 0x16, 0x1F);
            PANEL_BG = new Color(0x1E, 0x21, 0x2E);
            GRID = new Color(0x34, 0x39, 0x49);
            ROW_ALT_BG = new Color(0x25, 0x28, 0x38);
            ROW_HOVER_BG = new Color(0x2E, 0x32, 0x46);
            MAP_BG = new Color(0x18, 0x1B, 0x27);
            RIO_COLOR = new Color(0x2E, 0x4A, 0x57);
            RIO_LABEL = new Color(0x6F, 0x93, 0xA3);
            PALETA_GRAFICOS = new Color[]{
                    new Color(0x81, 0x8C, 0xF8), new Color(0x6D, 0xD5, 0xED), new Color(0xFB, 0xBC, 0x04),
                    new Color(0xF8, 0x71, 0x71), new Color(0x4A, 0xD6, 0x6D), new Color(0xBB, 0x86, 0xFC)
            };
        } else {
            PRIMARY = new Color(0x4F, 0x46, 0xE5);
            PRIMARY_DARK = new Color(0x33, 0x2E, 0xB0);
            PRIMARY_LIGHT = new Color(0xEE, 0xF0, 0xFF);
            ACCENT = new Color(0xEF, 0x44, 0x44);
            SECONDARY = new Color(0x64, 0x74, 0x8B);
            GRAY_TEXT = new Color(0x47, 0x55, 0x69);
            TEXT_PRIMARY = new Color(0x1E, 0x29, 0x3B);
            BG = new Color(0xF3, 0xF5, 0xF9);
            PANEL_BG = Color.WHITE;
            GRID = new Color(0xE2, 0xE8, 0xF0);
            ROW_ALT_BG = new Color(0xF6, 0xF8, 0xFB);
            ROW_HOVER_BG = new Color(0xEA, 0xED, 0xFB);
            MAP_BG = new Color(0xEF, 0xF5, 0xF7);
            RIO_COLOR = new Color(0xBF, 0xE3, 0xEA);
            RIO_LABEL = new Color(0x7F, 0xB3, 0xC2);
            PALETA_GRAFICOS = new Color[]{
                    new Color(0x4F, 0x46, 0xE5), new Color(0x06, 0xB6, 0xD4), new Color(0xF5, 0x9E, 0x0B),
                    new Color(0xEF, 0x44, 0x44), new Color(0x10, 0xB9, 0x81), new Color(0x8B, 0x5C, 0xF6)
            };
        }
    }

    // Aplica la paleta actual a las claves globales de UIManager. Esto es lo que permite que
    // los componentes NATIVOS de Swing (JComboBox y su lista desplegable, JSpinner, JOptionPane,
    // JFileChooser, tooltips, menús emergentes, etc.) también cambien de color al alternar entre
    // modo claro y oscuro, en vez de quedarse "atascados" con los colores del look and feel.
    static void actualizarUIManager() {
        Font base = new Font("SansSerif", Font.PLAIN, 13);
        Font negrita = new Font("SansSerif", Font.BOLD, 13);

        UIManager.put("Panel.background", BG);
        UIManager.put("Panel.foreground", TEXT_PRIMARY);

        UIManager.put("OptionPane.background", PANEL_BG);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
        UIManager.put("OptionPane.font", base);
        UIManager.put("OptionPane.buttonFont", negrita);

        UIManager.put("ComboBox.background", PANEL_BG);
        UIManager.put("ComboBox.foreground", TEXT_PRIMARY);
        UIManager.put("ComboBox.selectionBackground", PRIMARY_LIGHT);
        UIManager.put("ComboBox.selectionForeground", PRIMARY_DARK);
        UIManager.put("ComboBox.buttonBackground", PANEL_BG);
        UIManager.put("ComboBox.font", base);

        UIManager.put("List.background", PANEL_BG);
        UIManager.put("List.foreground", TEXT_PRIMARY);
        UIManager.put("List.selectionBackground", PRIMARY_LIGHT);
        UIManager.put("List.selectionForeground", PRIMARY_DARK);
        UIManager.put("List.font", base);

        UIManager.put("TextField.background", PANEL_BG);
        UIManager.put("TextField.foreground", TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground", TEXT_PRIMARY);
        UIManager.put("TextField.selectionBackground", PRIMARY_LIGHT);
        UIManager.put("TextField.selectionForeground", PRIMARY_DARK);
        UIManager.put("TextField.font", base);

        UIManager.put("FormattedTextField.background", PANEL_BG);
        UIManager.put("FormattedTextField.foreground", TEXT_PRIMARY);
        UIManager.put("FormattedTextField.caretForeground", TEXT_PRIMARY);

        UIManager.put("Spinner.background", PANEL_BG);
        UIManager.put("Spinner.foreground", TEXT_PRIMARY);
        UIManager.put("Spinner.font", base);

        UIManager.put("ToolTip.background", PANEL_BG);
        UIManager.put("ToolTip.foreground", TEXT_PRIMARY);
        UIManager.put("ToolTip.font", base);

        UIManager.put("PopupMenu.background", PANEL_BG);
        UIManager.put("PopupMenu.foreground", TEXT_PRIMARY);
        UIManager.put("MenuItem.background", PANEL_BG);
        UIManager.put("MenuItem.foreground", TEXT_PRIMARY);

        UIManager.put("Table.background", PANEL_BG);
        UIManager.put("Table.foreground", TEXT_PRIMARY);
        UIManager.put("Table.gridColor", GRID);
        UIManager.put("Table.selectionBackground", PRIMARY_LIGHT);
        UIManager.put("Table.selectionForeground", PRIMARY_DARK);
        UIManager.put("Table.font", base);
        UIManager.put("TableHeader.background", PRIMARY);
        UIManager.put("TableHeader.foreground", Color.WHITE);
        UIManager.put("TableHeader.font", negrita);

        UIManager.put("ScrollPane.background", BG);
        UIManager.put("Viewport.background", PANEL_BG);
        UIManager.put("ScrollBar.thumb", SECONDARY);
        UIManager.put("ScrollBar.track", BG);

        UIManager.put("Button.background", PANEL_BG);
        UIManager.put("Button.foreground", TEXT_PRIMARY);
        UIManager.put("Button.font", negrita);

        UIManager.put("CheckBox.background", BG);
        UIManager.put("CheckBox.foreground", TEXT_PRIMARY);
        UIManager.put("RadioButton.background", BG);
        UIManager.put("RadioButton.foreground", TEXT_PRIMARY);

        UIManager.put("Label.foreground", TEXT_PRIMARY);
        UIManager.put("Label.font", base);

        UIManager.put("TabbedPane.background", PANEL_BG);
        UIManager.put("TabbedPane.foreground", TEXT_PRIMARY);
        UIManager.put("TabbedPane.selected", PRIMARY_LIGHT);
        UIManager.put("TabbedPane.selectHighlight", PRIMARY);
        UIManager.put("TabbedPane.contentAreaColor", PANEL_BG);
        UIManager.put("TabbedPane.borderHightlightColor", GRID);
        UIManager.put("TabbedPane.font", negrita);
        UIManager.put("TabbedPane.unselectedBackground", BG);

        UIManager.put("FileChooser.background", PANEL_BG);
        UIManager.put("FileChooser.foreground", TEXT_PRIMARY);

        UIManager.put("TextArea.background", PANEL_BG);
        UIManager.put("TextArea.foreground", TEXT_PRIMARY);

        UIManager.put("Viewport.font", base);
        UIManager.put("Separator.foreground", GRID);
    }

    // Crea una tarjeta con título en negrita, lista para recibir contenido al centro.
    static JPanel crearTarjeta(String titulo) {
        CardPanel card = new CardPanel();
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(14, 18, 16, 18));
        JLabel lbl = new JLabel(titulo);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        lbl.setForeground(PRIMARY_DARK);
        card.add(lbl, BorderLayout.NORTH);
        return card;
    }

    // Aplica un estilo consistente (borde redondeado, relleno y tipografía) a campos de entrada.
    static void estilizarCampo(JComponent c) {
        c.setFont(new Font("SansSerif", Font.PLAIN, 13));
        c.setBackground(PANEL_BG);
        c.setForeground(TEXT_PRIMARY);
        c.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(GRID, 1, true),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
    }

    // Reconstruye el modelo de un combo con las zonas actuales (predefinidas + escritas por el usuario).
    static void refrescarZonas(JComboBox<String> combo, DataStore store, boolean incluirTodas) {
        Object actual = combo.getSelectedItem();
        DefaultComboBoxModel<String> modelo = new DefaultComboBoxModel<>();
        if (incluirTodas) modelo.addElement("Todas las zonas");
        for (String z : store.getZonas()) modelo.addElement(z);
        combo.setModel(modelo);
        if (actual != null && modelo.getIndexOf(actual) >= 0) {
            combo.setSelectedItem(actual);
        } else if (modelo.getSize() > 0) {
            combo.setSelectedIndex(0);
        }
    }
}

// Un registro individual de venta mensual en una zona
class SalesRecord {
    String zona;
    int anio;
    int mes;
    double monto;

    SalesRecord(String zona, int anio, int mes, double monto) {
        this.zona = zona;
        this.anio = anio;
        this.mes = mes;
        this.monto = monto;
    }

    String getPeriodoKey() {
        return String.format(Locale.US, "%04d-%02d", anio, mes);
    }
}

//Almacén central de datos con notificación simple a los paneles suscritos.
class DataStore {
    private final List<SalesRecord> registros = new ArrayList<>();
    private final List<Runnable> listeners = new ArrayList<>();
    // Zonas conocidas: empieza con las predefinidas y crece con las que el usuario escriba.
    private final LinkedHashSet<String> zonas = new LinkedHashSet<>(Arrays.asList(AppConstants.ZONAS));

    List<SalesRecord> getRegistros() {
        return registros;
    }

    // Todas las zonas conocidas: las predefinidas más las que el usuario haya agregado.
    List<String> getZonas() {
        return new ArrayList<>(zonas);
    }

    void addListener(Runnable r) {
        listeners.add(r);
    }

    // Se usa al reconstruir la interfaz (p. ej. al alternar el modo claro/oscuro) para evitar
    // que se acumulen escuchas apuntando a paneles ya descartados.
    void clearListeners() {
        listeners.clear();
    }

    private void notifyListeners() {
        for (Runnable r : listeners) r.run();
    }

    void add(SalesRecord s) {
        registros.add(s);
        zonas.add(s.zona);
        notifyListeners();
    }

    void addAll(List<SalesRecord> nuevos) {
        for (SalesRecord s : nuevos) zonas.add(s.zona);
        registros.addAll(nuevos);
        notifyListeners();
    }

    void removeAt(int index) {
        if (index >= 0 && index < registros.size()) {
            registros.remove(index);
            notifyListeners();
        }
    }

    void clear() {
        registros.clear();
        notifyListeners();
    }

    // Suma de ventas agrupadas por periodo (año-mes) para una zona, o para "Todas las zonas".
    Map<String, Double> totalesPorPeriodo(String zona) {
        Map<String, Double> mapa = new TreeMap<>();
        for (SalesRecord r : registros) {
            if ("Todas las zonas".equals(zona) || r.zona.equals(zona)) {
                mapa.merge(r.getPeriodoKey(), r.monto, Double::sum);
            }
        }
        return mapa;
    }

    //Suma total de ventas por zona (incluye zonas sin ventas, con 0.0).
    Map<String, Double> totalesPorZona() {
        Map<String, Double> mapa = new LinkedHashMap<>();
        for (String z : zonas) mapa.put(z, 0.0);
        for (SalesRecord r : registros) {
            mapa.merge(r.zona, r.monto, Double::sum);
        }
        return mapa;
    }

    double totalGeneral() {
        double total = 0;
        for (SalesRecord r : registros) total += r.monto;
        return total;
    }

    String zonaConMasVentas() {
        Map<String, Double> totales = totalesPorZona();
        String mejor = "—";
        double max = Double.NEGATIVE_INFINITY;
        for (Map.Entry<String, Double> e : totales.entrySet()) {
            if (e.getValue() > max) {
                max = e.getValue();
                mejor = e.getKey();
            }
        }
        return mejor;
    }
}

/* =====================  LECTURA Y ESCRITURA DE EXCEL (.xlsx) SIN LIBRERÍAS EXTERNAS  =====================
   Genera y lee archivos .xlsx reales (formato OOXML: un ZIP con XML adentro), por lo que se
   abren correctamente en Microsoft Excel, LibreOffice Calc, Google Sheets, etc. El archivo se
   puede editar en Excel y luego volver a cargar en la aplicación con "Cargar Excel".
*/
class XlsxUtil {

    static void escribir(File archivo, List<SalesRecord> registros) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(archivo)))) {
            escribirEntrada(zos, "[Content_Types].xml", contentTypesXml());
            escribirEntrada(zos, "_rels/.rels", relsXml());
            escribirEntrada(zos, "xl/workbook.xml", workbookXml());
            escribirEntrada(zos, "xl/_rels/workbook.xml.rels", workbookRelsXml());
            escribirEntrada(zos, "xl/styles.xml", stylesXml());
            escribirEntrada(zos, "xl/worksheets/sheet1.xml", sheetXml(registros));
        }
    }

    private static void escribirEntrada(ZipOutputStream zos, String nombre, String contenido) throws IOException {
        ZipEntry entry = new ZipEntry(nombre);
        zos.putNextEntry(entry);
        zos.write(contenido.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private static String contentTypesXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
                + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                + "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>"
                + "</Types>";
    }

    private static String relsXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
                + "</Relationships>";
    }

    private static String workbookXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
                + "<sheets><sheet name=\"Ventas\" sheetId=\"1\" r:id=\"rId1\"/></sheets>"
                + "</workbook>";
    }

    private static String workbookRelsXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
                + "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>"
                + "</Relationships>";
    }

    private static String stylesXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                + "<numFmts count=\"1\"><numFmt numFmtId=\"164\" formatCode=\"&quot;$&quot;#,##0.00\"/></numFmts>"
                + "<fonts count=\"2\"><font><sz val=\"11\"/><name val=\"Calibri\"/></font>"
                + "<font><b/><sz val=\"11\"/><color rgb=\"FFFFFFFF\"/><name val=\"Calibri\"/></font></fonts>"
                + "<fills count=\"2\"><fill><patternFill patternType=\"none\"/></fill>"
                + "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FF4F46E5\"/><bgColor indexed=\"64\"/></patternFill></fill></fills>"
                + "<borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders>"
                + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
                + "<cellXfs count=\"3\">"
                + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
                + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"1\" borderId=\"0\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\"/>"
                + "<xf numFmtId=\"164\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyNumberFormat=\"1\"/>"
                + "</cellXfs>"
                + "</styleSheet>";
    }

    private static String sheetXml(List<SalesRecord> registros) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        sb.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
        sb.append("<sheetViews><sheetView workbookViewId=\"0\">")
          .append("<pane ySplit=\"1\" topLeftCell=\"A2\" activePane=\"bottomLeft\" state=\"frozen\"/>")
          .append("</sheetView></sheetViews>");
        sb.append("<cols>")
          .append("<col min=\"1\" max=\"1\" width=\"22\" customWidth=\"1\"/>")
          .append("<col min=\"2\" max=\"2\" width=\"10\" customWidth=\"1\"/>")
          .append("<col min=\"3\" max=\"3\" width=\"10\" customWidth=\"1\"/>")
          .append("<col min=\"4\" max=\"4\" width=\"14\" customWidth=\"1\"/>")
          .append("<col min=\"5\" max=\"5\" width=\"16\" customWidth=\"1\"/>")
          .append("</cols>");
        sb.append("<sheetData>");
        sb.append("<row r=\"1\">");
        sb.append(celdaTexto("A1", "Zona", 1));
        sb.append(celdaTexto("B1", "Anio", 1));
        sb.append(celdaTexto("C1", "Mes", 1));
        sb.append(celdaTexto("D1", "Monto", 1));
        sb.append(celdaTexto("E1", "Mes (nombre)", 1));
        sb.append("</row>");
        int fila = 2;
        for (SalesRecord r : registros) {
            sb.append("<row r=\"").append(fila).append("\">");
            sb.append(celdaTexto("A" + fila, r.zona, 0));
            sb.append(celdaNumero("B" + fila, r.anio, 0));
            sb.append(celdaNumero("C" + fila, r.mes, 0));
            sb.append(celdaNumero("D" + fila, r.monto, 2));
            String nombreMes = (r.mes >= 1 && r.mes <= 12) ? AppConstants.MESES[r.mes - 1] : "";
            sb.append(celdaTexto("E" + fila, nombreMes, 0));
            sb.append("</row>");
            fila++;
        }
        sb.append("</sheetData></worksheet>");
        return sb.toString();
    }

    private static String celdaTexto(String ref, String valor, int estilo) {
        return "<c r=\"" + ref + "\" t=\"inlineStr\"" + (estilo > 0 ? " s=\"" + estilo + "\"" : "")
                + "><is><t>" + escaparXml(valor) + "</t></is></c>";
    }

    private static String celdaNumero(String ref, double valor, int estilo) {
        return "<c r=\"" + ref + "\"" + (estilo > 0 ? " s=\"" + estilo + "\"" : "")
                + "><v>" + valorNumerico(valor) + "</v></c>";
    }

    private static String valorNumerico(double v) {
        if (v == Math.rint(v) && !Double.isInfinite(v)) return String.valueOf((long) v);
        return String.valueOf(v);
    }

    private static String escaparXml(String s) {
        if (s == null) return "";
        StringBuilder out = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '&': out.append("&amp;"); break;
                case '<': out.append("&lt;"); break;
                case '>': out.append("&gt;"); break;
                case '"': out.append("&quot;"); break;
                case '\'': out.append("&apos;"); break;
                default: out.append(c);
            }
        }
        return out.toString();
    }

    // ---- Lectura ----
    // Robusta ante archivos re-guardados por Excel: resuelve la ruta real de la primera hoja
    // a través de workbook.xml / workbook.xml.rels, y admite tanto texto en línea (inlineStr,
    // como el que generamos nosotros) como cadenas compartidas (sharedStrings.xml, como suele
    // usar Excel al volver a guardar el archivo).
    static List<SalesRecord> leer(File archivo) throws Exception {
        List<SalesRecord> resultado = new ArrayList<>();
        try (ZipFile zf = new ZipFile(archivo)) {
            String rutaHoja = resolverRutaPrimeraHoja(zf);
            ZipEntry hojaEntry = zf.getEntry(rutaHoja);
            if (hojaEntry == null) hojaEntry = zf.getEntry("xl/worksheets/sheet1.xml");
            if (hojaEntry == null) {
                throw new IOException("El archivo no parece un Excel (.xlsx) válido: no se encontró la hoja de datos.");
            }

            Map<Integer, String> compartidas = new HashMap<>();
            ZipEntry sharedEntry = zf.getEntry("xl/sharedStrings.xml");
            if (sharedEntry != null) {
                compartidas = leerSharedStrings(zf.getInputStream(sharedEntry));
            }

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(zf.getInputStream(hojaEntry));
            NodeList filas = doc.getElementsByTagName("row");

            for (int i = 0; i < filas.getLength(); i++) {
                Element fila = (Element) filas.item(i);
                Map<String, String> celdas = new HashMap<>();
                NodeList cs = fila.getElementsByTagName("c");
                for (int j = 0; j < cs.getLength(); j++) {
                    Element c = (Element) cs.item(j);
                    String ref = c.getAttribute("r");
                    String col = ref.replaceAll("[0-9]", "");
                    String tipo = c.getAttribute("t");
                    String valor = obtenerValorCelda(c, tipo, compartidas);
                    celdas.put(col, valor);
                }
                if (i == 0) continue; // fila de encabezado
                if (celdas.isEmpty()) continue;
                String zona = celdas.getOrDefault("A", "").trim();
                if (zona.isEmpty()) continue;
                try {
                    int anio = (int) Math.round(Double.parseDouble(celdas.getOrDefault("B", "0").replace(",", ".")));
                    int mes = (int) Math.round(Double.parseDouble(celdas.getOrDefault("C", "0").replace(",", ".")));
                    double monto = Double.parseDouble(celdas.getOrDefault("D", "0").replace(",", "."));
                    if (mes < 1 || mes > 12) continue;
                    resultado.add(new SalesRecord(zona, anio, mes, monto));
                } catch (NumberFormatException ignored) {
                    // Fila con datos no numéricos: se omite silenciosamente.
                }
            }
        }
        return resultado;
    }

    private static String resolverRutaPrimeraHoja(ZipFile zf) {
        try {
            ZipEntry wbEntry = zf.getEntry("xl/workbook.xml");
            ZipEntry relsEntry = zf.getEntry("xl/_rels/workbook.xml.rels");
            if (wbEntry == null || relsEntry == null) return "xl/worksheets/sheet1.xml";

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document wbDoc = db.parse(zf.getInputStream(wbEntry));
            NodeList sheets = wbDoc.getElementsByTagName("sheet");
            if (sheets.getLength() == 0) return "xl/worksheets/sheet1.xml";
            Element primerSheet = (Element) sheets.item(0);
            String rId = primerSheet.getAttribute("r:id");
            if (rId == null || rId.isEmpty()) return "xl/worksheets/sheet1.xml";

            Document relsDoc = db.parse(zf.getInputStream(relsEntry));
            NodeList rels = relsDoc.getElementsByTagName("Relationship");
            for (int i = 0; i < rels.getLength(); i++) {
                Element rel = (Element) rels.item(i);
                if (rId.equals(rel.getAttribute("Id"))) {
                    String target = rel.getAttribute("Target");
                    if (target.startsWith("/")) return target.substring(1);
                    return "xl/" + target;
                }
            }
        } catch (Exception ignored) {
            // Ante cualquier problema, se recurre a la ruta habitual por defecto.
        }
        return "xl/worksheets/sheet1.xml";
    }

    private static String obtenerValorCelda(Element c, String tipo, Map<Integer, String> compartidas) {
        if ("inlineStr".equals(tipo)) {
            NodeList tNodes = c.getElementsByTagName("t");
            return tNodes.getLength() > 0 ? tNodes.item(0).getTextContent() : "";
        }
        NodeList vNodes = c.getElementsByTagName("v");
        String v = vNodes.getLength() > 0 ? vNodes.item(0).getTextContent() : "";
        if ("s".equals(tipo)) {
            try {
                int idx = Integer.parseInt(v);
                return compartidas.getOrDefault(idx, "");
            } catch (NumberFormatException e) {
                return v;
            }
        }
        return v;
    }

    private static Map<Integer, String> leerSharedStrings(InputStream is) throws Exception {
        Map<Integer, String> mapa = new HashMap<>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(is);
        NodeList items = doc.getElementsByTagName("si");
        for (int i = 0; i < items.getLength(); i++) {
            Element si = (Element) items.item(i);
            NodeList ts = si.getElementsByTagName("t");
            StringBuilder texto = new StringBuilder();
            for (int j = 0; j < ts.getLength(); j++) texto.append(ts.item(j).getTextContent());
            mapa.put(i, texto.toString());
        }
        return mapa;
    }
}

/* =====================  PESTAÑA: CARGA DE VENTAS  ===================== */

class PanelCarga extends JPanel {
    private final DataStore store;
    private JComboBox<String> cbZona;
    private JComboBox<String> cbMes;
    private JSpinner spAnio;
    private JTextField tfMonto;
    private JTable table;
    private RegistrosTableModel tableModel;
    private int filaResaltada = -1;

    private StatCard cardTotal;
    private StatCard cardRegistros;
    private StatCard cardZonaTop;

    PanelCarga(DataStore store) {
        this.store = store;
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setBackground(AppConstants.BG);

        JPanel norte = new JPanel(new BorderLayout(0, 12));
        norte.setOpaque(false);
        norte.add(buildResumenPanel(), BorderLayout.NORTH);
        norte.add(buildFormPanel(), BorderLayout.SOUTH);
        add(norte, BorderLayout.NORTH);

        tableModel = new RegistrosTableModel(store);
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setBackground(AppConstants.PANEL_BG);
        table.setForeground(AppConstants.TEXT_PRIMARY);
        table.setShowVerticalLines(false);
        table.setGridColor(AppConstants.GRID);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(AppConstants.PRIMARY_LIGHT);
        table.setSelectionForeground(AppConstants.PRIMARY_DARK);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(AppConstants.PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 34));
        table.setDefaultRenderer(Object.class, new AlternatingRowRenderer(() -> filaResaltada));
        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int fila = table.rowAtPoint(e.getPoint());
                if (fila != filaResaltada) {
                    filaResaltada = fila;
                    table.repaint();
                }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                filaResaltada = -1;
                table.repaint();
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(AppConstants.PANEL_BG);

        CardPanel tableCard = new CardPanel();
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        tableCard.add(scroll, BorderLayout.CENTER);
        add(tableCard, BorderLayout.CENTER);

        add(buildBottomPanel(), BorderLayout.SOUTH);

        store.addListener(() -> {
            tableModel.fireTableDataChanged();
            AppConstants.refrescarZonas(cbZona, store, false);
            actualizarResumen();
        });
        actualizarResumen();
    }

    private JPanel buildResumenPanel() {
        JPanel fila = new JPanel(new java.awt.GridLayout(1, 3, 12, 0));
        fila.setOpaque(false);
        cardTotal = new StatCard("Ventas totales", "$0.00", AppConstants.PRIMARY);
        cardRegistros = new StatCard("Registros cargados", "0", AppConstants.ACCENT);
        cardZonaTop = new StatCard("Zona con más ventas", "—", new Color(0x10, 0xB9, 0x81));
        fila.add(cardTotal);
        fila.add(cardRegistros);
        fila.add(cardZonaTop);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        wrapper.add(fila, BorderLayout.CENTER);
        return wrapper;
    }

    private void actualizarResumen() {
        cardTotal.setValor(String.format(Locale.US, "$%,.2f", store.totalGeneral()));
        cardRegistros.setValor(String.valueOf(store.getRegistros().size()));
        cardZonaTop.setValor(store.zonaConMasVentas());
    }

    private JPanel buildFormPanel() {
        JPanel card = AppConstants.crearTarjeta("Registrar venta mensual");

        JPanel campos = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        campos.setOpaque(false);

        cbZona = new JComboBox<>(store.getZonas().toArray(new String[0]));
        cbZona.setEditable(true);
        cbZona.setToolTipText("Elija una zona existente o escriba el nombre de una nueva "
                + "(por ejemplo, un recinto o sector cercano a un río).");
        ((JTextField) cbZona.getEditor().getEditorComponent())
                .setToolTipText(cbZona.getToolTipText());
        AppConstants.estilizarCampo(cbZona);

        cbMes = new JComboBox<>(AppConstants.MESES);
        AppConstants.estilizarCampo(cbMes);

        spAnio = new JSpinner(new SpinnerNumberModel(2026, 2000, 2100, 1));
        spAnio.setEditor(new JSpinner.NumberEditor(spAnio, "#"));
        AppConstants.estilizarCampo(spAnio);

        tfMonto = new JTextField(10);
        AppConstants.estilizarCampo(tfMonto);

        JLabel lZona = new JLabel("Zona:");
        JLabel lMes = new JLabel("Mes:");
        JLabel lAnio = new JLabel("Año:");
        JLabel lMonto = new JLabel("Monto ($):");
        for (JLabel l : new JLabel[]{lZona, lMes, lAnio, lMonto}) {
            l.setFont(new Font("SansSerif", Font.BOLD, 12));
            l.setForeground(AppConstants.SECONDARY);
        }

        campos.add(lZona);
        campos.add(cbZona);
        campos.add(lMes);
        campos.add(cbMes);
        campos.add(lAnio);
        campos.add(spAnio);
        campos.add(lMonto);
        campos.add(tfMonto);

        RoundedButton btnAgregar = new RoundedButton("➕  Agregar", AppConstants.PRIMARY, Color.WHITE);
        btnAgregar.addActionListener(e -> agregarRegistro());
        campos.add(btnAgregar);

        card.add(campos, BorderLayout.CENTER);

        JLabel hint = new JLabel("Puede escribir el nombre de su propia zona o sector (ej. cercano a un río).");
        hint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        hint.setForeground(AppConstants.GRAY_TEXT);
        card.add(hint, BorderLayout.SOUTH);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildBottomPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        RoundedButton btnEliminar = new RoundedButton("🗑  Eliminar seleccionado", AppConstants.ACCENT, Color.WHITE);
        btnEliminar.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) store.removeAt(row);
        });

        RoundedButton btnCargarExcel = new RoundedButton("📂  Cargar Excel (.xlsx)", AppConstants.PRIMARY, Color.WHITE);
        btnCargarExcel.addActionListener(e -> cargarExcel());

        RoundedButton btnGuardarExcel = new RoundedButton("💾  Guardar como Excel (.xlsx)", AppConstants.PRIMARY, Color.WHITE);
        btnGuardarExcel.addActionListener(e -> guardarExcel());

        RoundedButton btnLimpiar = new RoundedButton("🧹  Limpiar todo", AppConstants.SECONDARY, Color.WHITE);
        btnLimpiar.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this, "¿Eliminar todos los registros?",
                    "Confirmar", JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) store.clear();
        });

        p.add(btnEliminar);
        p.add(btnCargarExcel);
        p.add(btnGuardarExcel);
        p.add(btnLimpiar);
        return p;
    }

    private void agregarRegistro() {
        String zona = String.valueOf(cbZona.getEditor().getItem()).trim();
        if (zona.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Escriba o seleccione el nombre de la zona.",
                    "Dato inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            int mes = cbMes.getSelectedIndex() + 1;
            int anio = (Integer) spAnio.getValue();
            String montoTxt = tfMonto.getText().trim().replace(",", ".");
            if (montoTxt.isEmpty()) throw new NumberFormatException();
            double monto = Double.parseDouble(montoTxt);
            if (monto < 0) throw new NumberFormatException();
            store.add(new SalesRecord(zona, anio, mes, monto));
            tfMonto.setText("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingrese un monto válido (número positivo).",
                    "Dato inválido", JOptionPane.WARNING_MESSAGE);
        }
    }

    // Carga registros desde un archivo Excel (.xlsx) real, incluyendo archivos que hayan sido
    // editados y vueltos a guardar desde Microsoft Excel o LibreOffice Calc.
    private void cargarExcel() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Seleccionar archivo Excel (columnas: Zona, Anio, Mes, Monto)");
        fc.setFileFilter(new FileNameExtensionFilter("Archivos de Excel (*.xlsx)", "xlsx"));
        int res = fc.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        File f = fc.getSelectedFile();
        try {
            List<SalesRecord> nuevos = XlsxUtil.leer(f);
            int cargados = nuevos.size();
            store.addAll(nuevos);
            JOptionPane.showMessageDialog(this,
                    "Registros cargados desde Excel: " + cargados,
                    "Carga completada", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo leer el archivo Excel: " + ex.getMessage()
                            + "\nVerifique que sea un archivo .xlsx válido con las columnas Zona, Anio, Mes y Monto.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Guarda todos los registros en un archivo .xlsx real (formato nativo de Excel), listo
    // para abrirse, editarse y volver a cargarse sin perder el formato.
    private void guardarExcel() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Guardar ventas como Excel (.xlsx)");
        fc.setFileFilter(new FileNameExtensionFilter("Archivos de Excel (*.xlsx)", "xlsx"));
        fc.setSelectedFile(new File("ventas_babahoyo.xlsx"));
        int res = fc.showSaveDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        File f = fc.getSelectedFile();
        if (!f.getName().toLowerCase(Locale.US).endsWith(".xlsx")) {
            f = new File(f.getParentFile(), f.getName() + ".xlsx");
        }
        try {
            XlsxUtil.escribir(f, store.getRegistros());
            JOptionPane.showMessageDialog(this,
                    "Archivo Excel guardado correctamente:\n" + f.getAbsolutePath()
                            + "\n\nPuede abrirlo y editarlo en Excel; luego use 'Cargar Excel' para traer los cambios.",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar el archivo Excel: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

// Renderer que alterna el color de fondo de las filas para mejorar la lectura de la tabla y
// resalta ligeramente la fila sobre la que pasa el cursor (efecto "hover" moderno).
// Lee los colores de AppConstants en cada pintado, por lo que responde al tema activo.
class AlternatingRowRenderer extends DefaultTableCellRenderer {
    private final IntSupplier filaResaltada;

    AlternatingRowRenderer(IntSupplier filaResaltada) {
        this.filaResaltada = filaResaltada;
    }

    @Override
    public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                              boolean hasFocus, int row, int column) {
        java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (!isSelected) {
            if (filaResaltada != null && filaResaltada.getAsInt() == row) {
                c.setBackground(AppConstants.ROW_HOVER_BG);
            } else {
                c.setBackground(row % 2 == 0 ? AppConstants.PANEL_BG : AppConstants.ROW_ALT_BG);
            }
            c.setForeground(AppConstants.GRAY_TEXT);
        }
        setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        return c;
    }
}

class RegistrosTableModel extends AbstractTableModel {
    private final DataStore store;
    private final String[] columnas = {"Zona", "Año", "Mes", "Monto ($)"};

    RegistrosTableModel(DataStore store) {
        this.store = store;
    }

    @Override
    public int getRowCount() {
        return store.getRegistros().size();
    }

    @Override
    public int getColumnCount() {
        return columnas.length;
    }

    @Override
    public String getColumnName(int col) {
        return columnas[col];
    }

    @Override
    public Object getValueAt(int row, int col) {
        SalesRecord r = store.getRegistros().get(row);
        switch (col) {
            case 0: return r.zona;
            case 1: return r.anio;
            case 2: return AppConstants.MESES[r.mes - 1];
            case 3: return String.format(Locale.US, "%.2f", r.monto);
            default: return "";
        }
    }
}

/* =====================  PESTAÑA: TENDENCIAS  ===================== */

class PanelTendencias extends JPanel {
    private final DataStore store;
    private JComboBox<String> cbZona;
    private ChartLineaPanel chartPanel;
    private JLabel lblResumen;

    PanelTendencias(DataStore store) {
        this.store = store;
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setBackground(AppConstants.BG);

        JPanel card = AppConstants.crearTarjeta("Tendencia de ventas mensuales");
        JPanel campos = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        campos.setOpaque(false);
        cbZona = new JComboBox<>();
        AppConstants.estilizarCampo(cbZona);
        AppConstants.refrescarZonas(cbZona, store, true);
        cbZona.addActionListener(e -> actualizar());
        JLabel lZona = new JLabel("Zona:");
        lZona.setFont(new Font("SansSerif", Font.BOLD, 12));
        lZona.setForeground(AppConstants.SECONDARY);
        campos.add(lZona);
        campos.add(cbZona);
        card.add(campos, BorderLayout.CENTER);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        wrapper.add(card, BorderLayout.CENTER);
        add(wrapper, BorderLayout.NORTH);

        chartPanel = new ChartLineaPanel();
        CardPanel chartCard = new CardPanel();
        chartCard.setLayout(new BorderLayout());
        chartCard.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        chartCard.add(chartPanel, BorderLayout.CENTER);
        add(chartCard, BorderLayout.CENTER);

        lblResumen = new JLabel(" ");
        lblResumen.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblResumen.setForeground(AppConstants.GRAY_TEXT);
        lblResumen.setBorder(BorderFactory.createEmptyBorder(10, 4, 0, 0));
        add(lblResumen, BorderLayout.SOUTH);

        store.addListener(() -> {
            AppConstants.refrescarZonas(cbZona, store, true);
            actualizar();
        });
        actualizar();
    }

    private void actualizar() {
        String zona = (String) cbZona.getSelectedItem();
        Map<String, Double> datos = store.totalesPorPeriodo(zona);
        chartPanel.setDatos(datos);
        if (datos.isEmpty()) {
            lblResumen.setText("Sin datos para mostrar. Registre ventas en la pestaña 'Carga de Ventas'.");
            return;
        }
        double total = 0, max = Double.NEGATIVE_INFINITY, min = Double.POSITIVE_INFINITY;
        String periodoMax = "", periodoMin = "";
        for (Map.Entry<String, Double> e : datos.entrySet()) {
            total += e.getValue();
            if (e.getValue() > max) { max = e.getValue(); periodoMax = e.getKey(); }
            if (e.getValue() < min) { min = e.getValue(); periodoMin = e.getKey(); }
        }
        double promedio = total / datos.size();
        lblResumen.setText(String.format(Locale.US,
                "Promedio mensual: $%.2f   |   Mes más alto: %s ($%.2f)   |   Mes más bajo: %s ($%.2f)",
                promedio, periodoMax, max, periodoMin, min));
    }
}

class ChartLineaPanel extends JPanel {
    private Map<String, Double> datos = new TreeMap<>();

    ChartLineaPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(600, 350));
    }

    void setDatos(Map<String, Double> datos) {
        this.datos = datos;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        g2.setColor(AppConstants.PANEL_BG);
        g2.fillRect(0, 0, w, h);

        int marginLeft = 70, marginRight = 30, marginTop = 20, marginBottom = 50;
        int chartW = w - marginLeft - marginRight;
        int chartH = h - marginTop - marginBottom;

        if (datos.isEmpty()) {
            g2.setColor(AppConstants.GRAY_TEXT);
            g2.drawString("No hay datos disponibles", w / 2 - 60, h / 2);
            return;
        }

        double max = Collections.max(datos.values());
        double min = 0;
        if (max <= 0) max = 1;

        for (int i = 0; i <= 4; i++) {
            int y = marginTop + chartH - (int) ((double) i / 4 * chartH);
            g2.setColor(AppConstants.GRID);
            g2.drawLine(marginLeft, y, marginLeft + chartW, y);
            double valor = min + (max - min) * i / 4.0;
            g2.setColor(AppConstants.GRAY_TEXT);
            g2.drawString(String.format(Locale.US, "%.0f", valor), 8, y + 4);
        }

        g2.setColor(AppConstants.SECONDARY);
        g2.drawLine(marginLeft, marginTop, marginLeft, marginTop + chartH);
        g2.drawLine(marginLeft, marginTop + chartH, marginLeft + chartW, marginTop + chartH);

        List<String> claves = new ArrayList<>(datos.keySet());
        int n = claves.size();
        int[] xs = new int[n];
        int[] ys = new int[n];
        for (int i = 0; i < n; i++) {
            double valor = datos.get(claves.get(i));
            int x = marginLeft + (n == 1 ? chartW / 2 : (int) ((double) i / (n - 1) * chartW));
            int y = marginTop + chartH - (int) ((valor - min) / (max - min) * chartH);
            xs[i] = x;
            ys[i] = y;
        }

        g2.setColor(AppConstants.PRIMARY);
        g2.setStroke(new BasicStroke(2.5f));
        for (int i = 0; i < n - 1; i++) g2.drawLine(xs[i], ys[i], xs[i + 1], ys[i + 1]);

        g2.setColor(AppConstants.ACCENT);
        for (int i = 0; i < n; i++) g2.fillOval(xs[i] - 4, ys[i] - 4, 8, 8);

        g2.setColor(AppConstants.GRAY_TEXT);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        FontMetrics fm = g2.getFontMetrics();
        for (int i = 0; i < n; i++) {
            if (n > 12 && i % 2 != 0) continue;
            String etiqueta = claves.get(i);
            g2.drawString(etiqueta, xs[i] - fm.stringWidth(etiqueta) / 2, marginTop + chartH + 18);
        }
    }
}

/* =====================  PESTAÑA: PRONÓSTICOS  ===================== */

class PanelPronosticos extends JPanel {
    private final DataStore store;
    private JComboBox<String> cbZona;
    private JLabel lblResultado;
    private ChartLineaPronosticoPanel chartPanel;

    PanelPronosticos(DataStore store) {
        this.store = store;
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setBackground(AppConstants.BG);

        JPanel card = AppConstants.crearTarjeta("Pronóstico simple (regresión lineal)");
        JPanel campos = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        campos.setOpaque(false);
        cbZona = new JComboBox<>();
        AppConstants.estilizarCampo(cbZona);
        AppConstants.refrescarZonas(cbZona, store, true);
        cbZona.addActionListener(e -> calcular());

        RoundedButton btn = new RoundedButton("🔮  Calcular pronóstico", AppConstants.PRIMARY, Color.WHITE);
        btn.addActionListener(e -> calcular());

        JLabel lZona = new JLabel("Zona:");
        lZona.setFont(new Font("SansSerif", Font.BOLD, 12));
        lZona.setForeground(AppConstants.SECONDARY);

        campos.add(lZona);
        campos.add(cbZona);
        campos.add(btn);
        card.add(campos, BorderLayout.CENTER);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        wrapper.add(card, BorderLayout.CENTER);
        add(wrapper, BorderLayout.NORTH);

        chartPanel = new ChartLineaPronosticoPanel();
        CardPanel chartCard = new CardPanel();
        chartCard.setLayout(new BorderLayout());
        chartCard.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        chartCard.add(chartPanel, BorderLayout.CENTER);
        add(chartCard, BorderLayout.CENTER);

        lblResultado = new JLabel(" ");
        lblResultado.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblResultado.setForeground(AppConstants.GRAY_TEXT);
        lblResultado.setBorder(BorderFactory.createEmptyBorder(10, 4, 4, 4));
        add(lblResultado, BorderLayout.SOUTH);

        store.addListener(() -> {
            AppConstants.refrescarZonas(cbZona, store, true);
            calcular();
        });
        calcular();
    }

    private void calcular() {
        String zona = (String) cbZona.getSelectedItem();
        Map<String, Double> datos = store.totalesPorPeriodo(zona);
        if (datos.size() < 2) {
            lblResultado.setText("Se necesitan al menos 2 meses con datos para generar un pronóstico.");
            chartPanel.setDatos(datos, -1);
            return;
        }
        List<Double> valores = new ArrayList<>(datos.values());
        int n = valores.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < n; i++) {
            double x = i + 1;
            double y = valores.get(i);
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }
        double denom = (n * sumX2 - sumX * sumX);
        double pendiente = denom == 0 ? 0 : (n * sumXY - sumX * sumY) / denom;
        double intercepto = (sumY - pendiente * sumX) / n;
        double pronostico = pendiente * (n + 1) + intercepto;
        if (pronostico < 0) pronostico = 0;

        String tendencia = pendiente > 1 ? "creciente" : (pendiente < -1 ? "decreciente" : "estable");
        lblResultado.setText(String.format(Locale.US,
                "Tendencia %s (variación aprox. $%.2f por mes). Pronóstico para el próximo mes: $%.2f",
                tendencia, pendiente, pronostico));

        chartPanel.setDatos(datos, pronostico);
    }
}

class ChartLineaPronosticoPanel extends JPanel {
    private Map<String, Double> datos = new TreeMap<>();
    private double pronostico = -1;

    ChartLineaPronosticoPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(600, 320));
    }

    void setDatos(Map<String, Double> datos, double pronostico) {
        this.datos = datos;
        this.pronostico = pronostico;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        g2.setColor(AppConstants.PANEL_BG);
        g2.fillRect(0, 0, w, h);

        int marginLeft = 70, marginRight = 30, marginTop = 20, marginBottom = 50;
        int chartW = w - marginLeft - marginRight;
        int chartH = h - marginTop - marginBottom;

        if (datos.isEmpty()) {
            g2.setColor(AppConstants.GRAY_TEXT);
            g2.drawString("No hay datos disponibles", w / 2 - 60, h / 2);
            return;
        }

        List<String> claves = new ArrayList<>(datos.keySet());
        List<Double> valores = new ArrayList<>(datos.values());
        int n = valores.size();
        boolean hayPronostico = pronostico >= 0;
        int totalPuntos = hayPronostico ? n + 1 : n;

        double max = Collections.max(valores);
        if (hayPronostico) max = Math.max(max, pronostico);
        double min = 0;
        if (max <= 0) max = 1;

        for (int i = 0; i <= 4; i++) {
            int y = marginTop + chartH - (int) ((double) i / 4 * chartH);
            g2.setColor(AppConstants.GRID);
            g2.drawLine(marginLeft, y, marginLeft + chartW, y);
            double valor = min + (max - min) * i / 4.0;
            g2.setColor(AppConstants.GRAY_TEXT);
            g2.drawString(String.format(Locale.US, "%.0f", valor), 8, y + 4);
        }
        g2.setColor(AppConstants.SECONDARY);
        g2.drawLine(marginLeft, marginTop, marginLeft, marginTop + chartH);
        g2.drawLine(marginLeft, marginTop + chartH, marginLeft + chartW, marginTop + chartH);

        int[] xs = new int[totalPuntos];
        int[] ys = new int[totalPuntos];
        int denomPuntos = Math.max(1, totalPuntos - 1);
        for (int i = 0; i < n; i++) {
            int x = marginLeft + (int) ((double) i / denomPuntos * chartW);
            int y = marginTop + chartH - (int) ((valores.get(i) - min) / (max - min) * chartH);
            xs[i] = x;
            ys[i] = y;
        }
        if (hayPronostico) {
            int x = marginLeft + (int) ((double) n / denomPuntos * chartW);
            int y = marginTop + chartH - (int) ((pronostico - min) / (max - min) * chartH);
            xs[n] = x;
            ys[n] = y;
        }

        g2.setColor(AppConstants.PRIMARY);
        g2.setStroke(new BasicStroke(2.5f));
        for (int i = 0; i < n - 1; i++) g2.drawLine(xs[i], ys[i], xs[i + 1], ys[i + 1]);
        for (int i = 0; i < n; i++) g2.fillOval(xs[i] - 4, ys[i] - 4, 8, 8);

        if (hayPronostico) {
            g2.setColor(AppConstants.ACCENT);
            float[] dash = {6f, 4f};
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dash, 0f));
            g2.drawLine(xs[n - 1], ys[n - 1], xs[n], ys[n]);
            g2.fillOval(xs[n] - 5, ys[n] - 5, 10, 10);
        }

        g2.setColor(AppConstants.GRAY_TEXT);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        FontMetrics fm = g2.getFontMetrics();
        for (int i = 0; i < n; i++) {
            if (n > 10 && i % 2 != 0) continue;
            String etiqueta = claves.get(i);
            g2.drawString(etiqueta, xs[i] - fm.stringWidth(etiqueta) / 2, marginTop + chartH + 18);
        }
        if (hayPronostico) {
            g2.setColor(AppConstants.ACCENT);
            String etiqueta = "Pronóstico";
            g2.drawString(etiqueta, xs[n] - fm.stringWidth(etiqueta) / 2, marginTop + chartH + 18);
        }
    }
}

/* =====================  PESTAÑA: GRÁFICOS ESTADÍSTICOS  ===================== */

class PanelGraficos extends JPanel {
    private final DataStore store;
    private ChartBarraPanel barraPanel;
    private ChartPastelPanel pastelPanel;
    private JPanel cards;
    private CardLayout cardLayout;

    PanelGraficos(DataStore store) {
        this.store = store;
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setBackground(AppConstants.BG);

        JPanel card = AppConstants.crearTarjeta("Gráficos estadísticos por zona");
        JPanel campos = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        campos.setOpaque(false);
        JComboBox<String> cbTipo = new JComboBox<>(new String[]{
                "Barras (total por zona)", "Circular (participación %)"
        });
        AppConstants.estilizarCampo(cbTipo);
        cbTipo.addActionListener(e ->
                cardLayout.show(cards, cbTipo.getSelectedIndex() == 0 ? "barras" : "pastel"));
        JLabel lTipo = new JLabel("Tipo:");
        lTipo.setFont(new Font("SansSerif", Font.BOLD, 12));
        lTipo.setForeground(AppConstants.SECONDARY);
        campos.add(lTipo);
        campos.add(cbTipo);
        card.add(campos, BorderLayout.CENTER);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        wrapper.add(card, BorderLayout.CENTER);
        add(wrapper, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);
        cards.setOpaque(false);
        barraPanel = new ChartBarraPanel();
        pastelPanel = new ChartPastelPanel();
        cards.add(barraPanel, "barras");
        cards.add(pastelPanel, "pastel");

        CardPanel chartCard = new CardPanel();
        chartCard.setLayout(new BorderLayout());
        chartCard.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        chartCard.add(cards, BorderLayout.CENTER);
        add(chartCard, BorderLayout.CENTER);

        store.addListener(this::actualizar);
        actualizar();
    }

    private void actualizar() {
        Map<String, Double> totales = store.totalesPorZona();
        barraPanel.setDatos(totales);
        pastelPanel.setDatos(totales);
    }
}

class ChartBarraPanel extends JPanel {
    private Map<String, Double> datos = new LinkedHashMap<>();

    ChartBarraPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(600, 350));
    }

    void setDatos(Map<String, Double> d) {
        this.datos = d;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        g2.setColor(AppConstants.PANEL_BG);
        g2.fillRect(0, 0, w, h);

        int marginLeft = 80, marginRight = 30, marginTop = 20, marginBottom = 70;
        int chartW = w - marginLeft - marginRight;
        int chartH = h - marginTop - marginBottom;

        if (datos.isEmpty() || Collections.max(datos.values()) <= 0) {
            g2.setColor(AppConstants.GRAY_TEXT);
            g2.drawString("No hay datos disponibles", w / 2 - 60, h / 2);
            return;
        }
        double max = Collections.max(datos.values());

        for (int i = 0; i <= 4; i++) {
            int y = marginTop + chartH - (int) ((double) i / 4 * chartH);
            g2.setColor(AppConstants.GRID);
            g2.drawLine(marginLeft, y, marginLeft + chartW, y);
            double valor = max * i / 4.0;
            g2.setColor(AppConstants.GRAY_TEXT);
            g2.drawString(String.format(Locale.US, "%.0f", valor), 8, y + 4);
        }
        g2.setColor(AppConstants.SECONDARY);
        g2.drawLine(marginLeft, marginTop, marginLeft, marginTop + chartH);
        g2.drawLine(marginLeft, marginTop + chartH, marginLeft + chartW, marginTop + chartH);

        List<String> zonas = new ArrayList<>(datos.keySet());
        int n = zonas.size();
        int slot = chartW / n;
        int barW = Math.max(20, slot - 20);
        int i = 0;
        for (String z : zonas) {
            double val = datos.get(z);
            int barH = (int) ((val / max) * chartH);
            int x = marginLeft + i * slot + (slot - barW) / 2;
            int y = marginTop + chartH - barH;
            g2.setColor(AppConstants.PALETA_GRAFICOS[i % AppConstants.PALETA_GRAFICOS.length]);
            g2.fillRoundRect(x, y, barW, barH, 8, 8);

            g2.setColor(AppConstants.GRAY_TEXT);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            String valStr = String.format(Locale.US, "$%.0f", val);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(valStr, x + barW / 2 - fm.stringWidth(valStr) / 2, y - 4);

            String etiqueta = z.length() > 10 ? z.substring(0, 9) + "." : z;
            g2.drawString(etiqueta, x + barW / 2 - fm.stringWidth(etiqueta) / 2, marginTop + chartH + 16);
            i++;
        }
    }
}

class ChartPastelPanel extends JPanel {
    private Map<String, Double> datos = new LinkedHashMap<>();

    ChartPastelPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(600, 350));
    }

    void setDatos(Map<String, Double> d) {
        this.datos = d;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        g2.setColor(AppConstants.PANEL_BG);
        g2.fillRect(0, 0, w, h);

        double total = 0;
        for (double v : datos.values()) total += v;

        if (total <= 0) {
            g2.setColor(AppConstants.GRAY_TEXT);
            g2.drawString("No hay datos disponibles", w / 2 - 60, h / 2);
            return;
        }

        int diametro = Math.min(w, h) - 140;
        if (diametro < 60) diametro = 60;
        int cx = 60, cy = (h - diametro) / 2;
        double anguloInicio = 0;
        int i = 0;
        int legendY = cy;
        for (Map.Entry<String, Double> e : datos.entrySet()) {
            double porcentaje = e.getValue() / total;
            double angulo = porcentaje * 360.0;
            g2.setColor(AppConstants.PALETA_GRAFICOS[i % AppConstants.PALETA_GRAFICOS.length]);
            g2.fillArc(cx, cy, diametro, diametro, (int) anguloInicio, (int) Math.round(angulo) + 1);
            anguloInicio += angulo;

            g2.setColor(AppConstants.PALETA_GRAFICOS[i % AppConstants.PALETA_GRAFICOS.length]);
            g2.fillRoundRect(cx + diametro + 30, legendY, 12, 12, 4, 4);
            g2.setColor(AppConstants.GRAY_TEXT);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2.drawString(String.format(Locale.US, "%s: %.1f%%", e.getKey(), porcentaje * 100),
                    cx + diametro + 48, legendY + 11);
            legendY += 22;
            i++;
        }
        g2.setColor(AppConstants.PANEL_BG);
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(cx, cy, diametro, diametro);
    }
}

/* =====================  PESTAÑA: CARTOGRAMA  ===================== */

class PanelCartograma extends JPanel {
    private final DataStore store;
    private CartogramaPanel mapa;

    PanelCartograma(DataStore store) {
        this.store = store;
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setBackground(AppConstants.BG);

        JLabel titulo = new JLabel("Cartograma esquemático — Zonas cercanas a Babahoyo, Los Ríos", SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 15));
        titulo.setForeground(AppConstants.PRIMARY_DARK);
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        add(titulo, BorderLayout.NORTH);

        mapa = new CartogramaPanel();
        CardPanel mapaCard = new CardPanel();
        mapaCard.setLayout(new BorderLayout());
        mapaCard.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        mapaCard.add(mapa, BorderLayout.CENTER);
        add(mapaCard, BorderLayout.CENTER);

        JLabel nota = new JLabel(
                "<html><center>El tamaño y el color de cada círculo representan el total de ventas de cada zona. "
                        + "Distribución esquemática, no georreferenciada; las zonas que usted escriba se ubican "
                        + "automáticamente alrededor del mapa.</center></html>", SwingConstants.CENTER);
        nota.setFont(new Font("SansSerif", Font.ITALIC, 11));
        nota.setForeground(AppConstants.GRAY_TEXT);
        nota.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        add(nota, BorderLayout.SOUTH);

        store.addListener(this::actualizar);
        actualizar();
    }

    private void actualizar() {
        mapa.setDatos(store.totalesPorZona());
    }
}

class CartogramaPanel extends JPanel {
    private Map<String, Double> datos = new LinkedHashMap<>();
    // Posiciones conocidas (predefinidas) + las que se generan automáticamente para zonas nuevas.
    private final Map<String, double[]> posiciones = new LinkedHashMap<>(AppConstants.POSICIONES);
    private int contadorExtra = 0;

    CartogramaPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(600, 420));
    }

    void setDatos(Map<String, Double> d) {
        this.datos = d;
        // A cualquier zona nueva (escrita por el usuario) se le asigna una posición
        // esquemática automática, distribuida alrededor del centro del mapa.
        for (String zona : d.keySet()) {
            if (!posiciones.containsKey(zona)) {
                double anguloDeg = (contadorExtra * 137.508) % 360; // ángulo áureo: buena distribución
                double radio = 0.38;
                double x = 0.5 + radio * Math.cos(Math.toRadians(anguloDeg));
                double y = 0.5 + radio * Math.sin(Math.toRadians(anguloDeg));
                x = Math.max(0.08, Math.min(0.92, x));
                y = Math.max(0.10, Math.min(0.90, y));
                posiciones.put(zona, new double[]{x, y});
                contadorExtra++;
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        g2.setColor(AppConstants.MAP_BG);
        g2.fillRect(0, 0, w, h);

        int pad = 60;

        g2.setColor(AppConstants.RIO_COLOR);
        g2.setStroke(new BasicStroke(18f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        GeneralPath rio = new GeneralPath();
        rio.moveTo(pad * 0.4, h * 0.15);
        rio.curveTo(w * 0.3, h * 0.35, w * 0.6, h * 0.55, w * 0.75, h * 0.9);
        g2.draw(rio);
        g2.setColor(AppConstants.RIO_LABEL);
        g2.setFont(new Font("SansSerif", Font.ITALIC, 11));
        g2.drawString("Río Babahoyo (referencial)", (int) (w * 0.55), (int) (h * 0.5));

        if (datos.isEmpty()) return;

        double max = Collections.max(datos.values());
        double min = Collections.min(datos.values());
        if (max <= 0) max = 1;

        double radioMax = Math.min(w, h) * 0.16;
        double radioMin = Math.min(w, h) * 0.07;

        for (Map.Entry<String, double[]> pos : posiciones.entrySet()) {
            String zona = pos.getKey();
            if (!datos.containsKey(zona)) continue;
            double[] p = pos.getValue();
            double valor = datos.getOrDefault(zona, 0.0);
            double factor = (max == min) ? 0.5 : (valor - min) / (max - min);
            double radio = radioMin + factor * (radioMax - radioMin);

            int cx = (int) (pad + p[0] * (w - 2 * pad));
            int cy = (int) (pad + p[1] * (h - 2 * pad));

            Color color = interpolarColor(AppConstants.PRIMARY, AppConstants.ACCENT, factor);
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 210));
            g2.fillOval((int) (cx - radio), (int) (cy - radio), (int) (radio * 2), (int) (radio * 2));
            g2.setColor(AppConstants.PANEL_BG);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval((int) (cx - radio), (int) (cy - radio), (int) (radio * 2), (int) (radio * 2));

            g2.setColor(AppConstants.PRIMARY_DARK);
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(zona, cx - fm.stringWidth(zona) / 2, (int) (cy - radio - 8));

            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            String valTxt = String.format(Locale.US, "$%.0f", valor);
            fm = g2.getFontMetrics();
            g2.setColor(AppConstants.PANEL_BG);
            g2.drawString(valTxt, cx - fm.stringWidth(valTxt) / 2, cy + 4);
        }
    }

    private Color interpolarColor(Color c1, Color c2, double t) {
        int r = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * t);
        int gg = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * t);
        int b = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * t);
        return new Color(r, gg, b);
    }
}