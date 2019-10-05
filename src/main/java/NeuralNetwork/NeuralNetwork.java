package NeuralNetwork;

import java.util.List;

public interface NeuralNetwork {

     double[] calculateToArray();

     List<Double> calculate();

     void loadData(double[] data);

     void loadData(List<Double> data);
}
