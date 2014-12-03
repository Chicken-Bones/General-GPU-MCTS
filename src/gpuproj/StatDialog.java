package gpuproj;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.List;

public class StatDialog extends JDialog
{
    private static TreeMap<String, StatDialog> stats = new TreeMap<String, StatDialog>();
    private static File logDir;

    public static StatDialog get(String s) {
        StatDialog stat = stats.get(s);
        if(stat == null)
            stats.put(s, stat = new StatDialog(s));
        return stat;
    }

    private static File getLogDir() {
        if(logDir == null) {
            DateFormat fmt = new SimpleDateFormat("dd-MM-yyyy HH.mm.ss");
            logDir = new File("logs/"+fmt.format(new Date()));
            logDir.mkdirs();
        }
        return logDir;
    }

    private static MainFrame mainFrame = new MainFrame();
    private static class MainFrame extends JFrame implements LayoutManager, ActionListener
    {
        private List<JButton> buttons = new LinkedList<JButton>();

        public MainFrame() {
            super("GPU Project");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLayout(this);
            setSize(getPreferredSize());
            setLocationRelativeTo(null);
            setVisible(true);
        }

        public void addButton(String title) {
            JButton button = new JButton(title);
            button.setActionCommand(title);
            button.addActionListener(this);
            buttons.add(button);
            add(button);
            validate();
        }

        @Override public void addLayoutComponent(String name, Component comp) {}
        @Override public void removeLayoutComponent(Component comp) {}

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            return new Dimension(150, 200);
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return null;
        }

        @Override
        public void layoutContainer(Container parent) {
            int w = parent.getWidth();
            int h = parent.getHeight();
            int buttonWidth = w-20;
            int buttonHeight = 20;
            int sep = 5;
            int totalHeight = buttons.size()*(buttonHeight+sep)-sep;
            int y = (h-totalHeight)/2;
            if(y < 10) y = 10;
            int x = 10;
            for(JButton b : buttons) {
                b.setBounds(x, y, buttonWidth, buttonHeight);
                y += buttonHeight+sep;
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            stats.get(e.getActionCommand()).setVisible(true);
        }
    }

    private JTextArea textArea;
    private PrintStream log;

    private StatDialog(String title) {
        setTitle(title);
        File logFile = new File(getLogDir(), title+".txt");
        try {
            logFile.createNewFile();
            log = new PrintStream(new BufferedOutputStream(new FileOutputStream(logFile)), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        ((DefaultCaret)textArea.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane pane = new JScrollPane(textArea);
        add(pane);

        setSize(400, 300);
        setVisible(true);
        mainFrame.addButton(title);
    }

    public void print(String s) {
        textArea.append(s);
        log.print(s);
    }

    public void println(String s) {
        print(s+'\n');
    }

    public void println(Object o) {
        println(o.toString());
    }

    public void clear() {
        textArea.setText("");
        log.println();
    }

    public void setText(String s) {
        clear();
        println(s);
    }
}
