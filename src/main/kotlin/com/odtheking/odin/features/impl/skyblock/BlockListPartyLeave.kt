package com.odtheking.odin.features.impl.skyblock

import com.odtheking.odin.clickgui.settings.impl.ActionSetting
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ListSetting
import com.odtheking.odin.clickgui.settings.impl.StringSetting
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.MessageSentEvent
import com.odtheking.odin.events.PartyEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.sendCommand
import com.odtheking.odin.utils.skyblock.LocationUtils
import com.odtheking.odin.utils.skyblock.PartyUtils

object BlockListPartyLeave : Module(
    name = "Block List Party Leave",
    description = "Leaves parties that contain players from your cached Hypixel block list."
) {
    private val onlySkyblock by BooleanSetting("Only Skyblock", true, desc = "Only leave parties while in Skyblock.")
    private val leaveMessage by StringSetting("Leave Message", "I left this party because I don't play with <NAME>", 128, desc = "Party chat message sent before leaving. Use <NAME> or {name}.")
    private val refreshBlockList by ActionSetting("Refresh Block List", desc = "Runs /block list so Odin can update the cached block list.") {
        sendCommand("block list")
    }
    private val clearBlockList by ActionSetting("Clear Block Cache", desc = "Clears Odin's cached block list.") {
        blockedPlayers.clear()
        scannedPages.clear()
        modMessage("§aCleared cached block list.")
    }
    private val blockedPlayers by ListSetting<String, MutableList<String>>("Blocked Players", mutableListOf()).hide()

    private val sentBlockCommandRegex = Regex("^/?block\\s+(add|remove)\\s+(\\w{1,16})(?:\\s.*)?$", RegexOption.IGNORE_CASE)
    private val blockListHeaderRegex = Regex("^-+ Blocked Players \\(Page (\\d+) of (\\d+)\\) -+$")
    private val blockListEntryRegex = Regex("^\\d+\\.\\s+(\\w{1,16})\\s*$")
    private val blockAddRegex = Regex("^(?:Added|You (?:have )?added) .*?(\\w{1,16}).*\\bblock(?:ed)?\\b.*$", RegexOption.IGNORE_CASE)
    private val blockRemoveRegex = Regex("^(?:Removed|You (?:have )?removed) .*?(\\w{1,16}).*\\bblock(?:ed)?\\b.*$", RegexOption.IGNORE_CASE)
    private val emptyBlockListRegex = Regex("^(?:You have no blocked players\\.?|You don't have any blocked players\\.?|Your block list is empty\\.?)$", RegexOption.IGNORE_CASE)

    private val scannedPages = linkedMapOf<Int, MutableSet<String>>()

    private var currentListPage: Int? = null
    private var expectedListPages = 0
    private var scanToken = 0
    private var leavingParty = false

    init {
        on<MessageSentEvent> {
            val match = sentBlockCommandRegex.find(message.trim()) ?: return@on
            val playerName = match.groupValues[2]

            when (match.groupValues[1].lowercase()) {
                "add" -> addBlockedPlayer(playerName)
                "remove" -> removeBlockedPlayer(playerName)
            }
        }

        on<ChatPacketEvent> {
            if (handleBlockListMessage(value)) return@on

            blockAddRegex.find(value)?.groupValues?.getOrNull(1)?.let {
                addBlockedPlayer(it)
                return@on
            }

            blockRemoveRegex.find(value)?.groupValues?.getOrNull(1)?.let {
                removeBlockedPlayer(it)
                return@on
            }

            if (emptyBlockListRegex.matches(value)) {
                blockedPlayers.clear()
                scannedPages.clear()
                checkParty()
                return@on
            }
        }

        on<PartyEvent.Join> {
            checkParty()
        }

        on<PartyEvent.Leave> {
            schedule(1) {
                if (!PartyUtils.isInParty) leavingParty = false
            }
        }

        on<LevelEvent.Load> {
            leavingParty = false
        }
    }

    override fun onEnable() {
        super.onEnable()
        checkParty()
    }

    private fun handleBlockListMessage(message: String): Boolean {
        blockListHeaderRegex.find(message)?.let { match ->
            val page = match.groupValues[1].toIntOrNull() ?: return true
            val totalPages = match.groupValues[2].toIntOrNull() ?: return true

            currentListPage = page
            expectedListPages = totalPages
            if (page == 1) scannedPages.clear()
            scannedPages[page] = linkedSetOf()

            val token = ++scanToken
            schedule(40) {
                if (scanToken == token) currentListPage = null
            }
            return true
        }

        val page = currentListPage ?: return false
        val entry = blockListEntryRegex.find(message)
        if (entry == null) {
            currentListPage = null
            return false
        }

        val playerName = entry.groupValues[1]
        scannedPages.getOrPut(page) { linkedSetOf() }.add(playerName)
        addBlockedPlayer(playerName)
        replaceCacheIfFullListWasScanned()
        return true
    }

    private fun replaceCacheIfFullListWasScanned() {
        if (expectedListPages <= 0 || scannedPages.size < expectedListPages) return
        if ((1..expectedListPages).any { it !in scannedPages }) return

        blockedPlayers.clear()
        scannedPages.values.flatten().forEach { addBlockedPlayer(it, checkPartyNow = false) }
        checkParty()
    }

    private fun addBlockedPlayer(playerName: String, checkPartyNow: Boolean = true) {
        if (!playerName.isValidIgn()) return

        val existingIndex = blockedPlayers.indexOfFirst { it.equals(playerName, true) }
        if (existingIndex >= 0) blockedPlayers[existingIndex] = playerName
        else blockedPlayers.add(playerName)

        if (checkPartyNow) checkParty()
    }

    private fun removeBlockedPlayer(playerName: String) {
        blockedPlayers.removeAll { it.equals(playerName, true) }
        checkParty()
    }

    private fun checkParty() {
        if (leavingParty || !PartyUtils.isInParty) return
        if (onlySkyblock && !LocationUtils.isInSkyblock) return

        val ownName = mc.player?.gameProfile?.name
        val blockedMember = PartyUtils.members.firstOrNull { partyMember ->
            !partyMember.equals(ownName, true) && blockedPlayers.any { it.equals(partyMember, true) }
        } ?: return

        leavingParty = true
        val message = leaveMessage
            .replace("<NAME>", blockedMember)
            .replace("{name}", blockedMember)
            .trim()

        if (message.isNotEmpty()) sendCommand("pchat $message")
        schedule(8) {
            if (PartyUtils.isInParty) sendCommand("p leave")
        }
    }

    private fun String.isValidIgn(): Boolean =
        length in 1..16 && all { it == '_' || it in '0'..'9' || it in 'A'..'Z' || it in 'a'..'z' }
}
