package restaurant;

import javax.activation.MimeType;
import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PostPersist;

import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeTypeUtils;

@Entity
@Table(name="Deposit_table")
public class Deposit {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long reservationNo;
    private String restaurantNo;
    private String day;
    private String status;

    @PostPersist
    public void onPostPersist(){

        if(this.getStatus().equals("DepositPayed")){
            
            PayCompleted payCompleted = new PayCompleted();
            this.setReservationNo(this.getId());

            BeanUtils.copyProperties(this, payCompleted);
            payCompleted.publishAfterCommit();

        }else{

            PayCanceled payCanceled = new PayCanceled();
            this.setReservationNo(this.getId());

            BeanUtils.copyProperties(this, payCanceled);
            payCanceled.publishAfterCommit();
        }
    }


    public Long getId() {
        return id;
    }

    public Long getReservationNo() {
        return reservationNo;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setReservationNo(Long reservationNo) {
        this.reservationNo = reservationNo;
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
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
