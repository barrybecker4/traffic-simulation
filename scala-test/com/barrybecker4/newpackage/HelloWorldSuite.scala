package com.barrybecker4.newpackage

import org.scalatest.funsuite.AnyFunSuite


class HelloWorldSuite extends AnyFunSuite {

  test("some test") {
    assertResult("Hello world!") {
      HelloWorld.h()
    }
  }
}