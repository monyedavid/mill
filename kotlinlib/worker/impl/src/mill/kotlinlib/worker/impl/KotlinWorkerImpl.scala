/*
 * Original code copied from https://github.com/lefou/mill-kotlin
 * Original code published under the Apache License Version 2
 * Original Copyright 2020-2024 Tobias Roeser
 */
package mill.kotlinlib.worker.impl

import mill.api.{Ctx, Result}
import mill.kotlinlib.worker.api.{KotlinWorker, KotlinWorkerTarget}
import org.jetbrains.kotlin.cli.js.K2JSCompiler
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler

import scala.annotation.nowarn

class KotlinWorkerImpl extends KotlinWorker {

  def compile(target: KotlinWorkerTarget, args: Seq[String])(implicit ctx: Ctx): Result[Unit] = {
    ctx.log.debug("Using kotlin compiler arguments: " + args.map(v => s"'${v}'").mkString(" "))

    @nowarn("msg=match may not be exhaustive") // false positive
    val compiler = target match {
      case KotlinWorkerTarget.Jvm => new K2JVMCompiler()
      case KotlinWorkerTarget.Js => new K2JSCompiler()
    }
    val exitCode = compiler.exec(ctx.log.streams.err, args*)
    if (exitCode.getCode != 0) {
      Result.Failure(s"Kotlin compiler failed with exit code ${exitCode.getCode} ($exitCode)")
    } else {
      Result.Success(())
    }
  }

}
