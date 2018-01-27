import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangqiang on 2018/1/25 0025.
 */
public class ReadFile {
    public String[] readLinesRe(String filename) throws IOException
    {
        InputStream is = getClass().getResourceAsStream(filename);
        InputStreamReader isr = new InputStreamReader(is,"UTF-8");
        BufferedReader bufferedReader = new BufferedReader(isr);
        List<String> lines = new ArrayList<String>();
        String line = null;

        while ((line = bufferedReader.readLine()) != null)
        {
            lines.add(line);
        }

        bufferedReader.close();

        return lines.toArray(new String[lines.size()]);

    }
    public String[] readLines(String filename) throws IOException
    {
        FileReader fileReader = new FileReader(filename);

        BufferedReader bufferedReader = new BufferedReader(fileReader);
        List<String> lines = new ArrayList<String>();
        String line = null;

        while ((line = bufferedReader.readLine()) != null)
        {
            lines.add(line);
        }

        bufferedReader.close();

        return lines.toArray(new String[lines.size()]);
    }
}
