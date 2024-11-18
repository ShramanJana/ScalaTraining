package scheduler

import mail.MailUtils.composeAndSendEmailAllGuests
import repositories.{GuestRepository, MenuRepository}

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

@Singleton
class GuestService @Inject()(guestRepository: GuestRepository, menuDao: MenuRepository)(implicit ec: ExecutionContext) {

  def fetchGuestListAndSendMenu(): Unit = {
    val menuList = Await.result(menuDao.list(), Duration.apply(3, TimeUnit.SECONDS))
    guestRepository.findActiveGuests().map(composeAndSendEmailAllGuests(_, menuList))

  }
}
