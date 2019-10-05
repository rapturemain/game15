package NeuralNetwork;

import java.util.ArrayList;
import java.util.List;

public class AutoCreatingNN implements NeuralNetwork {
    public AutoCreatingNN(int inputSize, int outputSize, double maxWeight) {
        if (inputSize <= 0 || outputSize <= 0) {
            throw new IllegalArgumentException("Size of NN cannot be less than 1");
        }
        this.inputSize = inputSize;
        this.outputSize = outputSize;
        this.hiddenNeurons = 0;

        this.neurons = new ArrayList<Neuron>(inputSize + outputSize + inputSize * 10);
        this.links = new ArrayList<Link>(inputSize + outputSize + inputSize * 100);

        this.weight = (Math.random() - 0.5) * maxWeight;

        for (int i = 0; i < inputSize + outputSize; i++) {
            neurons.add(new Neuron());
        }
    }

    private int inputSize;
    private int outputSize;
    private int hiddenNeurons;
    private List<Neuron> neurons;
    private List<Link> links;
    private Double weight;

    public double[] calculateToArray() {
        for (int i = 0; i < links.size(); i++) {
            links.get(i).apply();
        }
        double[] returnArray = new double[outputSize];
        int counter = 0;
        for (int i = inputSize + hiddenNeurons; i < neurons.size(); i++) {
            returnArray[counter++] = neurons.get(i).getValue();
        }
        return returnArray;
    }

    public List<Double> calculate() {
        for (int i = 0; i < links.size(); i++) {
            links.get(i).apply();
        }
        List<Double> returnList = new ArrayList<Double>(outputSize);
        for (int i = inputSize + hiddenNeurons; i < neurons.size(); i++) {
            returnList.add(neurons.get(i).getValue());
        }
        return returnList;
    }

    public List<Double> calculate(int[] data) {
        for (int i = inputSize; i < neurons.size(); i++) {
            neurons.get(i).setValue(0.0);
        }
        loadData(data);
        return calculate();
    }

    public void loadData(double[] data) {
        if (data.length != this.inputSize) {
            throw new IllegalArgumentException("Wrong size of input data.");
        }
        for (int i = 0; i < this.inputSize; i++) {
            neurons.get(i).setValue(data[i] / 16.0);
        }
    }

    public void loadData(int[] data) {
        if (data.length != this.inputSize) {
            throw new IllegalArgumentException("Wrong size of input data.");
        }
        for (int i = 0; i < this.inputSize; i++) {
            neurons.get(i).setValue(data[i]);
        }
    }

    public void loadData(List<Double> data) {
        if (data.size() != this.inputSize) {
            throw new IllegalArgumentException("Wrong size of input data.");
        }
        for (int i = 0; i < this.inputSize; i++) {
            neurons.get(i).setValue(data.get(i));
        }
    }

    public void updateNNStruct() {
        if (this.links.size() == 0) {
            this.addLink();
            return;
        }
        if (this.links.size() > 200) {
            this.updateWeights(0.1);
        }
        if (this.neurons.size() > 80) {
            this.addLink();
        }

        final int COUNT = 5;
        int index = (int) (Math.random() * COUNT - 0.01);
        switch (index) {
            case 0:
                this.addLink();
                return;
            case 1:
                this.addNeuron();
                return;
            case 2:
                this.changeActivation();
                return;
            case 3:
                this.invertLink();
                return;
            case 4:
                updateWeights(0.2);
                return;
        }

    }

    private void updateWeights(double mutationRate) {
        for (int i = 0; i < links.size(); i++) {
            links.get(i).updateWeight(mutationRate);
        }
    }

    private void addLink() {
        int firstNeuronIndex = (int) (Math.random() * (neurons.size() - 1 - outputSize));
        Neuron firstNeuron = this.neurons.get(firstNeuronIndex);
        int secondNeuronIndex;
        do {
            secondNeuronIndex = (int) (Math.random() * (neurons.size() - 1 - inputSize)) + inputSize;
        } while (secondNeuronIndex == firstNeuronIndex);
        Neuron secondNeuron = this.neurons.get(secondNeuronIndex);

        boolean firstNeuronLinksFound = false;
        boolean firstNeuronLinksFoundSecond = false;
        for (int i = 0; i < this.links.size(); i++) {
            if (firstNeuronLinksFound) {
                if (this.links.get(i).getFirstNeuron() != firstNeuron) {
                    firstNeuronIndex = i;
                    break;
                }
            } else {
                if (links.get(i).getFirstNeuron() == firstNeuron) {
                    firstNeuronLinksFound = true;
                    firstNeuronIndex = i + 1;
                }
                if (links.get(i).getSecondNeuron() == firstNeuron) {
                    firstNeuronLinksFoundSecond = true;
                    secondNeuronIndex = i;
                }
            }
        }
        Link link = new Link(firstNeuron, secondNeuron, weight);
        if (!firstNeuronLinksFound) {
            if (firstNeuronLinksFoundSecond) {
                links.add(secondNeuronIndex + 1, link);
            } else {
                links.add(0, link);
            }
        } else {
            links.add(firstNeuronIndex, link);
        }
    }

