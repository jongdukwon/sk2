# sk2
Restaurant Reservation

# 서비스 시나리오

기능적 요구사항
1. 고객이 예약서비스에서 식사를 위한 식당을 예약한다.
1. 고객이 예약 보증금을 결제한다.
1. 보증금 결제가 완료되면 예약내역이 식당에 전달된다.
1. 식당에 예약정보가 전달되면 예약서비스에 예약상태를 완료 상태로 변경한다.
1. 예약이 완료되면 예약서비스에서 현재 예약상태를 조회할 수 있다.
1. 고객이 예약을 취소할 수 있다.
1. 고객이 예약 보증금에 대한 결제상태를 Deposit 서비스에서 조회 할 수 있다.
1. 고객이 모든 진행내역을 볼 수 있어야 한다.

비기능적 요구사항
1. 트랜잭션
    1. No Show를 방지하기 위해 Deposit이 결재되지 않으면 예약이 안되도록 한다.(Sync)
    1. 예약을 취소하면 Deposit을 환불하고 Restaurant에 예약취소 내역을 전달한다.(Async)
1. 장애격리
    1. Deposit 시스템이 과중되면 예약을 받지 않고 잠시후에 하도록 유도한다(Circuit breaker, fallback)
    1. Restaurant 서비스가 중단되더라도 예약은 받을 수 있다.(Asyncm, Event Dirven)
1. 성능
    1. 고객이 예약상황을 조회할 수 있도록 별도의 view로 구성한다.(CQRS)

# 체크포인트

1. Saga
1. CQRS
1. Correlation
1. Req/Resp
1. Gateway
1. Deploy/ Pipeline
1. Circuit Breaker
1. Autoscale (HPA)
1. Zero-downtime deploy (Readiness Probe)
1. Config Map/ Persistence Volume
1. Polyglot
1. Self-healing (Liveness Probe)

# 분석/설계

## AS-IS 조직 (Horizontally-Aligned)

## TO-BE 조직 (Vertically-Aligned)

