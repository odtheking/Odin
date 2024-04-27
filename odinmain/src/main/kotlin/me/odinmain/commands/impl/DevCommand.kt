package me.odinmain.commands.impl

import com.github.stivais.commodore.utils.GreedyString
import com.github.stivais.ui.UIScreen
import com.github.stivais.ui.basic
import com.github.stivais.ui.clickGUI
import kotlinx.coroutines.launch
import me.odinmain.OdinMain.display
import me.odinmain.OdinMain.mc
import me.odinmain.OdinMain.scope
import me.odinmain.commands.commodore
import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.features.ModuleManager.generateReadme
import me.odinmain.features.impl.dungeon.puzzlesolvers.TPMaze
import me.odinmain.features.impl.render.DevPlayers.updateDevs
import me.odinmain.utils.*
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.cryptsCount
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.deathCount
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.secretCount
import me.odinmain.utils.skyblock.dungeon.ScanUtils
import net.minecraft.util.ChatComponentText
import net.minecraftforge.common.MinecraftForge
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection


val devCommand = commodore("oddev") {
    //if (!isDev) return@commodore // i forgot to add require to recode lol

    literal("ui") {
        literal("basic").runs { display = UIScreen(basic()) }
        literal("clickgui").runs { display = UIScreen(clickGUI()) }
    }

    literal("getdata") {
        literal("entity").runs { copyEntityData() }
        literal("block").runs { copyBlockData() }
    }

    literal("testtp").runs {
        TPMaze.getCorrectPortals(
            mc.thePlayer.positionVector,
            mc.thePlayer.rotationYaw,
            mc.thePlayer.rotationPitch
        )
    }

    literal("particles").runs {
        sendCommand("particle flame 84 18 95 1 1 1 1 100")
        sendCommand("particle flame 57 18 125 1 1 1 1 100")
        sendCommand("particle flame 26 18 95 1 1 1 1 100")
        sendCommand("particle flame 27 18 60 1 1 1 1 100")
        sendCommand("particle flame 84 18 56 1 1 1 1 100")
    }

    literal("resettp").runs {
        TPMaze.correctPortals = listOf()
        TPMaze.portals = setOf()
    }

    literal("giveaotv").runs {
        sendCommand("give @p minecraft:diamond_shovel 1 0 {ExtraAttributes:{ethermerge:1b}}")
    }

    literal("devlist").runs {
        modMessage(updateDevs().entries.joinToString("\n\n"))
    }

    literal("sendServer").runs { string: String ->
        // """{"username": "${mc.thePlayer.name}", "version": "${if (OdinMain.onLegitVersion) "legit" else "cheater"} ${OdinMain.VERSION}"}"""
        scope.launch {
            sendDataToServer(string)
        }
        modMessage(string)
    }

    literal("adddev").runs { name: String, password: String ->
        modMessage("Sending data...")
        scope.launch {
            modMessage(sendDataToServer("$name, [1,2,3], [1,2,3], false, $password", "https://tj4yzotqjuanubvfcrfo7h5qlq0opcyk.lambda-url.eu-north-1.on.aws/"))
        }
    }


    literal("getServer").runs { string: String ->
        scope.launch {
            val data = getDataFromServer(string)
            modMessage(data)
        }
    }

    literal("getteammates").runs {
        modMessage("Teammates: ${DungeonUtils.dungeonTeammates.joinToString { "${it.name} (${it.clazz})" }}")
        modMessage("TeammatesNoSelf: ${DungeonUtils.dungeonTeammatesNoSelf.map { it.name }}")
        modMessage("LeapTeammates: ${DungeonUtils.leapTeammates.map { it.name }}")
        modMessage("Deaths: $deathCount, Secrets: $secretCount, Crypts: $cryptsCount")
    }

    literal("getlocation").runs {
        modMessage("currentarea: ${LocationUtils.currentArea}, currentroom: ${DungeonUtils.currentRoom}, currentdungeon ${LocationUtils.currentDungeon}, kuudratier ${LocationUtils.kuudraTier}, phase ${DungeonUtils.getPhase()?.displayName}")
    }

    literal("simulate").runs { str: GreedyString ->
        mc.thePlayer.addChatMessage(ChatComponentText(str.string))
        MinecraftForge.EVENT_BUS.post(ChatPacketEvent(str.string))
    }

	literal("roomdata").runs {
        val room = DungeonUtils.currentRoom //?: return@does modMessage("§cYou are not in a dungeon!")
        val x = ((mc.thePlayer.posX + 200) / 32).toInt()
        val z = ((mc.thePlayer.posZ + 200) / 32).toInt()
        val xPos = -185 + x * 32
        val zPos = -185 + z * 32
        val core = ScanUtils.getCore(xPos, zPos)
        modMessage(
            """
                    ${getChatBreak()}
                    Middle: $xPos, $zPos
                    Room: ${room?.room?.data?.name}
                    Core: $core
                    Rotation: ${room?.room?.rotation}
                    Positions: ${room?.positions}
                    ${getChatBreak()}
                    """.trimIndent(), false
        )
    }

    literal("getCore").runs {
        val core = ScanUtils.getCore(mc.thePlayer.posX.floor().toInt(), mc.thePlayer.posZ.floor().toInt())
        writeToClipboard(core.toString(), "Copied $core to clipboard!")
    }

    literal("generatereadme").runs {
        val readmeContent = generateReadme()

        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val stringSelection = StringSelection(readmeContent)
        clipboard.setContents(stringSelection, null)
        modMessage("§aCopied readme to clipboard!")
        println("Copied readme to clipboard!")
    }

    literal("test").runs {
        modMessage("Test")
    }

    literal("alert").runs {string: String ->
        PlayerUtils.alert(string)
    }
}


