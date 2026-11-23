package core.cache.bzip2

class BZip2Decompressor {

    companion object {
        private var transform: IntArray = IntArray(0)
        private var state: BZip2BlockEntry = BZip2BlockEntry()

        @JvmStatic
        fun decompress(decompressedData: ByteArray, packedData: ByteArray, containerSize: Int, blockSize: Int) {
            synchronized (state) {
                state.input = packedData
                state.inputOffset = blockSize
                state.output = decompressedData
                state.outputOffset = 0
                state.outputRemaining = decompressedData.size
                state.bitsAvailable = 0
                state.bitBuffer = 0
                state.totalInput = 0
                state.totalOutput = 0
                decodeBlock(state)
                state.input = ByteArray(0)
                state.output = ByteArray(0)
            }
        }
        private fun buildSymbolMap(entry: BZip2BlockEntry) {
            entry.inUseCount = 0
            for (i in 0..255) {
                if (entry.inUse[i]) {
                    entry.symbolMap[entry.inUseCount] = i.toByte()
                    entry.inUseCount++
                }
            }
        }
        private fun buildHuffmanTable(ai: IntArray, ai1: IntArray, ai2: IntArray, abyte0: ByteArray, i: Int, j: Int, k: Int) {
            var l = 0
            for (i1 in i..j) {
                for (l2 in 0 until k) {
                    if (abyte0[l2].toInt() == i1) {
                        ai2[l] = l2
                        l++
                    }
                }
            }
            for (j1 in 0..22) {
                ai1[j1] = 0
            }
            for (k1 in 0 until k) {
                ai1[abyte0[k1] + 1]++
            }
            for (l1 in 1..22) {
                ai1[l1] += ai1[l1 - 1]
            }
            for (i2 in 0..22) {
                ai[i2] = 0
            }
            var i3 = 0
            for (j2 in i..j) {
                i3 += ai1[j2 + 1] - ai1[j2]
                ai[j2] = i3 - 1
                i3 = i3 shl 1
            }
            for (k2 in i + 1..j) {
                ai1[k2] = (ai[k2 - 1] + 1 shl 1) - ai1[k2]
            }
        }
        private fun writeBlock(entry: BZip2BlockEntry) {
            var byte4 = entry.aByte2201
            var i = entry.anInt2222
            var j = entry.anInt2227
            var k = entry.anInt2221
            var ai = transform
            var l = entry.anInt2208
            var abyte0 = entry.output
            var i1 = entry.outputOffset
            var j1 = entry.outputRemaining
            var k1 = j1
            var l1 = entry.anInt2225 + 1
            label0@ do {
                if (i > 0) {
                    do {
                        if (j1 == 0) {
                            break@label0
                        }
                        if (i == 1) {
                            break
                        }
                        abyte0[i1] = byte4
                        i--
                        i1++
                        j1--
                    }
                    while (true)
                        abyte0[i1] = byte4
                    i1++
                    j1--
                }
                var flag = true
                while (flag) {
                    flag = false
                    if (j == l1) {
                        i = 0
                        break@label0
                    }
                    byte4 = k.toByte()
                    l = ai[l]
                    var byte0 = (l and 0xff).toByte()
                    l = l shr 8
                    j++
                    if (byte0.toInt() != k) {
                        k = byte0.toInt()
                        if (j1 == 0) {
                            i = 1
                        } else {
                            abyte0[i1] = byte4
                            i1++
                            j1--
                            flag = true
                            continue
                        }
                        break@label0
                    }
                    if (j != l1) {
                        continue
                    }
                    if (j1 == 0) {
                        i = 1
                        break@label0
                    }
                    abyte0[i1] = byte4
                    i1++
                    j1--
                    flag = true
                }
                i = 2
                l = ai[l]
                var byte1 = (l and 0xff).toByte()
                l = l shr 8
                if (++j != l1) {
                    if (byte1.toInt() != k) {
                        k = byte1.toInt()
                    } else {
                        i = 3
                        l = ai[l]
                        var byte2 = (l and 0xff).toByte()
                        l = l shr 8
                        if (++j != l1) {
                            if (byte2.toInt() != k) {
                                k = byte2.toInt()
                            } else {
                                l = ai[l]
                                var byte3 = (l and 0xff).toByte()
                                l = l shr 8
                                j++
                                i = (byte3.toInt() and 0xff) + 4
                                l = ai[l]
                                k = (l and 0xff).toByte().toInt()
                                l = l shr 8
                                j++
                            }
                        }
                    }
                }
            }
            while (true)
                entry.totalOutput += k1 - j1
            entry.aByte2201 = byte4
            entry.anInt2222 = i
            entry.anInt2227 = j
            entry.anInt2221 = k
            transform = ai
            entry.anInt2208 = l
            entry.output = abyte0
            entry.outputOffset = i1
            entry.outputRemaining = j1
        }
        private fun readBit(entry: BZip2BlockEntry): Byte = readBits(1, entry).toByte()
        private fun readByte(state: BZip2BlockEntry): Byte = readBits(8, state).toByte()
        private fun readBits(i: Int, entry: BZip2BlockEntry): Int {
            var j: Int
            do {
                if (entry.bitsAvailable >= i) {
                    var k = (entry.bitBuffer shr entry.bitsAvailable - i) and (1 shl i) - 1
                    entry.bitsAvailable -= i
                    j = k
                    break
                }
                entry.bitBuffer = (entry.bitBuffer shl 8) or (entry.input[entry.inputOffset].toInt() and 0xff)
                entry.bitsAvailable += 8
                entry.inputOffset++
                entry.totalInput++
            }
            while (true)
                return j
        }
        private fun decodeBlock(state: BZip2BlockEntry) {
            var j8 = 0
            var ai: IntArray = IntArray(0)
            var ai1: IntArray = IntArray(0)
            var ai2: IntArray = IntArray(0)
            state.blockSize = 1
            if (transform.isEmpty()) {
                transform = IntArray(state.blockSize * 0x186a0)
            }
            var flag18 = true
            while (flag18) {
                var byte0 = readByte(state)
                if (byte0.toInt() == 23) {
                    return
                }
                repeat(9) { readByte(state) }
                readBit(state)
                state.originalPointer = 0
                byte0 = readByte(state)
                state.originalPointer = (state.originalPointer shl 8) or (byte0.toInt() and 0xff)
                byte0 = readByte(state)
                state.originalPointer = (state.originalPointer shl 8) or (byte0.toInt() and 0xff)
                byte0 = readByte(state)
                state.originalPointer = (state.originalPointer shl 8) or (byte0.toInt() and 0xff)
                for (j in 0..15) {
                    var byte1 = readBit(state)
                    if (byte1.toInt() == 1) {
                        state.inUseGroups[j] = true
                    } else {
                        state.inUseGroups[j] = false
                    }
                }
                for (k in 0..255) {
                    state.inUse[k] = false
                }
                for (l in 0..15) {
                    if (state.inUseGroups[l]) {
                        for (i3 in 0..15) {
                            var byte2 = readBit(state)
                            if (byte2.toInt() == 1) {
                                state.inUse[l * 16 + i3] = true
                            }
                        }
                    }
                }
                buildSymbolMap(state)
                var i4 = state.inUseCount + 2
                var j4 = readBits(3, state)
                var k4 = readBits(15, state)
                for (i1 in 0 until k4) {
                    var j3 = 0
                    do {
                        var byte3 = readBit(state)
                        if (byte3.toInt() == 0) {
                            break
                        }
                        j3++
                    }
                    while (true)
                        state.selectors[i1] = j3.toByte()
                }
                var abyte0 = ByteArray(6)
                for (byte16 in 0 until j4) {
                    abyte0[byte16.toInt()] = byte16.toByte()
                }
                for (j1 in 0 until k4) {
                    var byte17 = state.selectors[j1]
                    var byte15 = abyte0[byte17.toInt()]
                    while (byte17 > 0) {
                        abyte0[byte17.toInt()] = abyte0[byte17 - 1]
                        byte17--
                    }
                    abyte0[0] = byte15
                    state.selectorMtf[j1] = byte15
                }
                for (k3 in 0 until j4) {
                    var k6 = readBits(5, state)
                    for (k1 in 0 until i4) {
                        do {
                            var byte4 = readBit(state)
                            if (byte4.toInt() == 0) {
                                break
                            }
                            byte4 = readBit(state)
                            if (byte4.toInt() == 0) {
                                k6++
                            } else {
                                k6--
                            }
                        }
                        while (true)
                            state.codeLengths[k3][k1] = k6.toByte()
                    }
                }
                for (l3 in 0 until j4) {
                    var byte8: Byte = 32
                    var i = 0
                    for (l1 in 0 until i4) {
                        if (state.codeLengths[l3][l1] > i) {
                            i = state.codeLengths[l3][l1].toInt()
                        }
                        if (state.codeLengths[l3][l1] < byte8) {
                            byte8 = state.codeLengths[l3][l1]
                        }
                    }
                    buildHuffmanTable(state.limits[l3], state.permutations[l3], state.bases[l3], state.codeLengths[l3], byte8.toInt(), i, i4)
                    state.minimumLengths[l3] = byte8.toInt()
                }
                var l4 = state.inUseCount + 1
                var i5 = -1
                var j5 = 0
                for (i2 in 0..255) {
                    state.frequencies[i2] = 0
                }
                var i9 = 4095
                for (k8 in 15 downTo 0) {
                    for (l8 in 15 downTo 0) {
                        state.mtfSymbols[i9] = (k8 * 16 + l8).toByte()
                        i9--
                    }
                    state.mtfBases[k8] = i9 + 1
                }
                var l5 = 0
                if (j5 == 0) {
                    i5++
                    j5 = 50
                    var byte12 = state.selectorMtf[i5]
                    j8 = state.minimumLengths[byte12.toInt()]
                    ai = state.limits[byte12.toInt()]
                    ai2 = state.bases[byte12.toInt()]
                    ai1 = state.permutations[byte12.toInt()]
                }
                j5--
                var l6 = j8
                var k7: Int
                var byte9: Byte
                k7 = readBits(l6, state)
                while (k7 > ai[l6]) {
                    l6++
                    byte9 = readBit(state)
                    k7 = (k7 shl 1) or byte9.toInt()
                }
                var k5 = ai2[k7 - ai1[l6]]
                while (k5 != l4) {
                    if (k5 == 0 || k5 == 1) {
                        var i6 = -1
                        var j6 = 1
                        do {
                            if (k5 == 0) {
                                i6 += j6
                            } else if (k5 == 1) {
                                i6 += 2 * j6
                            }
                            j6 *= 2
                            if (j5 == 0) {
                                i5++
                                j5 = 50
                                var byte13 = state.selectorMtf[i5]
                                j8 = state.minimumLengths[byte13.toInt()]
                                ai = state.limits[byte13.toInt()]
                                ai2 = state.bases[byte13.toInt()]
                                ai1 = state.permutations[byte13.toInt()]
                            }
                            j5--
                            var i7 = j8
                            var l7: Int
                            var byte10: Byte
                            l7 = readBits(i7, state)
                            while (l7 > ai[i7]) {
                                i7++
                                byte10 = readBit(state)
                                l7 = (l7 shl 1) or byte10.toInt()
                            }
                            k5 = ai2[l7 - ai1[i7]]
                        }
                        while (k5 == 0 || k5 == 1)
                            i6++
                        var byte5 = state.symbolMap[state.mtfSymbols[state.mtfBases[0]].toInt() and 0xff]
                        state.frequencies[byte5.toInt() and 0xff] += i6
                        while (i6 > 0) {
                            transform[l5] = byte5.toInt() and 0xff
                            l5++
                            i6--
                        }
                    } else {
                        var i11 = k5 - 1
                        var byte6: Byte
                        if (i11 < 16) {
                            var i10 = state.mtfBases[0]
                            byte6 = state.mtfSymbols[i10 + i11]
                            while (i11 > 3) {
                                var j11 = i10 + i11
                                state.mtfSymbols[j11] = state.mtfSymbols[j11 - 1]
                                state.mtfSymbols[j11 - 1] = state.mtfSymbols[j11 - 2]
                                state.mtfSymbols[j11 - 2] = state.mtfSymbols[j11 - 3]
                                state.mtfSymbols[j11 - 3] = state.mtfSymbols[j11 - 4]
                                i11 -= 4
                            }
                            while (i11 > 0) {
                                state.mtfSymbols[i10 + i11] = state.mtfSymbols[(i10 + i11) - 1]
                                i11--
                            }
                            state.mtfSymbols[i10] = byte6
                        } else {
                            var k10 = i11 / 16
                            var l10 = i11 % 16
                            var j10 = state.mtfBases[k10] + l10
                            byte6 = state.mtfSymbols[j10]
                            while (j10 > state.mtfBases[k10]) {
                                state.mtfSymbols[j10] = state.mtfSymbols[j10 - 1]
                                j10--
                            }
                            state.mtfBases[k10]++
                            while (k10 > 0) {
                                state.mtfBases[k10]--
                                state.mtfSymbols[state.mtfBases[k10]] = state.mtfSymbols[(state.mtfBases[k10 - 1] + 16) - 1]
                                k10--
                            }
                            state.mtfBases[0]--
                            state.mtfSymbols[state.mtfBases[0]] = byte6
                            if (state.mtfBases[0] == 0) {
                                var l9 = 4095
                                for (j9 in 15 downTo 0) {
                                    for (k9 in 15 downTo 0) {
                                        state.mtfSymbols[l9] = state.mtfSymbols[state.mtfBases[j9] + k9]
                                        l9--
                                    }
                                    state.mtfBases[j9] = l9 + 1
                                }
                            }
                        }
                        state.frequencies[state.symbolMap[byte6.toInt() and 0xff].toInt() and 0xff]++
                        transform[l5] = state.symbolMap[byte6.toInt() and 0xff].toInt() and 0xff
                        l5++
                        if (j5 == 0) {
                            i5++
                            j5 = 50
                            var byte14 = state.selectorMtf[i5]
                            j8 = state.minimumLengths[byte14.toInt()]
                            ai = state.limits[byte14.toInt()]
                            ai2 = state.bases[byte14.toInt()]
                            ai1 = state.permutations[byte14.toInt()]
                        }
                        j5--
                        var j7 = j8
                        var i8: Int
                        var byte11: Byte
                        i8 = readBits(j7, state)
                        while (i8 > ai[j7]) {
                            j7++
                            byte11 = readBit(state)
                            i8 = (i8 shl 1) or byte11.toInt()
                        }
                        k5 = ai2[i8 - ai1[j7]]
                    }
                }
                state.anInt2222 = 0
                state.aByte2201 = 0
                state.cumulativeFrequencies[0] = 0
                for (j2 in 1..256) {
                    state.cumulativeFrequencies[j2] = state.frequencies[j2 - 1]
                }
                for (k2 in 1..256) {
                    state.cumulativeFrequencies[k2] += state.cumulativeFrequencies[k2 - 1]
                }
                for (l2 in 0 until l5) {
                    var byte7 = (transform[l2].toInt() and 0xff).toByte()
                    transform[state.cumulativeFrequencies[byte7.toInt() and 0xff]] = transform[state.cumulativeFrequencies[byte7.toInt() and 0xff]] or (l2 shl 8)
                    state.cumulativeFrequencies[byte7.toInt() and 0xff]++
                }
                state.anInt2208 = transform[state.originalPointer].toInt() shr 8
                state.anInt2227 = 0
                state.anInt2208 = transform[state.anInt2208]
                state.anInt2221 = (state.anInt2208 and 0xff).toByte().toInt()
                state.anInt2208 = state.anInt2208 shr 8
                state.anInt2227++
                state.anInt2225 = l5
                writeBlock(state)
                if (state.anInt2227 == state.anInt2225 + 1 && state.anInt2222 == 0) {
                    flag18 = true
                } else {
                    flag18 = false
                }
            }
        }
    }
}
