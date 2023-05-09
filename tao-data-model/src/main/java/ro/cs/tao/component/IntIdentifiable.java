package ro.cs.tao.component;

public class IntIdentifiable implements Identifiable<Integer>{
    protected Integer id;

    public IntIdentifiable() { this.id = defaultId(); }

    public IntIdentifiable(Integer id) {
        this.id = id;
    }

    @Override
    public Integer getId() { return this.id ; }

    @Override
    public void setId(Integer id) { this.id = id; }

    @Override
    public Integer defaultId() { return null; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IntIdentifiable that = (IntIdentifiable) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
