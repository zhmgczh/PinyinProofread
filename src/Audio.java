import jaco.mp3.player.MP3Player;
import javax.swing.*;
public class Audio
{
    static boolean is_play=false;
    public static void audio_play(String pinyin)
    {
        java.net.URL file=Audio.class.getResource("yinjies/"+pinyin+".mp3");
        MP3Player mp3Player=new MP3Player(file);
        mp3Player.play();
        while(!(mp3Player.isStopped()||mp3Player.isPaused()))
        {
            try
            {
                Thread.sleep(25);
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
    public static void play(JFrame jFrame,JTable jTable)
    {
        is_play=true;
        if(0==FileProcessor.pages.size())
        {
            return;
        }
        int row_index=Math.max(0,jTable.getSelectedRow())/2;
        int column_index=Math.max(0,jTable.getSelectedColumn());
        boolean blanked=false;
        while(is_play)
        {
            jTable.changeSelection(2*row_index,column_index,false,false);
            FileProcessor.Block block=FileProcessor.pages.get(FileProcessor.current_page_index)[row_index][column_index];
            if(null!=block.pinyin)
            {
                blanked=false;
                audio_play(block.pinyin);
            }
            else if(!blanked)
            {
                blanked=true;
                try
                {
                    Thread.sleep(1000);
                }
                catch(InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
            row_index=FileProcessor.get_next_row_index(row_index,column_index,Main.nrow,Main.ncol);
            column_index=FileProcessor.get_next_column_index(row_index,column_index,Main.nrow,Main.ncol);
            if(row_index==0&&column_index==0)
            {
                Main.next_page(jFrame,jTable);
            }
        }
    }
    public static void stop()
    {
        is_play=false;
    }
}