import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;
public class FileProcessor
{
    static String filename;
    static class Block
    {
        boolean visible=false;
        String content=null,character=null,pinyin=null;
        public Block(){}
        public Block(String content)
        {
            this.content=content;
            if(content.contains("<ruby>")&&content.contains("<rt>"))
            {
                visible=true;
                if(content.contains("<rt>"))
                {
                    character=content.split("<ruby>")[1].split("<rt>")[0].charAt(0)+"";
                    pinyin=content.split("<rt>")[1].split("</rt>")[0];
                }
                else
                {
                    character=content.split("<ruby>")[1].split("</ruby>")[0].charAt(0)+"";
                }
            }
        }
        public String toString()
        {
            if(visible)
            {
                return "<ruby>"+(null==character?"":character)+"<rt>"+(null==pinyin?"":pinyin)+"</rt></ruby>";
            }
            else
            {
                return null==content?"":content;
            }
        }
    }
    static LinkedList<Block>file_contents=new LinkedList<>();
    static ArrayList<Block[][]>pages=new ArrayList<>();
    static boolean[][][]page_cell_editable;
    static int current_page_index=0;
    public static void clear()
    {
        file_contents=new LinkedList<>();
        pages=new ArrayList<>();
        current_page_index=0;
    }
    public static String get_file_content()throws IOException
    {
        StringBuilder content=new StringBuilder();
        FileInputStream fileInputStream=new FileInputStream(filename);
        Scanner input=new Scanner(fileInputStream,"UTF-8");
        while(input.hasNextLine())
        {
            content.append(input.nextLine()).append('\n');
        }
        input.close();
        fileInputStream.close();
        String result=content.toString();
        while(result.endsWith("\n"))
        {
            result=result.substring(0,result.length()-1);
        }
        return result;
    }
    public static int get_next_row_index(int row_index,int column_index,int nrow,int ncol)
    {
        if(column_index==ncol-1)
        {
            return (row_index+1)%nrow;
        }
        return row_index;
    }
    public static int get_next_column_index(int row_index,int column_index,int nrow,int ncol)
    {
        if(column_index==ncol-1)
        {
            return 0;
        }
        return (column_index+1)%ncol;
    }
    public static void get_pages(int nrow,int ncol)
    {
        Block blank=new Block();
        int row_index=0,column_index=0;
        Block[][]current_page=new Block[nrow][ncol];
        pages.add(current_page);
        for(Block block:file_contents)
        {
            if(block.visible)
            {
                current_page[row_index][column_index]=block;
//                System.out.println(block+" "+row_index+" "+column_index);
                row_index=get_next_row_index(row_index,column_index,nrow,ncol);
                column_index=get_next_column_index(row_index,column_index,nrow,ncol);
                if(0==row_index&&0==column_index)
                {
                    current_page=new Block[nrow][ncol];
                    pages.add(current_page);
                }
            }
            else if(block.content.contains("</p>"))
            {
                for(int i=column_index;i<ncol;++i)
                {
                    current_page[row_index][column_index]=blank;
//                    System.out.println(row_index+" "+column_index);
                    row_index=get_next_row_index(row_index,column_index,nrow,ncol);
                    column_index=get_next_column_index(row_index,column_index,nrow,ncol);
                }
                if(0==row_index&&0==column_index)
                {
                    current_page=new Block[nrow][ncol];
                    pages.add(current_page);
                }
            }
        }
        if(pages.get(pages.size()-1)[0][0]==null)
        {
            pages.remove(pages.size()-1);
        }
        else
        {
            while(!(0==row_index&&0==column_index))
            {
                current_page[row_index][column_index]=blank;
                row_index=get_next_row_index(row_index,column_index,nrow,ncol);
                column_index=get_next_column_index(row_index,column_index,nrow,ncol);
            }
        }
//        for(Block[][]page:pages)
//        {
//            for(int i=0;i<nrow;++i)
//            {
//                for(int j=0;j<nrow;++j)
//                {
//                    if(page[i][j]==null)
//                    {
//                        System.out.println(i+" "+j+" !!!!!!!!!!!!!!!!");
//                    }
//                    System.out.print(page[i][j].character+" ");
//                }
//                System.out.println();
//            }
//        }
        page_cell_editable=new boolean[pages.size()][nrow][ncol];
        for(int i=0;i<pages.size();++i)
        {
            for(int j=0;j<pages.get(i).length;++j)
            {
                for(int k=0;k<pages.get(i)[0].length;++k)
                {
                    page_cell_editable[i][j][k]=(null!=pages.get(i)[j][k].character);
                }
            }
        }
    }
    public static void read_file()throws IOException
    {
        String file_content=get_file_content();
        int pos=0;
        while(pos<file_content.length())
        {
            int new_pos;
            if(pos==file_content.indexOf("<ruby>",pos))
            {
                new_pos=file_content.indexOf("</ruby>",pos)+"</ruby>".length();
            }
            else
            {
                new_pos=file_content.indexOf("<ruby>",pos);
            }
            if(-1==new_pos)
            {
                new_pos=file_content.length();
            }
            file_contents.add(new Block(file_content.substring(pos,new_pos)));
            pos=new_pos;
        }
    }
    public static String generate_file_content()
    {
        StringBuilder result=new StringBuilder();
        for(Block block:file_contents)
        {
            result.append(block);
        }
        return result.toString();
    }
    public static void push_up()
    {
        Block[][]current_page=pages.get(current_page_index);
        for(int i=0;i<current_page.length;++i)
        {
            for(int j=0;j<current_page[0].length;++j)
            {
                Main.characters[i][j]=(null==current_page[i][j].character?"":current_page[i][j].character);
                Main.pinyins[i][j]=(null==current_page[i][j].pinyin?"":current_page[i][j].pinyin);
                Main.cell_editable[i][j]=page_cell_editable[current_page_index][i][j];
            }
        }
    }
    public static void pull_back()
    {
        Block[][]current_page=pages.get(current_page_index);
        for(int i=0;i<current_page.length;++i)
        {
            for(int j=0;j<current_page[0].length;++j)
            {
                current_page[i][j].character=(""==Main.characters[i][j]?null:Main.characters[i][j]);
                current_page[i][j].pinyin=(""==Main.pinyins[i][j]?null:Main.pinyins[i][j]);
            }
        }
    }
    public static boolean save()
    {
        pull_back();
        try
        {
            FileOutputStream fileOutputStream=new FileOutputStream(filename);
            PrintWriter output=new PrintWriter(new OutputStreamWriter(fileOutputStream,StandardCharsets.UTF_8));
            output.print(generate_file_content());
            output.close();
            fileOutputStream.close();
            return true;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean load()
    {
        clear();
        try
        {
            read_file();
            get_pages(Main.nrow,Main.ncol);
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }
        push_up();
        Main.current_page.setText(((Integer)(current_page_index+1)).toString());
        Main.total_pages.setText(((Integer)(pages.size())).toString());
        return true;
    }
    public static void previous_page()
    {
        pull_back();
        if(current_page_index>0)
        {
            --current_page_index;
            Main.current_page.setText(((Integer)(current_page_index+1)).toString());
        }
        push_up();
    }
    public static void next_page()
    {
        pull_back();
        if(current_page_index<pages.size()-1)
        {
            ++current_page_index;
            Main.current_page.setText(((Integer)(current_page_index+1)).toString());
        }
        push_up();
    }
}