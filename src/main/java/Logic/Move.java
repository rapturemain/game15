package Logic;


/**
 * First 16 - from
 * Second 16 - to
 */
public class Move {
    public Move(int from, int to) {
        this.move += (from << 16) + to;
    }

    private int move;

    public int getFrom() {
        return this.move >> 16;
    }

    public int getTo() {
        return this.move & 0x0000FFFF;
    }
}
