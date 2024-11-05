import scala.concurrent.{Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}
import scala.util.Random

@main def application(): Unit = {
    val stopSignal = Promise[Boolean]()
    val stopFuture = stopSignal.future
    val rand = new Random

    def generateRandomNumber() = {
        while(!stopFuture.isCompleted) {
            val number: Int = 1000 + rand.nextInt(2000)
            this.synchronized {
                if(!stopFuture.isCompleted) {
                    if(number == 1567) {
                        stopSignal.success(true)
                        println(s"${Thread.currentThread().getName()} Generated $number successfully, stopping other threads")
                    }
                    else    println(s"${Thread.currentThread().getName()} Generated $number, continuing.....")
                }
            }
            Thread.sleep(10)
        }
    }

    def startThreeThreads() = {
        val thread1 = new Thread(() => generateRandomNumber(), "Thread1")
        val thread2 = new Thread(() => generateRandomNumber(), "Thread2")
        val thread3 = new Thread(() => generateRandomNumber(), "Thread3")
        thread1.start()
        thread2.start()
        thread3.start()
    }

    println("======Application started======")
    startThreeThreads()
    while(!stopFuture.isCompleted) {
        
    }
    println("======Application stopped!!======")
}   