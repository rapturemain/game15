package Logic;

import javafx.util.Pair;

import java.util.*;
import java.util.function.Consumer;

public class Solver {
    public Solver(double multiplier) {
        this.MULTIPLIER = multiplier;
    }

    public Solver() {

    }

    private Game game;
    private HashSet<Long> looked = new HashSet<>(100000);
    private EdgeContainer edge = new EdgeContainer(1000);

    // DOES NOT WORK
    private final boolean USE_IDA_STAR = false;

    private final boolean SLOW_SOLUTION_PROTECTION = true;
    private double MULTIPLIER = 1.6;

    private Node lastSuccessfulNode;
    private int lastMoveIndex;
    private Game lastGame;

    public void nextMove(Game game) {
        if (!game.equals(lastGame)) {
            findSolution(game);
        }
        if (lastSuccessfulNode == null) {
            return;
        }
        if (lastMoveIndex >= lastSuccessfulNode.getMoves().size()) {
            return;
        }

        lastGame.makeMove(lastSuccessfulNode.getMoves().get(lastMoveIndex));
        game.makeMove(lastSuccessfulNode.getMoves().get(lastMoveIndex++));
    }

    public int getLastSolutionLength() {
        return lastSuccessfulNode.getLength();
    }

    public Pair<Game, Integer> findSolution(Game game) {
        if (game.isRight()) {
            return new Pair<>(game, 0);
        }
        if (lastGame != null && lastGame.equals(game)) {
            return new Pair<>(this.game, 0);
        }

        this.game = game;
        int iterations = findSolution();
        lastMoveIndex = 0;
        lastGame = game.clone();

        return new Pair<>(this.game, iterations);
    }

    private int findSolution() {
        int iterations = 0;
        looked.clear();
//        edge = new ArrayList<>(1000);
        edge.clear();

        if (USE_IDA_STAR) {
            idaStar(new Node(game, 0, 0, null, null));
            return iterations;
        } else {
            long startTime = System.currentTimeMillis();
            double mult = MULTIPLIER;
            while (!game.isRight()) {
                iterations++;
                if (aStar()) {
                    MULTIPLIER = mult;
                    return iterations;
                }
                if (SLOW_SOLUTION_PROTECTION && iterations % 500 == 0 && MULTIPLIER < 1.95) {
                    if (System.currentTimeMillis() - startTime > 15000) {
                        looked.clear();
                        edge.clear();
                        MULTIPLIER += 0.2;
                        iterations = 0;
                        startTime = System.currentTimeMillis();
                    }
                }
            }

            return iterations;
        }
    }

    private boolean aStar() {
        // Finding best node.
//        Node node = null;
//        double maxEvaluation = -Double.MAX_VALUE;
//        for (int i = 0; i < edge.size(); i++) {
//            if (edge.get(i).getEvaluation() - edge.get(i).getLength() > maxEvaluation) {
//                maxEvaluation = edge.get(i).getEvaluation() - edge.get(i).getLength();
//                node = edge.get(i);
//            }
//        }

        Node node = edge.getBest();

        if (node == null) {
             node = new Node(game, 0, 0, null, null);
        }

        // Updating data.
        edge.remove(node);
        looked.add(node.getGame().clone().getAsLong());
        int len = node.getLength();

        // Looking for new nodes.
        Game gameThis = node.getGame();
        Node finalNode = node;
        node.getGame().getMoves().forEach(new Consumer<Move>() {
            @Override
            public void accept(Move move) {
                gameThis.makeMove(move);
                if (!looked.contains(gameThis.getAsLong())) {
                    //    This multiplier is responsible for speed and precision. \ / Bigger - faster and less accurate.
                    Node newNode = new Node(gameThis.clone(), fitness(gameThis) * MULTIPLIER, len + 1, finalNode, move);
                    edge.add(newNode);
                    if (gameThis.isRight()) {
                        lastSuccessfulNode = newNode;
                        return;
                    }
                }
                gameThis.unMove(move);
            }
        });

        if (gameThis.isRight()) {
            this.game = gameThis;
            return true;
        } else {
            return false;
        }
    }

    private int fitness(Game game) {
        int[] board = game.getBoard();
        int score = 0;

//        if (game.isRight()) {
//            score += 1000;
//        }

        for (int i = 0; i < 16; i++) {
            if (board[i] != i && board[i] != 15) {
                score -= Math.abs(board[i] / 4 - i / 4) + Math.abs(board[i] % 4 - i % 4);
            }
        }

        // TOO SLOW, TRY AT OWN RISK

//        // Linear (horizontal)
//        for (int i = 0; i < 4; i++) {
//            for (int j = 0; j < 3; j++) {
//                for (int k = j + 1; j < 4; j++) {
//                    if (board[i * 4 + j] > board[i * 4 + k]
//                            && isRightHorizontal(board, i * 4 + j) && isRightHorizontal(board, i * 4 + k)) {
//                        score -= 2;
//                    }
//                }
//            }
//        }
//
//        // Linear (vertical)
//        for (int i = 0; i < 4; i++) {
//            for (int j = 0; j < 3; j++) {
//                for (int k = j + 1; j < 4; j++) {
//                    if (board[i + j * 4] > board[i + k * 4]
//                            && isRightVertical(board, i + j * 4) && isRightVertical(board, i + k * 4)) {
//                        score -= 2;
//                    }
//                }
//            }
//        }

        return score;
    }

