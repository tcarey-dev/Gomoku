package learn.gomoku.ui;

import learn.gomoku.App;
import learn.gomoku.game.Gomoku;
import learn.gomoku.game.Result;
import learn.gomoku.game.Stone;
import learn.gomoku.players.HumanPlayer;
import learn.gomoku.players.Player;
import learn.gomoku.players.RandomPlayer;
import learn.gomoku.players.SmartNPC;

import java.util.List;
import java.util.Scanner;

public class GameController {
    Scanner console = new Scanner(System.in);
    char[][] board;
    Gomoku game;

    public void run() {
        setup();
        play();
        boolean answer = playAgain();

        if (answer) {
            run();
        } else {
            System.out.println("Goodbye!");
        }
    }

    private void setup () {
        board = new char[Gomoku.WIDTH][Gomoku.WIDTH];

        System.out.println();
        System.out.println("Welcome to Gomoku");
        System.out.println("=".repeat("Welcome to Gomoku".length()));

        Player player1 = getPlayer(1);
        Player player2 = getPlayer(2);

        game = new Gomoku(player1, player2);
        System.out.println("\n(Randomizing)");
        System.out.printf("%n%s goes first.%n", game.getCurrent().getName());

    }

    private Player getPlayer(int playerNumber) {

        Player player;

        boolean isValid = false;
        do {
            System.out.println();
            System.out.printf("Player %s is:%n", playerNumber);
            System.out.println("1. Human");
            System.out.println("2. Random Player [Easy]");
            System.out.println("3. Random Player [Hard]");
            int choice = readInt("Select [1-3]: ", 1, 3);

            switch (choice) {
                case 1:
                    String message = String.format("\nPlayer %s, enter your name: ", playerNumber);
                    String name = readRequiredString(message);
                    player = new HumanPlayer(name);
                    isValid = true;
                    break;
                case 2:
                    player = new RandomPlayer();
                    isValid = true;
                    break;
                case 3:
                    player = new SmartNPC();
                    isValid = true;
                    break;
                default:
                    player = new RandomPlayer(); //this should never happen
                    System.out.println("Something went terribly, terribly wrong. Try again.");
            }
        } while (!isValid);

        return player;
    }

    private void play() {
        Result result;

        do {
            System.out.println();
            System.out.printf("%s's Turn%n", game.getCurrent().getName());

            int row;
            int col;
            Stone stone;

            if (game.getCurrent().getClass() == HumanPlayer.class){
                row = readInt("Enter a row: ", 0, 15);
                col = readInt("Enter a column: ", 0, 15);
                stone = new Stone(row -1, col -1, game.isBlacksTurn());
            } else {
                stone = game.getCurrent().generateMove(game.getStones());
                System.out.printf("Enter a row: %s", stone.getRow() +1);
                System.out.println();
                System.out.printf("Enter a column: %s", stone.getColumn() +1);
                System.out.println();
            }

            result = game.place(stone);

            printBoard();
        } while (!game.isOver());

        System.out.println();
        System.out.println(result.getMessage());

    }

    private void printBoard() {

        List<Stone> stones = game.getStones();

        for (Stone stone : stones) {
            int x = stone.getRow();
            int y = stone.getColumn();
            boolean black = stone.isBlack();

            if(black) {
                board[x][y] = 'B';
            } else {
                board[x][y] = 'W';
            }
        }

        // print column numbers
        System.out.print("   ");
        for (int i = 1; i < board.length +1; i++) {
            System.out.print(String.format("%02d ", i));
        }

        System.out.println();

        for (int row = 0; row < board.length; row++) {
            // print row numbers
            System.out.print(String.format("%02d", row +1));

            for (int col = 0; col < board.length; col++) {
                if (board[row][col] == 'B') {
                    System.out.print("  " + 'B');
                } else if (board[row][col] == 'W') {
                    System.out.print("  " + 'W');
                } else {
                    char emptySpot = board[row][col] = '_';
                    System.out.print("  " + emptySpot);
                }
            }
            System.out.println();
        }
    }

    private String readRequiredString(String message) {
        String result;
        String resultMinusWhitespace = "";

        do {
            System.out.print(message);
            result = console.nextLine();

            for (int i = 0; i < result.length(); i++) {
                if (!Character.isWhitespace(result.charAt(i))) {
                    resultMinusWhitespace += result.charAt(i);
                }
            }
        } while (resultMinusWhitespace.length() == 0);

        return result;
    }

    private int readInt(String message, int min, int max) {
        String input;
        String numbers = "0123456789";
        int result = -1;

        boolean isValidInt;

        do {
            isValidInt = true;
            input = readRequiredString(message);

            for (int i =0; i < input.length(); i++) {
                if (!numbers.contains(input.substring(i, i+1))) {
                    System.out.println("\nError: only positive integers allowed. Please try again.");
                    isValidInt = false;
                    break;
                }
            }

            if (isValidInt) {
                result = Integer.parseInt(input);

                if (result < min || result > max) {
                    System.out.printf("%nError: choice must be between %s and %s. Please try again.%n", min, max);
                    isValidInt = false;
                }
            }

        } while (!isValidInt);

        return result;
    }

    private boolean playAgain() {
        System.out.println();
        String answer = readRequiredString("Play Again? [y/n]: ");
        System.out.println();

        return answer.equalsIgnoreCase("y");
    }
}
