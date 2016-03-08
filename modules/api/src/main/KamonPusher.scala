package lila.api

import akka.actor._
import java.lang.management.ManagementFactory
import kamon.Kamon.metrics
import scala.concurrent.duration._

import lila.socket.actorApi.NbMembers

private final class KamonPusher extends Actor {

  override def preStart() {
    context.system.lilaBus.subscribe(self, 'nbMembers)
    context.system.scheduler.schedule(1 second, 1 second, self, Tick)
  }

  private case object Tick

  private val threadStats = ManagementFactory.getThreadMXBean
  private val app = lila.common.PlayApp

  def receive = {

    case NbMembers(nb) =>
      metrics.histogram("socket.member") record nb

    case Tick =>
      metrics.histogram("jvm.thread") record threadStats.getThreadCount
      metrics.histogram("jvm.daemon") record threadStats.getDaemonThreadCount
      metrics.histogram("jvm.uptime") record app.uptime.toStandardSeconds.getSeconds
  }
}