    private boolean isRightHorizontal(int[] board, int index) {
        return board[index] >= (index / 4) * 4 && board[index] < (index / 4 + 1) * 4 && board[index] != 15;
    }

    private boolean isRightVertical(int[] board, int index) {
        return board[index] % 4 == index % 4 && board[index] != 15;
    }

    private class Node {
        private Node(Game game, double evaluation, int length, Node parent, Move move) {
            this.game = game;
            this.evaluation = evaluation;
            this.length = length;
            this.parent = parent;
            this.move = move;
            this.moves = null;
        }

        private Game game;
        private double evaluation;
        private int length;

        private Node parent;
        private Move move;
        private List<Move> moves;

        Game getGame() {
            return game;
        }

        double getEvaluation() {
            return evaluation;
        }

        int getLength() {
            return length;
        }

        Node getParent() {
            return parent;
        }

        Move getMove() {
            return move;
        }

        public List<Move> getMoves() {
            if (moves == null) {
                moves = new ArrayList<>(length + 1);

                // Root check
                if (parent == null) {
                    return moves;
                }

                // Filling moves
                moves.add(move);
                Node current = parent;
                while (current.parent != null) {
                    moves.add(current.getMove());
                    current = current.getParent();
                }

                // Reversing list
                int j = moves.size() - 1;
                Move buffer;
                for (int i = 0; i < j; i++) {
                    j = moves.size() - 1 - i;
                    buffer = moves.get(i);
                    moves.set(i, moves.get(j));
                    moves.set(j, buffer);
                }
            }

            return moves;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj.getClass() != this.getClass()) {
                if (obj.getClass() == Game.class) {
                    return this.game.equals(obj);
                }
                return false;
            }
            return this.game.equals(((Node) obj).game);
        }
    }

    private class EdgeContainer {
        private EdgeContainer(int size) {
            list = new ArrayList<LinkedList<Node>>(size);
            for (int i = 0; i < size; i++) {
                list.add(new LinkedList<Node>());
            }
            sublistSize = new int[size];
            this.size = size;
        }

        private int size;
        private int[] sublistSize;
        private ArrayList<LinkedList<Node>> list;

        public void add(Node node) {
            list.get(indexOf(node)).add(node);
            sublistSize[indexOf(node)] += 1;
        }

        public void remove(Node node) {
            list.get(indexOf(node)).remove(node);
            sublistSize[indexOf(node)] -= 1;
        }

        public Node getBest() {
            for (int i = 0; i < size; i++) {
                if (sublistSize[i] > 0) {
                    return list.get(i).get(0);
                }
            }
            return null;
        }

        public void clear() {
            for (int i = 0; i < size; i++) {
                list.get(i).clear();
                sublistSize[i] = 0;
            }
        }

        private int indexOf(Node node) {
            return (int) (node.getLength() - node.getEvaluation());
        }
    }

    public static void main(String[] args) {
        Game game = new Game();
        int[] b = game.getBoard();
        b[0] = 0;
        b[1] = 4;
        b[2] = 8;
        b[3] = 12;

        b[4] = 1;
        b[5] = 5;
        b[6] = 9;
        b[7] = 13;

        b[8] = 2;
        b[9] = 6;
        b[10] = 10;
        b[11] = 14;

        b[12] = 3;
        b[13] = 7;
        b[14] = 11;
        b[15] = 15;

        game.index = 15;

        Solver solver = new Solver(1.9);
        solver.findSolution(game);
        System.out.println(solver.getLastSolutionLength());

    }

    private void idaStar(Node root) {
        double bestEvaluation = fitness(root.getGame());
        while (true) {
            double t = seatch(root, 0, bestEvaluation);
            if (t > 90000.0) {
                return;
            }
            bestEvaluation = t;
            System.out.println(bestEvaluation);
        }
    }

    private double seatch(Node node, int length, double bestEvaluation) {
        double evaluation = fitness(node.getGame()) * 4 - length ;
        if (evaluation < bestEvaluation) {
            return evaluation;
        }
        if (node.getGame().isRight()) {
            lastSuccessfulNode = node;
            return 100000.0;
        }
        double max = -Double.MAX_VALUE;
        List<Move> moves = node.getGame().getMoves();
        for (Move move : moves) {
            node.getGame().makeMove(move);
            double t = seatch(new Node(node.getGame().clone(), fitness(node.getGame()), node.getLength() + 1, node, move), length + 1, bestEvaluation);
            node.getGame().unMove(move);
            if (t > 90000.0) {
                return 100000.0;
            }
            if (t > max) {
                max = t;
            }
        }
        return max;
    }
}
