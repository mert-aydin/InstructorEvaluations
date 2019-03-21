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

    public static void main(String[] args) throws IOException {

        Scanner scanner = new Scanner(System.in);

        System.out.print("Dept code: ");

        String dept = scanner.next().toUpperCase();

        System.out.print("Course code: ");

        int course = scanner.nextInt();

        JSONArray array = fetchHtml2String(dept, course);

        ArrayList<String> instructorNames = new ArrayList<>();
        ArrayList<Double> sectionGPAs = new ArrayList<>();
        ArrayList<String> combined = new ArrayList<>();

        for (Object o : array) {

            JSONObject section = (JSONObject) o;

            instructorNames.add(section.getString("instructor"));
            sectionGPAs.add(Double.valueOf(section.getString("section gpa")));

        }

        Set<String> namesSet = new HashSet<>(instructorNames);
        int secCount = 0;
        double sum = 0;
        int maxLength = 0;
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

            combined.add(name + ": % " + " ! " + String.format("%.16f", sum / secCount) + " ! " + " ?" + secCount + "? " + "#" + calculateSD(GPAs));
            secCount = 0;
            sum = 0;

        }

        combined.sort((o1, o2) -> {

            String a = o1.substring(o1.indexOf("!") + 2, o1.lastIndexOf("!") - 1);
            String b = o2.substring(o2.indexOf("!") + 2, o2.lastIndexOf("!") - 1);
            return Double.compare(Double.parseDouble(b), Double.parseDouble(a));

        });

        int i = 1;
        for (String s : combined) {
            System.out.printf("%5s", i++ + ". ");
            System.out.printf("%-" + (maxLength + 2) + "s", s.substring(0, s.indexOf("%")));
            System.out.printf("\"%4.2f\" ", Double.parseDouble(s.substring(s.indexOf("!") + 1, s.lastIndexOf("!"))));
            System.out.printf("%5s", "\"" + s.substring(s.indexOf("?") + 1, s.lastIndexOf("?")) + "\" ");
            System.out.printf("\"%4.2f\"\n", Double.parseDouble(s.substring(s.indexOf("#") + 1)));
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

    private static JSONArray fetchHtml2String(String dept, int course) throws IOException {

        URLConnection connection = new URL("https://stars.bilkent.edu.tr/evalreport/index.php?mode=crs&crsCode=" + dept + "&crsNum=" + course).openConnection();
        Scanner scanner = new Scanner(connection.getInputStream());
        scanner.useDelimiter("\\Z");
        String content = scanner.next();
        scanner.close();

        return table2Json(content);

    }

    private static JSONArray table2Json(String source) throws JSONException {
        Document doc = Jsoup.parse(source);
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