## Event Storming 결과
![eventStorming](https://user-images.githubusercontent.com/77368612/107878112-d3003500-6f13-11eb-8fd8-aaf056f10f56.png)

### 기능적 요구사항 검증

![1](https://user-images.githubusercontent.com/77368612/107893185-15099500-6f6d-11eb-8a93-acf90d472651.png)

    - 고객이 예약서비스에서 식사를 위한 식당을 예약한다.(OK)
    - 고객이 예약 보증금을 결제한다.(OK)
    - 보증금 결제가 완료되면 예약내역이 식당에 전달된다.(OK)
    - 식당에 예약정보가 전달되면 예약서비스에 예약상태를 완료 상태로 변경한다.(OK)
    - 예약이 완료되면 예약서비스에서 현재 예약상태를 조회할 수 있다.(OK)
    
![2](https://user-images.githubusercontent.com/77368612/107893188-189d1c00-6f6d-11eb-9925-89954a8166c7.png)

    - 고객이 예약을 취소할 수 있다.(OK)
    - 예약을 취소하면 보증금을 환불한다.(OK)
    - 고객이 예약 보증금에 대한 결제상태를 Deposit 서비스에서 조회 할 수 있다.(OK)
     
![3](https://user-images.githubusercontent.com/77368612/107893192-1aff7600-6f6d-11eb-8266-2ea3bdb817fe.png)

    - 고객이 모든 진행내역을 볼 수 있어야 한다.(OK)

### 비기능 요구사항 검증

    - Deposit이 결재되지 않으면 예약이 안되도록 해아 한다.(Req/Res)
    - Restaurant 서비스가 중단되더라도 예약은 받을 수 있어야 한다.(Pub/Sub)
    - Deposit 시스템이 과중되면 예약을 받지 않고 잠시후에 하도록 유도한다(Circuit breaker)
    - 예약을 취소하면 Deposit을 환불하고 Restaurant에 예약취소 내역을 업데이트해야 한다.(SAGA)
    - 고객이 예약상황을 조회할 수 있도록 별도의 view로 구성한다.(CQRS)
    

# 구현:

서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8081 ~ 8084 이다)

```
cd reservation
mvn spring-boot:run

cd deposit
mvn spring-boot:run  

cd customercenter
mvn spring-boot:run 

cd restaurant
mvn spring-boot:run 
```

## DDD 의 적용

- 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다: (예시는 reservation 마이크로 서비스).

![20210215_120254](https://user-images.githubusercontent.com/77368612/107901177-5c504f80-6f86-11eb-94af-48fa5a03d79e.png)

- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리가 없도록 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다

![20210215_120624](https://user-images.githubusercontent.com/77368612/107901239-7f7aff00-6f86-11eb-8cc0-17d18e75b2cb.png)

- 적용 후 REST API 의 테스트

```
# reservation 서비스의 예약처리
http localhost:8081/reservations restaurantNo=1 day=20210215

# reservation 서비스의 예약상태 확인
http localhost:8081/reservations/1

# restaurant 서비스의 예약현황 확인
http localhost:8084/restaurant/1

```


## 폴리글랏 퍼시스턴스

Reservation, Deposit, Customerservice는 H2로 구현하고 Restaurant 서비스의 경우 Hsql로 구현하여 MSA간의 서로 다른 종류의 Database에도 문제없이 작동하여 다형성을 만족하는지 확인하였다.

reservation, deposit, customercenter의 pom.xml 파일 설정

![20210215_151200_10](https://user-images.githubusercontent.com/77368612/107911566-359f1280-6fa0-11eb-98ff-a15e7f95d942.png)

restaurant의 pom.xml 파일 설정

![20210215_151200_9](https://user-images.githubusercontent.com/77368612/107911570-3637a900-6fa0-11eb-818e-df269a61ae2d.png)


## 동기식 호출 과 Fallback 처리

분석단계에서의 조건 중 하나로 예약(reservation)->예치금 결제(deposit) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다. 

- 예치금 결제서비스를 호출하기 위하여 Stub과 (FeignClient) 를 이용하여 Service 대행 인터페이스 (Proxy) 를 구현 

#(Reservation) DepositService.java

![20210215_152121_11](https://user-images.githubusercontent.com/77368612/107912260-8d8a4900-6fa1-11eb-801d-61eaf1bf8fa0.png)

- 예약을 받은 직후(@PostPersist) 예치금 결제를 요청하도록 처리

![20210215_152121_12](https://user-images.githubusercontent.com/77368612/107912264-8ebb7600-6fa1-11eb-8f14-3468a9a51478.png)

- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 예치금 결제 시스템이 장애가 나면 예약도 못받는다는 것을 확인:

```
# 결제 (deposit) 서비스를 잠시 내려놓음
# 예약 처리
kubectl delete deploy deposit -n skteam02
```

![20210215_152729_14](https://user-images.githubusercontent.com/77368612/107912865-a9421f00-6fa2-11eb-80f8-309050271489.png)

```
# 결재(deposit)서비스 재기동
kubectl create deploy deposit --image=skteam02.azurecr.io/deposit:latest -n skteam02
```

![20210215_152729_13](https://user-images.githubusercontent.com/77368612/107912870-aa734c00-6fa2-11eb-9b22-b78f27d39cb9.png)

## Gateway 적용
gateway > application.yml
![20210215_154035_15](https://user-images.githubusercontent.com/77368612/107913732-43569700-6fa4-11eb-96e4-5ffac8ad85cd.png)
```
# Gateway 테스트
# Gateway의 External-IP 확인
```
![20210215_154035_16](https://user-images.githubusercontent.com/77368612/107913733-43ef2d80-6fa4-11eb-98b4-dbe191a93c83.png)
```
# External-IP 로 Reservation서비스에 접근
```
![20210215_154035_17](https://user-images.githubusercontent.com/77368612/107913727-42be0080-6fa4-11eb-90b5-cf7b0e0cbe04.png)


# 운영

## Deploy
```
# Namespace 생성
kubectl create ns skteam02

# 소스를 가져와 각각의 MSA 별로 빌드 진행

# 도커라이징 : Azure Registry에 Image Push 
az acr build --registry skteam02 --image skteam02.azurecr.io/reservation:latest .  
az acr build --registry skteam02 --image skteam02.azurecr.io/deposit:latest . 
az acr build --registry skteam02 --image skteam02.azurecr.io/restaurant:latest .   
az acr build --registry skteam02 --image skteam02.azurecr.io/customercenter:latest .   
az acr build --registry skteam02 --image skteam02.azurecr.io/gateway:latest . 

# 컨테이터라이징 : Deploy, Service 생성
kubectl create deploy reservation --image=skteam02.azurecr.io/reservation:latest -n skteam02
kubectl expose deploy reservation --type="ClusterIP" --port=8080 -n skteam02
kubectl create deploy deposit --image=skteam02.azurecr.io/deposit:latest -n skteam02
kubectl expose deploy deposit --type="ClusterIP" --port=8080 -n skteam02
kubectl create deploy restaurant --image=skteam02.azurecr.io/restaurant:latest -n skteam02
kubectl expose deploy restaurant --type="ClusterIP" --port=8080 -n skteam02
kubectl create deploy customercenter --image=skteam02.azurecr.io/customercenter:latest -n skteam02
kubectl expose deploy customercenter --type="ClusterIP" --port=8080 -n skteam02
kubectl create deploy gateway --image=skteam02.azurecr.io/gateway:latest -n skteam02
kubectl expose deploy gateway --type=LoadBalancer --port=8080 -n skteam02

#kubectl get all -n skteam02
```
![20210215_155905_18](https://user-images.githubusercontent.com/77368612/107914935-c7118300-6fa6-11eb-83c3-169869bcd5ce.png)



## Circuit Breaker

* 서킷 브레이킹 프레임워크의 선택: Spring FeignClient + Hystrix 옵션을 사용하여 구현함

시나리오는 예약(reservation)-->예치금 결제(deposit) 시의 연결을 RESTful Request/Response 로 연동하여 구현이 되어있고, 예치금 결제 요청이 과도할 경우 CB 를 통하여 장애격리.

- Hystrix 를 설정:  요청처리 쓰레드에서 처리시간이 300 밀리가 넘어서기 시작하여 어느정도 유지되면 CB 회로가 닫히도록 (요청을 빠르게 실패처리, 차단) 설정

```
application.yml 설정
```
![20210215_160633_19](https://user-images.githubusercontent.com/77368612/107915501-f379cf00-6fa7-11eb-9134-0aa25f7ce18b.png)


```
- 피호출 서비스(예치금 결제:deposit) 의 임의 부하 처리
(reservation) Reservation.java(entity)
```
![20210215_152121_8](https://user-images.githubusercontent.com/77368612/107915450-d93ff100-6fa7-11eb-8ac6-78c508828b29.png)


* 부하테스터 siege 툴을 통한 서킷 브레이커 동작 확인:
- 동시사용자 100명
- 60초 동안 실시

```
$ siege -c100 -t60S -r10 --content-type "application/json" 'http://localhost:8081/orders POST {"item": "chicken"}'
```
![20210215_160633_20](https://user-images.githubusercontent.com/77368612/107915504-f4126580-6fa7-11eb-97a6-9c5f58ca0a46.png)



** SIEGE 4.0.5
** Preparing 100 concurrent users for battle.
The server is now under siege...

HTTP/1.1 201     0.68 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.68 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.70 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.70 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.73 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.75 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.77 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.97 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.81 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.87 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.12 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.16 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.17 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.26 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.25 secs:     207 bytes ==> POST http://localhost:8081/orders

* 요청이 과도하여 CB를 동작함 요청을 차단

HTTP/1.1 500     1.29 secs:     248 bytes ==> POST http://localhost:8081/orders   
HTTP/1.1 500     1.24 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     1.23 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     1.42 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     2.08 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.29 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     1.24 secs:     248 bytes ==> POST http://localhost:8081/orders

* 요청을 어느정도 돌려보내고나니, 기존에 밀린 일들이 처리되었고, 회로를 닫아 요청을 다시 받기 시작

HTTP/1.1 201     1.46 secs:     207 bytes ==> POST http://localhost:8081/orders  
HTTP/1.1 201     1.33 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.36 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.63 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.65 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.68 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.69 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.71 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.71 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.74 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.76 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.79 secs:     207 bytes ==> POST http://localhost:8081/orders

* 다시 요청이 쌓이기 시작하여 건당 처리시간이 610 밀리를 살짝 넘기기 시작 => 회로 열기 => 요청 실패처리

HTTP/1.1 500     1.93 secs:     248 bytes ==> POST http://localhost:8081/orders    
HTTP/1.1 500     1.92 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     1.93 secs:     248 bytes ==> POST http://localhost:8081/orders

* 생각보다 빨리 상태 호전됨 - (건당 (쓰레드당) 처리시간이 610 밀리 미만으로 회복) => 요청 수락

HTTP/1.1 201     2.24 secs:     207 bytes ==> POST http://localhost:8081/orders  
HTTP/1.1 201     2.32 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     2.16 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     2.19 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     2.19 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     2.19 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     2.21 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     2.29 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     2.30 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     2.38 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     2.59 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     2.61 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     2.62 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     2.64 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.01 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.27 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.33 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.45 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.52 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.57 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.69 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.70 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.69 secs:     207 bytes ==> POST http://localhost:8081/orders

* 이후 이러한 패턴이 계속 반복되면서 시스템은 도미노 현상이나 자원 소모의 폭주 없이 잘 운영됨


HTTP/1.1 500     4.76 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     4.23 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.76 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.74 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     4.82 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.82 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.84 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.66 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     5.03 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     4.22 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     4.19 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     4.18 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.69 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.65 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     5.13 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     4.84 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     4.25 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     4.25 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.80 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     4.87 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     4.33 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.86 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     4.96 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     4.34 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     4.04 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.50 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.95 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.54 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.65 secs:     207 bytes ==> POST http://localhost:8081/orders


:
:

Transactions:		        1025 hits
Availability:		       63.55 %
Elapsed time:		       59.78 secs
Data transferred:	        0.34 MB
Response time:		        5.60 secs
Transaction rate:	       17.15 trans/sec
Throughput:		        0.01 MB/sec
Concurrency:		       96.02
Successful transactions:        1025
Failed transactions:	         588
Longest transaction:	        9.20
Shortest transaction:	        0.00

```
- 운영시스템은 죽지 않고 지속적으로 CB 에 의하여 적절히 회로가 열림과 닫힘이 벌어지면서 자원을 보호하고 있음을 보여줌. 하지만, 63.55% 가 성공하였고, 46%가 실패했다는 것은 고객 사용성에 있어 좋지 않기 때문에 Retry 설정과 동적 Scale out (replica의 자동적 추가,HPA) 을 통하여 시스템을 확장 해주는 후속처리가 필요.

- Retry 의 설정 (istio)
- Availability 가 높아진 것을 확인 (siege)

### 오토스케일 아웃
앞서 CB 는 시스템을 안정되게 운영할 수 있게 해줬지만 사용자의 요청을 100% 받아들여주지 못했기 때문에 이에 대한 보완책으로 자동화된 확장 기능을 적용하고자 한다. 


- 결제서비스에 대한 replica 를 동적으로 늘려주도록 HPA 를 설정한다. 설정은 CPU 사용량이 15프로를 넘어서면 replica 를 10개까지 늘려준다:
```
kubectl autoscale deploy pay --min=1 --max=10 --cpu-percent=15
```
- CB 에서 했던 방식대로 워크로드를 2분 동안 걸어준다.
```
siege -c100 -t120S -r10 --content-type "application/json" 'http://localhost:8081/orders POST {"item": "chicken"}'
```
- 오토스케일이 어떻게 되고 있는지 모니터링을 걸어둔다:
```
kubectl get deploy pay -w
```
- 어느정도 시간이 흐른 후 (약 30초) 스케일 아웃이 벌어지는 것을 확인할 수 있다:
```
NAME    DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
pay     1         1         1            1           17s
pay     1         2         1            1           45s
pay     1         4         1            1           1m
:
```
- siege 의 로그를 보아도 전체적인 성공률이 높아진 것을 확인 할 수 있다. 
```
Transactions:		        5078 hits
Availability:		       92.45 %
Elapsed time:		       120 secs
Data transferred:	        0.34 MB
Response time:		        5.60 secs
Transaction rate:	       17.15 trans/sec
Throughput:		        0.01 MB/sec
Concurrency:		       96.02
```


## 무정지 재배포

* 먼저 무정지 재배포가 100% 되는 것인지 확인하기 위해서 Autoscaler 이나 CB 설정을 제거함

- seige 로 배포작업 직전에 워크로드를 모니터링 함.
```
siege -c100 -t120S -r10 --content-type "application/json" 'http://localhost:8081/orders POST {"item": "chicken"}'

** SIEGE 4.0.5
** Preparing 100 concurrent users for battle.
The server is now under siege...

HTTP/1.1 201     0.68 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.68 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.70 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.70 secs:     207 bytes ==> POST http://localhost:8081/orders
:

```

- 새버전으로의 배포 시작
```
kubectl set image ...
```

- seige 의 화면으로 넘어가서 Availability 가 100% 미만으로 떨어졌는지 확인
```
Transactions:		        3078 hits
Availability:		       70.45 %
Elapsed time:		       120 secs
Data transferred:	        0.34 MB
Response time:		        5.60 secs
Transaction rate:	       17.15 trans/sec
Throughput:		        0.01 MB/sec
Concurrency:		       96.02

```
배포기간중 Availability 가 평소 100%에서 70% 대로 떨어지는 것을 확인. 원인은 쿠버네티스가 성급하게 새로 올려진 서비스를 READY 상태로 인식하여 서비스 유입을 진행한 것이기 때문. 이를 막기위해 Readiness Probe 를 설정함:

```
# deployment.yaml 의 readiness probe 의 설정:


kubectl apply -f kubernetes/deployment.yaml
```

- 동일한 시나리오로 재배포 한 후 Availability 확인:
```
Transactions:		        3078 hits
Availability:		       100 %
Elapsed time:		       120 secs
Data transferred:	        0.34 MB
Response time:		        5.60 secs
Transaction rate:	       17.15 trans/sec
Throughput:		        0.01 MB/sec
Concurrency:		       96.02

```

배포기간 동안 Availability 가 변화없기 때문에 무정지 재배포가 성공한 것으로 확인됨.


# 신규 개발 조직의 추가

  ![image](https://user-images.githubusercontent.com/487999/79684133-1d6c4300-826a-11ea-94a2-602e61814ebf.png)


## 마케팅팀의 추가
    - KPI: 신규 고객의 유입률 증대와 기존 고객의 충성도 향상
    - 구현계획 마이크로 서비스: 기존 customer 마이크로 서비스를 인수하며, 고객에 음식 및 맛집 추천 서비스 등을 제공할 예정

## 이벤트 스토밍 
    ![image](https://user-images.githubusercontent.com/487999/79685356-2b729180-8273-11ea-9361-a434065f2249.png)


## 헥사고날 아키텍처 변화 

![image](https://user-images.githubusercontent.com/487999/79685243-1d704100-8272-11ea-8ef6-f4869c509996.png)

## 구현  

기존의 마이크로 서비스에 수정을 발생시키지 않도록 Inbund 요청을 REST 가 아닌 Event 를 Subscribe 하는 방식으로 구현. 기존 마이크로 서비스에 대하여 아키텍처나 기존 마이크로 서비스들의 데이터베이스 구조와 관계없이 추가됨. 

## 운영과 Retirement

Request/Response 방식으로 구현하지 않았기 때문에 서비스가 더이상 불필요해져도 Deployment 에서 제거되면 기존 마이크로 서비스에 어떤 영향도 주지 않음.

* [비교] 결제 (pay) 마이크로서비스의 경우 API 변화나 Retire 시에 app(주문) 마이크로 서비스의 변경을 초래함:

예) API 변화시
```
# Order.java (Entity)

    @PostPersist
    public void onPostPersist(){

        fooddelivery.external.결제이력 pay = new fooddelivery.external.결제이력();
        pay.setOrderId(getOrderId());
        
        Application.applicationContext.getBean(fooddelivery.external.결제이력Service.class)
                .결제(pay);

                --> 

        Application.applicationContext.getBean(fooddelivery.external.결제이력Service.class)
                .결제2(pay);

    }
```

예) Retire 시
```
# Order.java (Entity)

    @PostPersist
    public void onPostPersist(){

        /**
        fooddelivery.external.결제이력 pay = new fooddelivery.external.결제이력();
        pay.setOrderId(getOrderId());
        
        Application.applicationContext.getBean(fooddelivery.external.결제이력Service.class)
                .결제(pay);

        **/
    }
```

