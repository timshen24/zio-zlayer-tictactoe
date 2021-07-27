package com.example.tictactoe.parser.game

import com.example.tictactoe.domain.Board.Field
import com.example.tictactoe.domain.{ GameCommand, ParseError }
import zio._
import zio.test.Assertion._
import zio.test._

object GameCommandParserSpec extends DefaultRunnableSpec {
  def spec =
    suite("GameCommandParser")(
      suite("parse")(
        test("menu returns Menu command") {
          val result = GameCommandParser.parse("menu").either
          assertM(result)(isRight(equalTo(GameCommand.Menu)))
        },
        test("number in range 1-9 returns Put command") {
          val results = ZIO.foreach(1 to 9) { n =>
            for {
              result        <- GameCommandParser.parse(s"$n").either
              expectedField <- ZIO.from(Field.make(n))
            } yield assert(result)(isRight(equalTo(GameCommand.Put(expectedField))))
          }
          results.flatMap(results => ZIO.from(results.reduceOption(_ && _)))
        },
        test("invalid command returns error") {
          checkM(invalidCommandsGen) { input =>
            val result = GameCommandParser.parse(input).either
            assertM(result)(isLeft(equalTo(ParseError)))
          }
        }
      )
    ).provideCustomLayer(GameCommandParserLive.layer)

  private val validCommands      = List(1 to 9)
  private val invalidCommandsGen = Gen.anyString.filter(!validCommands.contains(_))
}
