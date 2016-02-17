package alexwilton.handwritingAssistant;

public class Key<T> {

    private final T x;
    private final T y;

    public Key(T x, T y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Key)) return false;
        Key key = (Key) o;
        return x == key.x && y == key.y;
    }

    @Override
    public int hashCode() {
        int xHashCode = x.hashCode();
        int yHashCode = y.hashCode();
        return xHashCode >> 3 + yHashCode << 5;
    }

}