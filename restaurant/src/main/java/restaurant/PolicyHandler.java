package restaurant;

import restaurant.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired
    RestaurantRepository restaurantRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString){

    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayCompleted_(@Payload PayCompleted payCompleted){

        if(payCompleted.isMe()){
            System.out.println("#### listener : " + payCompleted.toJson());

            Restaurant restaurant = new Restaurant();
            restaurant.setReservationNo(payCompleted.getReservationNo());
            restaurant.setRestaurantNo(payCompleted.getRestaurantNo());
            restaurant.setDay(payCompleted.getDay());
            restaurant.setStatus("Reserved");

            restaurantRepository.save(restaurant);
            
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverModified_(@Payload Modified modified){

        if(modified.isMe()){
            System.out.println("#### listener: " + modified.toJson());

            Restaurant restaurant = new Restaurant();
            restaurant.setId(modified.getId());
            restaurant.setRestaurantNo(modified.getRestaurantNo());
            restaurant.setDay(modified.getDay());
            //restaurant.setStatus(payCompleted.getStatus());
            restaurantRepository.save(restaurant);
        }
    }

}
