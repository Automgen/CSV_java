package csv;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CsvFile {

    public final static char DEFAULT_SEPARATOR = ',';

    public final static List<Character> AVAILABLE_SEPARATORS = Arrays.asList(',', ';', '\t', '|', ':');

    private boolean autoDetectSeparatorMode;
    private int numberOfLineForAutoDetectSeparator = 5;
    private char separator = DEFAULT_SEPARATOR;

    private File file;
    private List<String> lines;
    private List<String> cleanedLines;
    private List<String[]> data;
    private String[] titles;
    private List<Map<String, String>> mappedData;


    // Constructeurs

    public CsvFile(File file) {
        this(file, DEFAULT_SEPARATOR);
    }

    public CsvFile(File file, char separator) {
        this(file, separator, false);
    }

    public CsvFile(File file, char separator, boolean autoDetectSeparatorMode) {
        if(file == null) {
            throw new IllegalArgumentException("Le fichier ne peut pas être null");
        }
        this.file = file;

        if(!autoDetectSeparatorMode) {
            if(!this.isValidSeparator(separator)) {
                throw new IllegalArgumentException("Le séparateur spécifié n'est pas pris en charge.");
            }
            this.separator = separator;
        }
        this.autoDetectSeparatorMode = autoDetectSeparatorMode;

        this.init();
    }


    // Getters & Setters

    public File getFile() {
        return this.file;
    }

    public List<String[]> getData() {
        return this.data;
    }

    public String[] getTitles() {
        return this.titles;
    }

    public List<Map<String, String>> getMappedData() {
        return this.mappedData;
    }


    // Méthodes & Fonctions

    public Character selectBestSeparator() {
        if(this.lines.size() == 0) {
            throw new ArrayIndexOutOfBoundsException("Le fichier n'a pas de ligne");
        }

        // Ajustement du nombre de lignes dispo
        if(this.cleanedLines.size() < this.numberOfLineForAutoDetectSeparator) {
            this.numberOfLineForAutoDetectSeparator = this.cleanedLines.size();
        }

        List<Character> reste = new ArrayList<>();

        for(Character separator : AVAILABLE_SEPARATORS) {
            int previous = 0;
            boolean isGoodCandidate = false;
            for(int i = 0; i < this.numberOfLineForAutoDetectSeparator; i++) {
                int compte = compterSeparateurs(this.cleanedLines.get(i), separator);
                if(compte == 0) {
                    // pas de séparateur dans cette ligne
                    isGoodCandidate = false;
                    break;
                }
                if(compte != previous && previous != 0) {
                    // pas le même nombre de séparateur que la ligne précédente
                    isGoodCandidate = false;
                    break;
                }

                previous = compte;
                isGoodCandidate = true;
            }
            if(isGoodCandidate) {
                reste.add(separator);
            }
        }

        if(reste.isEmpty()) {
            throw new IllegalStateException("Pas de candidat trouvé");
        }

        if(1 < reste.size()) {
            throw new IllegalStateException("Trop de candidats trouvés");
        }

        return reste.get(0);
    }

    public int compterSeparateurs(String line, char separator) {
        int number = 0;

        int pos = line.indexOf(separator);
        while (pos != -1) {
            number++;
            line = line.substring(pos+1);
            pos = line.indexOf(separator);
        }
        return number;
    }

    private boolean isValidSeparator(char separator) {
        return AVAILABLE_SEPARATORS.contains(separator);
    }

    private void cleanLines() {
        this.cleanedLines = new ArrayList<>();
        for(String line : this.lines) {
            // Suppression des espaces de fin de ligne
            line = line.trim();

            // On saute les lignes vides
            if(line.length() == 0) {
                continue;
            }

            // On saute les lignes de commentraire
            if(line.startsWith("#")) {
                continue;
            }

            cleanedLines.add(line);
        }
    }

    private String[] listToArray(List<String> liste) {
        String[] oneData = new String[liste.size()];
        for(int i = 0; i < oneData.length; i++) {
            oneData[i] = liste.get(i);
        }
        return oneData;
    }

    private void init() {
        this.lines = CsvFileHelper.readFile(this.file);

        this.cleanLines();

        if(this.autoDetectSeparatorMode) {
            this.separator = selectBestSeparator();
        }

        this.data = new ArrayList<>(this.cleanedLines.size());
        final String regex = "(^|(?<="+this.separator+"))([^\""+this.separator+"])*((?="+this.separator+")|$)|((?<=^\")|(?<="+this.separator+"\"))([^\"]|\"\")*((?=\""+this.separator+")|(?=\"$))";
        Pattern p = Pattern.compile(regex);
        boolean first = true;
        for(String line : this.cleanedLines) {
            Matcher m = p.matcher(line);

            List<String> temp = new ArrayList<>();
            while (m.find()) {
                temp.add(m.group());
            }

            String[] oneData = listToArray(temp);
            if(first) {
                titles = oneData;
                first = false;
            } else {
                data.add(oneData);
            }
        }

        // On map les lignes trouvées
        this.mapData();
    }

    private void mapData() {
        this.mappedData = new ArrayList<>(this.data.size());

        final int titlesLength = titles.length;

        for(String[] oneData : this.data) {
            final Map<String, String> map = new HashMap<>();
            for(int i = 0; i < titlesLength; i++) {
                final String key = this.titles[i];
                final String value = oneData[i];
                map.put(key, value);
            }

            this.mappedData.add(map);
        }
    }

}
