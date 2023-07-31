package learn.gomoku.players;

import learn.gomoku.game.Gomoku;
import learn.gomoku.game.Stone;

import java.util.List;
import java.util.Random;

public class SmartNPC extends RandomPlayer {

    private static String[] titles = {"Dr.", "Professor", "Chief Exec", "Specialist", "The Honorable",
            "Prince", "Princess", "The Venerable", "The Eminent"};
    private static String[] names = {
            "Evelyn", "Wyatan", "Jud", "Danella", "Sarah", "Johnna",
            "Vicki", "Alano", "Trever", "Delphine", "Sigismundo",
            "Shermie", "Filide", "Daniella", "Annmarie", "Bartram",
            "Pennie", "Rafael", "Celine", "Kacey", "Saree", "Tu",
            "Erny", "Evonne", "Charita", "Anny", "Mavra", "Fredek",
            "Silvio", "Cam", "Hulda", "Nanice", "Iolanthe", "Brucie",
            "Kara", "Paco"};
    private static String[] lastNames = {"Itch", "Potato", "Mushroom", "Grape", "Mouse", "Feet",
            "Nerves", "Sweat", "Sweet", "Bug", "Piles", "Trumpet", "Shark", "Grouper", "Flutes", "Showers",
            "Humbug", "Cauliflower", "Shoes", "Hopeless", "Zombie", "Monster", "Fuzzy"};
    private final String[][] board = new String[Gomoku.WIDTH][Gomoku.WIDTH];
    private final Random random = new Random();

    public SmartNPC() {
    }

    @Override
    public Stone generateMove(List<Stone> previousMoves) {

        int row = 0;
        int column = 0;
        boolean isBlack = true;
        String direction;
        Stone result;

        if (previousMoves != null && !previousMoves.isEmpty()) {
            Stone lastMove = previousMoves.get(previousMoves.size() - 1);
            isBlack = !lastMove.isBlack();

            // get game state
            for (Stone stone : previousMoves) {
                int x = stone.getRow();
                int y = stone.getColumn();
                boolean black = stone.isBlack();

                if(black) {
                    board[x][y] = "B";
                } else {
                    board[x][y] = "W";
                }
            }
        }

        // Opening move
        if (board[6][6] != "B" && board[6][6] != "W") {
            return new Stone(6, 6, isBlack);
        } else if (board[5][6] != "B" && board[5][6] != "W") {
            return new Stone(5, 6, isBlack);
        }

        // Defense: block opponent runs of 3 or above
        for (int i = 4; i > 2; i--) {
            result = defensiveMove(previousMoves, isBlack, i);
            if (result != null) {
                return result;
            }
        }

        // Defense: prevent scenarios where villain creates a gap to be filled in such as 1 1 _ 1
        result = blockGaps(previousMoves, isBlack);
        if (result != null) {
            return result;
        }

        // Offense: add to longest runs first
        for (int i = 4; i > 1; i--) {
            result = offensiveMove(previousMoves, isBlack, i);
            if (result != null) {
                return result;
            }
        }

        // Offense: add to adjacent stone
        for (int r = 0; r < board.length; r++) {
            for (int c = 0; c < board.length; c++) {
                if (isBlack && board[r][c] == "B"
                        && r+1 < 15 && r-1 > 0
                        && c+1 < 15 && c-1 >0 ) {
                    int[] opening = getAdjacentOpening(board, r, c);
                    row = opening[0];
                    column = opening[1];
                } else if (!isBlack && board[r][c] == "W"
                        && r+1 < 15 && r-1 > 0
                        && c+1 < 15 && c-1 >0 ) {
                    int[] opening = getAdjacentOpening(board, r, c);
                    row = opening[0];
                    column = opening[1];
                }
            }
        }
        return new Stone(row, column, isBlack);
    }

    private Stone defensiveMove(List<Stone> previousMoves, boolean isBlack, int howMany) {
        String direction;
        if (previousMoves != null && !previousMoves.isEmpty()) {
            for (Stone stone : previousMoves) {
                if (isSequential(stone, howMany) && (stone.isBlack() == !isBlack)) {
                    direction = determineDirection(stone, howMany);
                    if (direction != null) {
                        Stone result = checkUpperAndLower(board, stone, isBlack, direction);
                        if (result != null) {
                            return result;
                        }
                    }
                }
            }
        }
        return null;
    }

