import com.giaybac.traprange.PDFTableExtractor;
import com.giaybac.traprange.entity.Table;
import com.giaybac.traprange.entity.TableRow;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PDFTable {

    public static void main(String[] args) {
        PDFTableExtractor extractor = new PDFTableExtractor();
        String filepath="D:\\tmp\\all.pdf";
        String folderpath="D:\\tmp\\test\\";

        List<Table> tables = extractor.setSource(filepath).addPages(4)
            //    .addPage(0).addPage(1).addPage(2).addPage(3)
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
            lines = rf.readLines(filename);
        }
        catch(IOException e)
        {
            System.out.println("Unable to create "+filename+": "+e.getMessage());
        }

        try {
            SplitPages(folderpath,filepath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        HashMap<String,String> map=new HashMap<String, String>();
        int start=0;
        int end=0;
        String rowcontent=null;

      for(String line:lines){
          //System.out.println("begin deal="+line);
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
              MergePDFs(folderpath,line,start,end,4);


          } catch (IOException e) {
              e.printStackTrace();
          }
      }
        // String html = tables.get(0).toHtml();//table in html format
      //  System.out.println(html);
       // String csv = tables.get(0).toString();//table in csv format using semicolon as a delimiter
    }

    public static void SplitPages(String folderpath, String path) throws IOException {
        File file = new File(path);
        PDDocument document = PDDocument.load(file);
        Splitter splitter = new Splitter();
        List<PDDocument> Pages = splitter.split(document);
        Iterator<PDDocument> iterator = Pages.listIterator();
        int i = 1;
        while(iterator.hasNext()) {
            PDDocument pd = iterator.next();
            pd.save(folderpath+"sample"+ i++ +".pdf");
        }
        System.out.println("Multiple PDF’s created");
        document.close();
    }

    public static void MergePDFs (String folderpath, String filename, int start, int end, int offset) throws IOException {
        PDFMergerUtility PDFmerger = new PDFMergerUtility();
        PDFmerger.setDestinationFileName(folderpath+filename+".pdf");
     //   List<PDDocument> files=new ArrayList<PDDocument>();
        for(int i=start+offset;i<=end+offset;i++){
            File file = new File(folderpath+"sample"+i+".pdf");
            PDDocument doc = PDDocument.load(file);
            //files.add(doc);
            PDFmerger.addSource(file);
        }
      /*  PDDocument doc =null;
        for(int i=0;i<files.size();i++){
            doc=files.get(i);
            PDFmerger.addSource(file);
        }*/
        PDFmerger.mergeDocuments();
      /*  for(int i=0;i<files.size();i++){
            doc=files.get(i);
            doc.close();
        }*/
    }


}