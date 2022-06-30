import sun.font.FontDesignMetrics;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeSet;
class LoadFont
{
    public static Font loadFont(String fontFileName,float fontSize)
    {
        Font[]fonts=GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        try
        {
            java.net.URL file=Main.class.getResource(fontFileName);
            Font dynamicFont=Font.createFont(Font.TRUETYPE_FONT,file.openStream());
//            System.out.println(dynamicFont.getFontName());
            for(Font font:fonts)
            {
//                System.out.println(font.getFontName());
                if(font.getFontName().equals(dynamicFont.getFontName()))
                {
                    return new Font(font.getFontName(),Font.PLAIN,(int)(fontSize+0.5));
                }
            }
            Font dynamicFontPt=dynamicFont.deriveFont(fontSize);
            return dynamicFontPt;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return new java.awt.Font("PMingLiU",Font.PLAIN,(int)(fontSize+0.5));
        }
    }
}
public class Main
{
    static int nrow=8,ncol=18;
    static int character_size=65,pinyin_size=25;
    static String character_font_name="PMingLiU.ttf",pinyin_font_name="GB Pinyinok-C.ttf";
    static Font character_font,pinyin_font;
    static String[][]characters;
    static String[][]pinyins;
    static boolean[][]cell_editable;
    static String[][]contents;
    static String[]columns;
    static int column_width;
    static Dimension screenSize;
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
                component.setFont(character_font);
            }
            else
            {
                component.setFont(pinyin_font);
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
                    cell.setFont(character_font);
                    table.setRowHeight(row,character_size);
                }
                else
                {
                    cell.setFont(pinyin_font);
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
            jTable.getColumn(column).setPreferredWidth(column_width);
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
    public static void previous_page(JFrame jFrame,JTable jTable)
    {
        File file=new File(path.getText());
        if(!file.exists())
        {
            JOptionPane.showMessageDialog(jFrame,"未打開文件。","錯誤",JOptionPane.ERROR_MESSAGE);
            return;
        }
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
    public static void next_page(JFrame jFrame,JTable jTable)
    {
        File file=new File(path.getText());
        if(!file.exists())
        {
            JOptionPane.showMessageDialog(jFrame,"未打開文件。","錯誤",JOptionPane.ERROR_MESSAGE);
            return;
        }
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
    public static void setComponents(JFrame jFrame,JTable jTable,Box jPanel)
    {
        Box vbox_1=new Box(BoxLayout.X_AXIS);
        JLabel path_label=new JLabel("文件路徑：");
        vbox_1.add(path_label);
        path.setColumns(6);
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
        JTextField alert_texts=new JTextField();
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
            previous_page(jFrame,jTable);
        });
        previous_page.registerKeyboardAction(e->
        {
            previous_page(jFrame,jTable);
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
            next_page(jFrame,jTable);
        });
        next_page.registerKeyboardAction(e->
        {
            next_page(jFrame,jTable);
        },KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,InputEvent.ALT_DOWN_MASK),JComponent.WHEN_IN_FOCUSED_WINDOW);
        vbox_3.add(next_page);
        vbox_1.setPreferredSize(new Dimension((int)(screenSize.getWidth()/2.0+0.5)-40,27));
        vbox_2.setPreferredSize(new Dimension((int)(screenSize.getWidth()/2.0+0.5)-40,27));
        vbox_3.setPreferredSize(new Dimension((int)(screenSize.getWidth()/2.0+0.5)-40,27));
        jPanel.add(vbox_1);
        jPanel.add(vbox_2);
        jPanel.add(vbox_3);
    }
    static String initial_characters="南無阿彌陀佛";
    static String[]initial_pinyins={"ná","mó","ē","mí","tuó","fó"};
    public static void main(String[]args)
    {
        character_font=LoadFont.loadFont(character_font_name,character_size);
        character_size=FontDesignMetrics.getMetrics(character_font).getHeight();
        pinyin_font=LoadFont.loadFont(pinyin_font_name,pinyin_size);
        pinyin_size=FontDesignMetrics.getMetrics(pinyin_font).getHeight();
        column_width=Math.max(FontDesignMetrics.getMetrics(character_font).stringWidth("邊"),FontDesignMetrics.getMetrics(pinyin_font).stringWidth("chuán"))+3;
        screenSize=GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getSize();
        JFrame frame=new JFrame("HTML拼音校對");
        try
        {
            frame.setIconImage(ImageIO.read(Main.class.getResource("icon-proofread.png")));
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        frame.setBounds(new Rectangle((int)(screenSize.getWidth()/2.0+0.5),0,(int)(screenSize.getWidth()/2.0+0.5),(int)(screenSize.getHeight()+0.5)));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Box all=new Box(BoxLayout.Y_AXIS);
        all.setBounds(new Rectangle((int)(screenSize.getWidth()/2.0+0.5),0,(int)(screenSize.getWidth()/2.0+0.5)-40,(int)(screenSize.getHeight()+0.5)-40));
        nrow=(int)((all.getSize().getHeight()-81)/(double)(character_size+pinyin_size));
        ncol=(int)(all.getSize().getWidth()/(double)column_width);
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
        Box jPanel=new Box(BoxLayout.Y_AXIS);
        jPanel.setSize(all.getWidth(),all.getHeight()-jTable.getHeight());
        setComponents(frame,jTable,jPanel);
        all.add(jPanel);
        JScrollPane jScrollPane=new JScrollPane(all);
        frame.getContentPane().add(jScrollPane,BorderLayout.CENTER);
        frame.add(all);
        frame.setVisible(true);
        frame.setResizable(false);
    }
}