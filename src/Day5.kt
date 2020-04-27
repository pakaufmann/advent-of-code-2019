fun main(args: Array<String>) {
    val program = State(0, readProgram("day5.txt"))

    print("Part 1: ")
    runProgram(program, System.out, "1".byteInputStream())

    print("Part 2: ")
    runProgram(program, System.out, "5".byteInputStream())
}