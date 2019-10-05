import Logic.Game;
import Logic.Solver;
import javafx.util.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class SolverTest {
    @Test
    public void solverTest() throws InterruptedException {
        final int THREADS = 1;
        final int ITERATIONS = 100;
        final boolean DISPLAY_DEGUB_INFO = false;
        final boolean DISPLAY_RESULT = true;

        for (int i = 0; i < THREADS; i++) {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    Game game = Game.createRandomGame();
                    Solver solver = new Solver(1.4);

                    int totalLength = 0;
                    int totalIterations = 0;
                    int shortest = 999;
                    int longest = 0;
                    long startTime = System.currentTimeMillis();

                    for (int i = 0; i < ITERATIONS; i++) {
                        if (i % (ITERATIONS / 50) == 0) {
                            System.out.println(i);
                        }
                        Pair<Game, Integer> pair = solver.findSolution(game);
                        if (DISPLAY_DEGUB_INFO) {
                            System.out.println(Arrays.toString(game.getBoard()));
                            System.out.println(Arrays.toString(pair.getKey().getBoard()));
                        }
                        totalLength += solver.getLastSolutionLength();
                        totalIterations += pair.getValue();
                        if (solver.getLastSolutionLength() < shortest) {
                            shortest = solver.getLastSolutionLength();
                        }
                        if (solver.getLastSolutionLength() > longest) {
                            longest = solver.getLastSolutionLength();
                        }
                        Assert.assertTrue(pair.getKey().isRight());

                        game = Game.createRandomGame();
                    }

                    if (DISPLAY_RESULT) {
                        System.out.println("Average Length / Iterations / Time (ms) / Longest / Shortest: "
                                + (totalLength * 1.0 / ITERATIONS) + " / "
                                + (totalIterations * 1.0 / ITERATIONS) + " / "
                                + ((System.currentTimeMillis() - startTime) * 1.0 / ITERATIONS) + " / "
                                + longest + " / " + shortest);
                    }
                }
            };
            thread.start();
        }
        while (true) {
            Thread.sleep(2000);
        }
    }
}
