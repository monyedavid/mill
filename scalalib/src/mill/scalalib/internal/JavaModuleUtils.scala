package mill.scalalib.internal

import mill.define.Module
import mill.scalalib.JavaModule

@mill.api.internal
object JavaModuleUtils {

  /**
   * Compute all transitive modules from module children and via moduleDeps + compileModuleDeps
   */
  def transitiveModules(module: Module, accept: Module => Boolean = _ => true): Seq[Module] = {
    def loop(mod: Module, found: Seq[Module]): Seq[Module] = {
      if (!accept(mod) || found.contains(mod))
        found
      else {
        val subMods = mod.moduleDirectChildren ++ (mod match {
          case jm: JavaModule => jm.moduleDepsChecked ++ jm.compileModuleDepsChecked
          case other => Seq.empty
        })
        subMods.foldLeft(found ++ Seq(mod)) { (all, mod) => loop(mod, all) }
      }
    }

    loop(module, Seq.empty)
  }

}
