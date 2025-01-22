package igu;

import logica.Controladora;
import logica.Joya;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class GroupBy {
    private JCheckBox todasLasJoyasCheckBox;
    private JComboBox<String> comboBoxCategoria;
    private JButton btnContar;
    private JList<Joya> listaJoyas; // Lista de Joya en lugar de String
    private JPanel mainPanel;
    private JButton btnVerificar;



    private Controladora controladora; // Instancia de la capa lógica
    private DefaultListModel<Joya> listModel; // Modelo de datos para la lista
    private boolean enVerificacion = false; // Para controlar si estamos en modo verificación
    private JDialog dialogVerificacion; // Referencia al cuadro de diálogo de verificación

    public GroupBy(JFrame parent) {
        // Crear la pantalla de carga
        LoadingScreen loadingScreen = new LoadingScreen(parent);

        new Thread(() -> {
            loadingScreen.show(); // Mostrar la animación de carga
            try {
                // Inicializar el modelo de lista y otros componentes
                controladora = new Controladora();
                listModel = new DefaultListModel<>();
                listaJoyas.setModel(listModel);


                // Configurar renderizado para la lista
                listaJoyas.setCellRenderer(new JoyaListCellRenderer());

                // Cargar todas las joyas inicialmente
                //cargarTodasLasJoyas();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                loadingScreen.hide(); // Ocultar la animación de carga
            }
        }).start();




        // Acción para "Todas las Joyas"
        todasLasJoyasCheckBox.addActionListener(e -> {
            if (!enVerificacion && todasLasJoyasCheckBox.isSelected()) {
                cargarTodasLasJoyas();
                //checkBoxCategoria.setSelected(false);
                comboBoxCategoria.setEnabled(false);
            }
        });

        // Acción para "Categoría"
        comboBoxCategoria.addActionListener(e -> {
            if (!enVerificacion) {
                cargarJoyasPorCategoria((String) comboBoxCategoria.getSelectedItem());
                todasLasJoyasCheckBox.setSelected(false);
            }
        });

        // Acción para el botón "Contar"
        btnContar.addActionListener(e -> {
            int cantidad = listModel.getSize();
            JOptionPane.showMessageDialog(mainPanel, "Total de joyas: " + cantidad, "Conteo", JOptionPane.INFORMATION_MESSAGE, new ImageIcon(new ImageIcon(getClass().getResource("/inversor.png")).getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH)));
        });

        // Acción para el botón "Verificar"
        btnVerificar.addActionListener(e -> {
            if (!enVerificacion) {
                bloquearOpciones();
                iniciarVerificacion();
            }else{
                dialogVerificacion.setVisible(true);
            }
        });
        listaJoyas.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int index = listaJoyas.locationToIndex(e.getPoint());
                    if (index != -1) {
                        Joya joyaSeleccionada = listModel.getElementAt(index);
                        mostrarDetallesJoya2(joyaSeleccionada);
                    }
                }
            }
        });

    }

    private void cargarTodasLasJoyas() {
        List<String> estado = Arrays.asList("disponible", "prestado");

        List<Joya> joyasNoVendidas = controladora.filtrarJoyas(
                false,
                null,                // No filtrar por ID
                false,
                null,                // No filtrar por categoría
                false,
                null,                // No filtrar por nombre
                true,
                estado);
        actualizarLista(joyasNoVendidas);
    }

    private void cargarJoyasPorCategoria(String categoriaSeleccionada) {
        // Filtrar joyas por categoría y que no estén vendidas
        List<String> estado = Arrays.asList("disponible", "prestado");


        List<Joya> joyasPorCategoriaNoVendidas = controladora.filtrarJoyas(
                false, null,                // No filtrar por ID
                true, categoriaSeleccionada, // Filtrar por categoría
                false, null,                // No filtrar por nombre
                true,
                estado

        );
        actualizarLista(joyasPorCategoriaNoVendidas);
    }

    private void actualizarLista(List<Joya> joyas) {
        listModel.clear();
        for (Joya joya : joyas) {
            listModel.addElement(joya);
        }
    }
    private void mostrarDetallesJoya2(Joya joya) {
        JDialog detallesDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(mainPanel), "Detalles de la Joya", true);
        detallesDialog.setSize(650, 900);
        detallesDialog.setLocationRelativeTo(mainPanel);
        DetallesJoya detallesJoya = new DetallesJoya(joya, null);
        detallesDialog.add(detallesJoya.getMainPanel());
        detallesDialog.setVisible(true);
    }



    private void verificarYEliminarJoya(String id) {
        boolean encontrada = false;

        // Iterar sobre el modelo de la lista
        for (int i = 0; i < listModel.getSize(); i++) {
            Joya joya = listModel.getElementAt(i);

            // Verificar si el ID coincide
            if (joya.getId().toString().equals(id)) {
                encontrada = true;

                // Mostrar detalles completos de la joya
                mostrarDetallesJoya(joya);

                // Eliminar la joya de la lista
                listModel.remove(i);
                break; // Salir del bucle después de encontrar y procesar
            }
        }

        if (!encontrada) {
            JOptionPane.showMessageDialog(mainPanel, "La joya con ID " + id + " no se encuentra en la lista.", "No Encontrada", JOptionPane.ERROR_MESSAGE,new ImageIcon(new ImageIcon(getClass().getResource("/llora.png")).getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH)));
        }

        // Comprobar si la lista está vacía
        if (listModel.getSize() == 0) {
            JOptionPane.showMessageDialog(mainPanel, "Todas las joyas han sido verificadas y la lista está vacía.", "Lista Completa", JOptionPane.INFORMATION_MESSAGE, new ImageIcon(new ImageIcon(getClass().getResource("/nino.png")).getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH)));
        }
    }

    private void iniciarVerificacionn() {
        enVerificacion = true;

        if (dialogVerificacion == null) {
            // Crear el cuadro de diálogo si no existe
            dialogVerificacion = new JDialog((JFrame) SwingUtilities.getWindowAncestor(mainPanel), "Verificación de Joyas", true);
            dialogVerificacion.setSize(400, 200);
            dialogVerificacion.setLocationRelativeTo(mainPanel);

            JPanel dialogPanel = new JPanel();
            dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));

            JLabel lblInfo = new JLabel("Ingrese el ID de la joya y presione Enter:");
            JTextField txtInputId = new JTextField();

            dialogPanel.add(lblInfo);
            dialogPanel.add(txtInputId);

            dialogVerificacion.add(dialogPanel);

            // Listener para verificar el ID al presionar Enter
            txtInputId.addActionListener(e -> {
                String inputId = txtInputId.getText().trim();
                if (!inputId.isEmpty()) {
                    verificarYEliminarJoya(inputId);
                    txtInputId.setText(""); // Limpiar el campo para el siguiente ID
                }
            });

            // Listener para cerrar el cuadro de diálogo al presionar Escape
            dialogVerificacion.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke("ESCAPE"), "closeDialog");
            dialogVerificacion.getRootPane().getActionMap().put("closeDialog", new AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    cerrarVerificacion(); // Cierra el cuadro sin destruirlo
                }
            });
        }

        dialogVerificacion.setVisible(true); // Reutilizar el cuadro si ya existe
    }

    private void iniciarVerificacion() {
        enVerificacion = true;

        if (dialogVerificacion == null) {
            // Crear el cuadro de diálogo si no existe
            dialogVerificacion = new JDialog((JFrame) SwingUtilities.getWindowAncestor(mainPanel), "Verificación de Joyas", true);
            dialogVerificacion.setSize(600, 400);
            dialogVerificacion.setLocationRelativeTo(mainPanel);

            // Panel principal del diálogo
            JPanel dialogPanel = new JPanel(new BorderLayout());

            // Panel de entrada de ID
            JPanel inputPanel = new JPanel(new BorderLayout());
            JLabel lblInfo = new JLabel("Ingrese el ID de la joya y presione Enter:");
            JTextField txtInputId = new JTextField();
            inputPanel.add(lblInfo, BorderLayout.NORTH);
            inputPanel.add(txtInputId, BorderLayout.CENTER);

            // Panel para mostrar los detalles de la joya
            JPanel detallesPanel = new JPanel();
            detallesPanel.setLayout(new BoxLayout(detallesPanel, BoxLayout.Y_AXIS));
            detallesPanel.setBorder(BorderFactory.createTitledBorder("Detalles de la Joya"));

            // Agregar los paneles al diálogo
            dialogPanel.add(inputPanel, BorderLayout.NORTH);
            dialogPanel.add(new JScrollPane(detallesPanel), BorderLayout.CENTER);

            dialogVerificacion.add(dialogPanel);

            // Listener para verificar el ID al presionar Enter
            txtInputId.addActionListener(e -> {
                String inputId = txtInputId.getText().trim();
                if (!inputId.isEmpty()) {
                    verificarYMostrarJoya(inputId, detallesPanel);
                    txtInputId.setText(""); // Limpiar el campo para el siguiente ID
                }
            });
            // Listener para cerrar el cuadro de diálogo al presionar Escape
            dialogVerificacion.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke("ESCAPE"), "closeDialog");
            dialogVerificacion.getRootPane().getActionMap().put("closeDialog", new AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    cerrarVerificacion(); // Cierra el cuadro sin destruirlo
                }
            });
        }

        dialogVerificacion.setVisible(true); // Reutilizar el cuadro si ya existe
    }




    private void cerrarVerificacion() {
        if (dialogVerificacion != null) {
            dialogVerificacion.setVisible(false); // Ocultar el cuadro sin destruirlo
            enVerificacion = false;
            desbloquearOpciones(); // Desbloquear las opciones si es necesario
        }
    }

    private void mostrarDetallesJoya(Joya joya) {
        // Crear un cuadro de diálogo para mostrar los detalles
        JDialog detallesDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(mainPanel), "Detalles de la Joya", true);
        detallesDialog.setSize(500, 600);
        detallesDialog.setLocationRelativeTo(mainPanel);

        JPanel detallesPanel = new JPanel();
        detallesPanel.setLayout(new BoxLayout(detallesPanel, BoxLayout.Y_AXIS));
        detallesPanel.setBackground(new Color(245, 245, 245)); // Fondo claro

        // Imagen de la joya
        try {
            String imagePath = "/bolsa-de-la-compra.png"; // Ruta simulada de imágenes
            ImageIcon icon = new ImageIcon(getClass().getResource(imagePath));
            Image scaledImage = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            JLabel lblImage = new JLabel(new ImageIcon(scaledImage));
            lblImage.setAlignmentX(Component.CENTER_ALIGNMENT);
            detallesPanel.add(lblImage);
        } catch (Exception e) {
            JLabel lblImageError = new JLabel("Imagen no disponible");
            lblImageError.setAlignmentX(Component.CENTER_ALIGNMENT);
            detallesPanel.add(lblImageError);
        }

        detallesPanel.add(Box.createRigidArea(new Dimension(0, 20))); // Espaciado

        // Crear paneles para cada atributo
        detallesPanel.add(crearTarjetaAtributo("ID", String.valueOf(joya.getId())));
        detallesPanel.add(crearTarjetaAtributo("Nombre", joya.getNombre()));
        detallesPanel.add(crearTarjetaAtributo("Categoría", joya.getCategoria()));
        detallesPanel.add(crearTarjetaAtributo("Peso", joya.getPeso() + " gramos"));
        detallesPanel.add(crearTarjetaAtributo("Precio", "$" + joya.getPrecio()));
        detallesPanel.add(crearTarjetaAtributo("Tiene Piedra", joya.isTienePiedra() ? "Sí 💎" : "No 🪨"));

        if (joya.isTienePiedra()) {
            detallesPanel.add(crearTarjetaAtributo("Información de Piedra", joya.getInfoPiedra()));
        }

        detallesPanel.add(crearTarjetaAtributo("Observación", joya.getObservacion()));
        detallesPanel.add(crearTarjetaAtributo("Fue Editada", joya.isFueEditada() ? "Sí" : "No"));

        // Fecha de Ingreso
        String fechaIngresoTexto = (joya.getFechaIngreso() != null) ? joya.getFechaIngreso().toString() : "No disponible";
        detallesPanel.add(crearTarjetaAtributo("Fecha de Ingreso", fechaIngresoTexto));

        // Fecha de Venta
        String fechaVendidaTexto = (joya.getFechaVendida() != null) ? joya.getFechaVendida().toString() : "No vendida";
        detallesPanel.add(crearTarjetaAtributo("Fecha de Venta", fechaVendidaTexto));

        detallesPanel.add(Box.createRigidArea(new Dimension(0, 20))); // Espaciado final

        // Configurar el cuadro de diálogo
        detallesDialog.add(detallesPanel);

        // Cerrar con la tecla Enter
        detallesDialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ENTER"), "closeDialog");
        detallesDialog.getRootPane().getActionMap().put("closeDialog", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                detallesDialog.dispose();
            }
        });

        detallesDialog.setVisible(true);
    }

    private JPanel crearTarjetaAtributoo(String titulo, String valor) {
        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BorderLayout());
        tarjeta.setBackground(new Color(255, 255, 255)); // Fondo blanco
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true), // Borde redondeado
                BorderFactory.createEmptyBorder(10, 15, 10, 15) // Espaciado interno
        ));
        tarjeta.setMaximumSize(new Dimension(450, 50));

        JLabel lblTitulo = new JLabel(titulo + ": ");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTitulo.setForeground(new Color(80, 80, 80));

        JLabel lblValor = new JLabel(valor);
        lblValor.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblValor.setForeground(new Color(50, 50, 50));

        tarjeta.add(lblTitulo, BorderLayout.WEST);
        tarjeta.add(lblValor, BorderLayout.CENTER);

        return tarjeta;
    }



    private JPanel crearTarjetaAtributo(String titulo, String valor) {
        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BorderLayout());
        tarjeta.setBackground(new Color(255, 255, 255)); // Fondo blanco
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true), // Borde redondeado
                BorderFactory.createEmptyBorder(10, 15, 10, 15) // Espaciado interno
        ));
        tarjeta.setMaximumSize(new Dimension(450, 50));

        JLabel lblTitulo = new JLabel(titulo + ": ");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTitulo.setForeground(new Color(80, 80, 80));

        JLabel lblValor = new JLabel(valor);
        lblValor.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblValor.setForeground(new Color(50, 50, 50));

        tarjeta.add(lblTitulo, BorderLayout.WEST);
        tarjeta.add(lblValor, BorderLayout.CENTER);

        return tarjeta;
    }

    private void verificarYMostrarJoya(String id, JPanel detallesPanel) {
        boolean encontrada = false;

        // Iterar sobre el modelo de la lista
        for (int i = 0; i < listModel.getSize(); i++) {
            Joya joya = listModel.getElementAt(i);

            // Verificar si el ID coincide
            if (joya.getId().toString().equals(id)) {
                encontrada = true;

                // Actualizar los detalles de la joya en el panel
                detallesPanel.removeAll();
                detallesPanel.add(crearTarjetaAtributo("ID", String.valueOf(joya.getId())));
                detallesPanel.add(crearTarjetaAtributo("Nombre", joya.getNombre()));
                detallesPanel.add(crearTarjetaAtributo("Categoría", joya.getCategoria()));
                detallesPanel.add(crearTarjetaAtributo("Peso", joya.getPeso() + " gramos"));
                detallesPanel.add(crearTarjetaAtributo("Precio", "$" + joya.getPrecio()));
                detallesPanel.add(crearTarjetaAtributo("Tiene Piedra", joya.isTienePiedra() ? "Sí 💎" : "No 🪨"));
                if (joya.isTienePiedra()) {
                    detallesPanel.add(crearTarjetaAtributo("Información de Piedra", joya.getInfoPiedra()));
                }
                detallesPanel.add(crearTarjetaAtributo("Observación", joya.getObservacion()));
                detallesPanel.revalidate();
                detallesPanel.repaint();

                // Eliminar la joya de la lista
                listModel.remove(i);
                break; // Salir del bucle después de encontrar y procesar
            }
        }

        if (!encontrada) {
            JOptionPane.showMessageDialog(mainPanel, "La joya con ID " + id + " no se encuentra en la lista.", "No Encontrada", JOptionPane.ERROR_MESSAGE, new ImageIcon(new ImageIcon(getClass().getResource("/llora.png")).getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH)));
        }

        // Comprobar si la lista está vacía
        if (listModel.getSize() == 0) {
            JOptionPane.showMessageDialog(mainPanel, "Todas las joyas han sido verificadas y la lista está vacía.", "Lista Completa", JOptionPane.INFORMATION_MESSAGE, new ImageIcon(new ImageIcon(getClass().getResource("/nino.png")).getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH)));
        }
    }


    private void bloquearOpciones() {
        todasLasJoyasCheckBox.setEnabled(false);
        //checkBoxCategoria.setEnabled(false);
        comboBoxCategoria.setEnabled(false);
    }

    private void desbloquearOpciones() {
        todasLasJoyasCheckBox.setEnabled(true);
        //checkBoxCategoria.setEnabled(true);
        comboBoxCategoria.setEnabled(true);
    }




    public JPanel getMainPanel() {
        return mainPanel;
    }
}
