package models

case class Reservation(
                        id: Int,
                        roomId: Int,
                        employeeName: String,
                        department: String,
                        purpose: String,
                        startTime: String,
                        endTime: String,
                        createdBy: Int
                      )
