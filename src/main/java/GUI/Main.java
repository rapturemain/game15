package GUI;

import Logic.Game;
import Logic.Solver;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class Main extends Application {
    public static void main(String[] args) {
        Application.launch(args);
    }


    private final int START_WIDTH = 906;
    private final int START_HEIGHT = 929;
    private Group root = new Group();
    private Group group = new Group();


    private Solver solver;
    private Game game;
    private Stage stage;
    private IntegerProperty moves;

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        primaryStage.setTitle("Game 15");
        primaryStage.setResizable(true);
        primaryStage.setWidth(START_WIDTH);
        primaryStage.setHeight(START_HEIGHT);
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);

        game = Game.createRandomGame();
        solver = new Solver(1.4);

        fillGameBoard(900);
        root.getChildren().add(group);

        moves = new SimpleIntegerProperty();
        moves.setValue(0);
        moves.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (newValue.intValue() != 0) {
                    if (newValue.intValue() >= solver.getLastSolutionLength()) {
                        stage.setTitle("Game 15 : Solved!");
                    } else {
                        stage.setTitle("Game 15 : Solution in " + (solver.getLastSolutionLength() - moves.getValue()));
                    }
                }
            }
        });
        primaryStage.show();
    }

    private long time = 0;
    private volatile boolean mousePressed = false;
    private volatile boolean autoSolveStarted = false;
    private volatile boolean searchingSolution = false;

    private void mousePressed() {
        time = System.currentTimeMillis();
        mousePressed = true;
        Thread thread = new Thread() {
            @Override
            public void run() {
                while (mousePressed) {
                    if (System.currentTimeMillis() - time > 500) {
                        autoSolveStarted = true;
                        if (!searchingSolution) {
                            searchingSolution = true;
                            solver.nextMove(game);
                            searchingSolution = false;
                        }
                        Platform.runLater(new Thread() {
                            @Override
                            public void run() {
                                moves.setValue(moves.getValue() + 1);
                            }
                        });
                        updateGameBoard();
                    }
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                autoSolveStarted = false;
            }
        };
        if (!autoSolveStarted) {
            thread.start();
        }
    }
    private void mouseReleased() {
        mousePressed = false;
    }

    private void fillGameBoard(int size) {
        int[] board = game.getBoard();
        group.addEventHandler(MouseEvent.ANY, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getEventType() == MouseEvent.MOUSE_PRESSED && event.getButton() == MouseButton.PRIMARY) {
                    mousePressed();
                }
                if ((event.getButton() == MouseButton.SECONDARY)
                        && (event.getEventType() == MouseEvent.MOUSE_PRESSED) && !mousePressed) {
                    game = Game.createRandomGame();
                    updateGameBoard();
                    stage.setTitle("Game 15");
                    moves.setValue(0);
                }
                if (event.getEventType() == MouseEvent.MOUSE_RELEASED  && event.getButton() == MouseButton.PRIMARY) {
                    mouseReleased();
                }
            }
        });
        for (int i = 0; i < board.length; i++) {
            Cell cell = new Cell(board[i], size / 4);
            cell.setLayoutX((i % 4) * cell.getSize());
            cell.setLayoutY((i / 4) * cell.getSize());
            cell.setIndex(i);
            cell.addEventHandler(MouseEvent.ANY, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getEventType() == MouseEvent.MOUSE_CLICKED && !autoSolveStarted) {
                        if (cell.getValue() != 15) {
                            game.makeMove(cell.getIndex());
                            if (game.isRight()) {
                                stage.setTitle("Game 15 : Solved!");
                            } else {
                                stage.setTitle("Game 15 : Custom move");
                            }
                            moves.setValue(0);
                        } else {
                            if (!searchingSolution) {
                                searchingSolution = true;
                                solver.nextMove(game);
                                searchingSolution = false;
                            }
                            moves.setValue(moves.getValue() + 1);
                        }
                        updateGameBoard();
                    }
                }
            });
            group.getChildren().add(cell);
        }
    }

    private void updateGameBoard() {
        int[] board = game.getBoard();
        group.getChildren().forEach(new Consumer<Node>() {
            @Override
            public void accept(Node node) {
                int i = indexOf(((Cell) node).getValue(), board);
                if (i == -1) {
                    System.out.println("Something went wrong");
                }
                ((Cell) node).setIndex(i);
                node.setLayoutX((i % 4) * ((Cell) node).getSize());
                node.setLayoutY((i / 4) * ((Cell) node).getSize());
            }
        });
    }

    private int indexOf(int value, int[] board) {
        for (int i = 0; i < board.length; i++) {
            if (board[i] == value) {
                return i;
            }
        }
        return -1;
    }
}
