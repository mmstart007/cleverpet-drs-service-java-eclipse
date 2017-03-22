package pet.clever.drs;

public interface ReadModifyWrite<T> {
  public void modify(T object);
}