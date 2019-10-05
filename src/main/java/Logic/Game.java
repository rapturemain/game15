package Logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class Game {
    public Game() {
        this.board = new int[16];
        for (int i = 0; i < this.board.length; i++) {
            this.board[i] = i;
        }
        this.index = 15;
    }

    int index;
    private int[] board;

    public int[] getBoard() {
        return board;
    }

    public List<Move> getMoves() {
        List<Move> list = new ArrayList<Move>(4);
        if (index % 4 != 0) {
            list.add(new Move(index, index - 1));
        }
        if ((index + 1) % 4 != 0) {
            list.add(new Move(index, index + 1));
        }
        if (index > 3) {
            list.add(new Move(index, index - 4));
        }
        if (index < 12) {
            list.add(new Move(index, index + 4));
        }
        return list;
    }

    public void unMove(Move move) {
        swap(move);
        index = move.getFrom();
    }

    public void makeMove(Move move) {
        swap(move);
        index = move.getTo();
    }

    public void makeMove(int i) {
        if (isRight()) {
            return;
        }
        List<Move> moves = this.getMoves();
        Game game = this;
        moves.forEach(new Consumer<Move>() {
            @Override
            public void accept(Move move) {
                if (move.getTo() == i) {
                    game.makeMove(move);
                    return;
                }
            }
        });
    }

    private void swap(Move move) {
        int buffer = this.board[move.getFrom()];
        this.board[move.getFrom()] = this.board[move.getTo()];
        this.board[move.getTo()] = buffer;
    }

    public boolean isRight() {
        for (int i = 0; i < board.length; i++) {
            if (board[i] != i) {
                return false;
            }
        }
        return true;
    }

    public Long getAsLong() {
        long result = 0;
        for (int i = 0; i < 16; i++) {
            result += board[i];
            result <<= 4;
        }
        return result;
    }

    public static Game createRandomGame() {
        List<Integer> list = new ArrayList<Integer>(16);
        int indexOf16 = 0;
        int[] board = new int[16];
        do {
            for (int i = 0; i < 16; i++) {
                list.add(i);
            }
            for (int i = 0; i < 16; i++) {
                int index = (int) (Math.random() * (list.size() - 0.01));
                board[i] = list.get(index);
                if (board[i] == 15) {
                    indexOf16 = i;
                }
                list.remove(index);
            }
        } while (!isRightCreation(board, indexOf16));
        Game game = new Game();
        game.index = indexOf16;
        game.board = board;
        return game;
    }

    private static boolean isRightCreation(int[] board, int index) {
        int value = 0;
        for (int i = 0; i < 15; i++) {
            if (board[i] == 15) {
                continue;
            }
            for (int j = i + 1; j < 16; j++) {
                if (board[i] > board[j] && board[j] != 15) {
                    value++;
                }
            }
        }
        value += index / 4 + 1;
        return value % 2 == 0;
    }

    public Game clone() {
        Game game = new Game();
        System.arraycopy(this.board, 0, game.board, 0, 16);
        game.index = this.index;
        return game;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != this.getClass()) {
            return false;
        }

        for (int i = 0; i < 16; i++) {
            if (((Game) obj).getBoard()[i] != board[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(board);
    }
}
