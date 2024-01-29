class Arrays {
    companion object {
        fun <T> copyOfN(array: Array<T>, size: Int) : Array<T?> {
            return array.copyOf(size)
        }

        fun <T> copyOf(array: Array<T?>, size: Int) : Array<T?> {
            return array.copyOf(size)
        }

        fun <T> arraycopy(sourceArr: Array<T>, sourcePos: Int, destArr: Array<T>, destPos: Int, size: Int) : Array<T> {
            var list = mutableListOf<T>()

            var count = 0
            for(i in 0 until destPos) {
                list.add(destArr[i])
                count++
            }

            for(i in sourcePos .. size) {
                list.add(sourceArr[i])
                count++
            }

            for(i in count until destArr.size) {
                list.add(destArr[i])
            }

            for(i in 0 until destArr.size) {
                destArr[i] = list[i]
            }

            return destArr
        }

        fun arraycopy(sourceArr: FloatArray, sourcePos: Int, destArr: FloatArray, destPos: Int, size: Int) : FloatArray {
            var list = mutableListOf<Float>()

            var count = 0
            for(i in 0 until destPos) {
                list.add(destArr[i])
                count++
            }

            for(i in sourcePos .. size) {
                list.add(sourceArr[i])
                count++
            }

            for(i in count until destArr.size) {
                list.add(destArr[i])
            }

            for(i in 0 until destArr.size) {
                destArr[i] = list[i]
            }

            return destArr
        }

        inline fun <reified T> copyOfNonNull(array: Array<T>, size: Int) : Array<T> {
            val res = array.copyOf(size)
            val ret : MutableList<T> = mutableListOf()
            res.forEach {
                ret.add(it!!)
            }
            return ret.toTypedArray()
        }

        fun copyOf(array: IntArray, size: Int) : IntArray {
            return array.copyOf(size)
        }

        fun copyOf(array: FloatArray, size: Int) : FloatArray {
            return array.copyOf(size)
        }

        fun copyOf(array: DoubleArray, size: Int) : DoubleArray {
            return array.copyOf(size)
        }

        fun copyOf(array: BooleanArray, size: Int) : BooleanArray {
            return array.copyOf(size)
        }

        fun <T> fill(array: Array<T?>, value: T) : Array<T?> {
            array.fill(value)
            return array
        }

        fun <T> fillN(array: Array<T?>, value: T?) : Array<T?> {
            array.fill(value)
            return array
        }

        fun fill(array: IntArray, value: Int) : IntArray {
            array.fill(value)
            return array
        }

        fun fill(array: FloatArray, value: Float) : FloatArray {
            array.fill(value)
            return array
        }

        fun fill(array: DoubleArray, value: Double) : DoubleArray {
            array.fill(value)
            return array
        }

        fun fill(array: BooleanArray, value: Boolean) : BooleanArray {
            array.fill(value)
            return array
        }

        fun sort(array: IntArray) : IntArray {
            array.sort()
            return array
        }

        fun toString(array: Any?) : String {
            return array.toString()
        }

        fun binarySearch(array: DoubleArray, value: Double): Int {
            return array.indexOfFirst {
                it == value
            }
        }

        fun binarySearch(array: FloatArray, value: Float): Int {
            return array.indexOfFirst {
                it == value
            }
        }

        fun <T> binarySearch(array: MutableList<T>, value: T): Int {
            return array.indexOfFirst {
                it == value
            }
        }
    }
}