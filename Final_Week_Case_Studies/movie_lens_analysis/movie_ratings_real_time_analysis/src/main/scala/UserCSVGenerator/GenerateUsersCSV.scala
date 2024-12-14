package UserCSVGenerator

import com.github.javafaker.Faker

import java.io.{BufferedWriter, FileWriter}
import scala.util.Random

object GenerateUsersCSV {
  def main(args: Array[String]): Unit = {
    // Number of users to generate
    val numUsers = 5000

    // File name for the CSV
    val fileName = "users.csv"

    // Generate user data
    val users = generateUserData(numUsers)

    // Write data to CSV
    writeToCSV(fileName, users)

    println(s"$numUsers users written to $fileName")
  }

  def generateUserData(numUsers: Int): Seq[Map[String, String]] = {
    val faker = new Faker()
    val genders = Seq("Male", "Female", "Non-Binary")
    (1 to numUsers).map { userId =>
      Map(
        "userId" -> userId.toString,
        "firstName" -> faker.name().firstName(),
        "lastName" -> faker.name().lastName(),
        "age" -> (Random.nextInt(63) + 18).toString, // Random age between 18 and 80
        "location" -> faker.address().city(),
        "gender" -> genders(Random.nextInt(genders.length))
      )
    }
  }

  def writeToCSV(fileName: String, users: Seq[Map[String, String]]): Unit = {
    val header = Seq("userId", "firstName", "lastName", "age", "location", "gender")
    val file = new BufferedWriter(new FileWriter(fileName))

    try {
      file.write(header.mkString(",") + "\n")
      users.foreach { user =>
        val line = header.map(field => user(field).toString).mkString(",")
        file.write(line + "\n")
      }
    } finally {
      file.close()
    }
  }
}