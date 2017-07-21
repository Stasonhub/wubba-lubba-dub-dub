package distance

import org.specs2.Specification
import service.{ImagePHash, PhotoService}

import scala.io.Source

class HashDistribution extends Specification {

  def is =
    s2"""
       This is not actual spec, just distribution of hash hashDistribution
       
        Distribution is: List((0,5000), 
         (4,2), 
         (7,2), 
         (9,480), 
         (10,1466), 
         (11,3894), 
         (12,10426), 
         (13,24596), 
         (14,53936), 
         (15,111500), 
         (16,214842), 
         (17,376834), 
         (18,621800), 
         (19,954160), 
         (20,1354942), 
         (21,1793204), 
         (22,2209374),
         (23,2528640),
         (24,2689938),
         (25,2659854),
         (26,2437028),
         (27,2083336),
         (28,1654386),
         (29,1216252),
         (30,830878),
         (31,527764),
         (32,310596),
         (33,170330),
         (34,86064),
         (35,40314),
         (36,17234),
         (37,7042),
         (38,2604),
         (39,914),
         (40,270),
         (41,82),
         (42,12),
         (43,2),
         (44,2))
      """

  val imageHash = new ImagePHash()

  def hashDistribution = {
    val hashSet = Source.fromURL(getClass.getResource("hash_second.csv"))
      .getLines()
      .map(_.toLong)
      .toSet

    println(s"Completed reading file of lines: ${hashSet.size}")

    val distribution = repeatedCombinations(hashSet, 2)
      .toList
      .map(v => imageHash.distance(v(0), v(1)))
      .groupBy(identity)
      .mapValues(_.size)
      .toList
      .sortBy(_._1)

    println(s"Distribution is: $distribution")

    0 must beEqualTo(0)
  }

  implicit class Crossable[A](as: Traversable[A]) {
    def X[B](bs: Traversable[B]) = for {a <- as; b <- bs} yield (a, b)
  }

  def repeatedCombinations[A](s: Set[A], n: Int): Traversable[List[A]] = n match {
    case 0 => List(Nil)
    case _ => for {(x, xs) <- s X repeatedCombinations(s, n - 1)} yield x :: xs
  }


}