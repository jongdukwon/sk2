package restaurant;

public class Modified extends AbstractEvent {

    private Long Id;
    private String restaurantNo;
    private String day;

    public Long getId() {
        return Id;
    }

    public void setId(Long Id) {
        this.Id = Id;
    }
    public String getRestaurantNo() {
        return restaurantNo;
    }

    public void setRestaurantNo(String restaurantNo) {
        this.restaurantNo = restaurantNo;
    }
    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }
}