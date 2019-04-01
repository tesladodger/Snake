package com.tesladodger.snake.managers;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;


public final class FileManager {
    private int hs;
    private int userDelay;
    private int aiDelay;
    private int fontSize;
    private boolean showFPS;


    public void setup() {
        boolean fileExists = new File("snake.conf").isFile();
        if (!fileExists) {
            try {
                @SuppressWarnings("CharsetObjectCanBeUsed")
                PrintWriter pr = new PrintWriter("snake.conf", "UTF-8");

                pr.println("high-score = 0");
                pr.println("user-delay = 100");
                pr.println("ai-delay = 100");
                pr.println("font-size = 12");
                pr.println("show-fps = false");

                pr.close();
            }
            catch (IOException e) {
                System.err.println("IOException: " + e.getMessage());
            }

            // Set the variables.
            hs = 0; userDelay = 100; aiDelay = 100; fontSize = 12; showFPS = false;

        }
        else {
            try {
                Scanner fs = new Scanner(new File("snake.conf"));

                String[] line = fs.nextLine().split(" = ");
                hs = Integer.parseInt(line[1]);

                line = fs.nextLine().split(" = ");
                userDelay = Integer.parseInt(line[1]);

                line = fs.nextLine().split(" = ");
                aiDelay = Integer.parseInt(line[1]);

                line = fs.nextLine().split(" = ");
                fontSize = Integer.parseInt(line[1]);

                line = fs.nextLine().split(" = ");
                showFPS = Boolean.parseBoolean(line[1]);

                fs.close();
            }
            catch (IOException e) {
                System.err.println("IOException: " + e.getMessage());
            }
            catch (NumberFormatException e) {
                System.err.println("NumberFormatException: " + e.getMessage());
            }
        }
    }


    public int getHs() {
        return hs;
    }

    public int getUserDelay() {
        return userDelay;
    }

    public int getAiDelay() {
        return aiDelay;
    }

    public int getFontSize() {
        return fontSize;
    }

    public boolean getShowFPS() {
        return showFPS;
    }


    public void update(int newHS) {
        hs = newHS;
        try {
            @SuppressWarnings("CharsetObjectCanBeUsed")
            PrintWriter pr = new PrintWriter("snake.conf", "UTF-8");

            pr.println("high-score = " + hs);
            pr.println("user-delay = " + userDelay);
            pr.println("ai-delay = " + aiDelay);
            pr.println("font-size = " + fontSize);
            pr.println("show-fps = " + showFPS);

            pr.close();
        }
        catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        }
    }


}