/*
object DevCommand : Commodore {


    override val command: CommandNode =
        literal("oddev") {
            requires {
                isDev
            }

            literal("getdata").runs { str: String ->
                if (str == "entity") copyEntityData()
                if (str == "block") copyBlockData()
            }

            literal("testtp").runs {
                TPMaze.getCorrectPortals(
                    mc.thePlayer.positionVector,
                    mc.thePlayer.rotationYaw,
                    mc.thePlayer.rotationPitch
                )
            }

            literal("particles").runs {
                sendCommand("particle flame 84 18 95 1 1 1 1 100")
                sendCommand("particle flame 57 18 125 1 1 1 1 100")
                sendCommand("particle flame 26 18 95 1 1 1 1 100")
                sendCommand("particle flame 27 18 60 1 1 1 1 100")
                sendCommand("particle flame 84 18 56 1 1 1 1 100")
            }

            literal("resettp").runs {
                TPMaze.correctPortals = listOf()
                TPMaze.portals = setOf()
            }

            literal("giveaotv").runs {
                sendCommand("give @p minecraft:diamond_shovel 1 0 {ExtraAttributes:{ethermerge:1b}}")
            }

            literal("devlist").runs {
                val regex = ".*${Regex.escape("[BOSS] Storm: hi!")}.*".toRegex()
                modMessage(regex)
                modMessage("aaaaa [BOSS] Storm: hi! aaaaa" matches regex)
                updateDevs()
            }

            literal("sendServer").runs { string: String ->
                // """{"username": "${mc.thePlayer.name}", "version": "${if (OdinMain.onLegitVersion) "legit" else "cheater"} ${OdinMain.VERSION}"}"""
                scope.launch {
                    sendDataToServer(string)
                }
                modMessage(string)
            }

            literal("adddev").runs { name: String, password: String ->
                modMessage("Sending data...")
                scope.launch {
                    modMessage(sendDataToServer("$name, [1,2,3], [1,2,3], false, $password", "https://tj4yzotqjuanubvfcrfo7h5qlq0opcyk.lambda-url.eu-north-1.on.aws/"))
                }
            }


            literal("getServer").runs { string: String ->
                scope.launch {
                    val data = getDataFromServer(string)
                    modMessage(data)
                }
            }

            literal("getteammates").runs {
                modMessage("Teammates: ${DungeonUtils.teammates.map { it.name }}")
                modMessage("TeammatesNoSelf: ${DungeonUtils.dungeonTeammatesNoSelf.map { it.name }}")
                modMessage("LeapTeammates: ${DungeonUtils.leapTeammates.map { it.name }}")
            }

            literal("simulate").runs { str: GreedyString ->
                mc.thePlayer.addChatMessage(ChatComponentText(str.string))
                MinecraftForge.EVENT_BUS.post(ChatPacketEvent(str.string))
            }

            literal("roomdata").runs {
                val room = DungeonUtils.currentRoom //?: return@does modMessage("§cYou are not in a dungeon!")
                val x = ((mc.thePlayer.posX + 200) / 32).toInt()
                val z = ((mc.thePlayer.posZ + 200) / 32).toInt()
                val xPos = -185 + x * 32
                val zPos = -185 + z * 32
                val core = ScanUtils.getCore(xPos, zPos)
                val northPos = DungeonUtils.Vec2(xPos, zPos - 4)
                val northCores = room?.positions?.map {
                    modMessage("Scanning ${it.x}, ${it.z - 4}: ${ScanUtils.getCore(it.x, it.z - 4)}")
                    ScanUtils.getCore(it.x, it.z - 4)
                } ?: listOf()

                modMessage(
                    """
                    ${getChatBreak()}
                    Middle: $xPos, $zPos
                    Room: ${room?.room?.data?.name}
                    Core: $core
                    North Core: $northCores
                    North Pos: ${northPos.x}, ${northPos.z}
                    Rotation: ${room?.room?.rotation}
                    Positions: ${room?.positions}
                    ${getChatBreak()}
                    """.trimIndent(), false
                )
                writeToClipboard(northCores.toString(), "Copied $northCores to clipboard!")
            }

            literal("getCore").runs {
                val core = ScanUtils.getCore(mc.thePlayer.posX.floor().toInt(), mc.thePlayer.posZ.floor().toInt())
                writeToClipboard(core.toString(), "Copied $core to clipboard!")
            }

            literal("generatereadme").runs {
                val readmeContent = generateReadme()

                val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                val stringSelection = StringSelection(readmeContent)
                clipboard.setContents(stringSelection, null)
                modMessage("§aCopied readme to clipboard!")
                println("Copied readme to clipboard!")
            }

            literal("test").runs {
                modMessage("Test")
            }
        }
}


 */