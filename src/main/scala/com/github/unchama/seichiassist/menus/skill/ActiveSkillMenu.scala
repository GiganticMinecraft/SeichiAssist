package com.github.unchama.seichiassist.menus.skill

import cats.effect.IO
import cats.effect.concurrent.Ref
import com.github.unchama.generic.CachedFunction
import com.github.unchama.itemstackbuilder.{AbstractItemStackBuilder, IconItemStackBuilder, SkullItemStackBuilder, TippedArrowItemStackBuilder}
import com.github.unchama.menuinventory.slot.button.{Button, RecomputedButton}
import com.github.unchama.menuinventory.{ChestSlotRef, Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.activeskill.{ActiveSkill, ActiveSkillRange, AssaultSkill, AssaultSkillRange, SeichiSkill, SkillDependency, SkillRange}
import com.github.unchama.seichiassist.data.XYZTuple
import com.github.unchama.seichiassist.data.player.PlayerSkillState
import com.github.unchama.seichiassist.menus.CommonButtons
import org.bukkit.ChatColor._
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.potion.PotionType

object ActiveSkillMenu extends Menu {
  private sealed trait SkillSelectionState
  private case object Locked extends SkillSelectionState
  private case object Unlocked extends SkillSelectionState
  private case object Selected extends SkillSelectionState

  import com.github.unchama.menuinventory.syntax._

  override val frame: MenuFrame = MenuFrame(5.chestRows, s"$DARK_PURPLE${BOLD}整地スキル選択")

  private case class ButtonComputations(player: Player) {

    import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.layoutPreparationContext
    import player._

    val skillStateRef: Ref[IO, PlayerSkillState] = ???

    val totalActiveSkillPoint: IO[Int] = ???

    val availableActiveSkillPoint: IO[Int] =
      for {
        skillState <- skillStateRef.get
        totalPoint <- totalActiveSkillPoint
      } yield {
        totalPoint - skillState.obtainedSkills.map(_.requiredActiveSkillPoint).sum
      }

    val computeStatusButton: IO[Button] = RecomputedButton(
      for {
        state <- skillStateRef.get
        availablePoints <- availableActiveSkillPoint
      } yield {
        val activeSkillSelectionLore: Option[String] =
          state.activeSkill.map(activeSkill =>
            s"$RESET${GREEN}現在選択しているアクティブスキル：${activeSkill.name}"
          )

        val assaultSkillSelectionLore: Option[String] =
          state.assaultSkill.map { assaultSkill =>
            val heading =
              if (assaultSkill == SeichiSkill.AssaultArmor)
                s"$RESET${GREEN}現在選択しているアサルトスキル："
              else
                s"$RESET${GREEN}現在選択している凝固スキル："

            s"$heading${assaultSkill.name}"
          }

        val itemStack =
          new SkullItemStackBuilder(getUniqueId)
            .title(s"$YELLOW$UNDERLINE$BOLD${getName}のアクティブスキルデータ")
            .lore(
              activeSkillSelectionLore.toList ++
                assaultSkillSelectionLore.toList ++
                List(s"$RESET${YELLOW}使えるアクティブスキルポイント：$availablePoints")
            )
            .build()
        Button(itemStack)
      }
    )

    def baseSkillIcon(skill: SeichiSkill): AbstractItemStackBuilder[Nothing] = {
      skill match {
        case skill: ActiveSkill =>
          skill match {
            case SeichiSkill.DualBreak =>
              new IconItemStackBuilder(Material.GLASS)
            case SeichiSkill.TrialBreak =>
              new IconItemStackBuilder(Material.STONE)
            case SeichiSkill.Explosion =>
              new IconItemStackBuilder(Material.COAL_ORE)
            case SeichiSkill.MirageFlare =>
              new IconItemStackBuilder(Material.IRON_ORE)
            case SeichiSkill.Dockarn =>
              new IconItemStackBuilder(Material.GOLD_ORE)
            case SeichiSkill.GiganticBomb =>
              new IconItemStackBuilder(Material.REDSTONE_ORE)
            case SeichiSkill.BrilliantDetonation =>
              new IconItemStackBuilder(Material.LAPIS_ORE)
            case SeichiSkill.LemuriaImpact =>
              new IconItemStackBuilder(Material.EMERALD_ORE)
            case SeichiSkill.EternalVice =>
              new IconItemStackBuilder(Material.DIAMOND_ORE)

            case SeichiSkill.TomBoy =>
              new IconItemStackBuilder(Material.SADDLE)
            case SeichiSkill.Thunderstorm =>
              new IconItemStackBuilder(Material.MINECART)
            case SeichiSkill.StarlightBreaker =>
              new IconItemStackBuilder(Material.STORAGE_MINECART)
            case SeichiSkill.EarthDivide =>
              new IconItemStackBuilder(Material.POWERED_MINECART)
            case SeichiSkill.HeavenGaeBolg =>
              new IconItemStackBuilder(Material.EXPLOSIVE_MINECART)
            case SeichiSkill.Decision =>
              new IconItemStackBuilder(Material.HOPPER_MINECART)

            case SeichiSkill.EbifriDrive =>
              new TippedArrowItemStackBuilder(PotionType.REGEN)
            case SeichiSkill.HolyShot =>
              new TippedArrowItemStackBuilder(PotionType.FIRE_RESISTANCE)
            case SeichiSkill.TsarBomba =>
              new TippedArrowItemStackBuilder(PotionType.INSTANT_HEAL)
            case SeichiSkill.ArcBlast =>
              new TippedArrowItemStackBuilder(PotionType.NIGHT_VISION)
            case SeichiSkill.PhantasmRay =>
              new TippedArrowItemStackBuilder(PotionType.SPEED)
            case SeichiSkill.Supernova =>
              new TippedArrowItemStackBuilder(PotionType.INSTANT_DAMAGE)
          }
        case skill: AssaultSkill =>
          skill match {
            case SeichiSkill.WhiteBreath =>
              new IconItemStackBuilder(Material.SNOW_BLOCK)
            case SeichiSkill.AbsoluteZero =>
              new IconItemStackBuilder(Material.ICE)
            case SeichiSkill.DiamondDust =>
              new IconItemStackBuilder(Material.PACKED_ICE)
            case SeichiSkill.LavaCondensation =>
              new IconItemStackBuilder(Material.NETHERRACK)
            case SeichiSkill.MoerakiBoulders =>
              new IconItemStackBuilder(Material.NETHER_BRICK)
            case SeichiSkill.Eldfell =>
              new IconItemStackBuilder(Material.MAGMA)
            case SeichiSkill.VenderBlizzard =>
              new IconItemStackBuilder(Material.NETHER_STAR)
            case SeichiSkill.AssaultArmor =>
              new IconItemStackBuilder(Material.DIAMOND_CHESTPLATE)
          }
      }
    }

    def prerequisiteSkillName(skill: SeichiSkill): String =
      SkillDependency.prerequisites(skill)
        .headOption.map(_.name)
        .getOrElse("なし")

    def breakRangeDescription(range: SkillRange): String = {
      val colorPrefix = s"$RESET$GREEN"
      val description = range match {
        case range: ActiveSkillRange =>
          range match {
            case ActiveSkillRange.MultiArea(effectChunkSize, areaCount) =>
              val XYZTuple(x, y, z) = effectChunkSize
              val count = if (areaCount > 1) s" x$areaCount" else ""
              s"${x}x${y}x${z}ブロック破壊" + count

            case ActiveSkillRange.RemoteArea(effectChunkSize) =>
              val XYZTuple(x, y, z) = effectChunkSize
              s"遠${x}x${y}x${z}ブロック破壊"
          }
        case range: AssaultSkillRange =>
          val XYZTuple(x, y, z) = range.effectChunkSize
          range match {
            case AssaultSkillRange.Armor(_) =>
              s"周囲のブロック${x}x${y}x${z}を破壊します"
            case AssaultSkillRange.Lava(_) =>
              s"周囲の溶岩${x}x${y}x${z}ブロックを固めます"
            case AssaultSkillRange.Liquid(_) =>
              s"周囲の水/溶岩${x}x${y}x${z}ブロックを固めます"
            case AssaultSkillRange.Water(_) =>
              s"周囲の水${x}x${y}x${z}ブロックを凍らせます"
          }
      }

      colorPrefix + description
    }

    def coolDownDescription(skill: SeichiSkill): String = {
      val colorPrefix = s"$RESET$DARK_GRAY"

      val coolDownAmount = skill.maxCoolDownTicks.map { ticks =>
        (ticks * 50).toString
          .replaceFirst("(\\d\\d)$", ".$1")
          .replaceFirst("0$", "")
      }.getOrElse("なし")

      colorPrefix + coolDownAmount
    }

    def selectionStateOf(skill: SeichiSkill)(skillState: PlayerSkillState): SkillSelectionState = {
      if (skillState.obtainedSkills.contains(skill)) {
        val selected = skill match {
          case skill: ActiveSkill =>
            skillState.activeSkill.contains(skill)
          case skill: AssaultSkill =>
            skillState.assaultSkill.contains(skill)
        }

        if (selected) Selected else Unlocked
      } else {
        Locked
      }
    }

    val seichiSkillButton: CachedFunction[(SkillSelectionState, SeichiSkill), Button] =
      CachedFunction { case (state, skill) =>
        val itemStack = {
          val base = state match {
            case Locked =>
              new IconItemStackBuilder(Material.BEDROCK)
            case Selected | Unlocked =>
              baseSkillIcon(skill)
          }

          val clickEffectDescription: List[String] = state match {
            case Locked => skill match {
              case skill: AssaultSkill => skill match {
                case SeichiSkill.VenderBlizzard =>
                  List(
                    s"$RESET${YELLOW}必要アクティブスキルポイント：${skill.requiredActiveSkillPoint}",
                    s"$RESET${DARK_RED}水凝固/熔岩凝固の双方を扱える者にのみ発現する上位凝固スキル",
                    s"$RESET${DARK_RED}アサルト・アーマーの発現には影響しない",
                    s"$RESET$AQUA${UNDERLINE}クリックで解除"
                  )
                case SeichiSkill.AssaultArmor =>
                  List(
                    s"$RESET${YELLOW}全てのスキルを獲得すると解除されます"
                  )
                case _ =>
                  List(
                    s"前提スキル：${prerequisiteSkillName(skill)}",
                    s"$RESET$AQUA${UNDERLINE}クリックで解除"
                  )
              }
              case skill: ActiveSkill =>
                List(
                  s"前提スキル：${prerequisiteSkillName(skill)}",
                  s"$RESET$AQUA${UNDERLINE}クリックで解除"
                )
            }
            case Unlocked => List(s"$RESET$DARK_RED${UNDERLINE}クリックでセット")
            case Selected => List(s"$RESET$DARK_RED${UNDERLINE}クリックで選択解除")
          }

          base
            .title(s"$RED$UNDERLINE$BOLD${skill.name}")
            .lore(
              List(
                s"$RESET$GREEN${breakRangeDescription(skill.range)}",
                s"$RESET${DARK_GRAY}クールダウン：${coolDownDescription(skill)}",
                s"$RESET${BLUE}消費マナ：${skill.manaCost}",
              ) ++ clickEffectDescription
            )

          if (state == Selected) base.enchanted()

          base.build()
        }

        ???
      }

    def computeSkillButtonFor(skill: SeichiSkill): IO[Button] = {
      for {
        state <- skillStateRef.get
      } yield seichiSkillButton((selectionStateOf(skill)(state), skill))
    }
  }

  private object ConstantButtons {
    val skillEffectMenuButton: Button = {
      ???
    }

    val resetSkillsButton: Button = {
      ???
    }
  }

  override def computeMenuLayout(player: Player): IO[MenuSlotLayout] = {
    import cats.implicits._
    import eu.timepit.refined.auto._

    val buttonComputations = ButtonComputations(player)
    import ConstantButtons._
    import buttonComputations._

    val constantPart = Map(
      ChestSlotRef(0, 1) -> resetSkillsButton,
      ChestSlotRef(0, 2) -> skillEffectMenuButton,
      ChestSlotRef(4, 0) -> CommonButtons.openStickMenu
    )

    import SeichiSkill._

    val dynamicPartComputation = List(
      ChestSlotRef(0, 0) -> computeStatusButton,

      ChestSlotRef(0, 3) -> computeSkillButtonFor(EbifriDrive),
      ChestSlotRef(0, 4) -> computeSkillButtonFor(HolyShot),
      ChestSlotRef(0, 5) -> computeSkillButtonFor(TsarBomba),
      ChestSlotRef(0, 6) -> computeSkillButtonFor(ArcBlast),
      ChestSlotRef(0, 7) -> computeSkillButtonFor(PhantasmRay),
      ChestSlotRef(0, 8) -> computeSkillButtonFor(Supernova),

      ChestSlotRef(1, 3) -> computeSkillButtonFor(TomBoy),
      ChestSlotRef(1, 4) -> computeSkillButtonFor(Thunderstorm),
      ChestSlotRef(1, 5) -> computeSkillButtonFor(StarlightBreaker),
      ChestSlotRef(1, 6) -> computeSkillButtonFor(EarthDivide),
      ChestSlotRef(1, 7) -> computeSkillButtonFor(HeavenGaeBolg),
      ChestSlotRef(1, 8) -> computeSkillButtonFor(Decision),

      ChestSlotRef(2, 0) -> computeSkillButtonFor(DualBreak),
      ChestSlotRef(2, 1) -> computeSkillButtonFor(TrialBreak),
      ChestSlotRef(2, 2) -> computeSkillButtonFor(Explosion),
      ChestSlotRef(2, 3) -> computeSkillButtonFor(MirageFlare),
      ChestSlotRef(2, 4) -> computeSkillButtonFor(Dockarn),
      ChestSlotRef(2, 5) -> computeSkillButtonFor(GiganticBomb),
      ChestSlotRef(2, 6) -> computeSkillButtonFor(BrilliantDetonation),
      ChestSlotRef(2, 7) -> computeSkillButtonFor(LemuriaImpact),
      ChestSlotRef(2, 8) -> computeSkillButtonFor(EternalVice),

      ChestSlotRef(3, 3) -> computeSkillButtonFor(WhiteBreath),
      ChestSlotRef(3, 4) -> computeSkillButtonFor(AbsoluteZero),
      ChestSlotRef(3, 5) -> computeSkillButtonFor(DiamondDust),

      ChestSlotRef(4, 3) -> computeSkillButtonFor(LavaCondensation),
      ChestSlotRef(4, 4) -> computeSkillButtonFor(MoerakiBoulders),
      ChestSlotRef(4, 5) -> computeSkillButtonFor(Eldfell),

      ChestSlotRef(1, 1) -> computeSkillButtonFor(AssaultArmor),
      ChestSlotRef(3, 1) -> computeSkillButtonFor(VenderBlizzard),
    )
      .map(_.sequence)
      .sequence

    for {
      dynamicPart <- dynamicPartComputation
    } yield MenuSlotLayout(constantPart ++ dynamicPart)
  }
}
