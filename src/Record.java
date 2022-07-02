import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.TreeMap;
public class Record
{
    static File record_file=new File("records.txt");
    static TreeMap<String,Integer>records=null;
    public static void read_records()
    {
        try
        {
            if(!record_file.exists())
            {
                record_file.createNewFile();
            }
            FileInputStream fileInputStream=new FileInputStream(record_file);
            Scanner input=new Scanner(fileInputStream,"UTF-8");
            while(input.hasNextLine())
            {
                String input_text=input.nextLine();
                if(input_text.contains(">"))
                {
                    String[]record=input_text.split(">");
                    records.put(record[0],Integer.parseInt(record[1]));
                }
            }
            input.close();
            fileInputStream.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
    public static void write_records()
    {
        try
        {
            FileOutputStream fileOutputStream=new FileOutputStream(record_file);
            PrintWriter output=new PrintWriter(new OutputStreamWriter(fileOutputStream,StandardCharsets.UTF_8));
            for(String file_path:records.keySet())
            {
                output.println(file_path+">"+records.get(file_path));
            }
            output.close();
            fileOutputStream.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
    public static void add(String file_path,int page_index)
    {
        if(null==records)
        {
            records=new TreeMap<>();
            read_records();
        }
        records.put(file_path,page_index);
    }
    public static int get(String file_path)
    {
        if(null==records)
        {
            records=new TreeMap<>();
            read_records();
        }
        return null==records.get(file_path)?0:records.get(file_path);
    }
}