    private void addNeuron() {
        int selectedLink = (int) (Math.random() * (links.size() - 0.01));
        if (selectedLink >= links.size()) {
            selectedLink = links.size() - 1;
        }
        Neuron newNeuron = new Neuron();
        neurons.add(neurons.size() - outputSize, newNeuron);
        Neuron oldNeuron = links.get(selectedLink).getSecondNeuron();
        links.get(selectedLink).setSecondNeuron(newNeuron);
        links.add(selectedLink + 1, new Link(newNeuron, oldNeuron, weight));
        hiddenNeurons++;
    }

    private void invertLink() {
        int selectedLink = (int) (Math.random() * (links.size() - 0.01));
        links.get(selectedLink).invert();
    }

    private void changeActivation() {
        neurons.get((int) (Math.random() * (neurons.size() - 1 - inputSize)) + inputSize)
                .setActivation(Neuron.Activation.getRandom());
    }

    public AutoCreatingNN clone() {
        AutoCreatingNN nn = new AutoCreatingNN(inputSize, outputSize, weight);
        nn.neurons.clear();
        nn.weight = weight;
        nn.hiddenNeurons = this.hiddenNeurons;
        for (int i = 0; i < this.neurons.size(); i++) {
            nn.neurons.add(this.neurons.get(i).clone());
        }
        for (int i = 0; i < this.links.size(); i++) {
            Link link = this.links.get(i);
            nn.links.add(new Link(nn.neurons.get(this.neurons.indexOf(link.getFirstNeuron())),
                    nn.neurons.get(this.neurons.indexOf(link.getSecondNeuron())), link.weight));
        }
        return nn;
    }

    private static class Neuron {
        private Neuron() {
            this.value = 0;
            this.activation = Activation.RELU;
        }

        private Activation activation;

        private double value;

        public void setValue(double value) {
            this.value = value;
        }

        public double getValue() {
            return this.activation.apply(this.value);
        }

        public void applyNeuron(Neuron neuron, double weight, boolean isPositive) {
            this.value += neuron.getValue() * (isPositive ? 1.0 : -1.0);
        }

        public void setActivation(Activation activation) {
            this.activation = activation;
        }

        public Neuron clone() {
            Neuron n = new Neuron();
            n.value = this.value;
            n.activation = this.activation;
            return n;
        }

        private enum Activation {
            RELU(0), SIGMOID(1), TANH(2);

            Activation(int index) {
                this.index = index;
            }

            int index;

            private static final int COUNT = 3;

            public static Activation getRandom() {
                int index = (int) (Math.random() * COUNT - 0.01);
                switch (index) {
                    case 1:
                        return RELU;
                    case 2:
                        return SIGMOID;
                    case 3:
                        return TANH;
                    default:
                        return RELU;
                }
            }

            public double apply(double value) {
                switch (index) {
                    case 0:
                        return value > 0 ? value : value * 0.01;
                    case 1:
                        return 1.0 / (1 + Math.exp(-value));
                    case 2:
                        return Math.tanh(value);
                }
                return 0;
            }
        }
    }

    private class Link {
        public Link() {}
        public Link(Neuron firstNeuron, Neuron secondNeuron, double weight) {
            this.neuronFirst = firstNeuron;
            this.neuronSecond = secondNeuron;
            this.weight = weight;
        }

        private Neuron neuronFirst;
        private Neuron neuronSecond;
        private boolean positive;
        private double weight;

        public void updateWeight(double mutationRate) {
            weight += Math.random() * mutationRate;
        }

        public void setFirstNeuron(Neuron neuron) {
            this.neuronFirst = neuron;
        }

        public void setSecondNeuron(Neuron neuron) {
            this.neuronSecond = neuron;
        }

        public void apply() {
            neuronSecond.applyNeuron(neuronFirst, weight, positive);
        }

        public void invert() {
            this.positive = !this.positive;
        }

        public Neuron getSecondNeuron() {
            return this.neuronSecond;
        }

        public Neuron getFirstNeuron() {
            return this.neuronFirst;
        }

        public Link clone() {
            return new Link(this.neuronFirst.clone(), this.neuronSecond.clone(), weight);
        }
    }
}
