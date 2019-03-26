import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Main {

    private static ArrayList<Section> sections = new ArrayList<>();
    static int maxLength = 0;
    private static int course;
    private static String dept;

    public static void main(String[] args) throws IOException {

        Scanner scanner = new Scanner(System.in);

        System.out.print("Dept code: ");

        dept = scanner.next().toUpperCase();

        System.out.print("Course code: ");

        course = scanner.nextInt();

        System.out.print("Sort by gpa(1), # of sections(2), standard deviation(3), standard error(4): ");

        int sortBy = scanner.nextInt();

        JSONArray array = fetchHtml2String();

        ArrayList<String> instructorNames = new ArrayList<>();
        ArrayList<Double> sectionGPAs = new ArrayList<>();

        for (Object o : array) {

            JSONObject section = (JSONObject) o;

            instructorNames.add(section.getString("instructor"));
            sectionGPAs.add(Double.valueOf(section.getString("section gpa")));

        }

        Set<String> namesSet = new HashSet<>(instructorNames);
        int secCount = 0;
        double sum = 0;
        for (String name : namesSet) {

            ArrayList<Double> GPAs = new ArrayList<>();

            for (int i = 0; i < sectionGPAs.size(); i++) {
                if (instructorNames.get(i).equals(name)) {
                    maxLength = Math.max(maxLength, instructorNames.get(i).length());
                    sum += sectionGPAs.get(i);
                    GPAs.add(sectionGPAs.get(i));
                    secCount++;
                }
            }

            sections.add(new Section(name, sum / secCount, secCount, calculateSD(GPAs)));

            secCount = 0;
            sum = 0;

        }


        if (!(sortBy == 1 || sortBy == 2 || sortBy == 3 || sortBy == 4)) {
            System.out.println("Invalid sorting criteria. Sorting by gpa.");
        }

        sections.sort((o1, o2) -> {

            switch (sortBy) {

                default:
                case 1:
                    double gpa1 = o1.getGpa();
                    double gpa2 = o2.getGpa();
                    return Double.compare(gpa2, gpa1);

                case 2:
                    int secCount1 = o1.getSecCount();
                    int secCount2 = o2.getSecCount();
                    return Integer.compare(secCount2, secCount1);

                case 3:
                    double SD1 = o1.getSD();
                    double SD2 = o2.getSD();
                    return Double.compare(SD1, SD2);

                case 4:
                    SD1 = o1.getSD();
                    SD2 = o2.getSD();
                    secCount1 = o1.getSecCount();
                    secCount2 = o2.getSecCount();

                    double se1 = SD1 / Math.sqrt(secCount1);
                    double se2 = SD2 / Math.sqrt(secCount2);

                    return Double.compare(se1, se2);

            }

        });

        double courseGPASum = 0;
        int totalSecCount = 0;

        for (Section section : sections) {
            courseGPASum += section.getGpa() * section.getSecCount();
            totalSecCount += section.getSecCount();
        }

        double courseMean = courseGPASum / totalSecCount;

        System.out.println("Course average: " + courseMean);

        System.out.printf("%-" + (Main.maxLength + 7) + "s", "Instructor Name");
        System.out.printf("%5s", "GPA");
        System.out.printf("%6s", "#");
        System.out.printf("%6s", "SD");
        System.out.printf("%16s\n", "SE");

        boolean infoPrinted = false;
        for (int i = 0; i < sections.size(); i++) {
            if (sections.get(i).getGpa() < courseMean && !infoPrinted && !(sortBy == 2 || sortBy == 3 || sortBy == 4)) {
                System.out.println("Following instructors have lower averages than the course's overall average");
                infoPrinted = true;
            }
            System.out.println(String.format("%3d. ", i + 1) + sections.get(i));
        }

    }

    private static Double calculateSD(ArrayList<Double> numArray) {
        double sum = 0.0, standardDeviation = 0.0;
        int length = numArray.size();

        for (double num : numArray) {
            sum += num;
        }

        double mean = sum / length;

        for (double num : numArray) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        return Math.sqrt(standardDeviation / length);
    }

    private static JSONArray fetchHtml2String() throws IOException {

        URLConnection connection = new URL("https://stars.bilkent.edu.tr/evalreport/index.php?mode=crs&crsCode=" + dept + "&crsNum=" + course).openConnection();
        Scanner scanner = new Scanner(connection.getInputStream());
        scanner.useDelimiter("\\Z");
        String content = scanner.next();
        scanner.close();

        return table2Json(content);

    }

    private static JSONArray table2Json(String source) throws JSONException {
        Document doc = Jsoup.parse(source);
        String cname = "";
        try {
            cname = doc.select("h2").first().toString();
        } catch (NullPointerException e) {
            System.out.println("No sections found!");
            System.exit(0);
        }
        try {
            cname = cname.substring(18, cname.lastIndexOf(")") - 11);
        } catch (StringIndexOutOfBoundsException e) {
            System.out.println("Malformed evaluations page! See " + "https://stars.bilkent.edu.tr/evalreport/index.php?mode=crs&crsCode=" + dept + "&crsNum=" + course);
            System.exit(0);
        }
        System.out.println(cname);
        JSONArray array = new JSONArray();
        boolean firstRow = true;
        for (Element table : doc.select("table")) {
            for (Element row : table.select("tr")) {
                if (firstRow) {
                    firstRow = false;
                    continue;
                }
                JSONObject jsonObject = new JSONObject();
                Elements tds = row.select("td");
                String gpa = tds.get(4).text();
                String name = tds.get(6).text();

                jsonObject.put("instructor", name);
                jsonObject.put("section gpa", gpa);
                array.put(jsonObject);
            }
        }
        return array;
    }

}
