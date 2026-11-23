package core.cache.bzip2

class BZip2BlockEntry {
    var inUseGroups: BooleanArray = BooleanArray(16)
    var inUse: BooleanArray = BooleanArray(256)
    var aByte2201: Byte = 0
    var mtfSymbols: ByteArray = ByteArray(4096)
    var symbolMap: ByteArray = ByteArray(256)
    var output: ByteArray = ByteArray(0)
    var selectors: ByteArray = ByteArray(18002)
    var selectorMtf: ByteArray = ByteArray(18002)
    var input: ByteArray = ByteArray(0)
    var codeLengths: Array<ByteArray> = emptyArray()
    var blockSize: Int = 0
    var outputOffset: Int = 0
    var outputRemaining: Int = 0
    var bitBuffer: Int = 0
    var anInt2208: Int = 0
    var inputOffset: Int = 0
    var inUseCount: Int = 0
    var totalOutput: Int = 0
    var totalInput: Int = 0
    var anInt2221: Int = 0
    var anInt2222: Int = 0
    var originalPointer: Int = 0
    var anInt2225: Int = 0
    var anInt2227: Int = 0
    var bitsAvailable: Int = 0
    var minimumLengths: IntArray = IntArray(6)
    var cumulativeFrequencies: IntArray = IntArray(257)
    var mtfBases: IntArray = IntArray(16)
    var frequencies: IntArray = IntArray(256)
    var bases: Array<IntArray> = emptyArray()
    var permutations: Array<IntArray> = emptyArray()
    var limits: Array<IntArray> = emptyArray()

    init {
        permutations = Array(6) { IntArray(258) }
        bases = Array(6) { IntArray(258) }
        codeLengths = Array(6) { ByteArray(258) }
        limits = Array(6) { IntArray(258) }
    }
}