    private Stone offensiveMove(List<Stone> previousMoves, boolean isBlack, int howMany) {
        String direction;
        if (previousMoves != null && !previousMoves.isEmpty()) {
            for (Stone stone : previousMoves) {
                if (isSequential(stone, howMany) && (stone.isBlack() == isBlack)) {
                    direction = determineDirection(stone, howMany);
                    if (direction != null) {
                        Stone result = checkUpperAndLower(board, stone, isBlack, direction);
                        if (result != null) {
                            return result;
                        }
                    }
                }
            }
        }
        return null;
    }

    private Stone checkUpperAndLower(String[][] boardState, Stone stone, boolean isBlack, String direction) {

        Stone result = checkLowerBound(boardState, stone, isBlack, direction);
        if (result != null) {
            return result;
        }
        result = checkUpperBound(boardState, stone, isBlack, direction);
        if (result != null) {
            return result;
        }
        return null;
    }

    private Stone checkLowerBound(String[][] boardState, Stone stone, boolean isBlack, String direction) {
        boolean occupied;
        int row = stone.getRow();
        int column = stone.getColumn();
        char symbol = boardState[stone.getRow()][stone.getColumn()].charAt(0);

        switch (direction) {
            case "vertical":
                row = getStartOfColumn(stone.getRow(), stone.getColumn(), symbol);
                column = stone.getColumn();
                row -= 1;
                break;
            case "horizontal":
                row = stone.getRow();
                column = getStartOfRow(stone.getRow(), stone.getColumn(), symbol);
                column -= 1;
                break;
            case "diagonal up":
                row = getDiagonalUpStartRow(stone.getRow(), stone.getColumn(), symbol);
                column = getDiagonalUpStartColumn(stone.getRow(), stone.getColumn(), symbol);
                row += 1;
                column -= 1;
                break;
            case "diagonal down":
                row = getDiagonalDownStartRow(stone.getRow(), stone.getColumn(), symbol);
                column = getDiagonalDownStartColumn(stone.getRow(), stone.getColumn(), symbol);
                row -= 1;
                column -= 1;
                break;
        }

        occupied = isOccupied(boardState, isBlack, row, column);

        if (!occupied) {
            return new Stone(row, column, isBlack);
        }
        return null;
    }

    private Stone checkUpperBound(String[][] boardState, Stone stone, boolean isBlack, String direction) {
        boolean occupied;
        int row = stone.getRow();
        int column = stone.getColumn();
        char symbol = boardState[stone.getRow()][stone.getColumn()].charAt(0);

        switch (direction) {
            case "vertical":
                row = getEndOfColumn(stone.getRow(), stone.getColumn(), symbol);
                column = stone.getColumn();
                row += 1;
                break;
            case "horizontal":
                row = stone.getRow();
                column = getEndOfRow(stone.getRow(), stone.getColumn(), symbol);
                column += 1;
                break;
            case "diagonal up":
                row = getDiagonalUpEndRow(stone.getRow(), stone.getColumn(), symbol);
                column = getDiagonalUpEndColumn(stone.getRow(), stone.getColumn(), symbol);
                row -= 1;
                column += 1;
                break;
            case "diagonal down":
                row = getDiagonalDownEndRow(stone.getRow(), stone.getColumn(), symbol);
                column = getDiagonalDownEndColumn(stone.getRow(), stone.getColumn(), symbol);
                row += 1;
                column += 1;
                break;
        }

        occupied = isOccupied(boardState, isBlack, row, column);

        if (!occupied) {
            return new Stone(row, column, isBlack);
        }

        return null;
    }

    private boolean isOccupied(String[][] boardState, boolean isBlack, int row, int column) {
        if (row < 15 && row > 0 && column < 15 && column >0) {
            return (isOccupiedByHero(boardState, isBlack, row, column) || isOccupiedByVillain(boardState, isBlack, row, column));
        }
        return true;
    }
    private boolean isOccupiedByHero(String[][] boardState, boolean isBlack, int row, int column) {
        return (isBlack && boardState[row][column] == "B" || !isBlack && boardState[row][column] == "W");
    }

