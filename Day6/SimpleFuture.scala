import scala.util.{Success, Failure}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

def provideMeData(): Future[String] = {
    Future {
        println("Making the resource ready")
        // Thread.sleep(5000)
        for(i <- 1 to 10){
            println(i)
        }
        "Resource is ready"
    }
}

@main def caller()= {
    provideMeData().onComplete{
        case Success(value) => println("Received: "+value)
        case Failure(exception) => println(exception.getMessage) 
    }

    println("Something Else")
    for(i <- 11 to 20){
            println(i)
        }
    println("Going to Wait for 50 seconds")
    Thread.sleep(10000)
}