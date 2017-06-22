package data.scripts.world.systems;

public class StockDescriptor {
  public String  variant;
  public int     count_cap;
  public float   wait_days;

  public StockDescriptor(
    String  variant,
    int     count_cap,
    float   wait_days )
  {
    this.variant = variant;
    this.count_cap = count_cap;
    this.wait_days = wait_days;
  }
}
