package models

case class Reservation(
                      id: Int,
                      roomId: Int,
                      email: String,
                      purpose: String,
                      startTime: String,
                      endTime: String,
                      username: String,
                      )
