package restaurant;

import restaurant.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PolicyHandler{
    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString){

    }

    @Autowired
    DepositRepository depositRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCanceled_(@Payload Canceled canceled){

        if(canceled.isMe()){
            System.out.println("##### listener  : " + canceled.toJson());

            Optional<Deposit> depositOptional = depositRepository.findById(canceled.getId());
            Deposit deposit = depositOptional.get();
            deposit.setStatus("Canceled");
            depositRepository.save(deposit);
        }
    }

}
