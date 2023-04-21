# yesaladin_delivery
YesAladin Delivery는 사용자의 주문/결제 이후 동작하는 더미 서버입니다. 상품 배송에 대한 구체적인 시나리오를 구현하지 않고
간단하게 일정 시간 이후 주문의 상태를 배송 완료 상태로 변경하기 위한 스케줄링 프로젝트 입니다.

## Getting Started

```bash
./mvnw spring-boot:run
```

## Features

### [@송학현](https://github.com/alanhakhyeonsong)

- 배송 등록/조회 API 구현
  - 배송 등록 이후 Scheduler를 통해 Shop API 서버와 연계하여 주문 상태 변경 이력에 배송 완료 상태 등록
- Spring Cloud Config를 연동하여 설정 정보 외부화

## ERD

![ERD](https://user-images.githubusercontent.com/60968342/219932120-67ad68bf-d549-450d-af66-2fa2de5dc9a1.png)

## Project Architecture

<img width="1055" alt="스크린샷 2023-02-22 오전 10 15 46" src="https://user-images.githubusercontent.com/60968342/220496210-6f6e188e-e9fd-4e67-bf50-8dfedc09a1ad.png">

## CI/CD

<img width="1102" alt="스크린샷 2023-02-22 오후 7 24 08" src="https://user-images.githubusercontent.com/60968342/220593590-58f50bd3-302f-455d-bf99-78371b2a1ba7.png">

## Tech Stack

### Languages

![Java](https://img.shields.io/badge/Java-007396?style=flat-square&logo=Java)

### Frameworks

![SpringBoot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=flat&logo=SpringBoot&logoColor=white)
![SpringCloud](https://img.shields.io/badge/Spring%20Cloud-6DB33F?style=flat&logo=Spring&logoColor=white)
![SpringDataJPA](https://img.shields.io/badge/Spring%20Data%20JPA-6DB33F?style=flat&logo=Spring&logoColor=white)
![QueryDSL](http://img.shields.io/badge/QueryDSL-4479A1?style=flat-square&logo=Hibernate&logoColor=white)

### Build Tool

![ApacheMaven](https://img.shields.io/badge/Maven-C71A36?style=flat&logo=ApacheMaven&logoColor=white)

### Database

![MySQL](http://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=MySQL&logoColor=white)

### DevOps

![NHN Cloud](https://img.shields.io/badge/-NHN%20Cloud-blue?style=flat&logo=iCloud&logoColor=white)
![GitHubActions](https://img.shields.io/badge/GitHub%20Actions-2088FF?style=flat&logo=GitHubActions&logoColor=white)
![SonarQube](https://img.shields.io/badge/SonarQube-4E98CD?style=flat&logo=SonarQube&logoColor=white)
![Grafana](https://img.shields.io/badge/Grafana-F46800?style=flat&logo=Grafana&logoColor=white)

### 형상 관리 전략

![Git](https://img.shields.io/badge/Git-F05032?style=flat&logo=Git&logoColor=white)
![GitHub](https://img.shields.io/badge/GitHub-181717?style=flat&logo=GitHub&logoColor=white)

- Git Flow 전략을 사용하여 Branch를 관리하며 Main/Develop Branch로 Pull Request 시 코드 리뷰 진행 후 merge 합니다.
  ![image](https://user-images.githubusercontent.com/60968342/219870689-9b9d709c-aa55-47db-a356-d1186b434b4a.png)
- Main: 배포시 사용
- Develop: 개발 단계가 끝난 부분에 대해 Merge 내용 포함
- Feature: 기능 개발 단계
- Hot-Fix: Merge 후 발생한 버그 및 수정 사항 반영 시 사용

## Contributors

<a href="https://github.com/NHN-YesAladin/yesaladin_delivery/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=NHN-YesAladin/yesaladin_front" />
</a>
