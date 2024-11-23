cd MeetingRoomReservationService
docker build -t meeting-room-reservation-service:latest .

cd ..
cd NotificationService
docker build -t meeting-room-notification-service:latest .

cd ..
docker network create kafka-network