public class Section {

    private String name;
    private double gpa;
    private int secCount;
    private double SD;

    Section(String name, double gpa, int secCount, double SD) {
        this.name = name;
        this.gpa = gpa;
        this.secCount = secCount;
        this.SD = SD;
    }

    double getGpa() {
        return gpa;
    }

    int getSecCount() {
        return secCount;
    }

    double getSD() {
        return SD;
    }

    @Override
    public String toString() {
        String output = "";
        output += String.format("%-" + (Main.maxLength + 2) + "s", name + ": ");
        output += String.format("\"%.2f\"", gpa);
        output += String.format("%5s", "\"" + secCount + "\"");
        output += String.format(" \"%.2f\"", SD);
        output += String.format(" \"%.18f\"", SD / Math.sqrt(secCount));

        return output;
    }

}