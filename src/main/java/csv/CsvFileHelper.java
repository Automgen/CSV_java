package csv;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class CsvFileHelper {

    /**
     * @param fileName :String le nom du fichier
     * @return Le chemin absolu du fichier
     */
    public static String getResourcePath(String fileName) {
        final File f = new File("");
        return f.getAbsolutePath() + File.separator + fileName;
    }

    /**
     * @param fileName :String le nom du fichier
     * @return la ressource File correspondant au nom du fichier
     */
    public static File getResource(String fileName) {
        final String completeFileName = getResourcePath(fileName);
        return new File(completeFileName);
    }

    public static List<String> readFile(File file) {
        List<String> result = new ArrayList<>();

        try {
            result = Files.readAllLines(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

}
