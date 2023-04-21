# yesaladin_gateway

YesAladin Gateway는 각 마이크로서비스 또는 데이터와 접속하고 API 호출을 위한 관리, 모니터링을 담당하는 API 관리 시스템 입니다.

## Getting Started

```bash
./mvnw spring-boot:run
```

## Features

### [@김홍대](https://github.com/mongmeo-dev)

- NGINX 연동
  - 도메인 설정 및 서브 도메인 추가
  - Reverse Proxy 설정
- `YAML`을 이용하여 각 마이크로서비스에 대한 URL Rewriting 및 요청 라우팅 설정

### [@송학현](https://github.com/alanhakhyeonsong)

- Custom Filter를 적용하여 각 요청에 대한 정보와 응답 결과 Logging
- NHN Cloud Log & Crash를 연동하여 모니터링 환경 구축
- Spring Cloud Config를 연동하여 설정 정보 외부화

## Project Architecture

<img width="1055" alt="스크린샷 2023-02-22 오전 10 15 46" src="https://user-images.githubusercontent.com/60968342/220496058-1c3dce6d-9473-4a1c-acd7-834ab511bf85.png">

## CI/CD

<img width="1102" alt="스크린샷 2023-02-22 오후 7 24 08" src="https://user-images.githubusercontent.com/60968342/220593590-58f50bd3-302f-455d-bf99-78371b2a1ba7.png">

## Technical Issue

### 인증/인가 처리

각 서버 서비스에 접근할 때 필요한 인증 및 일반 엑세스 제어를 Gateway Service에서 처리 한 뒤,
인가된 경우 추가적인 HTTP Custom Header에 사용자의 권한 정보 등을 담아 API Server에서 체크 후 해당 API에 접근 하는 flow가 좋은 방법인지에 대해 고민하였습니다.

그 결과 Gateway Server에선 구현하던 Custom Authentication Filter를 비롯한 인증/인가 처리를 제거하고 각 요청에 대한 라우팅 및 모니터링의 역할만 부여하도록 변경하였습니다.

변경된 flow는 다음과 같습니다.

Shop API Server는 Front Server로부터 Authorization Header에 담긴 JWT 토큰 정보를 받아 이에 대한 인가 처리를 사전에 Auth Server로 위임합니다. 
Auth Server에서 해당 JWT 토큰의 유효성 검증이 완료되어 인가 된 경우, payload에 들어있는 사용자 식별 정보와 권한 정보를 추출하여 Shop API Server에 돌려줍니다.
이 정보를 바탕으로 Shop API Server 내에서 Spring Security를 통해 자체적으로 `Authentication` 을 생성하도록 처리하였으며, `FilterSecurityInterceptor` 및
method security를 적용하여 API 보안을 강화하였습니다.

## Tech Stack

### Languages

![Java](https://img.shields.io/badge/Java-007396?style=flat-square&logo=Java)

### Frameworks

![SpringBoot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=flat&logo=SpringBoot&logoColor=white)
![SpringCloud](https://img.shields.io/badge/Spring%20Cloud-6DB33F?style=flat&logo=Spring&logoColor=white)

### Build Tool

![ApacheMaven](https://img.shields.io/badge/Maven-C71A36?style=flat&logo=ApacheMaven&logoColor=white)

### DevOps

![NHN Cloud](https://img.shields.io/badge/-NHN%20Cloud-blue?style=flat&logo=iCloud&logoColor=white)
![Jenkins](http://img.shields.io/badge/Jenkins-D24939?style=flat-square&logo=Jenkins&logoColor=white)
![SonarQube](https://img.shields.io/badge/SonarQube-4E98CD?style=flat&logo=SonarQube&logoColor=white)
![Grafana](https://img.shields.io/badge/Grafana-F46800?style=flat&logo=Grafana&logoColor=white)

### Web Server

![NGINX](https://img.shields.io/badge/NGINX-009639?style=flat&logo=NGINX&logoColor=white)

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

<a href="https://github.com/NHN-YesAladin/yesaladin_gateway/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=NHN-YesAladin/yesaladin_front" />
</a>
