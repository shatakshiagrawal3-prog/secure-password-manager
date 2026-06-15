package gui;

import core.PasswordManager;
import core.UserManager;
import model.*;
import util.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.function.Predicate;

/**
 * Class: PasswordManagerGUI
 *
 * The main GUI window for the Password Manager.
 * Built using Java Swing (a standard GUI library).
 *
 * Screens:
 *  1. LOGIN   - Enter username + password to log in
 *  2. REGISTER - Create a new account
 *  3. DASHBOARD - View, add, edit, delete, search accounts
 *
 * Navigation between screens is done with a CardLayout.
 */
public class PasswordManagerGUI extends JFrame {

    private static final Color BG_DARK     = new Color(15, 23, 42);   
    private static final Color BG_CARD     = new Color(30, 41, 59);   
    private static final Color BG_ALT_ROW  = new Color(22, 33, 52);   
    private static final Color ACCENT      = new Color(99, 102, 241); 
    private static final Color ACCENT_DARK = new Color(79, 70, 229);  
    private static final Color TEXT_WHITE  = new Color(248, 250, 252);
    private static final Color TEXT_MUTED  = new Color(148, 163, 184);
    private static final Color SUCCESS     = new Color(34, 197, 94);  
    private static final Color WARNING     = new Color(245, 158, 11); 
    private static final Color DANGER      = new Color(239, 68, 68);  
    private static final Color FIELD_BG    = new Color(15, 23, 42);
    private static final Color GHOST_BG    = new Color(51, 65, 85);

    private PasswordManager pm;                        
    private UserManager userManager = new UserManager();
    private String currentUser;                        
    private AutoLockTimer lockTimer = new AutoLockTimer();
    private boolean pwdVisible = false;                

