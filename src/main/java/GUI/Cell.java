package GUI;

import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class Cell extends Canvas {
    public Cell(int value, int size) {
        super(size > 10 ? size : 10, size > 10 ? size : 10);
        this.value = value;
        if (value == 15) {
            init("", size);
        } else {
            init(Integer.toString(value + 1), size);
        }
    }

    private int index = 0;
    private int value;

    public int getSize() {
        return (int) this.getWidth();
    }

    public int getValue() {
        return value;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    private void init(String value, int size) {
        int sizeC = size > 10 ? size : 10;
        double sizeText = sizeC / 3.8;
        if (this.value > 8) {
            sizeText = -10;
        }
        this.getGraphicsContext2D().setFill(new Color(0.45, 0.45, 0.45, 1));
        this.getGraphicsContext2D().fillRect(0, 0, sizeC, sizeC);
        this.getGraphicsContext2D().setFill(new Color(0.7, 0.7, 0.7, 1));
        this.getGraphicsContext2D().fillRect(3, 3, sizeC - 6, sizeC - 6);
        this.getGraphicsContext2D().setFill(new Color(0.8, 0.8, 0.8, 1));
        this.getGraphicsContext2D().setFont(Font.font(sizeC - 6));
        this.getGraphicsContext2D().fillText(value, sizeText, sizeC * 0.9 - 3);
    }
}
