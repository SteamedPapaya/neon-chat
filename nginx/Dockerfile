# Nginx의 경량 버전인 Alpine 이미지를 사용
FROM nginx:alpine

# 기본 설정 파일 삭제 (필요한 경우, default.conf 파일이 남아 있으면 충돌할 수 있으므로)
RUN rm /etc/nginx/conf.d/default.conf

# 스프링 프로젝트 내의 nginx 디렉토리에 있는 커스텀 nginx.conf를 컨테이너 내부로 복사
COPY nginx.conf /etc/nginx/nginx.conf