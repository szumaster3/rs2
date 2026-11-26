package core.net.packet.`in`

import com.moandjiezana.toml.Toml
import core.ServerConstants
import core.ServerStore
import core.ServerStore.Companion.addToList
import core.ServerStore.Companion.getList
import core.api.log
import core.auth.AuthResponse
import core.cache.crypto.ISAACCipher
import core.cache.crypto.ISAACPair
import core.cache.misc.buffer.ByteBufferUtils
import core.game.node.entity.player.Player
import core.game.node.entity.player.info.*
import core.game.node.entity.player.info.login.LoginParser
import core.game.world.GameWorld
import core.game.world.repository.Repository
import core.integration.discord.Discord
import core.net.IoSession
import core.tools.Log
import core.tools.StringUtils
import core.worker.ManagementEvents.publish
import proto.management.JoinClanRequest
import proto.management.PlayerStatusUpdate
import proto.management.RequestContactInfo
import java.io.File
import java.math.BigInteger
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64
import core.game.system.task.Pulse
import core.game.system.SystemManager

object Login {
    private const val ENCRYPTION_VERIFICATION_BYTE: Int = 10
    private const val NORMAL_LOGIN_OP = 16
    private const val RECONNECT_LOGIN_OP = 18
    const val CACHE_INDEX_COUNT = 29

    private var exceptionData: Toml? = null
    private var lastModifiedData = 0L

    private val loginKey: RSAPrivateKey by lazy {
        val keyFile = File("data/", "private.pem")

        require(keyFile.isFile) {
            "RSA key not found: ${keyFile.absolutePath}"
        }

        val encoded = keyFile.readLines()
            .filterNot { it.startsWith("-----") }
            .joinToString("")
            .trim()

        KeyFactory.getInstance("RSA")
            .generatePrivate(PKCS8EncodedKeySpec(Base64.getDecoder().decode(encoded))) as RSAPrivateKey
    }

    fun decodeFromBuffer(buffer: ByteBuffer, serverKey: Long): Pair<AuthResponse, LoginInfo?> {
        try {
            val info = LoginInfo.createDefault()

            info.opcode = buffer.get().toInt()
            if ((buffer.short.toInt() and 0xffff) != buffer.remaining()) {
                return Pair(AuthResponse.BadSessionID, null)
            }
            val revision = buffer.int
            if (revision != ServerConstants.REVISION) {
                return Pair(AuthResponse.Updated, null)
            }
            if (info.opcode != NORMAL_LOGIN_OP && info.opcode != RECONNECT_LOGIN_OP) {
                log(this::class.java, Log.WARN, "Invalid Login Opcode: ${info.opcode}")
                return Pair(AuthResponse.InvalidLoginServer, null)
            }

            noop(buffer)
            info.windowMode = buffer.get().toInt() and 0xff
            info.screenWidth = buffer.short.toInt() and 0xffff
            info.screenHeight = buffer.short.toInt() and 0xffff
            info.displayMode = buffer.get().toInt() and 0xff
            noop(buffer, 24) //Skip past a bunch of random (actually random) data the client sends
            ByteBufferUtils.getString(buffer) //same as above
            info.adAffiliateId = buffer.int
            noop(buffer, buffer.get().toInt() and 0xff)
            info.currentPacketCount = buffer.short.toInt() and 0xffff

            //Read client-reported CRC sums
            for (i in 0 until CACHE_INDEX_COUNT) info.crcSums[i] = buffer.int

            val decryptedBuffer = decryptRSABuffer(buffer, loginKey.privateExponent, loginKey.modulus)
            if (buffer.hasRemaining()) return Pair(AuthResponse.BadSessionID, null)

            if (decryptedBuffer.get().toInt() != ENCRYPTION_VERIFICATION_BYTE) {
                return Pair(AuthResponse.UnexpectedError, info)
            }

            val seed = IntArray(4) { decryptedBuffer.int }
            if (seed[2] != (serverKey ushr 32).toInt() || seed[3] != serverKey.toInt()) {
                return Pair(AuthResponse.BadSessionID, null)
            }
            info.isaacPair = produceISAACPairFrom(seed)
            info.username = StringUtils.longToString(decryptedBuffer.long)
            info.password = ByteBufferUtils.getString(decryptedBuffer)

            if (decryptedBuffer.hasRemaining()) return Pair(AuthResponse.BadSessionID, null)
            if (info.opcode == NORMAL_LOGIN_OP && Repository.getPlayerByName(info.username) != null) {
                return Pair(AuthResponse.AlreadyOnline, info)
            }

            return Pair(AuthResponse.Success, info)
        } catch (buf: BufferUnderflowException) {
            //some issue in either the data they sent us or how we read it, either way out of scope of this class's handling.
            return Pair(AuthResponse.UnexpectedError, null)
        } catch (e: Exception) {
            log(this::class.java, Log.ERR, "Exception encountered during login packet parsing! See stack trace below.")
            e.printStackTrace()
            return Pair(AuthResponse.UnexpectedError, null)
        }
    }

    private fun produceISAACPairFrom(incomingSeed: IntArray): ISAACPair {
        val inCipher = ISAACCipher(incomingSeed)
        for (i in incomingSeed.indices) {
            incomingSeed[i] += 50
        }
        val outCipher = ISAACCipher(incomingSeed)
        return ISAACPair(inCipher, outCipher)
    }

