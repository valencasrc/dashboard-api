import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import BCrypt.BCrypt;


public class FormularioDeRegistro extends JDialog {
    private JTextField tfName;
    private JTextField tfEmail;
    private JTextField tfPhone;
    private JTextField tfAddress;
    private JPasswordField pfPassword;
    private JPasswordField pfConfirmPassword;
    private JButton btnRegister;
    private JButton btnCancel;
    private JPanel registerPanel;

    public FormularioDeRegistro(JFrame parent) {
        super(parent);
        setTitle("Crie uma nova conta");
        setContentPane(registerPanel);
        setMinimumSize(new Dimension(500, 400));
        setModal(true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        btnRegister.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerUser();
            }
        });

        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int response = JOptionPane.showOptionDialog(
                        FormularioDeRegistro.this,
                        "Tem certeza que deseja sair?",
                        "Confirmação de saída",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        new String[]{"Sair", "Cancelar"},
                        "Cancelar"
                );

                if (response == JOptionPane.YES_OPTION) {
                    dispose();
                }
            }
        });

        setVisible(true);
    }

    private void registerUser() {
        String name = tfName.getText();
        String email = tfEmail.getText();
        String phone = tfPhone.getText();
        String address = tfAddress.getText(); // Endereço é opcional
        String password = String.valueOf(pfPassword.getPassword());
        String confirmPassword = String.valueOf(pfConfirmPassword.getPassword());

        // Verificação de campos obrigatórios
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Por favor, preencha todos os campos obrigatórios.",
                    "Tente novamente",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Verificação se as senhas correspondem
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this,
                    "As senhas não correspondem.",
                    "Tente novamente",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Criptografar a senha
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // Registrar usuário no banco de dados
        usuario = adicionarUsuarioAoBancoDeDados(name, email, phone, address, hashedPassword);

        if (usuario != null) {
            JOptionPane.showMessageDialog(this,
                    "Usuário registrado com sucesso!",
                    "Registro bem-sucedido",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Falha ao registrar o usuário. Tente novamente.",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public Usuario usuario;

    private Usuario adicionarUsuarioAoBancoDeDados(String name, String email, String phone, String address, String password) {
        Usuario usuario = null;
        final String DB_URL = "jdbc:mysql://localhost:3306/exemplobd";
        final String USERNAME = "root";
        final String PASSWORD = "blink182";

        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            // Conexão feita com sucesso no banco de dados

            String sql = "INSERT INTO users (name, email, phone, address, password) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, email);
            preparedStatement.setString(3, phone);
            preparedStatement.setString(4, address);
            preparedStatement.setString(5, password);

            // Inserir linha na tabela
            int addedRows = preparedStatement.executeUpdate();
            if (addedRows > 0) {
                usuario = new Usuario();
                usuario.name = name;
                usuario.email = email;
                usuario.phone = phone;
                usuario.address = address;
                usuario.password = password; // Armazena a senha criptografada no objeto
            }

            preparedStatement.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return usuario;
    }

    public static void main(String[] args) {
        int choice = WelcomeDialog.showWelcomeDialog();

        if (choice == JOptionPane.YES_OPTION) {
            FormularioDeRegistro myForm = new FormularioDeRegistro(null);
            Usuario usuario = myForm.usuario;

            if (usuario != null) {
                System.out.println("Usuário registrado com sucesso " + usuario.name);
            } else {
                System.out.println("Registro cancelado!");
            }
        } else {
            System.exit(0); // Sair da aplicação
        }
    }
}

class WelcomeDialog {
    public static int showWelcomeDialog() {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Mensagem de boas-vindas e opções
        String message = "Bem-vindo(a) à minha aplicação!" + "\n\nEsta aplicação é apenas uma demonstração de um projeto desenvolvido por:" + "\n\nLucas Valença." +
                "\n\nDeseja continuar?";
        String title = "Boas-vindas";

        // Mostrar janela de diálogo
        int option = JOptionPane.showOptionDialog(frame,
                message,
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,     // Use o ícone padrão
                new String[]{"Continuar", "Sair"},  // Opções de botões
                "Continuar"); // Botão padrão selecionado

        frame.dispose(); // Liberar recursos do frame

        return option;
    }
}

