package data.scripts.world.systems;

public class StockDescriptor {
  
  public static final String SHIP = "SHIP";
  public static final String FIGHTER_LPC = "FIGHTER_LPC";
  public static final String HULLMOD_SPEC = "HULLMOD_SPEC";
  
  public String  type;
  public String  id;
  public int     count_cap;
  public float   wait_days;

  public StockDescriptor(
    String  type,
    String  id,
    int     count_cap,
    float   wait_days )
  {
    this.type = type;
    this.id = id;
    this.count_cap = count_cap;
    this.wait_days = wait_days;
  }
}
