import java.sql.{Connection, DriverManager, ResultSet, Statement, PreparedStatement}
import scala.language.implicitConversions

case class Candidate(sno: Int, name: String, city: String)

def insertDataToDB(connection: Connection, candidate: Candidate) = {
    val statement = connection.prepareStatement("INSERT INTO candidates (sno, name, city) VALUES (?, ?, ?)")
    // Inserting candidate data
    statement.setInt(1, candidate.sno)
    statement.setString(2, candidate.name)
    statement.setString(3, candidate.city)
    statement.executeUpdate()
    println(s"Candidate data ${candidate.sno} inserted successfully.")
}

implicit def tupleToCandidate(tuple: (Int, String, String)): Candidate = Candidate(tuple._1, tuple._2, tuple._3)

def verifyInsertion(connection: Connection): Unit = {
    try {
        val query = "SELECT * FROM candidates"
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery(query)
        println("Candidates in the database:")
        while (resultSet.next()) {
            val sno = resultSet.getInt("sno")
            val name = resultSet.getString("name")
            val city = resultSet.getString("city")
            println(s"ID: $sno, Name: $name, City: $city")
        }
    } catch {
        case e: Exception => e.printStackTrace()
    }
}

@main def main(): Unit = {
    val candidateData: Array[(Int, String, String)] = Array(
        (1, "Alice", "New York"),
        (2, "Bob", "Los Angeles"),
        (3, "Charlie", "Chicago"),
        (4, "Diana", "Houston"),
        (5, "Eve", "Phoenix"),
        (6, "Frank", "Philadelphia"),
        (7, "Grace", "San Antonio"),
        (8, "Hank", "San Diego"),
        (9, "Ivy", "Dallas"),
        (10, "Jack", "San Jose"),
        (11, "Kathy", "Austin"),
        (12, "Leo", "Jacksonville"),
        (13, "Mona", "Fort Worth"),
        (14, "Nina", "Columbus"),
        (15, "Oscar", "Charlotte"),
        (16, "Paul", "San Francisco"),
        (17, "Quinn", "Indianapolis"),
        (18, "Rita", "Seattle"),
        (19, "Steve", "Denver"),
        (20, "Tina", "Washington"),
        (21, "Uma", "Boston"),
        (22, "Vince", "El Paso"),
        (23, "Wendy", "Detroit"),
        (24, "Xander", "Nashville"),
        (25, "Yara", "Portland"),
        (26, "Zane", "Oklahoma City"),
        (27, "Aiden", "Las Vegas"),
        (28, "Bella", "Louisville"),
        (29, "Caleb", "Baltimore"),
        (30, "Daisy", "Milwaukee"),
        (31, "Ethan", "Albuquerque"),
        (32, "Fiona", "Tucson"),
        (33, "George", "Fresno"),
        (34, "Hazel", "Mesa"),
        (35, "Ian", "Sacramento"),
        (36, "Jill", "Atlanta"),
        (37, "Kyle", "Kansas City"),
        (38, "Luna", "Colorado Springs"),
        (39, "Mason", "Miami"),
        (40, "Nora", "Raleigh"),
        (41, "Owen", "Omaha"),
        (42, "Piper", "Long Beach"),
        (43, "Quincy", "Virginia Beach"),
        (44, "Ruby", "Oakland"),
        (45, "Sam", "Minneapolis"),
        (46, "Tara", "Tulsa"),
        (47, "Ursula", "Arlington"),
        (48, "Victor", "New Orleans"),
        (49, "Wade", "Wichita"),
        (50, "Xena", "Cleveland")
        )
    // Database setup
    // Load the JDBC driver
    Class.forName("com.mysql.cj.jdbc.Driver")

    // Establish a connection
    val url = "jdbc:mysql://scaladb.mysql.database.azure.com:3306/shraman_jana"
    val username = "mysqladmin"
    val password = ""
    val connection: Connection = DriverManager.getConnection(url, username, password)
    try {

        // Create a statement
        val statement: Statement = connection.createStatement()

        // Create a table
        val createTableSQL =
            """
            CREATE TABLE IF NOT EXISTS candidates (
                sno INT PRIMARY KEY,
                name VARCHAR(100),
                city VARCHAR(100)
            )
            """

        statement.execute(createTableSQL)
        println("Table created successfully.")

        candidateData.foreach(candidate => insertDataToDB(connection, candidate))
        verifyInsertion(connection)

    } catch {
        case e: Exception => e.printStackTrace()
    } finally {
    // Close Statement and Connection
        connection.close()
    }
}


