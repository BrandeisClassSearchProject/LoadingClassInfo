

import java.io.*;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class parses all class information from the Brandeis class search website to a txt file
 * The txt will be used as an asset in both IOS and Android version of the BrandeisClassSearch app
 */
public class LoadingAll {

    private static final String time=    "        TIMES: ";
    private static final String block=   "        BLOCK: ";
    private static final String books=   "        BOOKS: ";
    private static final String syllabus="     SYLLABUS: ";
    private static final String desc=    "  DESCRIPTION: ";//11
    private static final String teacher= "      TEACHER: ";
    private static final String name=    "         NAME: ";
    

    private static int[] terms=new int[]{1171,1163,1162,1161,1152,1151,1153} ;
    private static boolean doUpdate = true;

    public static int countLine=0;
    public static FileOutputStream outputStream;


    public static void main(String [ ] args)  {
        File f=null;
        try {
            f = new File("C:/Users/rozoa/Desktop/DATA.txt");
            if (!f.exists()||doUpdate) {
                outputStream =new FileOutputStream( f);
                f.createNewFile();
                for(int i:terms){
                    countLine=0;
                    loadAll(i);
                }
                outputStream.flush();
                outputStream.close();
            }

        } catch (Exception e) {
            System.out.println("error");
        }

        long startTime = System.currentTimeMillis();
        try {
            InputStreamReader inputStream = new InputStreamReader(new FileInputStream(f));
            BufferedReader br = new BufferedReader(inputStream);
            String temp="";
            while((temp=br.readLine())!=null){
                System.out.println(temp);
            }

        } catch (Exception e) {
            System.out.println("error 2");
        }

        System.out.println("Generating takes "+(System.currentTimeMillis() - startTime)/1000.0+"s");
    }

    private static void loadAll(int term) throws IOException {

        URL url;
        InputStream is;
        BufferedReader br;
        String line;
        boolean isStart;
        boolean isDone;
        //boolean isHead;

        int number=0;
        int temp=0;
        int counter=0;
        String days="";
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date();
        outputStream.write((dateFormat.format(date)+" "+getTerm(term)+"\n").getBytes());
        for(int i=1;i<=20;i++) {
            try {
                url = new URL("http://registrar-prod.unet.brandeis.edu/registrar/schedule/search?strm="+String.valueOf(term)+"&view=all&status=&time=time&day=mon&day=tues&day=wed&day=thurs&day=fri&start_time=07%3A00%3A00&end_time=22%3A30%3A00&block=&keywords=&order=class&search=Search&subsequent=1&page="+String.valueOf(i));

                //System.out.println("http://registrar-prod.unet.brandeis.edu/registrar/schedule/search?strm=1171&view=all&status=&time=time&day=mon&day=tues&day=wed&day=thurs&day=fri&start_time=07%3A00%3A00&end_time=22%3A30%3A00&block=&keywords=&order=class&search=Search&subsequent=1&page="+String.valueOf(i)+"\n*****");
                is = url.openStream();  // throws an IOException
                br = new BufferedReader(new InputStreamReader(is));
                isStart=false;
                isDone=false;


                while ((line = br.readLine()) != null &&(!isDone)) {
                    counter++;
                    if(!line.isEmpty()) {
                        if ((!isStart) && line.equals("\t/ Wait")) {
                            isStart = true;
                        }//start processing

                        if (isStart) {
                            line = line.trim();
                            //System.out.println(line);
                            if (isDaysString(line)) {
                                temp = counter;
                                days = line;
                            }//get the day

                            if (counter - temp == 4) {
                                printTimes(days, line);
                            }//get time

                            if (line.equals("<div class=\"paging\">")) {// the end
                                isDone = true;
                            } else {

                                if (line.length() > 10 && line.substring(0, 10).equals("Block&nbsp")) {
                                    outputStream.write((block + line.substring(11)+"\n").getBytes());
                                    //System.out.println("   BLOCK: " + line.substring(11));
                                    countLine++;
                                    //find out the block
                                } else {
                                    number = isTitle(line, number);
                                    doSpecificSearch(line);
                                }
                            }
                        }
                    }

                }



            } catch (IOException e) {
                i--;
                e.printStackTrace();
            }
        }
        for (int h = 13 - countLine; h > 0; h--) {
            outputStream.write(".\n".getBytes());
        }//




    }

