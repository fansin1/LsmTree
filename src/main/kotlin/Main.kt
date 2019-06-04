object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        val avl = AvlTree<Int, String>(Comparator { o1, o2 -> o1.minus(o2) })

        while (true) {
            val line = readLine() ?: ""
            val lArgs = line.split(' ')
            when (lArgs[0].trim()) {
                "ins" -> avl.insert(Integer.parseInt(lArgs[1].trim()), lArgs[2].trim())
                "del" -> avl.delete(Integer.parseInt(lArgs[1].trim()))
                "print" -> avl.printRoot()
                "find" -> print((avl.find(Integer.parseInt(lArgs[1].trim()))?.value  ?: "Not found") +  "\n")
            }
        }
    }
}