package parser;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class RevisarArbol {

    public static void correctSyntaxTree(String filePath) {
        try {
            List<String> lines = readFile(filePath);
            List<String> processedLines = processLines(lines);
            writeFile(filePath, processedLines);

        } catch (IOException e) {
            System.err.println("Error al procesar el archivo: " + e.getMessage());
        }
    }

    private static List<String> readFile(String filePath) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    private static List<String> processLines(List<String> lines) {
        List<String> processedLines = new ArrayList<>();
        int lastRightBraceIndex = -1;

        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("DERECHALLAVE")) {
                lastRightBraceIndex = i;
            }
        }

        if (lastRightBraceIndex == -1) {
            return lines;
        }

        for (int i = 0; i <= lastRightBraceIndex; i++) {
            processedLines.add(lines.get(i));
        }

        if (lastRightBraceIndex < lines.size() - 1) {
        }

        return processedLines;
    }

    private static void writeFile(String filePath, List<String> lines) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }
}
