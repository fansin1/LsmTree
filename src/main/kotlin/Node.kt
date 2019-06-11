data class Node<K, V>(val key: K, var value: V?, var height: Int = 1,
                      var left: Node<K, V?>? = null, var right: Node<K, V?>? = null,
                      var parent: Node<K, V?>? = null, var isTombstone: Boolean = false,
                      var isUpdate: Boolean = false, var isWritten: Boolean = false)