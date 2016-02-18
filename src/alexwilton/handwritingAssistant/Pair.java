package alexwilton.handwritingAssistant;

public class Pair<T> {

    private final T x;
    private final T y;

    public Pair(T x, T y) {
        this.x = x;
        this.y = y;
    }

    public T getY() {
        return y;
    }

    public T getX() {
        return x;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair)) return false;
        Pair pair = (Pair) o;
        return x == pair.x && y == pair.y;
    }

    @Override
    public int hashCode() {
        int xHashCode = x.hashCode();
        int yHashCode = y.hashCode();
        return xHashCode >> 3 + yHashCode << 5;
    }

}