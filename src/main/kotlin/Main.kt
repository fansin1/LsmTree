import java.io.File

object Main {

    @JvmStatic
    fun main(args: Array<String>) {

        //delete all old files
        var id = 0
        var file = File("$id.txt")

        while (file.exists()) {
            file.delete()
            id++
            file = File("$id.txt")
        }


        //Start LsmTree
        val lsm = LsmTree(2)

        while (true) {
            val line = readLine() ?: ""
            val lArgs = line.split(' ')
            when (lArgs[0].trim()) {
                "ins" -> lsm.insert(Integer.parseInt(lArgs[1].trim()), lArgs[2].trim())
                "rem" -> lsm.remove(Integer.parseInt(lArgs[1].trim()))
                "print" -> lsm.printRoot()
                "find" -> print((lsm.getNode(Integer.parseInt(lArgs[1].trim()))) + "\n")
            }
        }
    }
}