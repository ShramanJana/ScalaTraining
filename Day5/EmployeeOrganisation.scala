import scala.collection.mutable.ListBuffer
import scala.io.StdIn.readLine
import scala.language.implicitConversions

// Employee class
case class Employee(eid: Int, ename: String, salary: Int) {
    override def toString: String = s"|__($eid, $ename, $salary)"
}

// Treenode represents each department with list of sub departments and employees
case class TreeNode(departmentName: String, employeeList: ListBuffer[Employee] = ListBuffer(), subDepartments: ListBuffer[TreeNode] = ListBuffer()) {
    override def toString: String = s"|__$departmentName"
}

// Method to implicitly convert tuple to Employee instance
implicit def tupleToEmployee(tuple: (Int, String, Int)): Employee = Employee(tuple._1, tuple._2, tuple._3)


class OrgOperations {

    // Using DFS to traverse the Tree data structure and find the required node
    private def checkNodePresent(root: TreeNode, department: String): Option[TreeNode] = {

        // if department name is found, return the current node
        if(root.departmentName.equalsIgnoreCase(department)) {
            return Some(root)
        }

        // else traverse the child nodes to search for the department
        var ans: Option[TreeNode] = None
        for(children <- root.subDepartments){
            val result: Option[TreeNode] = checkNodePresent(children, department)
            if(ans.isEmpty) ans = checkNodePresent(children, department)
        }

        return ans
    }

    // method is used to update new department or employee details after search for the parent department in the origanisation tree
    def addTreeNode(root: TreeNode, parentDept: String, department: String, employees: ListBuffer[Employee]): String = {
        try {
            // first find the parent department provided, if not found we throw an exception
            val parentNode: TreeNode = checkNodePresent(root, parentDept).getOrElse(throw new Exception(s"Department with the name of $parentDept doesn't exist"))

            // second we find the current department name where change needs to happen, if it doesn't exist we create a new node and add it to the parent department
            val currNode: TreeNode = checkNodePresent(root, department).getOrElse({
                val newNode: TreeNode = TreeNode(department)
                parentNode.subDepartments += newNode
                newNode
            })

            // we check if the curr department is a direct child of the parent dept, if not we throw an error
            if(!parentNode.subDepartments.exists(_ eq currNode))
                throw new Exception(s"$department is not a direct sub department of $parentDept")
                
            currNode.employeeList ++= employees

            "Department Details updated successfully"

        } catch {
            case e: Exception => e.getMessage
        }
    }
    // Prints Organisation tree in desired output structure
    def printTree(root: TreeNode): Unit = {
        println()
        print(s"$root\n")
        root.subDepartments.foreach(child => printUtil(child, 1))
    }

    private def printUtil(root: TreeNode, gap: Int): Unit = {
        for(i <- 0 until gap)
            print("   ")
        print(s"$root\n")
        for(employee <- root.employeeList) {
            for(i <- 0 to gap)
                print("   ")
            print(s"$employee\n")
        }
        for(dept <- root.subDepartments) {
            printUtil(dept, gap+1)
        }
    }
}

def startApplication(): Unit = {
    val root: TreeNode = TreeNode("Organisation")
    val ops: OrgOperations = new OrgOperations()
    println("======Application Started======")
    var continue: Boolean= true ; 
    while(continue) {
        println("Enter your choice")
        println("1. View Current Organisation structure")
        println("2. Modify Origanisation structure")
        println("3. Exit")
        val choice = readLine().trim
        choice match {
            case "1" => {
                ops.printTree(root)
                println()
            }
            case "2" => {
                val (parentDept, department, employeeList) = fetchUserEntries()
                println(s"${ops.addTreeNode(root, parentDept, department, employeeList)}")
            }
            case "3" =>
                continue = false
            case _ =>
                println("Invalid choice. Please try again!!\n")
        }
    }
}

def fetchUserEntries(): (String, String, ListBuffer[Employee]) = {
    val parentDept: String = readLine("Enter the parent department name: ").trim
    val department: String = readLine("Enter the current department name: ").trim
    val employeeList: ListBuffer[Employee] = ListBuffer()
    var continue: Boolean = true
    println("======Add Employee Details======");
    while(continue) {
        println("Press")
        println("1. To add employee details")
        println("2. To save and exit")
        val choice = readLine().trim
        choice match {
            case "1" => {
                val eid: Int = readLine("Enter the Employee Id: ").trim.toInt
                val ename: String = readLine("Enter the name: ").trim
                val salary: Int = readLine("Enter the salary: ").trim.toInt

                val employee: Employee = (eid, ename, salary)
                employeeList += employee
            }
            case "2" => continue = false
            case _ => println("Invalid choice. Please try again!!\n")
        }
    }
    
    (parentDept, department, employeeList)
} 
@main def OrganisationApplication(): Unit = {
    startApplication()
} 