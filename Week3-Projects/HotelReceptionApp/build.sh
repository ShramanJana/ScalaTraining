cd BookingService
docker build -t hotel-reception-booking-service:latest .

cd ..
cd RestaurantService
docker build -t hotel-reception-restaurant-service:latest .

cd ..
cd RoomService
docker build -t hotel-reception-room-service:latest .

cd ..
cd WifiService
docker build -t hotel-reception-wifi-service:latest .

cd ..
docker network create kafka-network