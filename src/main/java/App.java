import com.giaybac.traprange.PDFTableExtractor;
import com.giaybac.traprange.entity.Table;
import com.giaybac.traprange.entity.TableRow;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yangqiang on 2018/1/26 0026.
 */
public class App  extends JFrame{
    private JTextField count;
    private JButton button;
    private JPanel panelMain;
    private JTextField filename;
    private JButton filebutton;

    private String dirname;
    public static void main(String[] args) {
       JFrame frame=new JFrame("App");
       frame.setBounds(100,100,400,400);
       frame.setContentPane(new App().panelMain);
       frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       frame.pack();
       frame.setVisible(true);
    }
    public App() {
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

            }
        });
        filebutton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser c = new JFileChooser();
                int rVal = c.showOpenDialog(App.this);
                if (rVal == JFileChooser.APPROVE_OPTION) {
                    filename.setText(c.getCurrentDirectory().toString()+"\\"+c.getSelectedFile().getName());
                    dirname=c.getCurrentDirectory().toString()+"\\";
                }
                if (rVal == JFileChooser.CANCEL_OPTION) {
                    filename.setText("You pressed cancel");
                }
            }
        });
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                PDFTableExtractor extractor = new PDFTableExtractor();
              //  String filepath="D:\\tmp\\all.pdf";
                String filepath=filename.getText();
                String folderpath=dirname;
              //  String folderpath="D:\\tmp\\test\\";
                int pagecount=4;
                if (count.getText()!=null){
                    pagecount=Integer.parseInt(count.getText());
                }

                List<Table> tables = extractor.setSource(filepath)
                        .addPages(pagecount)
                        .exceptLine(new int[]{0,1,-1})
                        .extract();
                //  System.out.println(tables.size());
                List<TableRow> mulu=new ArrayList<TableRow>();
                for (int j=0;j<tables.size();j++){
                    Table t=   tables.get(j);
                    List<TableRow> rows= t.getRows();
                    String cellcontentt=null;
                    for (int i=0;i<rows.size();i++){
                        TableRow row=rows.get(i);
                        cellcontentt= row.getCells().get(0).getContent();
                        if (cellcontentt.length()>0){
                            Pattern p= Pattern.compile("\\d");
                            Matcher m=p.matcher(cellcontentt);
                            if(m.find())
                                mulu.add(row);
                            //  System.out.println(row.toString());
                        }
                    }
                }

                //read config
                ReadFile rf = new ReadFile();
                String filename = "pdf.properties";
                String[] lines=null;
                try
                {
                    lines = rf.readLinesRe(filename);
                }
                catch(IOException e1)
                {
                 e1.printStackTrace();
                }

                try {
                    PDFTable.SplitPages(folderpath,filepath);
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                HashMap<String,String> map=new HashMap<String, String>();
                int start=0;
                int end=0;
                String rowcontent=null;

                for(String line:lines){
                    System.out.println("begin deal utf="+line);
                    System.out.println("begin deal="+line);
                    boolean started=false;
                    start=0;
                    end=0;
                    for (int i=0;i<mulu.size();i++){
                        TableRow row=mulu.get(i);
                        rowcontent= row.toString();
                        String pages=null;
                        //  System.out.println(rowcontent);
                        if(rowcontent.contains(line)&&!started) {
                            started=true;
                            pages=row.getCells().get(5).getContent();
                            if (pages.length()>0){
                                Pattern pattern= Pattern.compile("(\\d+)～(\\d+)");
                                Matcher m = pattern.matcher(pages);
                                if(m.find())
                                    start= Integer.parseInt(m.group(1));
                                else
                                    start= Integer.parseInt(pages);

                            }
                            if(i==mulu.size()-1){
                                Pattern pattern= Pattern.compile("(\\d+)～(\\d+)");
                                Matcher m = pattern.matcher(pages);
                                if(m.find())
                                    end= Integer.parseInt(m.group(2));
                                else
                                    end= Integer.parseInt(pages);
                            }
                        }
                        if(!rowcontent.contains(line)&&started) {
                            started=false;
                            row=mulu.get(i-1);
                            pages=row.getCells().get(5).getContent();
                            if (pages.length()>0){
                                Pattern pattern= Pattern.compile("(\\d+)～(\\d+)");
                                Matcher m = pattern.matcher(pages);
                                if(m.find())
                                    end= Integer.parseInt(m.group(2));
                                else
                                    end= Integer.parseInt(pages);
                            }
                        }
                    }
                    System.out.println(line+"from"+start+"to"+end);

                    try {
                        PDFTable.MergePDFs(folderpath,line,start,end,pagecount);

                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }


                JOptionPane.showMessageDialog(panelMain, "拆分成功");

            }
        });
    }

}
