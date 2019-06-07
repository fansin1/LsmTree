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
    inner class LsmFile {

        private var _id = _fileId

        private var _filter = BloomFilter.create(Funnels.integerFunnel(), maxHeight.toDouble().pow(2).toInt() - 1, 0.01)

        private var _tree: AvlTree<Int, String>? = null

        private var _lastHeight = 0

        private var _changed = false

        private var _newNodes = mutableListOf<AvlTree.Node<Int, String?>>()

        private var _tombstones = mutableListOf<AvlTree.Node<Int, String?>>()

        fun height(): Int = _tree?.height() ?: _lastHeight + (if (_newNodes.size > 0) log2(_newNodes.size.toDouble()).toInt() else 0)

        fun insert(node: AvlTree.Node<Int, String?>) {
            _changed = true
            _newNodes.add(node)
            _filter.put(node.key)
            node.isWritten = true
        }

        fun update(node: AvlTree.Node<Int, String?>) {
            _changed = true
            node.isUpdate = true
            _newNodes.add(node)
        }

        fun remove(node: AvlTree.Node<Int, String?>) {
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
                    when {
                        i.isUpdate -> if (_tree!!.update(i.key, i.value, i.isTombstone)) i.isWritten = true
                        else -> _tree!!.insert(i.key, i.value)
                    }

                for (i in _tombstones)
                    _tree!!.delete(i.key)

                _newNodes.clear()
                _lastHeight = _tree!!.height()

                try {
                    File("$_id.txt").printWriter().use{ out ->
                        val it = _tree!!.Iterator()
                        var node = it.node

                        while (node != null) {
                            out.println("${node.key} ${node.value}")
                            node = it.next()
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

        val iterator = _avl.Iterator()

        if (iterator.node == null)
            return

        var node: AvlTree.Node<Int, String?>? = iterator.node!!

        while (node != null) {
            for (i in _lsmFiles) {
                if (i.maybeHaveNode(node.key)) {
                    if (!node.isTombstone) {
                        i.update(node)
                    } else {
                        i.remove(node)
                    }
                }
            }
            node = iterator.next()
        }

        _lsmFiles.forEach { it.writeFile() }

        iterator.toStart()
        node = iterator.node

        while (node != null) {
            if (!node.isWritten && !node.isTombstone) {
                if (_lsmFiles.last().height() < maxHeight) {
                    _lsmFiles.last().insert(node)
                } else {
                    _lsmFiles.add(LsmFile())
                    _lsmFiles.last().insert(node)
                }
            }
            node = iterator.next()
        }

        _lsmFiles.forEach { it.writeFile() }
        _avl = AvlTree(Comparator { o1, o2 -> o1.minus(o2) })
    }
}