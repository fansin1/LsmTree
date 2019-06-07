import com.google.common.hash.BloomFilter
import com.google.common.hash.Funnels
import kotlin.math.log2
import kotlin.math.pow

class LsmTree(val maxHeight: Int){

    @Suppress("UnstableApiUsage")
    inner class LsmFile {
        private var _filter = BloomFilter.create(Funnels.integerFunnel(), maxHeight.toDouble().pow(2).toInt() - 1, 0.01)

        private var _tree: AvlTree<Int, String>? = null

        private var _lastHeight = 0

        private var _changed = false

        private var newNodes = mutableListOf<AvlTree.Node<Int, String?>>()

        fun height(): Int = _tree?.height() ?: _lastHeight + log2(newNodes.size.toDouble()).toInt()

        fun insert(node: AvlTree.Node<Int, String?>) {
            _changed = true
            newNodes.add(node)
            _filter.put(node.key)
            node.isWritten = true
        }

        fun update(node: AvlTree.Node<Int, String?>) {
            _changed = true
            node.isUpdate = true
            newNodes.add(node)
        }

        fun maybeHaveNode(node: AvlTree.Node<Int, String?>): Boolean {
            return _filter.mightContain(node.key)
        }

        fun writeFile() {
            if (_changed) {
                if (_tree == null)
                    readFile()

                for (i in newNodes)
                    when {
                        i.isTombstone -> {
                            _tree!!.delete(i.key)
                            i.isWritten = true
                        }
                        i.isUpdate -> if (_tree!!.update(i.key, i.value, i.isTombstone)) i.isWritten = true
                        else -> _tree!!.insert(i.key, i.value)
                    }

                newNodes.clear()

                //TODO writing to file :D
            }
            _changed = false
            clear()
        }

        fun readFile() {
            _tree = AvlTree(Comparator { o1, o2 -> o1.minus(o2) } )
            //TODO reading
        }

        fun clear() {
            _tree = null
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

        //add searching in files
        _avl.makeTombstone(key)
    }

    fun printRoot() {
        _avl.printRoot()
    }

    fun getNode(key: Int): String {
        return _avl.find(key)?.value ?: ""
    }

    private fun merge() {

        val iterator = _avl.Iterator()

        if (iterator.node == null)
            return

        var node: AvlTree.Node<Int, String?>? = iterator.node!!

        while (node != null) {
            for (i in _lsmFiles) {
                if (i.maybeHaveNode(node)) {
                    i.update(node)
                }
            }
            node = iterator.next()
        }

        _lsmFiles.forEach { it.writeFile() }

        iterator.toStart()
        node = iterator.node

        while (node != null) {
            if (!node.isWritten) {
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