// =====Output=====
// Table created successfully.
// Candidate data 1 inserted successfully.
// Candidate data 2 inserted successfully.
// Candidate data 3 inserted successfully.
// Candidate data 4 inserted successfully.
// Candidate data 5 inserted successfully.
// Candidate data 6 inserted successfully.
// Candidate data 7 inserted successfully.
// Candidate data 8 inserted successfully.
// Candidate data 9 inserted successfully.
// Candidate data 10 inserted successfully.
// Candidate data 11 inserted successfully.
// Candidate data 12 inserted successfully.
// Candidate data 13 inserted successfully.
// Candidate data 14 inserted successfully.
// Candidate data 15 inserted successfully.
// Candidate data 16 inserted successfully.
// Candidate data 17 inserted successfully.
// Candidate data 18 inserted successfully.
// Candidate data 19 inserted successfully.
// Candidate data 20 inserted successfully.
// Candidate data 21 inserted successfully.
// Candidate data 22 inserted successfully.
// Candidate data 23 inserted successfully.
// Candidate data 24 inserted successfully.
// Candidate data 25 inserted successfully.
// Candidate data 26 inserted successfully.
// Candidate data 27 inserted successfully.
// Candidate data 28 inserted successfully.
// Candidate data 29 inserted successfully.
// Candidate data 30 inserted successfully.
// Candidate data 31 inserted successfully.
// Candidate data 32 inserted successfully.
// Candidate data 33 inserted successfully.
// Candidate data 34 inserted successfully.
// Candidate data 35 inserted successfully.
// Candidate data 36 inserted successfully.
// Candidate data 37 inserted successfully.
// Candidate data 38 inserted successfully.
// Candidate data 39 inserted successfully.
// Candidate data 40 inserted successfully.
// Candidate data 41 inserted successfully.
// Candidate data 42 inserted successfully.
// Candidate data 43 inserted successfully.
// Candidate data 44 inserted successfully.
// Candidate data 45 inserted successfully.
// Candidate data 46 inserted successfully.
// Candidate data 47 inserted successfully.
// Candidate data 48 inserted successfully.
// Candidate data 49 inserted successfully.
// Candidate data 50 inserted successfully.
// Candidates in the database:
// ID: 1, Name: Alice, City: New York
// ID: 2, Name: Bob, City: Los Angeles
// ID: 3, Name: Charlie, City: Chicago
// ID: 4, Name: Diana, City: Houston
// ID: 5, Name: Eve, City: Phoenix
// ID: 6, Name: Frank, City: Philadelphia
// ID: 7, Name: Grace, City: San Antonio
// ID: 8, Name: Hank, City: San Diego
// ID: 9, Name: Ivy, City: Dallas
// ID: 10, Name: Jack, City: San Jose
// ID: 11, Name: Kathy, City: Austin
// ID: 12, Name: Leo, City: Jacksonville
// ID: 13, Name: Mona, City: Fort Worth
// ID: 14, Name: Nina, City: Columbus
// ID: 15, Name: Oscar, City: Charlotte
// ID: 16, Name: Paul, City: San Francisco
// ID: 17, Name: Quinn, City: Indianapolis
// ID: 18, Name: Rita, City: Seattle
// ID: 19, Name: Steve, City: Denver
// ID: 20, Name: Tina, City: Washington
// ID: 21, Name: Uma, City: Boston
// ID: 22, Name: Vince, City: El Paso
// ID: 23, Name: Wendy, City: Detroit
// ID: 24, Name: Xander, City: Nashville
// ID: 25, Name: Yara, City: Portland
// ID: 26, Name: Zane, City: Oklahoma City
// ID: 27, Name: Aiden, City: Las Vegas
// ID: 28, Name: Bella, City: Louisville
// ID: 29, Name: Caleb, City: Baltimore
// ID: 30, Name: Daisy, City: Milwaukee
// ID: 31, Name: Ethan, City: Albuquerque
// ID: 32, Name: Fiona, City: Tucson
// ID: 33, Name: George, City: Fresno
// ID: 34, Name: Hazel, City: Mesa
// ID: 35, Name: Ian, City: Sacramento
// ID: 36, Name: Jill, City: Atlanta
// ID: 37, Name: Kyle, City: Kansas City
// ID: 38, Name: Luna, City: Colorado Springs
// ID: 39, Name: Mason, City: Miami
// ID: 40, Name: Nora, City: Raleigh
// ID: 41, Name: Owen, City: Omaha
// ID: 42, Name: Piper, City: Long Beach
// ID: 43, Name: Quincy, City: Virginia Beach
// ID: 44, Name: Ruby, City: Oakland
// ID: 45, Name: Sam, City: Minneapolis
// ID: 46, Name: Tara, City: Tulsa
// ID: 47, Name: Ursula, City: Arlington
// ID: 48, Name: Victor, City: New Orleans
// ID: 49, Name: Wade, City: Wichita
// ID: 50, Name: Xena, City: Cleveland