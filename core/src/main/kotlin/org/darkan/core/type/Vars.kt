package org.darkan.core.type

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.darkan.core.net.Session
import org.darkan.core.net.prot.ClearVarps
import org.darkan.core.net.prot.setVarcPacket
import org.darkan.core.net.prot.setVarcStrPacket
import org.darkan.core.net.prot.setVarpPacket
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Config
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.definition.decoder.VarBitDecoder

@Serializable
class Vars(val vars: MutableMap<Int, Int> = HashMap()) {
    companion object {
        val BIT_MASKS = IntArray(32).apply {
            var value = 2
            for (i in 0 until 32) {
                this[i] = value - 1
                value *= 2
            }
        }
    }

    @Transient
    private var modified: MutableSet<Int> = HashSet()
    @Transient
    private lateinit var varpValues: IntArray
    @Transient
    private lateinit var session: Session

    fun setSession(session: Session): Vars {
        this.session = session
        varpValues = IntArray(Cache.get().fileCount(Index.CONFIGS, Config.VARP)+1)
        modified = HashSet()
        vars.forEach { (varId, value) -> setVar(varId, value) }
        return this
    }

    fun setVar(id: Int, value: Int, forceSend: Boolean = false, save: Boolean = false) {
        if (forceSend)
            modified.add(id)
        if (id < 0 || id >= varpValues.size || varpValues[id] == value)
            return
        varpValues[id] = value
        if (save)
            vars[id] = value
        modified.add(id)
    }

    fun saveVar(id: Int, value: Int) = setVar(id, value, forceSend = false, save = true)

    fun setVarBit(id: Int, value: Int, forceSend: Boolean = false, save: Boolean = false) {
        val defs = Cache.varbits[id]
        val bitLength = defs.endBit - defs.startBit
        val mask = BIT_MASKS[bitLength]

        val cappedValue = value.coerceIn(0, mask)
        val shiftedMask = mask shl defs.startBit
        val varpValue = (varpValues[defs.index] and shiftedMask.inv()) or ((cappedValue shl defs.startBit) and shiftedMask)

        if (varpValue != varpValues[defs.index])
            setVar(defs.index, varpValue, forceSend, save)
    }

    fun saveVarBit(id: Int, value: Int) = setVarBit(id, value, forceSend = false, save = true)

    fun getVar(id: Int) = varpValues[id]

    fun getVarBit(id: Int): Int {
        val defs = Cache.varbits[id]
        val bitLength = defs.endBit - defs.startBit
        return (varpValues[defs.index] shr defs.startBit) and BIT_MASKS[bitLength]
    }

    fun bitFlagged(id: Int, bit: Int) = (varpValues[id] and (1 shl bit)) != 0

    suspend fun syncVarsToClient() {
        modified.forEach { id ->
            session.send(setVarpPacket(id, varpValues[id]))
        }
        modified.clear()
    }

    suspend fun clearVars() {
        varpValues.fill(0)
        session.send(ClearVarps())
    }

    suspend fun setVarc(id: Int, value: Int) = session.send(setVarcPacket(id, value))
    suspend fun setVarcStr(id: Int, value: String) = session.send(setVarcStrPacket(id, value))
}