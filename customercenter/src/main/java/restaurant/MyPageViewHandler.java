package restaurant;

import restaurant.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class MyPageViewHandler {


    @Autowired
    private MyPageRepository myPageRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenReserved_then_CREATE_1 (@Payload Reserved reserved) {
        try {
            if (reserved.isMe()) {
                // view 객체 생성
                MyPage myPage = new MyPage();
                // view 객체에 이벤트의 Value 를 set 함
                myPage.setId(reserved.getId());
                myPage.setRestaurantNo(reserved.getRestaurantNo());
                myPage.setDay(reserved.getDay());
                myPage.setStatus(reserved.getStatus());
                // view 레파지 토리에 save
                myPageRepository.save(myPage);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whenReservAccepted_then_UPDATE_1(@Payload ReservAccepted reservAccepted) {
        try {
            if (reservAccepted.isMe()) {
                // view 객체 조회
                List<MyPage> myPageList = myPageRepository.findByReservationNo(reservAccepted.getReservationNo());
                for(MyPage myPage  : myPageList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    myPage.setReservationNo(reservAccepted.getReservationNo());
                    myPage.setStatus(reservAccepted.getStatus());
                    // view 레파지 토리에 save
                    myPageRepository.save(myPage);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenModified_then_UPDATE_2(@Payload Modified modified) {
        try {
            if (modified.isMe()) {
                // view 객체 조회
                List<MyPage> myPageList = myPageRepository.findByReservationNo(modified.getId());
                for(MyPage myPage  : myPageList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    myPage.setId(modified.getId());
                    myPage.setRestaurantNo(modified.getRestaurantNo());
                    myPage.setDay(modified.getDay());
                    // view 레파지 토리에 save
                    myPageRepository.save(myPage);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}