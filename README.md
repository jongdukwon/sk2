# sk2
Restaurant Reservation

# 서비스 시나리오

기능적 요구사항
1. 고객이 예약서비스에서 식사를 위한 식당을 예약한다.
1. 고객이 예약 보증금을 결제한다.
1. 보증금 결제가 완료되면 예약내역이 식당에 전달된다.(No Show방지)
1. 식당에 예약정보가 전달되면 예약서비스에 예약상태를 완료 상태를 변경한다.
1. 예약이 완료되면 예약서비스에서 현재 예약상태를 조회할 수 있다.
1. 고객이 예약을 취소할 수 있다.
1. 고객이 예약에 보증금에 대한 결제상태를 Deposit 서비스에서 조회 할 수 있다.
1. 고객이 모든 진행내역을 볼 수 있어야 한다.

비기능적 요구사항
1. 트랜잭션
    1. No Show를 방지하기 위해 Deposit이 결재되지 않으면 예약이 안되도록 한다.(Sync)
    1. 예약을 취소하면 Deposit을 환불하고 Restaurant 서비스에 예약취소 내역을 전달한다.(Async)
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



