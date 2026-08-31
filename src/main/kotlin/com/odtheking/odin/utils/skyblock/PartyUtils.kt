package com.odtheking.odin.utils.skyblock

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.ChatMessageEvent
import com.odtheking.odin.events.PartyEvent
import com.odtheking.odin.events.core.on

object PartyUtils {

    private val joinedSelf = Regex("^You have joined ((?:\\[[^]]*?])? ?)?(\\w{1,16})'s? party!$")
    private val joinedOther = Regex("^((?:\\[[^]]*?])? ?)?(\\w{1,16}) joined the party\\.$")
    private val leftParty = Regex("^((?:\\[[^]]*?])? ?)?(\\w{1,16}) has left the party\\.$")
    private val kickedParty = Regex("^((?:\\[[^]]*?])? ?)?(\\w{1,16}) has been removed from the party\\.$")
    private val kickedOffline = Regex("^Kicked ((?:\\[[^]]*?])? ?)?(\\w{1,16}) because they were offline\\.$")
    private val kickedDisconnected = Regex("^((?:\\[[^]]*?])? ?)?(\\w{1,16}) was removed from your party because they disconnected\\.$")
    private val transferLeave = Regex("^The party was transferred to ((?:\\[[^]]*?])? ?)?(\\w{1,16}) because ((?:\\[[^]]*?])? ?)?(\\w{1,16}) left$")
    private val transferBy = Regex("^The party was transferred to ((?:\\[[^]]*?])? ?)?(\\w{1,16}) by ((?:\\[[^]]*?])? ?)?(\\w{1,16})$")
    private val partyChat = Regex("^Party > ((?:\\[[^]]*?])? ?)?(\\w{1,16}): (.+)$")
    private val partyInvite = Regex("^((?:\\[[^]]*?])? ?)?(\\w{1,16}) invited ((?:\\[[^]]*?])? ?)?(\\w{1,16}) to the party! They have 60 seconds to accept.$")
    private val leaderDisconnected = Regex("^The party leader, ((?:\\[[^]]*?])? ?)?(\\w{1,16}) has disconnected, they have 5 minutes to rejoin before the party is disbanded\\.$")
    private val leaderRejoined = Regex("^The party leader ((?:\\[[^]]*?])? ?)?(\\w{1,16}) has rejoined\\.$")
    private val memberFormat = Regex("^((?:\\[[^]]*?])? ?)?(\\w{1,16})$")
    private val partyWith = Regex("^You'll be partying with: (.+)$")

    private val queuedInFinder = Regex("^Party Finder > Your party has been queued in the dungeon finder!$")
    private val dungeonJoin = Regex("^Party Finder > (\\w{1,16}) joined the dungeon group! \\((\\w+) Level (\\d+)\\)$")
    private val kuudraJoin = Regex("^Party Finder > ((?:\\[[^]]*?])? ?)?(\\w{1,16}) joined the group! \\(Combat Level (\\d+)\\)$")
    private val membersList = Regex("^Party (Leader|Moderators|Members): (.+)$")

    private val disbandPatterns = listOf(
        Regex("^((?:\\[[^]]*?])? ?)?(\\w{1,16}) has disbanded the party!$"),
        Regex("^You have been kicked from the party by ((?:\\[[^]]*?])? ?)?(\\w{1,16})$"),
        Regex("^The party was disbanded because all invites expired and the party was empty.$"),
        Regex("^The party was disbanded because the party leader disconnected.$"),
        Regex("^You left the party.$"),
        Regex("^You are not currently in a party.$")
    )

    val members = mutableListOf<String>()

    var partyLeader: String? = null
        private set

    var isInParty: Boolean = false
        private set

    init {
        on<ChatMessageEvent> {
            joinedOther.find(value)?.let { return@on addMember(it.groupValues[2]) }

            joinedSelf.find(value)?.let {
                addMember(it.groupValues[2])
                partyLeader = it.groupValues[2]
                addMember(mc.player?.gameProfile?.name ?: return@on)
                return@on
            }

            leftParty.find(value)?.let { return@on removeMember(it.groupValues[2]) }

            kickedParty.find(value)?.let { return@on removeMember(it.groupValues[2]) }

            kickedOffline.find(value)?.let { return@on removeMember(it.groupValues[2]) }

            kickedDisconnected.find(value)?.let { return@on removeMember(it.groupValues[2]) }

            transferBy.find(value)?.let {
                addMember(it.groupValues[2])
                addMember(it.groupValues[4])
                partyLeader = it.groupValues[2]
                return@on
            }

            transferLeave.find(value)?.let {
                addMember(it.groupValues[2])
                partyLeader = it.groupValues[2]
                removeMember(it.groupValues[4])
                return@on
            }

            leaderDisconnected.find(value)?.let {
                partyLeader = it.groupValues[2]
                return@on
            }

            leaderRejoined.find(value)?.let {
                partyLeader = it.groupValues[2]
                return@on
            }

            partyChat.find(value)?.let {
                addMember(it.groupValues[2])
                return@on
            }

            partyInvite.find(value)?.let {
                addMember(it.groupValues[2])
                if (partyLeader == null) partyLeader = it.groupValues[2]
                return@on
            }

            queuedInFinder.find(value)?.let {
                addMember(mc.player?.gameProfile?.name ?: return@on)
                if (partyLeader == null) partyLeader = mc.player?.gameProfile?.name
                return@on
            }

            for (pattern in disbandPatterns) {
                if (pattern.containsMatchIn(value)) return@on disband()
            }

            membersList.find(value)?.let { match ->
                val type = match.groupValues[1]

                match.groupValues[2].split(" ●").forEach { segment ->
                    val memberMatch = memberFormat.find(segment.trim()) ?: return@forEach
                    addMember(memberMatch.groupValues[2])
                    if (type == "Leader") partyLeader = memberMatch.groupValues[2]

                    return@on
                }
            }

            partyWith.find(value)?.let { match ->
                match.groupValues[1].split(", ").forEach { playerName ->
                    val memberMatch = memberFormat.find(playerName.trim()) ?: return@forEach
                    addMember(memberMatch.groupValues[2])
                }
                return@on
            }

            kuudraJoin.find(value)?.let { return@on addMember(it.groupValues[2]) }

            dungeonJoin.find(value)?.let { return@on addMember(it.groupValues[1]) }
        }
    }

    private fun addMember(playerName: String) {
        if (!isInParty) isInParty = true
        if (playerName !in members) members.add(playerName)
    }

    private fun removeMember(playerName: String) {
        if (playerName !in members) return
        members.remove(playerName)

        PartyEvent.Leave(members).postAndCatch()
        if (members.isEmpty()) disband()
    }

    private fun disband() {
        PartyEvent.Leave(members).postAndCatch()
        members.clear()
        partyLeader = null
        isInParty = false
    }

    fun isLeader(): Boolean = partyLeader == mc.player?.gameProfile?.name
}