    @JvmStatic
    fun decryptRegistrationRSA(buffer: ByteBuffer): ByteBuffer =
        decryptRSABuffer(buffer, loginKey.privateExponent, loginKey.modulus)

    @JvmStatic
    fun decryptRSABuffer(buffer: ByteBuffer, exponent: BigInteger, modulus: BigInteger): ByteBuffer {
        return try {
            val numBytes = buffer.get().toInt() and 0xFF
            val encryptedBytes = ByteArray(numBytes)
            buffer.get(encryptedBytes)

            val encryptedBigInt = BigInteger(encryptedBytes)
            ByteBuffer.wrap(encryptedBigInt.modPow(exponent, modulus).toByteArray())
        } catch (e: BufferUnderflowException) {
            ByteBuffer.wrap(byteArrayOf(-1))
        }
    }

    private fun noop(buffer: ByteBuffer, amount: Int = 1) {
        buffer.get(ByteArray(amount))
    }

    fun proceedWith(session: IoSession, details: PlayerDetails, opcode: Int) {
        if (opcode == RECONNECT_LOGIN_OP) {
            GameWorld.Pulser.submit(object : Pulse(1) {
                override fun pulse(): Boolean {
                    if (!session.isActive) return true
                    if (SystemManager.isUpdating) {
                        session.write(AuthResponse.Updating)
                        return true
                    }
                    val player = Repository.getPlayerByUid(details.uid)
                    if (player == null || player.session.isActive ||
                        Repository.disconnectionQueue[player.name] !== player || player.details.isBanned) {
                        session.write(AuthResponse.BadSessionID)
                        return true
                    }
                    Repository.disconnectionQueue.remove(player.name)
                    player.details.session = session
                    session.setObject(player)
                    session.lastPing = System.currentTimeMillis()
                    LoginParser(player.details).reconnect(player)
                    return true
                }
            })
            return
        }
        if (Repository.uid_map.contains(details.uid)) {
            session.write(AuthResponse.AlreadyOnline)
            return
        }
        details.session = session
        details.info.translate(UIDInfo(details.ipAddress, "DEPRECATED", "DEPRECATED", "DEPRECATED"))

        val archive = ServerStore.getArchive("flagged-ips")
        val flaggedIps = archive.getList<String>("ips")
        if (flaggedIps.contains(details.ipAddress)) {
            Discord.postPlayerAlert(details.username, "Login from flagged IP ${details.ipAddress}")
        }

        val player = Player(details)
        PlayerMonitor.log(player, LogType.IP_LOG, details.ipAddress)
        if (canBypassAccountLimitCheck(player)) {
            proceedWithAcceptableLogin(session, player, opcode)
        } else {
            if (checkAccountLimit(details.ipAddress, details.username)) {
                proceedWithAcceptableLogin(session, player, opcode)
            } else {
                session.write(AuthResponse.LoginLimitExceeded)
            }
        }
    }

    private fun canBypassAccountLimitCheck(player: Player): Boolean {
        return player.rights == Rights.ADMINISTRATOR || player.rights == Rights.PLAYER_MODERATOR
    }

    private fun proceedWithAcceptableLogin(session: IoSession, player: Player, opcode: Int) {
        Repository.addPlayer(player)
        session.lastPing = System.currentTimeMillis()
        try {
            LoginParser(player.details).initialize(player, opcode == RECONNECT_LOGIN_OP)
            sendMSEvents(player.details)
        } catch (e: Exception) {
            e.printStackTrace()
            session.disconnect()
        }
    }

    private fun checkAccountLimit(ipAddress: String, username: String): Boolean {
        var accountLimit = ServerConstants.DAILY_ACCOUNT_LIMIT

        val f = File(ServerConstants.CONFIG_PATH!!)
            .resolve("account_limit_exceptions.conf")

        if (f.exists()) {
            try {
                if (f.lastModified() != lastModifiedData) {
                    exceptionData = Toml().read(f)
                    lastModifiedData = f.lastModified()
                }

                if (exceptionData?.contains("exceptions.\"${ipAddress}\"") == true) {
                    accountLimit = exceptionData?.getLong("exceptions.\"${ipAddress}\"", 0L)?.toInt() ?: accountLimit
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val archive = ServerStore.getArchive("daily-accounts")
        val accounts = archive.getList<String>(ipAddress)
        if (username in accounts) return true

        if (accounts.size >= accountLimit) return false

        archive.addToList(ipAddress, username)
        return true
    }

    private fun sendMSEvents(details: PlayerDetails) {
        val statusEvent = PlayerStatusUpdate.newBuilder()
        statusEvent.username = details.username
        statusEvent.world = GameWorld.settings!!.worldId
        statusEvent.notifyFriendsOnly = false
        publish(statusEvent.build())

        val contactEvent = RequestContactInfo.newBuilder()
        contactEvent.username = details.username
        contactEvent.world = GameWorld.settings!!.worldId
        publish(contactEvent.build())

        if (!details.communication.currentClan.isNullOrEmpty() && details.communication.clan == null) {
            val clanEvent = JoinClanRequest.newBuilder()
            clanEvent.username = details.username
            clanEvent.clanName = details.communication.currentClan
            publish(clanEvent.build())
        }
    }
}
