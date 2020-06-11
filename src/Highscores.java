//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Scanner;
//
//public class Highscores {
//    private Text textSet;
//
//    private ArrayList<MappedScore> highscores = new ArrayList<MappedScore>();
//    private HashMap<Integer, String> comments = new HashMap<Integer, String>();
//
//    private File scoresFile;
//
//    public Highscores(File scoresFile, Text textSet) {
//        this.textSet = textSet;
//        this.scoresFile = scoresFile;
//        try {
//            Scanner scanner = new Scanner(scoresFile);
//            int currentLine = 0;
//            while(scanner.hasNextLine()) {
//                String line = scanner.nextLine();
//                if (!line.startsWith("//")) {
//                    String[] splitString = line.split(":");
//                    if (splitString.length >= 2) {
//                         MappedScore mappedScore = new MappedScore(splitString[0], Integer.parseInt(splitString[1]));
//                         highscores.add(mappedScore);
//                    }
//                } else {
//                    comments.put(currentLine, line);
//                }
//                currentLine++;
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void saveScores() {
//        try {
//            int currentLine = 0;
//            if (scoresFile.exists())
//                scoresFile.delete();
//            scoresFile.createNewFile();
//
//            PrintWriter printWriter = new PrintWriter(scoresFile);
//
//            if (comments.containsKey(currentLine))
//                printWriter.println(comments.get(currentLine));
//            currentLine++;
//
//            for (int i = 0; i < highscores.size(); i++) {
//                if (comments.containsKey(currentLine))
//                    printWriter.println(comments.get(currentLine));
//
//                MappedScore score = highscores.get(i);
//                printWriter.println(score.name + ":" + score.score);
//                currentLine++;
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public ArrayList<MappedScore> getHighScores() {
//        return highscores;
//    }
//
//    class MappedScore {
//        public String name;
//        public int score;
//
//        public MappedScore(String name, int score) {
//            this.name = name;
//            this.score = score;
//        }
//    }
//
//}

