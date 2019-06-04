import au.com.console.kassava.kotlinToString
import io.bretty.console.tree.TreeNodeConverter
import io.bretty.console.tree.TreePrinter

class AvlTree<K, V> (var comp: Comparator<K>) {

    var converter = object: TreeNodeConverter<Node<K, V>> {
        override fun name(t: Node<K, V>?): String {
            return if (t != null) {
                if (t.isTombstone) {
                    "deleted ${t.value.toString()}"
                } else {
                    t.value.toString()
                }
            } else
                ""
        }

        override fun children(t: Node<K, V>?): MutableList<Node<K, V>?> {
            val res = mutableListOf<Node<K, V>?>()

            if (t == null) return res

            if (t.left != null)
                res.add(t.left)

            if (t.right !=  null)
                res.add(t.right)

            return res
        }
    }

    private var root: Node<K, V>? = null

    data class Node<K, V>(val key: K, var value: V, var height: Int = 1,
                          var left: Node<K, V>? = null, var right: Node<K, V>? = null,
                          var parent: Node<K, V>? = null, var isTombstone: Boolean = false) {

        override fun toString(): String =
                "\n" + kotlinToString(properties = arrayOf(Node<K, V>::key, Node<K, V>::height,
                                                    Node<K, V>::left, Node<K, V>::right))
    }

    private fun height(node: Node<K, V>?) = node?.height ?: 0

    private fun balanceFactor(node: Node<K, V>) = height(node.right) - height(node.left)

    private fun fixHeight(node: Node<K, V>) {
        val leftHeight = height(node.left)
        val rightHeight = height(node.right)
        node.height = (if (leftHeight > rightHeight) leftHeight else rightHeight) + 1
    }

    private fun rotateRight(node: Node<K, V>): Node<K, V> {
        val left = node.left ?: Node(node.key, node.value)

        node.left = left.right
        left.right = node

        left.parent = node.parent
        node.parent = left
        node.left?.parent = node

        fixHeight(node)
        fixHeight(left)

        return left
    }

    private fun rotateLeft(node: Node<K, V>): Node<K, V> {
        val right = node.right ?: Node(node.key, node.value)

        node.right = right.left

        right.left = node

        right.parent = node.parent
        node.parent = right
        node.right?.parent = node

        fixHeight(node)
        fixHeight(right)

        return right
    }

    private fun balance(node: Node<K, V>): Node<K, V> {
        fixHeight(node)

        if (balanceFactor(node) == 2) {
            if (balanceFactor(node.right!!) < 0) {
                node.right = rotateRight(node.right!!)
                node.right!!.parent = node
            }

            return rotateLeft(node)
        }

        if (balanceFactor(node) == -2) {
            if (balanceFactor(node.left!!) > 0) {
                node.left = rotateLeft(node.left!!)
                node.left!!.parent = node
            }

            return rotateRight(node)
        }

        return node
    }

    private fun insert(node: Node<K, V>?, key: K, value: V): Node<K, V> {
        if (node == null) {
            return Node(key, value)
        }

        when {
            comp.compare(key, node.key) == 0 -> {
                node.value = value
            }
            comp.compare(key, node.key) < 0 -> {
                node.left = insert(node.left, key, value)
                node.left!!.parent = node
            }
            else -> {
                node.right = insert(node.right, key, value)
                node.right!!.parent = node
            }
        }

        return balance(node)
    }

    private fun findMin(node: Node<K, V>): Node<K, V> {
        return if (node.left != null) findMin(node.left!!) else node
    }

    private fun deleteMin(node: Node<K, V>?): Node<K, V>? {
        if (node!!.left == null) return node.right

        node.right = deleteMin(node.left!!)
        node.right!!.parent = node

        return balance(node)
    }

    private fun deleteNode(node: Node<K, V>) {
        when {
            node.parent == null -> root = null
            node.parent!!.left == node -> node.parent!!.left = null
            else -> node.parent!!.right = null
        }
    }

    private fun find(node: Node<K, V>?, key: K): Node<K, V>? {
        if (node == null) return null

        return when {
            comp.compare(key, node.key) == 0 -> node
            comp.compare(key, node.key) < 0 -> find(node.left, key)
            else -> find(node.right, key)
        }
    }

    fun find(key: K): Node<K, V>? {
        return find(root, key)
    }

    fun insert(key: K, value: V) {
        root = insert(root, key, value)
    }

    fun delete(key: K) {
        val node = find(key)
        if (node != null)
            node.isTombstone = true
    }

    fun printRoot() {
        print(TreePrinter.toString(root, converter))
    }

}