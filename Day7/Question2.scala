// Question 2
// ----------

// trait Task --->  doTask (implemented method)
// trait Cook  extends Task --> doTask (override)
// trait Garnish extends Cook --> doTask (overide)
// trait Pack extends Garnish --> doTask (overide)
// class Activity extends Task---> doActivity ---> Call for doTask


// create object in main method 

// val:Task = new Activity with Cook with Garnish with Pack

// observe the behavior

// observe the behavior by changing the order of inheritance


trait Task {
    def doTask() = {
        println("Simple Task going on")
    }
}

trait Cook extends Task {
    override def doTask() = {
        println("Cook is doing Task")
    }
}

trait Garnish extends Cook {
    override def doTask() = {
        println("Garnishing task going on")
    }
}

trait Pack extends Garnish {
    override def doTask() = {
        println("Packing is going on")
    }
}

class Activity extends Task {
    def doActivity() = {
        doTask()
        println("Activity completed")
    }
}

@main def ActivityTest = {
    val obj = new Activity() with Garnish with Pack with Cook 

    obj.doActivity()

}

// =====Output=====
// Packing is going on
// Activity completed