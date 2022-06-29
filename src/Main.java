import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeSet;
public class Main
{
    static int nrow=8,ncol=18;
    static int character_size=70,pinyin_size=36;
    static String character_font="PMingLiU",pinyin_font="Arial";
    static String[][]characters;
    static String[][]pinyins;
    static boolean[][]cell_editable;
    static String[][]contents;
    static String[]columns;
    static JTextField path=new JTextField();
    static TreeSet<String>alerts=new TreeSet<>();
    static Color alert_color=Color.red;
    static JLabel current_page=new JLabel();
    static JLabel total_pages=new JLabel();
    static class MyTableCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JTextField component=new JTextField();
        private boolean initialized=false;
        private int row;
        public Component getTableCellEditorComponent(JTable table,Object value,boolean isSelected,int rowIndex,int vColIndex) {
            String text=(String)value;
            row=rowIndex;
            if(rowIndex%2==1&&text.length()>1)
            {
                text=text.substring(text.length()-1);
            }
            component.setText(text);
            if(rowIndex%2==1)
            {
                if (!initialized)
                {
                    component.addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyTyped(KeyEvent e) {
                            if(row%2==1&&component.getText().length()>=1)
                            {
                                component.setText(component.getText().substring(component.getText().length()-1));
                                e.consume();
                            }
                        }
                    });
                    initialized=true;
                }
                component.setFont(new Font(character_font,Font.PLAIN,character_size-10));
            }
            else
            {
                component.setFont(new Font(pinyin_font,Font.PLAIN,pinyin_size-10));
            }
            return component;
        }
        @Override
        public Object getCellEditorValue() {
            return component.getText();
        }
    }
    public static void renew_contents()
    {
        for(int i=0;i<characters.length;++i)
        {
            for(int j=0;j<characters[0].length;++j)
            {
                contents[2*i][j]=pinyins[i][j];
                contents[2*i+1][j]=characters[i][j];
            }
        }
    }
    public static void pull_contents()
    {
        for(int i=0;i<characters.length;++i)
        {
            for(int j=0;j<characters[0].length;++j)
            {
                pinyins[i][j]=contents[2*i][j];
                characters[i][j]=contents[2*i+1][j];
            }
        }
    }
    public static JTable create_table(int nrow,int ncol)
    {
        columns=new String[ncol];
        for(int i=0;i<ncol;++i)
        {
            columns[i]=((Integer)(i+1)).toString();
        }
        contents=new String[nrow][ncol];
        JTable jTable=new JTable(contents,columns)
        {
            @Override
            public boolean isCellEditable(int row,int column)
            {
                return cell_editable[row/2][column];
            }
        };
        return jTable;
    }
    public static void placeComponents(JTable jTable)
    {
        DefaultTableCellRenderer cell_manager=new DefaultTableCellRenderer()
        {
            public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,boolean hasFocus,int row,int column)
            {
                setHorizontalAlignment(JLabel.CENTER);
                setVerticalAlignment(JLabel.CENTER);
                Component cell=super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
                if(row%2==1)
                {
                    cell.setFont(new Font(character_font,Font.PLAIN,character_size-10));
                    table.setRowHeight(row,character_size);
                }
                else
                {
                    cell.setFont(new Font(pinyin_font,Font.PLAIN,pinyin_size-10));
                    table.setRowHeight(row,pinyin_size);
                }
                if(alerts.contains(contents[row/2*2+1][column]))
                {
                    cell.setForeground(alert_color);
                }
                else
                {
                    cell.setForeground(Color.black);
                }
                return cell;
            }
        };
        for(String column:columns)
        {
            jTable.getColumn(column).setCellRenderer(cell_manager);
            jTable.getColumn(column).setCellEditor(new MyTableCellEditor());
        }
    }
    public static void open(JFrame jFrame)
    {
        File file=new File(path.getText());
        if(!file.exists())
        {
            JOptionPane.showMessageDialog(jFrame,"文件不存在。","錯誤",JOptionPane.ERROR_MESSAGE);
            return;
        }
        FileProcessor.filename=path.getText();
        if(FileProcessor.load())
        {
            renew_contents();
        }
        else
        {
            JOptionPane.showMessageDialog(jFrame,"文件無法讀取。","錯誤",JOptionPane.ERROR_MESSAGE);
        }
    }
    public static void save(JFrame jFrame,JButton jButton)
    {
        File file=new File(path.getText());
        if(!file.exists())
        {
            JOptionPane.showMessageDialog(jFrame,"文件不存在。","錯誤",JOptionPane.ERROR_MESSAGE);
            return;
        }
        FileProcessor.filename=path.getText();
        pull_contents();
        if(FileProcessor.save())
        {
            Date date=new Date();
            SimpleDateFormat simpleDateFormat=new SimpleDateFormat();
            jButton.setText("保存(Ctrl+S)"+simpleDateFormat.format(date));
        }
        else
        {
            JOptionPane.showMessageDialog(jFrame,"文件無法寫入。","錯誤",JOptionPane.ERROR_MESSAGE);
        }
    }
    public static void previous_page(JTable jTable)
    {
        jTable.setCellSelectionEnabled(false);
        if(jTable.isEditing())
        {
            jTable.getCellEditor().stopCellEditing();
        }
        pull_contents();
        FileProcessor.previous_page();
        renew_contents();
        jTable.setCellSelectionEnabled(true);
    }
    public static void next_page(JTable jTable)
    {
        jTable.setCellSelectionEnabled(false);
        if(jTable.isEditing())
        {
            jTable.getCellEditor().stopCellEditing();
        }
        pull_contents();
        FileProcessor.next_page();
        renew_contents();
        jTable.setCellSelectionEnabled(true);
    }
    public static void setComponents(JFrame jFrame,JTable jTable,JPanel jPanel)
    {
        jPanel.setLayout(new BoxLayout(jPanel,BoxLayout.Y_AXIS));
        Box vbox_1=new Box(BoxLayout.X_AXIS);
        JLabel path_label=new JLabel("文件路徑：");
        vbox_1.add(path_label);
        path.setColumns(6);
        path.setPreferredSize(new Dimension(character_size,path_label.getHeight()));
        vbox_1.add(path);
        JButton browse=new JButton("瀏覽");
        browse.addActionListener(e->
        {
            JFileChooser jFileChooser=new JFileChooser();
            jFileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            int index=jFileChooser.showOpenDialog(jFrame);
            if(index==JFileChooser.APPROVE_OPTION){
                @SuppressWarnings("unused")
                File selectedFile=jFileChooser.getSelectedFile();
                path.setText(jFileChooser.getSelectedFile().getAbsolutePath());
            }
        });
        vbox_1.add(browse);
        JButton load=new JButton("打開");
        load.addActionListener(e->
        {
            open(jFrame);
        });
        vbox_1.add(load);
        Box vbox_2=new Box(BoxLayout.X_AXIS);
        JLabel alert_label=new JLabel("突出字符集：");
        vbox_2.add(alert_label);
        JTextArea alert_texts=new JTextArea();
        alert_texts.setColumns(6);
        alert_texts.setPreferredSize(new Dimension(character_size*2,alert_label.getHeight()));
        vbox_2.add(alert_texts);
        JButton alert=new JButton("突出");
        alert.addActionListener(e->
        {
            alerts.clear();
            char[]alert_text=alert_texts.getText().toCharArray();
            for(char character:alert_text)
            {
                alerts.add(character+"");
            }
        });
        vbox_2.add(alert);
        JButton cancel_alert=new JButton("恢復");
        cancel_alert.addActionListener(e->
        {
            alerts.clear();
        });
        vbox_2.add(cancel_alert);
        Box vbox_3=new Box(BoxLayout.X_AXIS);
        JButton previous_page=new JButton("←（Alt+←）");
        vbox_3.add(previous_page);
        vbox_3.add(new JLabel("第"));
        vbox_3.add(current_page);
        vbox_3.add(new JLabel("頁"));
        previous_page.addActionListener(e->
        {
            previous_page(jTable);
        });
        previous_page.registerKeyboardAction(e->
        {
            previous_page(jTable);
        },KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,InputEvent.ALT_DOWN_MASK),JComponent.WHEN_IN_FOCUSED_WINDOW);
        JButton save=new JButton("保存（Ctrl+S）");
        save.addActionListener(e->
        {
            save(jFrame,save);
        });
        save.registerKeyboardAction(e->
        {
            save(jFrame,save);
        },KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.CTRL_DOWN_MASK),JComponent.WHEN_IN_FOCUSED_WINDOW);
        vbox_3.add(save);
        vbox_3.add(new JLabel("共"));
        vbox_3.add(total_pages);
        vbox_3.add(new JLabel("頁"));
        JButton next_page=new JButton("→（Alt+→）");
        next_page.addActionListener(e->
        {
            next_page(jTable);
        });
        next_page.registerKeyboardAction(e->
        {
            next_page(jTable);
        },KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,InputEvent.ALT_DOWN_MASK),JComponent.WHEN_IN_FOCUSED_WINDOW);
        vbox_3.add(next_page);
        jPanel.add(vbox_1);
        jPanel.add(vbox_2);
        jPanel.add(vbox_3);
    }
    static String initial_characters="南無阿彌陀佛";
    static String[]initial_pinyins={"ná","mó","ē","mí","tuó","fó"};
    public static void main(String[]args)
    {
        Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
        JFrame frame=new JFrame("HTML拼音校對");
        frame.setBounds(new Rectangle((int)(screenSize.getWidth()/2.0+0.5),0,(int)(screenSize.getWidth()/2.0+0.5),(int)(screenSize.getHeight()+0.5)));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel all=new JPanel();
        all.setBounds(new Rectangle((int)(screenSize.getWidth()/2.0+0.5),0,(int)(screenSize.getWidth()/2.0+0.5)-60,(int)(screenSize.getHeight()+0.5)));
        all.setLayout(new FlowLayout());
        nrow=(int)(all.getSize().getHeight()/(double)(character_size+pinyin_size)*5.0/6.0);
        ncol=(int)(all.getSize().getWidth()/(double)character_size);
        characters=new String[nrow][ncol];
        pinyins=new String[nrow][ncol];
        cell_editable=new boolean[nrow][ncol];
        for(int i=0;i<nrow;++i)
        {
            for(int j=0;j<ncol;++j)
            {
                cell_editable[i][j]=true;
            }
        }
        JTable jTable=create_table(2*nrow,ncol);
        jTable.setColumnSelectionAllowed(false);
        jTable.setRowSelectionAllowed(false);
        all.add(jTable);
        placeComponents(jTable);
        int pos=0;
        for(int i=0;i<nrow;++i)
        {
            for(int j=0;j<ncol;++j)
            {
                characters[i][j]=initial_characters.charAt(pos)+"";
                pinyins[i][j]=initial_pinyins[pos];
                pos=(pos+1)%initial_characters.length();
            }
        }
        renew_contents();
        JPanel jPanel=new JPanel();
        jPanel.setSize(all.getWidth(),all.getHeight()-jTable.getHeight());
        setComponents(frame,jTable,jPanel);
        all.add(jPanel);
        frame.add(all);
        frame.setVisible(true);
        frame.setResizable(false);
    }
}