    private CardLayout cardLayout = new CardLayout();  
    private JPanel mainPanel;
    private JLabel statusBar;                          
    private JLabel userLabel;                          
    private DefaultTableModel tableModel;              
    private JTable accountTable;                       

    
    public static void main(String[] args) {
        
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new PasswordManagerGUI().setVisible(true));
    }

    public PasswordManagerGUI() {
        setTitle("Password Manager");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1050, 680);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);

        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(BG_DARK);
        mainPanel.add(buildLoginPanel(),    "LOGIN");
        mainPanel.add(buildRegisterPanel(), "REGISTER");
        mainPanel.add(buildDashboard(),     "DASHBOARD");

        add(mainPanel, BorderLayout.CENTER);
        cardLayout.show(mainPanel, "LOGIN");

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (pm != null) pm.saveToFile();
                lockTimer.stop();
            }
        });
    }

    private JPanel buildLoginPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(BG_DARK);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(new EmptyBorder(40, 50, 40, 50));
        card.setPreferredSize(new Dimension(420, 420));

        // Labels
        JLabel title = makeLabel("Password Manager", TEXT_WHITE, Font.BOLD, 26);
        title.setAlignmentX(CENTER_ALIGNMENT);
        JLabel subtitle = makeLabel("Login to your vault", TEXT_MUTED, Font.PLAIN, 13);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        // Input fields
        JTextField usernameField = makeTextField("Username");
        JPasswordField passwordField = makePasswordField();
        JLabel errorLabel = makeLabel(" ", DANGER, Font.PLAIN, 12);
        errorLabel.setAlignmentX(CENTER_ALIGNMENT);

        // Track failed login attempts (to lock after 3 wrong tries)
        int[] attempts = {0};

        FlatButton loginBtn = accentBtn("Login");
        loginBtn.setAlignmentX(CENTER_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        loginBtn.addActionListener(e -> {
            String uname = usernameField.getText().trim();
            String pwd   = new String(passwordField.getPassword());

            if (uname.isEmpty() || uname.equals("Username")) {
                errorLabel.setText("Please enter your username.");
                return;
            }
            if (pwd.isEmpty()) {
                errorLabel.setText("Please enter your password.");
                return;
            }

            if (!userManager.authenticate(uname, pwd)) {
                attempts[0]++;
                if (!userManager.userExists(uname)) {
                    errorLabel.setText("User not found. Please register first.");
                } else {
                    errorLabel.setText("Wrong password! Attempt " + attempts[0] + " / 3");

                if (attempts[0] >= 3) {
                    loginBtn.setEnabled(false);
                    new Timer(30000, ev -> {
                        loginBtn.setEnabled(true);
                        attempts[0] = 0;
                        errorLabel.setText(" ");
                        ((Timer) ev.getSource()).stop();
                    }).start();
                }
                return;
            }

            attempts[0] = 0;
            currentUser = uname;
            pm = new PasswordManager(currentUser);
            pm.loadFromFile();
            lockTimer.reset();

            if (userLabel != null) userLabel.setText("  " + currentUser);
            populateTable(a -> true); // Show all accounts
            cardLayout.show(mainPanel, "DASHBOARD");
            passwordField.setText("");
            errorLabel.setText(" ");
        }});

        passwordField.addActionListener(e -> loginBtn.doClick());

        FlatButton registerLink = ghostBtn("Create New Account");
        registerLink.setAlignmentX(CENTER_ALIGNMENT);
        registerLink.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        registerLink.addActionListener(e -> cardLayout.show(mainPanel, "REGISTER"));

        card.add(title);
        card.add(gap(4));
        card.add(subtitle);
        card.add(gap(30));
        addLabeledField(card, "Username", usernameField);
        addLabeledField(card, "Password", passwordField);
        card.add(errorLabel);
        card.add(gap(16));
        card.add(loginBtn);
        card.add(gap(10));
        card.add(registerLink);

        outer.add(card);
        return outer;
    }

    private JPanel buildRegisterPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(BG_DARK);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(new EmptyBorder(40, 50, 40, 50));
        card.setPreferredSize(new Dimension(420, 480));

        JLabel title = makeLabel("Create Account", TEXT_WHITE, Font.BOLD, 26);
        title.setAlignmentX(CENTER_ALIGNMENT);
        JLabel subtitle = makeLabel("Set up a new vault", TEXT_MUTED, Font.PLAIN, 13);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        JTextField usernameField = makeTextField("Choose a username");
        JPasswordField passwordField = makePasswordField();
        JPasswordField confirmField  = makePasswordField();
        JLabel msgLabel = makeLabel(" ", DANGER, Font.PLAIN, 12);
        msgLabel.setAlignmentX(CENTER_ALIGNMENT);

        FlatButton registerBtn = accentBtn("Register");
        registerBtn.setAlignmentX(CENTER_ALIGNMENT);
        registerBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        registerBtn.addActionListener(e -> {
            String uname   = usernameField.getText().trim();
            String pwd     = new String(passwordField.getPassword());
            String confirm = new String(confirmField.getPassword());

            // Validate inputs
            if (uname.isEmpty() || uname.equals("Choose a username")) {
                showMsg(msgLabel, "Please enter a username.", DANGER); return;
            }
            if (pwd.isEmpty()) {
                showMsg(msgLabel, "Please enter a password.", DANGER); return;
            }
            if (pwd.length() < 4) {
                showMsg(msgLabel, "Password must be at least 4 characters.", DANGER); return;
            }
            if (!confirm.equals(pwd)) {
                showMsg(msgLabel, "Passwords do not match.", DANGER); return;
            }

            // Try to register
            if (!userManager.register(uname, pwd)) {
                showMsg(msgLabel, "Username already taken. Try another.", DANGER); return;
            }

            // Success — go back to login after a short delay
            showMsg(msgLabel, "Account created! Going to login...", SUCCESS);
            new Timer(1500, ev -> {
                ((Timer) ev.getSource()).stop();
                usernameField.setText("Choose a username");
                usernameField.setForeground(TEXT_MUTED);
                passwordField.setText("");
                confirmField.setText("");
                msgLabel.setText(" ");
                cardLayout.show(mainPanel, "LOGIN");
            }).start();
        });

        confirmField.addActionListener(e -> registerBtn.doClick());

        FlatButton backLink = ghostBtn("← Back to Login");
        backLink.setAlignmentX(CENTER_ALIGNMENT);
        backLink.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        backLink.addActionListener(e -> {
            usernameField.setText("Choose a username");
            usernameField.setForeground(TEXT_MUTED);
            passwordField.setText("");
            confirmField.setText("");
            msgLabel.setText(" ");
            cardLayout.show(mainPanel, "LOGIN");
        });

        card.add(title);
        card.add(gap(4));
        card.add(subtitle);
        card.add(gap(30));
        addLabeledField(card, "Username", usernameField);
        addLabeledField(card, "Password", passwordField);
        addLabeledField(card, "Confirm Password", confirmField);
        card.add(msgLabel);
        card.add(gap(16));
        card.add(registerBtn);
        card.add(gap(10));
        card.add(backLink);

        outer.add(card);
        return outer;
    }


    private JPanel buildDashboard() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);

        // ---- Top Bar ----
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(BG_CARD);
        topBar.setBorder(new EmptyBorder(12, 20, 12, 20));

        JPanel topLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        topLeft.setBackground(BG_CARD);
        topLeft.add(makeLabel("Password Manager", TEXT_WHITE, Font.BOLD, 18));
        userLabel = makeLabel("", TEXT_MUTED, Font.PLAIN, 13);
        topLeft.add(userLabel);

        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topRight.setBackground(BG_CARD);

        FlatButton backupBtn = ghostBtn("Backup");
        FlatButton statsBtn  = ghostBtn("Stats");
        FlatButton logoutBtn = ghostBtn("Logout");

        backupBtn.addActionListener(e -> {
            pm.backup();
            setStatus("Backup created in /backups folder.");
        });
        statsBtn.addActionListener(e -> showStatsDialog());
        logoutBtn.addActionListener(e -> {
            if (pm != null) pm.saveToFile();
            pm = null;
            currentUser = null;
            pwdVisible = false;
            lockTimer.stop();
            cardLayout.show(mainPanel, "LOGIN");
        });

        topRight.add(backupBtn);
        topRight.add(statsBtn);
        topRight.add(logoutBtn);
        topBar.add(topLeft,  BorderLayout.WEST);
        topBar.add(topRight, BorderLayout.EAST);

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(BG_CARD);
        sidebar.setBorder(new EmptyBorder(16, 10, 16, 10));
        sidebar.setPreferredSize(new Dimension(165, 0));

        sidebar.add(sideLabel("FILTER BY TYPE"));
        for (String type : new String[]{"All", "Social Media", "Banking", "Email", "Work"}) {
            FlatButton btn = sidebarBtn(type);
            btn.addActionListener(e -> {
                if (type.equals("All")) {
                    populateTable(a -> true);
                } else {
                    populateTable(a -> a.getType().equalsIgnoreCase(type));
                }
                setStatus("Filter: " + type);
            });
            sidebar.add(btn);
            sidebar.add(gap(4));
        }

        sidebar.add(gap(14));
        sidebar.add(sideLabel("SORT BY"));
        FlatButton sortTypeBtn    = sidebarBtn("Type");
        FlatButton sortWebsiteBtn = sidebarBtn("Website");
        sortTypeBtn.addActionListener(e -> {
            pm.sortByType();
            populateTable(a -> true);
            setStatus("Sorted by type.");
        });
        sortWebsiteBtn.addActionListener(e -> {
            pm.sortByWebsite();
            populateTable(a -> true);
            setStatus("Sorted by website.");
        });
        sidebar.add(sortTypeBtn);
        sidebar.add(gap(4));
        sidebar.add(sortWebsiteBtn);

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBackground(BG_DARK);
        content.setBorder(new EmptyBorder(14, 14, 10, 14));

        JPanel searchBar = new JPanel(new BorderLayout(8, 0));
        searchBar.setBackground(BG_DARK);
        JTextField searchField = makeTextField("Search by website, username, or tag...");
        FlatButton searchBtn = accentBtn("Search");
        FlatButton clearBtn  = ghostBtn("Clear");

        searchBtn.addActionListener(e -> {
            String kw = searchField.getText().trim();
            if (!kw.isEmpty() && !kw.equals("Search by website, username, or tag...")) {
                populateTable(a -> a.matches(kw));
                setStatus("Search results for: " + kw);
            }
        });
        clearBtn.addActionListener(e -> {
            populateTable(a -> true);
            searchField.setText("");
            setStatus("Showing all accounts.");
        });
        searchField.addActionListener(e -> searchBtn.doClick());

        JPanel searchBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchBtns.setBackground(BG_DARK);
        searchBtns.add(searchBtn);
        searchBtns.add(clearBtn);
        searchBar.add(searchField, BorderLayout.CENTER);
        searchBar.add(searchBtns,  BorderLayout.EAST);

        String[] columns = {"Type", "Website", "Username", "Password", "Strength", "Tag"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; } // Read-only table
        };
        accountTable = new JTable(tableModel);
        styleTable(accountTable);

        JScrollPane scrollPane = new JScrollPane(accountTable);
        scrollPane.getViewport().setBackground(BG_CARD);
        scrollPane.setBorder(BorderFactory.createLineBorder(GHOST_BG));

        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        actionBar.setBackground(BG_DARK);

        FlatButton addBtn     = accentBtn("+ Add Account");
        FlatButton editBtn    = ghostBtn("Edit Password");
        FlatButton deleteBtn  = dangerBtn("Delete");
        FlatButton histBtn    = ghostBtn("Password History");
        FlatButton genBtn     = ghostBtn("Generate Password");
        FlatButton exportBtn  = ghostBtn("Export CSV");
        FlatButton showPwdBtn = ghostBtn("Show / Hide Pwd");

        addBtn.addActionListener(e    -> showAddDialog());
        editBtn.addActionListener(e   -> showEditDialog());
        deleteBtn.addActionListener(e -> deleteSelected());
        histBtn.addActionListener(e   -> showHistoryDialog());
        genBtn.addActionListener(e    -> showGenerateDialog());
        exportBtn.addActionListener(e -> {
            pm.exportToCSV();
            setStatus("Exported to export.csv");
        });
        showPwdBtn.addActionListener(e -> {
            pwdVisible = !pwdVisible;
            populateTable(a -> true);
        });

        for (JButton btn : new JButton[]{addBtn, editBtn, deleteBtn, histBtn, genBtn, exportBtn, showPwdBtn}) {
            actionBar.add(btn);
        }

        content.add(searchBar,  BorderLayout.NORTH);
        content.add(scrollPane, BorderLayout.CENTER);
        content.add(actionBar,  BorderLayout.SOUTH);

        statusBar = makeLabel("  Ready", TEXT_MUTED, Font.PLAIN, 12);
        statusBar.setBorder(new EmptyBorder(4, 8, 4, 8));

        root.add(topBar,    BorderLayout.NORTH);
        root.add(sidebar,   BorderLayout.WEST);
        root.add(content,   BorderLayout.CENTER);
        root.add(statusBar, BorderLayout.SOUTH);

        return root;
    }

    /**
     * Fills the accounts table with accounts that pass the given filter.
     * @param filter A Predicate (function) that returns true for accounts to show.
     * Example: populateTable(a -> a.getType().equals("Banking"))
     */
    private void populateTable(Predicate<Account> filter) {
        tableModel.setRowCount(0); // Clear current rows

        for (Account a : pm.getAccounts()) {
            if (!filter.test(a)) continue;

            // Show dots or real password depending on toggle
            String displayPwd = pwdVisible ? a.getPassword() : "••••••••";

            tableModel.addRow(new Object[]{
                a.getType(),
                a.getWebsite(),
                a.getUsername(),
                displayPwd,
                PasswordStrengthChecker.check(a.getPassword()).name(),
                a.getTag()
            });
        }

        accountTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setBackground(isSelected ? ACCENT_DARK : (row % 2 == 0 ? BG_CARD : BG_ALT_ROW));
                setForeground(TEXT_WHITE);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                if (col == 4 && value != null) { // Strength column
                    switch (value.toString()) {
                        case "WEAK":       setForeground(DANGER);  break;
                        case "MODERATE":   setForeground(WARNING); break;
                        case "STRONG":     setForeground(SUCCESS); break;
                        case "VERY_STRONG":setForeground(new Color(100, 255, 150)); break;
                    }
                }
                return this;
            }
        });
    }

    private void showAddDialog() {
        JDialog dialog = makeDialog("Add New Account", 440, 540);
        JPanel panel = darkPanel();
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        String[] types = {"Social Media", "Banking", "Email", "Work"};
        JComboBox<String> typeBox = styledComboBox(types);

        JTextField websiteField  = makeTextField("e.g. instagram.com");
        JTextField usernameField = makeTextField("Username or email");
        JPasswordField passField = makePasswordField();
        JTextField extraField    = makeTextField("Platform / Bank / Recovery / Company");
        JTextField tagField      = makeTextField("Tag (e.g. personal, work)");

        JLabel strengthLabel = makeLabel("Strength: —", TEXT_MUTED, Font.PLAIN, 12);
        passField.getDocument().addDocumentListener(new DocumentListener() {
            void update() {
                PasswordStrengthChecker.Strength s = PasswordStrengthChecker.check(new String(passField.getPassword()));
                strengthLabel.setText("Strength: " + s.name());
                switch (s) {
                    case WEAK:     strengthLabel.setForeground(DANGER);  break;
                    case MODERATE: strengthLabel.setForeground(WARNING); break;
                    default:       strengthLabel.setForeground(SUCCESS); break;
                }
            }
            public void insertUpdate(DocumentEvent e)  { update(); }
            public void removeUpdate(DocumentEvent e)  { update(); }
            public void changedUpdate(DocumentEvent e) { update(); }
        });

        FlatButton generateBtn = ghostBtn("Generate Password");
        FlatButton saveBtn     = accentBtn("Save Account");
        generateBtn.setAlignmentX(LEFT_ALIGNMENT);
        saveBtn.setAlignmentX(LEFT_ALIGNMENT);
        saveBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        generateBtn.addActionListener(e -> passField.setText(PasswordGenerator.generate(14)));

        saveBtn.addActionListener(e -> {
            String website  = websiteField.getText().trim();
            String username = usernameField.getText().trim();
            String password = new String(passField.getPassword());
            String extra    = extraField.getText().trim();
            String tag      = tagField.getText().trim().isEmpty() ? "general" : tagField.getText().trim();

            if (website.isEmpty() || username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Website, Username, and Password are required.");
                return;
            }

            Account acc = null;
            switch ((String) typeBox.getSelectedItem()) {
                case "Social Media":
                    acc = new SocialAccount(website, username, password, extra.isEmpty() ? website : extra, tag);
                    break;
                case "Banking":
                    acc = new BankingAccount(website, username, password, extra.isEmpty() ? website : extra, tag);
                    break;
                case "Email":
                    acc = new EmailAccount(website, username, password, extra.isEmpty() ? "N/A" : extra, tag);
                    break;
                case "Work":
                    acc = new WorkAccount(website, username, password, extra.isEmpty() ? "N/A" : extra, tag);
                    break;
            }

            pm.addAccount(acc);
            lockTimer.reset();
            populateTable(a -> true);
            setStatus("Added account: " + website);
            dialog.dispose();
        });

        addLabeledField(panel, "Account Type", typeBox);
        addLabeledField(panel, "Website",      websiteField);
        addLabeledField(panel, "Username",     usernameField);
        addLabeledField(panel, "Password",     passField);
        panel.add(strengthLabel);
        panel.add(gap(4));
        panel.add(generateBtn);
        addLabeledField(panel, "Extra Info",   extraField);
        addLabeledField(panel, "Tag",          tagField);
        panel.add(gap(14));
        panel.add(saveBtn);

        JScrollPane sp = new JScrollPane(panel);
        sp.setBorder(null);
        sp.getViewport().setBackground(BG_CARD);
        dialog.add(sp);
        dialog.setVisible(true);
    }

    private void showEditDialog() {
        int row = accountTable.getSelectedRow();
        if (row < 0) { setStatus("Please select an account first."); return; }

        String website = (String) tableModel.getValueAt(row, 1);
        JDialog dialog = makeDialog("Edit Password — " + website, 380, 240);
        JPanel panel = darkPanel();
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPasswordField passField = makePasswordField();
        FlatButton generateBtn   = ghostBtn("Generate");
        FlatButton saveBtn       = accentBtn("Update Password");
        generateBtn.setAlignmentX(LEFT_ALIGNMENT);
        saveBtn.setAlignmentX(LEFT_ALIGNMENT);
        saveBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        generateBtn.addActionListener(e -> passField.setText(PasswordGenerator.generate(14)));
        saveBtn.addActionListener(e -> {
            String newPassword = new String(passField.getPassword());
            if (newPassword.isEmpty()) return;
            if (!PasswordStrengthChecker.isAcceptable(newPassword)) {
                JOptionPane.showMessageDialog(dialog, "Password is too weak. Use uppercase, lowercase, digits, and symbols.");
                return;
            }
            try {
                pm.editPassword(website, newPassword);
                lockTimer.reset();
                populateTable(a -> true);
                setStatus("Password updated for: " + website);
                dialog.dispose();
            } catch (AccountNotFoundException ex) {
                setStatus(ex.getMessage());
            }
        });

        addLabeledField(panel, "New Password", passField);
        panel.add(generateBtn);
        panel.add(gap(14));
        panel.add(saveBtn);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void deleteSelected() {
        int row = accountTable.getSelectedRow();
        if (row < 0) { setStatus("Please select an account first."); return; }

        String website = (String) tableModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete the account for: " + website + "?",
            "Delete Account",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                pm.delete(website);
                lockTimer.reset();
                populateTable(a -> true);
                setStatus("Deleted account: " + website);
            } catch (AccountNotFoundException e) {
                setStatus(e.getMessage());
            }
        }
    }

    private void showHistoryDialog() {
        int row = accountTable.getSelectedRow();
        if (row < 0) { setStatus("Please select an account first."); return; }

        String website = (String) tableModel.getValueAt(row, 1);
        JDialog dialog = makeDialog("Password History — " + website, 360, 260);
        JPanel panel = darkPanel();
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));
        panel.setLayout(new BorderLayout(0, 8));

        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (Account a : pm.getAccounts()) {
            if (a.getWebsite().equalsIgnoreCase(website)) {
                ArrayList<String> history = a.getPasswordHistory();
                if (history.isEmpty()) {
                    listModel.addElement("No history yet.");
                } else {
                    history.forEach(listModel::addElement);
                }
                break;
            }
        }

        JList<String> historyList = new JList<>(listModel);
        historyList.setBackground(FIELD_BG);
        historyList.setForeground(TEXT_WHITE);
        historyList.setFont(new Font("Monospaced", Font.PLAIN, 13));
        historyList.setBorder(new EmptyBorder(8, 8, 8, 8));

        panel.add(makeLabel("Last 5 passwords:", TEXT_MUTED, Font.PLAIN, 12), BorderLayout.NORTH);
        panel.add(new JScrollPane(historyList), BorderLayout.CENTER);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showGenerateDialog() {
        JDialog dialog = makeDialog("Password Generator", 380, 230);
        JPanel panel = darkPanel();
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JSlider lengthSlider = new JSlider(8, 32, 14);
        lengthSlider.setBackground(BG_CARD);
        JLabel lengthLabel = makeLabel("Length: 14", TEXT_MUTED, Font.PLAIN, 12);
        lengthSlider.addChangeListener(e -> lengthLabel.setText("Length: " + lengthSlider.getValue()));

        JTextField resultField = makeTextField("");
        resultField.setEditable(false);
        resultField.setFont(new Font("Monospaced", Font.BOLD, 13));

        FlatButton generateBtn = accentBtn("Generate");
        generateBtn.setAlignmentX(LEFT_ALIGNMENT);
        generateBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        generateBtn.addActionListener(e -> resultField.setText(PasswordGenerator.generate(lengthSlider.getValue())));

        panel.add(lengthLabel);
        panel.add(gap(4));
        panel.add(lengthSlider);
        panel.add(gap(10));
        panel.add(generateBtn);
        panel.add(gap(10));
        panel.add(resultField);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showStatsDialog() {
        int total = 0, social = 0, banking = 0, email = 0, work = 0, weak = 0;

        for (Account a : pm.getAccounts()) {
            total++;
            switch (a.getType()) {
                case "Social Media": social++;  break;
                case "Banking":      banking++; break;
                case "Email":        email++;   break;
                case "Work":         work++;    break;
            }
            if (PasswordStrengthChecker.check(a.getPassword()) == PasswordStrengthChecker.Strength.WEAK) {
                weak++;
            }
        }

        JDialog dialog = makeDialog("Account Statistics", 320, 300);
        JPanel panel = darkPanel();
        panel.setBorder(new EmptyBorder(20, 28, 20, 28));

        panel.add(statRow("Total Accounts", "" + total));   panel.add(gap(8));
        panel.add(statRow("Social Media",   "" + social));  panel.add(gap(8));
        panel.add(statRow("Banking",        "" + banking)); panel.add(gap(8));
        panel.add(statRow("Email",          "" + email));   panel.add(gap(8));
        panel.add(statRow("Work",           "" + work));    panel.add(gap(16));

        String weakMsg = weak > 0
            ? "⚠  " + weak + " weak password(s) — please update them!"
            : "✓  All passwords are strong!";
        JLabel weakLabel = makeLabel(weakMsg, weak > 0 ? DANGER : SUCCESS, Font.BOLD, 13);
        weakLabel.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(weakLabel);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    static class FlatButton extends JButton {
        private final Color normalColor, hoverColor;
        private boolean hovered = false;

        FlatButton(String text, Color normalColor, Color hoverColor, Color textColor) {
            super(text);
            this.normalColor = normalColor;
            this.hoverColor  = hoverColor;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFont(new Font("SansSerif", Font.BOLD, 13));
            setForeground(textColor);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(hovered ? hoverColor : normalColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private FlatButton accentBtn(String text) {
        return new FlatButton(text, ACCENT, ACCENT_DARK, Color.WHITE);
    }
    private FlatButton ghostBtn(String text) {
        return new FlatButton(text, GHOST_BG, new Color(71, 85, 105), Color.WHITE);
    }
    private FlatButton dangerBtn(String text) {
        return new FlatButton(text, DANGER, new Color(200, 50, 50), Color.WHITE);
    }
    private FlatButton sidebarBtn(String text) {
        FlatButton btn = new FlatButton(text, BG_CARD, ACCENT, Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(7, 10, 7, 10));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        return btn;
    }

    private JTextField makeTextField(String placeholder) {
        JTextField field = new JTextField(placeholder) {
            protected void paintComponent(Graphics g) {
                g.setColor(FIELD_BG);
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
            }
        };
        field.setOpaque(false);
        field.setBackground(FIELD_BG);
        field.setForeground(TEXT_MUTED);
        field.setCaretColor(TEXT_WHITE);
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setBorder(new EmptyBorder(8, 12, 8, 12));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        field.setAlignmentX(LEFT_ALIGNMENT);

        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(TEXT_WHITE);
                }
            }
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(TEXT_MUTED);
                }
            }
        });
        return field;
    }

    private JPasswordField makePasswordField() {
        JPasswordField field = new JPasswordField() {
            protected void paintComponent(Graphics g) {
                g.setColor(FIELD_BG);
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
            }
        };
        field.setOpaque(false);
        field.setBackground(FIELD_BG);
        field.setForeground(TEXT_WHITE);
        field.setCaretColor(TEXT_WHITE);
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setBorder(new EmptyBorder(8, 12, 8, 12));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        field.setAlignmentX(LEFT_ALIGNMENT);
        return field;
    }

    private JComboBox<String> styledComboBox(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setBackground(FIELD_BG);
        cb.setForeground(TEXT_WHITE);
        cb.setFont(new Font("SansSerif", Font.PLAIN, 13));
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        cb.setAlignmentX(LEFT_ALIGNMENT);
        return cb;
    }

    private void styleTable(JTable table) {
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_WHITE);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(34);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(ACCENT_DARK);
        table.setSelectionForeground(Color.WHITE);
        table.getTableHeader().setBackground(new Color(22, 22, 40));
        table.getTableHeader().setForeground(TEXT_MUTED);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setBorder(new EmptyBorder(0, 10, 0, 0));
    }

    private static JLabel makeLabel(String text, Color color, int style, int size) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(new Font("SansSerif", style, size));
        return label;
    }

    private JLabel sideLabel(String text) {
        JLabel label = makeLabel(text, TEXT_MUTED, Font.BOLD, 10);
        label.setBorder(new EmptyBorder(0, 4, 6, 0));
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    private static Component gap(int height) {
        return Box.createRigidArea(new Dimension(0, height));
    }

    private JPanel darkPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_CARD);
        return p;
    }

    private JDialog makeDialog(String title, int width, int height) {
        JDialog d = new JDialog(this, title, true);
        d.setSize(width, height);
        d.setLocationRelativeTo(this);
        d.getContentPane().setBackground(BG_CARD);
        return d;
    }

    private void addLabeledField(JPanel panel, String labelText, JComponent field) {
        JLabel label = makeLabel(labelText, TEXT_MUTED, Font.PLAIN, 12);
        label.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(label);
        panel.add(gap(5));
        field.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(field);
        panel.add(gap(12));
    }

    private JPanel statRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(BG_CARD);
        row.add(makeLabel(label, TEXT_MUTED, Font.PLAIN, 13), BorderLayout.WEST);
        row.add(makeLabel(value, TEXT_WHITE, Font.BOLD, 13),  BorderLayout.EAST);
        return row;
    }

    private void setStatus(String message) {
        statusBar.setText("  " + message);
    }

    private void showMsg(JLabel label, String text, Color color) {
        label.setText(text);
        label.setForeground(color);
    }
}
