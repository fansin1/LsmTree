class LsmTree(val maxHeight: Int){

    val avl = AvlTree<Int, String>(Comparator { o1, o2 -> o1.minus(o2) })

    fun insert(key: Int, value: String) {}
    fun delete(key: Int): Boolean = true
    fun getNode(key: Int): String = ""
    private fun merge() {}
}