    private static String getTerm(int term) {
        String year = String.valueOf((term-1000)/10);
        String semester=null;
        switch (term%10){
            case 1:
                semester="Spring";
                break;
            case 2:
                semester="Summer";
                break;
            case 3:
                semester="Fall";
                break;
            default:
                System.out.println(String.valueOf(term)+" is Wrong");
                break;
        }
        return year+" "+semester;
    }

    private static int isTitle(String line,int n) throws IOException {
        if(line.length()>=21) {
            if (line.substring(0,21).equals("<a class=\"def\" name=\"")) {
                //String[] a = line.substring(21,line.length()).split("\"");
                String classNumb = line.split("\"")[3];
                String[] classNumbID = classNumb.split(" ");

                n++;
                if(countLine!=0) {
                    for (int i = 13 - countLine; i > 0; i--) {
                        outputStream.write(".\n".getBytes());
                    }
                }
                countLine=1;
                try {
                    System.out.println((String.valueOf(n)+" "+classNumb+"\n"));
                    outputStream.write((classNumb+"\n").getBytes());
                    outputStream.write((books+"http://www.bkstr.com/webapp/wcs/stores/servlet/booklookServlet?bookstore_id-1=1391&term_id-1=1171&div-1=&dept-1="
                            +classNumbID[0]+"&course-1="+classNumbID[classNumbID.length-1]+"&sect-1=1\n").getBytes());

                }catch (Exception e){
                    System.out.println("isTitle failed"+line+" "+String.valueOf(n));
                    e.printStackTrace();
                }

                //System.out.println(String.valueOf(n));
                //System.out.println("Title:  "+classNumb);
                //System.out.println("   BOOKS: "+"http://www.bkstr.com/webapp/wcs/stores/servlet/booklookServlet?bookstore_id-1=1391&term_id-1=1171&div-1=&dept-1="+classNumbID[0]+"&course-1="+classNumbID[1]+"&sect-1=1");
                //countLine++;
            }
        }

        return n;
    }

    private static boolean isDaysString(String line){
        String[] ta= line.split(",");
        for(String t:ta){
            if(!(t.equals("M")||t.equals("W")||t.equals("Th")||t.equals("T")||t.equals("F"))) return false;
        }
        return true;

    }

    private static boolean doSpecificSearch(String line) throws IOException {


        if(line.length()>13){
            if(line.substring(0,7).equals("<a href")){//found the teacher page
                if(line.substring(line.length()-12).equals("Syllabus</a>")){
                    //System.out.println("   SYLLABUS: "+line.split("\"")[1]);
                    outputStream.write((syllabus+line.split("\"")[1]+"\n").getBytes());
                    countLine++;
                }else{
                    //System.out.println("   TEACHER: "+line.split("\"")[1]);
                    outputStream.write((teacher+line.split("\"")[1]+"\n").getBytes());
                    countLine++;
                }
                return false;
            }
        }else {
            return false;
        }

        if(line.length()>16){
            if(line.startsWith("<strong>")){
                String className=line.replace("<strong>","");
                className=className.replace("</strong>","");
                outputStream.write((name+className+"\n").getBytes());
                countLine++;
                return false;
            }
        }

        if(line.length()>22){
            if(line.substring(0,23).equals("href=\"javascript:popUp(")){//found the description page
                //System.out.println("   DESCRIPTION: "+"http://registrar-prod.unet.brandeis.edu/registrar/schedule/"+line.split("'")[1]);
                outputStream.write((desc+"http://registrar-prod.unet.brandeis.edu/registrar/schedule/"+line.split("'")[1]+"\n").getBytes());
                countLine++;
                return false;
            }
        }

        if(line.length()>24){
            if(line.substring(0,24).equals("<a target=\"_blank\" href=")){//found the book page
                //System.out.println("   BOOK: "+line.split("'")[1]);
                outputStream.write((books+line.split("'")[1]+"\n").getBytes());
                countLine++;
                return true;
            }
        }




        //

        return false;
    }

    private static void printTimes(String days, String line) {
        try {
            countLine++;
            String[] s = line.split("&ndash;");
            outputStream.write((time + days + "  ").getBytes());
            //System.out.print("   TIMES: " + days + "  ");
            outputStream.write((s[0] + " - ").getBytes());
            //System.out.print(s[0] + " - ");
            outputStream.write((s[1]+"\n").getBytes());
            //System.out.println(s[1]);
        }catch(Exception e){
            System.out.println("printTimes error"+days+" "+line);
            e.printStackTrace();
        }


    }

}
