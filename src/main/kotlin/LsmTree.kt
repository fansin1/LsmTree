import com.google.common.hash.BloomFilter
import com.google.common.hash.Funnels
import java.io.File
import java.io.IOException
import kotlin.math.log2
import kotlin.math.pow

class LsmTree(val maxHeight: Int){

    private var _fileId = 0
        get() {
            return field++
        }

    @Suppress("UnstableApiUsage")
    private inner class LsmFile {

        private var _id = _fileId

        private var _filter = BloomFilter.create(Funnels.integerFunnel(), maxHeight.toDouble().pow(2).toInt() - 1, 0.01)

        private var _tree: AvlTree<Int, String>? = null

        private var _lastHeight = 0

        private var _changed = false

        private var _newNodes = mutableListOf<Node<Int, String?>>()

        private var _tombstones = mutableListOf<Node<Int, String?>>()

        fun height(): Int = _tree?.height() ?: _lastHeight + (if (_newNodes.size > 0) log2(_newNodes.size.toDouble()).toInt() else 0)

        fun insert(node: Node<Int, String?>) {
            _changed = true
            _newNodes.add(node)
            _filter.put(node.key)
            node.isWritten = true
        }

        fun update(node: Node<Int, String?>) {
            _changed = true
            node.isUpdate = true
            _newNodes.add(node)
        }

        fun remove(node: Node<Int, String?>) {
            _changed = true
            _tombstones.add(node)
        }

        fun maybeHaveNode(key: Int): Boolean {
            return _filter.mightContain(key)
        }

        fun find(key: Int): String? {
            if (_tree == null)
                readFile()

            val value = _tree!!.find(key)?.value

            _tree = null

            return value
        }

        fun writeFile() {
            if (_changed) {
                if (_tree == null)
                    readFile()

                for (i in _newNodes)
                    when (i.isUpdate) {
                        true -> if (_tree!!.update(i.key, i.value, i.isTombstone)) i.isWritten = true
                        else -> _tree!!.insert(i.key, i.value)
                    }

                _tombstones.forEach {
                    _tree!!.delete(it.key)
                }

                _newNodes.clear()
                _lastHeight = _tree!!.height()

                try {
                    File("$_id.txt").printWriter().use{ out ->
                        val it = _tree!!.iterator()

                        while (it.hasNext()) {
                            val node = it.next()
                            out.println("${node.key} ${node.value}")
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            _changed = false
            _tree = null
        }

        private fun readFile() {
            _tree = AvlTree(Comparator { o1, o2 -> o1.minus(o2) } )
            try {
                val file = File("$_id.txt")
                if (file.exists())
                    file.forEachLine {
                        val a = it.split(' ')
                        _tree!!.insert(a[0].toInt(), a[1])
                    }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private var _avl = AvlTree<Int, String>(Comparator { o1, o2 -> o1.minus(o2) })
    private var _lsmFiles = mutableListOf(LsmFile())

    fun insert(key: Int, value: String) {
        _avl.insert(key, value)

        if (_avl.height() > maxHeight)
            merge()
    }

    fun remove(key: Int) {
        _avl.makeTombstone(key)
        if (_avl.height() > maxHeight)
            merge()
    }

    fun printRoot() {
        _avl.printRoot()
    }

    fun getNode(key: Int): String? {
        var value = _avl.find(key)?.value

        if (value == null) {
            for (i in _lsmFiles) {
                if (i.maybeHaveNode(key)) {
                    value = i.find(key)
                    if (value != null)
                        break
                }
            }
        }

        return value
    }

    private fun merge() {

        var iterator = _avl.iterator()

        if (!iterator.hasNext())
            return

        while (iterator.hasNext()) {
            val node = iterator.next()
            for (i in _lsmFiles) {
                if (i.maybeHaveNode(node.key)) {
                    if (!node.isTombstone) {
                        i.update(node)
                    } else {
                        i.remove(node)
                    }
                }
            }
        }

        _lsmFiles.forEach { it.writeFile() }

        iterator = _avl.iterator()

        while (iterator.hasNext()) {
            val node = iterator.next()
            if (!node.isWritten && !node.isTombstone) {
                if (_lsmFiles.last().height() < maxHeight) {
                    _lsmFiles.last().insert(node)
                } else {
                    _lsmFiles.add(LsmFile())
                    _lsmFiles.last().insert(node)
                }
            }

        }

        _lsmFiles.forEach { it.writeFile() }
        _avl = AvlTree(Comparator { o1, o2 -> o1.minus(o2) })
    }
}