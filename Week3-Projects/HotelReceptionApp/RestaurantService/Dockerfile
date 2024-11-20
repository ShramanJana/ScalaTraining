# Use an official lightweight Scala and SBT image as a parent image
FROM hseeberger/scala-sbt:11.0.13_1.6.1_2.13.7 as build

# Set the working directory in the container
WORKDIR /app

# Copy the current directory contents into the container at /app
COPY . /app

# Compile and package the application
RUN sbt clean compile stage

# Use the OpenJDK image for running the application
FROM openjdk:11-jre-slim

# Copy the binary files from the previous stage
COPY --from=build /app/target/universal/stage /app

# Set the working directory in the container
WORKDIR /app

# Make port 9000 available to the world outside this container
EXPOSE 9001

# Define environment variable
ENV PLAY_HTTP_SECRET=thisisasecretfortheapplicationandwekeepittosecuretheapplicationletuscheckandithastoworkagain
ENV BOOTSTRAP_SERVER=34.136.75.4:9092
ENV SENDER_MAIL="shramanjana2015@gmail.com"
ENV SENDER_MAIL_PASSWORD="snmp tlsk krks uedi"

# Run the binary script when the container launches
CMD ["./bin/restaurantservice", "-Dplay.http.secret.key=thisisasecretfortheapplicationandwekeepittosecuretheapplicationletuscheckandithastoworkagain", "-Dhttp.port=9001"]