    private boolean isOccupiedByVillain(String[][] boardState, boolean isBlack, int row, int column) {
        return (!isBlack && boardState[row][column] == "B" || isBlack && boardState[row][column] == "W");
    }

    private int[] getAdjacentOpening(String[][] boardState, int row, int column) {
        int[] opening = new int[2];
        if (boardState[row][column+1] != "B" && boardState[row][column+1] != "W") {
            opening[0] = row;
            opening[1] = column+1;
        }else if (boardState[row][column-1] != "B" && boardState[row][column-1] != "W") {
            opening[0] = row;
            opening[1] = column-1;
        }else if (boardState[row+1][column] != "B" && boardState[row+1][column] != "W") {
            opening[0] = row+1;
            opening[1] = column;
        }else if (boardState[row-1][column] != "B" && boardState[row-1][column] != "W") {
            opening[0] = row-1;
            opening[1] = column;
        } else {
            //return random
            opening[0] = random.nextInt(15);
            opening[1] = random.nextInt(15);
        }
        return opening;
    }

    private Stone blockGaps(List<Stone> previousMoves, boolean isBlack) {
        String direction;
        if (previousMoves != null && !previousMoves.isEmpty()) {
            for (Stone stone : previousMoves) {
                if (isSequential(stone, 2) && (stone.isBlack() == !isBlack)) {
                    direction = determineDirection(stone, 2);
                    if (direction != null) {
                        boolean opposite = !isBlack;
                        Stone lower = checkLowerBound(board, stone, opposite, direction);
                        Stone upper = checkUpperBound(board, stone, opposite, direction);
                        Stone nextLower = plusOneLower(direction, lower, isBlack);
                        Stone nextUpper = plusOneHiger(direction, upper, isBlack);
                        Stone result;

                        if (lower != null) {
                            result = checkNextTwo(direction, lower, nextLower, opposite);
                            if (result != null) {
                                return result;
                            }
                        }

                        if (upper != null) {
                            result = checkNextTwo(direction, upper, nextUpper, opposite);
                            if (result != null) {
                                return result;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private Stone checkNextTwo(String direction, Stone stone, Stone next, boolean isBlack) {
        Stone nextPosition = next;

        if (stone.getRow() > 0 && stone.getRow() < 15
                && stone.getColumn() > 0 && stone.getColumn() < 15
                && nextPosition.getRow() > 0 && nextPosition.getRow() < 15
                && nextPosition.getColumn() > 0 && nextPosition.getColumn() < 15) {
            // if next is empty
            if (board[stone.getRow()][stone.getColumn()] != "B"
                    && board[stone.getRow()][stone.getColumn()] != "W") {

                // and the following spot is villains
                if(stone.isBlack() && board[nextPosition.getRow()][nextPosition.getColumn()] == "B"
                        || !stone.isBlack() && board[nextPosition.getRow()][nextPosition.getColumn()] == "W") {

                    return new Stone(stone.getRow(), stone.getColumn(), !isBlack);

                }
            }
        }

        return null;
    }

    private Stone plusOneLower(String direction, Stone stone, boolean isBlack) {
        Stone result;
        if (stone != null) {
            int row = stone.getRow();
            int column = stone.getColumn();

            switch (direction) {
                case "vertical":
                    row -= 1;
                    break;
                case "horizontal":
                    column -= 1;
                    break;
                case "diagonal up":
                    row += 1;
                    column -= 1;
                    break;
                case "diagonal down":
                    row -= 1;
                    column -= 1;
                    break;
            }

            result = new Stone(row, column, isBlack);
            return result;
        }
        return null;
    }

    private Stone plusOneHiger(String direction, Stone stone, boolean isBlack) {
        Stone result;
        if (stone != null) {
            int row = stone.getRow();
            int column = stone.getColumn();

            switch (direction) {
                case "vertical":
                    row += 1;
                    break;
                case "horizontal":
                    column += 1;
                    break;
                case "diagonal up":
                    row -= 1;
                    column += 1;
                    break;
                case "diagonal down":
                    row += 1;
                    column += 1;
                    break;
            }

            result = new Stone(row, column, isBlack);
            return result;
        }
        return null;
    }

    private String determineDirection(Stone stone, int howMany) {
        char symbol = board[stone.getRow()][stone.getColumn()].charAt(0);
        if (isHorizontal(stone.getRow(), stone.getColumn(), symbol, howMany)) {
            return "horizontal";
        }
        if (isVertical(stone.getRow(), stone.getColumn(), symbol, howMany)) {
            return "vertical";
        }
        if (isDiagonalDown(stone.getRow(), stone.getColumn(), symbol, howMany)) {
            return "diagonal down";
        }
        if (isDiagonalUp(stone.getRow(), stone.getColumn(), symbol, howMany)) {
            return "diagonal up";
        }
        return null;
    }

    private boolean isSequential(Stone stone, int howMany) {
        char symbol = board[stone.getRow()][stone.getColumn()].charAt(0);
        return isHorizontal(stone.getRow(), stone.getColumn(), symbol, howMany)
                || isVertical(stone.getRow(), stone.getColumn(), symbol, howMany)
                || isDiagonalDown(stone.getRow(), stone.getColumn(), symbol, howMany)
                || isDiagonalUp(stone.getRow(), stone.getColumn(), symbol, howMany);
    }

    private int getStartOfColumn(int row, int column, char symbol) {
        int countLeft = count(row, column, -1, 0, symbol);
        return row - countLeft;
    }

    private int getEndOfColumn(int row, int column, char symbol) {
        int countRight = count(row, column, 1, 0, symbol);
        return row + countRight;
    }

    private int getStartOfRow(int row, int column, char symbol) {
        int countLeft = count(row, column, 0, -1, symbol);
        return column - countLeft;
    }
    private int getEndOfRow(int row, int column, char symbol) {
        int countRight = count(row, column, 0, 1, symbol);
        return column + countRight;

    }
    private int getDiagonalDownStartColumn(int row, int column, char symbol) {
        int countLeft = count(row, column, -1, -1, symbol);
        return column - countLeft;
    }

    private int getDiagonalUpStartColumn(int row, int column, char symbol) {
        int countLeft = count(row, column, 1, -1, symbol);
        return column - countLeft;
    }
    private int getDiagonalUpStartRow(int row, int column, char symbol) {
        int countLeft = count(row, column, 1, -1, symbol);
        return row + countLeft;
    }
    private int getDiagonalDownStartRow(int row, int column, char symbol) {
        int countLeft = count(row, column, -1, -1, symbol);
        return row - countLeft;
    }
    private int getDiagonalUpEndColumn(int row, int column, char symbol) {
        int countRight = count(row, column, -1, 1, symbol);
        return column + countRight;
    }
    private int getDiagonalDownEndColumn(int row, int column, char symbol) {
        int countRight = count(row, column, 1, 1, symbol);
        return column + countRight;
    }
    private int getDiagonalUpEndRow(int row, int column, char symbol) {
        int countRight = count(row, column, -1, 1, symbol);
        return row - countRight;

    }
    private int getDiagonalDownEndRow(int row, int column, char symbol) {
        int countRight = count(row, column, 1, 1, symbol);
        return row + countRight;
    }

    private boolean isVertical(int row, int column, char symbol, int howMany) {
        return count(row, column, 1, 0, symbol)
                + count(row, column, -1, 0, symbol) == howMany-1;
    }

    private boolean isHorizontal(int row, int column, char symbol, int howMany) {
        return count(row, column, 0, 1, symbol)
                + count(row, column, 0, -1, symbol) == howMany-1;
    }

    private boolean isDiagonalDown(int row, int column, char symbol, int howMany) {
        return count(row, column, 1, 1, symbol)
                + count(row, column, -1, -1, symbol) == howMany-1;
    }

    private boolean isDiagonalUp(int row, int column, char symbol, int howMany) {
        return count(row, column, -1, 1, symbol)
                + count(row, column, 1, -1, symbol) == howMany-1;
    }

    private int count(int row, int col, int deltaRow, int deltaCol, char symbol) {

        int result = 0;
        int r = row + deltaRow;
        int c = col + deltaCol;

        while (r >= 0 && r < 15 && c >= 0 && c < 15 && board[r][c] != null && board[r][c].charAt(0) == symbol) {
            result++;
            r += deltaRow;
            c += deltaCol;
        }

        return result;
    }
}
