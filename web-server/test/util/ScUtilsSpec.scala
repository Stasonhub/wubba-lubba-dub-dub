package util

import org.specs2.Specification
import util.ScUtils.binarySearchFirstGreaterOrEq

class ScUtilsSpec extends Specification {

  def is =
    s2"""
      The ScUtilsSpec binarySearchGreaterOrEq:
         should give greater or equal index of searching element $grOrEq
         should work on exact match $onExact
         should work on fisrt elem match $onFirst
         should work on last elem match $onLast
         should get None if not found greater $noneIfNotFound
         should work on some complex example $someComplexExample
      """


  def grOrEq = {
    val descItems = List(100L, 90L, 87L, 85L, 84L, 40L, 30L)
    binarySearchFirstGreaterOrEq(descItems, 83L) must beSome(beEqualTo(4))
  }

  def onExact = {
    val descItems = List(10000L, 90L, 87L, 84L, 83L, 70L, 40L, 0L, -123L)
    binarySearchFirstGreaterOrEq(descItems, 83L) must beSome(beEqualTo(4))
  }

  def onFirst = {
    val descItems = List(100L, 90L, 87L, 85L, 70L, 40L, 30L)
    binarySearchFirstGreaterOrEq(descItems, 99L) must beSome(beEqualTo(0))
  }

  def onLast = {
    val descItems = List(100L, 90L, 87L, 85L, 70L, 40L, 30L)
    binarySearchFirstGreaterOrEq(descItems, 20L) must beSome(beEqualTo(6))
  }

  def noneIfNotFound = {
    val descItems = List(100L, 90L, 87L, 85L, 70L, 40L, 30L, 20L, 10L)
    binarySearchFirstGreaterOrEq(descItems, 101L) must beNone
  }

  def someComplexExample = {
    val descItems = List(130, 90L, 70L, 69L, 68L, 10L, 9L, 3L, 2L, 1L)
    binarySearchFirstGreaterOrEq(descItems, 8L) must beSome(beEqualTo(6))
  }


}
